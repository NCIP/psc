#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

require 'yaml'

module StaticDataHelper
  def load_site(assigned_identifier)
    application_context['siteDao'].getByAssignedIdentifier(assigned_identifier)
  end

  # creates literate accessors for each of the sites defined in sites.yml
  YAML.load_file("#{File.dirname(__FILE__)}/../../../target/spec/resources/sites.yml").each do |short_name, attributes|
    define_method(short_name) do
      load_site(attributes['assignedIdentifier'])
    end
  end

  def load_user(username)
    application_context['pscUserService'].getAuthorizableUser(username)
  end

  # creates literate accessors for each of the users defined in users.yml
  YAML.load_file("#{File.dirname(__FILE__)}/../../../target/spec/resources/users.yml").each do |username, _|
    define_method(username) do
      load_user(username)
    end
  end

  def sample_activity(name)
    application_context['activityDao'].getByName(name) or raise "No test activity named #{name}"
  end
end

class Spec::Example::ExampleGroup
  include StaticDataHelper
end
