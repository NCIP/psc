#!/usr/bin/ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

###
# Generates a psc-state XML file that assigns lots and lots of
# subjects to a diverse group of SSCMs. It reuses the static setup for
# the RESTful API integrated tests, particularly for the expected
# users.
#
# The fake name data used for this is from FakeNameGenerator.com and
# so is licensed Creative Commons Attribution-ShareAlike.

fail 'Needs Ruby 1.9.2 or later' if RUBY_VERSION < '1.9.2'

require 'csv'
require 'erb'
require 'pathname'
require 'yaml'

RESTFUL_TEST_RESOURCES = Pathname.new(
  File.expand_path('../../../../test/restful-api/src/spec/resources', __FILE__))
SUBJECT_CSV = File.expand_path('../fake-names.csv', __FILE__)

class PscContext
  def template
    @template ||= ERB.new(DATA.read, nil, '-')
  end

  def sites
    @sites ||= YAML.load(File.read(RESTFUL_TEST_RESOURCES + 'sites.yml')).values
  end

  def users
    @users ||= YAML.load(File.read(RESTFUL_TEST_RESOURCES + 'users.yml'))
  end

  def subjects
    @subjects ||= CSV.read(SUBJECT_CSV, csv_options).
      collect { |row| Subject.new(row, self) }.
      slice(0, 1000)
  end

  def csv_options
    {
      :encoding => 'UTF-8',
      :headers => true,
      :header_converters => [
        CSV::HeaderConverters[:downcase],
        lambda { |v| v.strip }
      ]
    }
  end

  def sscms_for(site_ident)
    users.select { |username, details|
      sscm = details['roles']['study_subject_calendar_manager']
      sscm && sscm['sites'] &&
        (sscm['sites'].include?(site_ident) || sscm['sites'].include?('__ALL__'))
    }.collect { |username, details| username }
  end

  def write
    File.open(File.expand_path('../load-test.xml', __FILE__), 'w') do |f|
      f.write template.result(binding)
    end
  end
end

class Subject
  attr_reader :attributes, :properties, :index, :context

  def initialize(csv_row, context)
    @context = context
    number_header = csv_row.headers.find { |h| h =~ /number/ }
    @index = csv_row[number_header].to_i
    @attributes = {
      'first-name' => csv_row['givenname'],
      'last-name' => csv_row['surname'],
      'gender' => csv_row['gender'],
      'person-id' => csv_row['nationalid'],
      'birth-date' => convert_date(csv_row['birthday'])
    }
    @properties = {
      'City' => csv_row['city'],
      'State' => csv_row['state'],
      'Cell' => csv_row['telephonenumber']
    }
  end

  def pick_one(list)
    list[index % list.size]
  end

  def convert_date(us_date)
    parts = us_date.split('/').collect { |p| "%02d" % p.to_i }
    [parts[2], parts[0], parts[1]].join('-')
  end

  def site
    @site ||= pick_one(context.sites)['assignedIdentifier']
  end

  def study_subject_identifier
    @study_subject_identifier ||= ("%s%04d" % [site[1], index])
  end

  def coordinator
    pick_one(context.sscms_for(site))
  end

  def segment
    "Treatment: Regimen #{pick_one %w(A B)}"
  end

  # Anywhere from 6 months ago to 1 month from now
  def start_day
    rand(180) - 210
  end
end

PscContext.new.write

__END__
<?xml version='1.0'?>

<psc-state>

<% sites.each do |site| -%>
  <site name="<%= site['name'] %>" assigned-identifier="<%= site['assignedIdentifier'] %>"/>
<% end -%>

  <template assigned-identifier="NU-Cycles1" file="NU-Cycles1.xml">
<% sites.each do |site| -%>
    <participating-site assigned-identifier="<%= site['assignedIdentifier'] %>" approval="-365"/>
<% end -%>
  </template>

<% subjects.each do |subject| -%>
  <registration>
    <subject
<% subject.attributes.each do |attr, value| -%>
      <%= attr %>="<%= value %>"
<% end -%>
      >
<% subject.properties.each do |prop, value| -%>
      <subject-property name="<%= prop %>" value="<%= value %>"/>
<% end -%>
    </subject>
    <study-site
      template="NU-Cycles1"
      site="<%= subject.site %>"
      study-subject-identifier="<%= subject.study_subject_identifier %>"
      primary-coordinator="<%= subject.coordinator %>"
      >
      <scheduled-segment segment="<%= subject.segment %>" start="<%= subject.start_day %>"/>
    </study-site>
  </registration>

<% end -%>

</psc-state>
