require 'faraday'
require 'nokogiri'
require 'builder'
require 'highline'

module Psc
  class State
    attr_accessor :sites, :templates, :registrations

    def apply(connection)
      StateApplier.new(self).apply(connection)
    end

    def template(ident)
      templates.detect { |t| t.assigned_identifier == ident } or fail "No template #{ident}"
    end

    def self.from_file(path)
      self.new.tap do |state|
        doc = Nokogiri::XML(open(path))
        state.sites = read_sites(doc)
        state.templates = read_templates(doc, File.dirname(path))
        state.registrations = read_registrations(doc)
      end
    end

    private

    def self.parse_date(s)
      case s
      when /(\d{4})-(\d{1,2})-(\d{1,2})/
        Date.new($1.to_i, $2.to_i, $3.to_i)
      when /^\d+$/
        RelativeDate.new s.to_i
      end
    end

    def self.read_sites(doc)
      doc.xpath('/psc-state/site').collect do |site_elt|
        Site.new(site_elt['name'], site_elt['assigned-identifier'])
      end
    end

    def self.read_templates(doc, basedir)
      doc.xpath('/psc-state/template').collect do |t_elt|
        ident = t_elt['assigned-identifier']
        Template.create(ident,
          :filename =>
            if filename = t_elt['file']
              if filename =~ %r{^/}
                filename
              else
                File.join(basedir, filename)
              end
            else
              File.join(basedir, "#{ident}.xml")
            end,
          :participating_sites => t_elt.xpath('participating-site').collect { |ps_elt|
            ParticipatingSite.create(
              ps_elt['assigned-identifier'],
              :approval => case ps_elt['approval']
                           when "false"
                             false
                           else
                             parse_date(ps_elt['approval'])
                           end
              )
          })
      end
    end

    def self.read_registrations(doc)
      doc.xpath('/psc-state/registration').collect do |reg_elt|
        Registration.create(
          :subject => read_subject(reg_elt.xpath('subject').first),
          :study_sites => reg_elt.xpath('study-site').collect { |study_elt|
            StudySite.create(
              %w(template site primary_coordinator study_subject_identifier desired_assignment_identifier).
              inject({}) { |h, k| h[k] = study_elt[k.gsub('_', '-')]; h }.
              merge(
                :scheduled_segments => study_elt.xpath('scheduled-segment').collect { |seg_elt|
                  ScheduledSegment.create(seg_elt['segment'],
                    :mode => seg_elt['mode'],
                    :start => parse_date(seg_elt['start'])
                  )
                })
              )
          })
      end
    end

    def self.read_subject(subject_elt)
      Subject.create(
        %w(first_name last_name person_id gender).inject({}) { |h, k|
          h[k] = subject_elt[k.sub('_', '-')]; h
        }.merge(
          :birth_date => parse_date(subject_elt['birth-date']),
          :properties => subject_elt.xpath('subject-property').
            collect { |sp_elt| [sp_elt['name'], sp_elt['value']] }
        )
      )
    end
  end

  module BulkSettable
    def self.included(struct_class)
      struct_class.send(:extend, ClassMethods)
    end

    def attributes=(map)
      map.each do |k, v|
        setter = "#{k}="
        if self.respond_to?(setter)
          self.send setter, v
        end
      end
    end

    module ClassMethods
      def create(*args)
        attrs = args.pop
        i = self.new(*args)
        i.attributes = attrs
        i
      end
    end
  end

  class RelativeDate
    include Comparable

    attr_reader :days

    def initialize(days)
      @days = days
    end

    def <=>(other)
      self.days <=> other.days
    end

    def to_date
      Date.today + days
    end

    def to_s
      to_date.to_s
    end
  end

  class Site < Struct.new(:name, :assigned_identifier)
    include BulkSettable

    def apply(connection)
      connection.put "sites/#{assigned_identifier}",
        Psc.xml('site', 'site-name' => name, 'assigned-identifier' => assigned_identifier).to_s,
          'Content-Type' => 'text/xml'
    end
  end

  class Template < Struct.new(:assigned_identifier)
    include BulkSettable

    attr_accessor :filename, :participating_sites

    def apply(connection)
      connection.put "studies/#{assigned_identifier}/template", File.read(filename),
        'Content-Type' => 'text/xml'
      (participating_sites || []).each do |ps|
        ps.apply(connection, self)
      end
    end

    def document
      @document ||= Nokogiri::XML(open(filename, 'r'))
    end

    def resolve_segment(ident)
      s = (
        segment_index.detect { |s|
          ident.downcase.split(/\s*:\s*/) == [s[:epoch_name].downcase, s[:name].downcase]
        } ||
        segment_index.detect { |s| ident == s[:id] } ||
        segment_index.detect { |s| ident == s[:name] }
        )
      raise "Unable to resolve segment #{ident.inspect}" unless s
      s[:id]
    end

    private

    def segment_index
      # TODO: this will not resolve the enclosing epoch correctly for
      # segments added in amendments.
      @ss_index ||= document.css('epoch').inject({}) { |h, ep_elt|
        (h[ep_elt['name']] ||= []).push(
          *ep_elt.css('study-segment').collect { |ss_elt|
            { :id => ss_elt['id'], :name => ss_elt['name'] }
          })
        h
      }.collect { |epoch, segments|
        segments.each { |seg| seg[:epoch_name] = epoch }; segments
      }.flatten
    end
  end

  class ParticipatingSite < Struct.new(:assigned_identifier)
    include BulkSettable

    attr_accessor :approval

    def approval
      @approval = RelativeDate.new(0) if @approval.nil?
      @approval
    end

    def apply(connection, template)
      study_site_path = "studies/#{template.assigned_identifier}/sites/#{assigned_identifier}"
      connection.put study_site_path, "<study-site-link/>", 'Content-Type' => 'text/xml'
      if approval
        template.document.css('amendment').
          collect { |am_elt| [am_elt['date'], am_elt['name']].compact }.
          sort { |a, b| a[0] <=> b[0]  }.
          collect { |pair| pair.join('~') }.
          each do |am_ident|
            connection.post "#{study_site_path}/approvals",
              Psc.xml('amendment-approval', :date => approval.to_s, :amendment => am_ident).to_s
          end
      end
    end
  end

  class Registration
    include BulkSettable
    attr_accessor :subject, :study_sites

    def apply(connection, state)
      study_sites.each do |study_site|
        study_site.apply(connection, subject, state)
      end
    end
  end

  class Subject
    include BulkSettable
    attr_accessor :first_name, :last_name, :gender, :birth_date, :person_id, :properties

    def build_on(xml_builder)
      xml_builder.subject(
        'first-name' => first_name,
        'last-name' => last_name,
        'gender' => gender,
        'person-id' => person_id,
        'birth-date' => birth_date.to_s
        ) { |subj_elt|
        (properties || []).each { |prop|
          subj_elt.property(:name => prop[0], :value => prop[1])
        }
      }
    end
  end

  class StudySite
    include BulkSettable
    attr_accessor :template, :site, :study_subject_identifier, :primary_coordinator,
      :desired_assignment_identifier, :scheduled_segments

    def apply(connection, subject, state)
      template_details = state.template(template)

      schedule_resp = connection.post("studies/#{template}/sites/#{site}/subject-assignments",
        Psc.xml(
          'registration',
          'subject-coordinator-name' => primary_coordinator,
          'desired-assignment-id' => desired_assignment_identifier,
          'study-subject-id' => study_subject_identifier,
          'first-study-segment-id' => template_details.resolve_segment(
            scheduled_segments.first.identifier),
          'date' => (scheduled_segments.first.start || Psc::RelativeDate.new(0)).to_s
        ) { |reg_elt| subject.build_on(reg_elt) }
      )

      schedule_url = schedule_resp.headers['Location']

      scheduled_segments[1, scheduled_segments.size].each { |ss|
        ss.apply(connection, template_details, schedule_url)
      }
    end
  end

  class ScheduledSegment < Struct.new(:identifier)
    include BulkSettable
    attr_accessor :start, :mode

    def mode
      @mode ||= "per-protocol"
    end

    def apply(connection, template, schedule_url)
      connection.post(schedule_url, Psc.xml('next-scheduled-study-segment',
          'study-segment-id' => template.resolve_segment(identifier),
          'start-date' => start.to_s,
          'mode' => mode
        ))
    end
  end

  class StateApplier
    def initialize(state, out=DefaultOutput.new)
      @state = state
      @out = out
    end

    def apply(connection)
      c = ConnectionProxy.new(connection, @out)
      puts (@state.sites || []).collect { |s|
        @out.monitor("Adding site #{s.assigned_identifier}") { s.apply(c) }
      }.compact.tap { |result| return false if result.size > 0 }

      (@state.templates || []).collect { |t|
        @out.monitor("Adding template #{t.assigned_identifier}") { t.apply(c) }
      }.compact.tap { |result| return false if result.size > 0 }

      (@state.registrations || []).collect { |r|
        @out.monitor("Registering #{r.subject.first_name} #{r.subject.last_name}") {
          r.apply(c, @state)
        }
      }.compact.tap { |result| return false if result.size > 0 }
    end

    class ConnectionProxy
      def initialize(conn, out)
        @conn = conn
        @out = out
      end

      def make_request(method, *args)
        @out.trace("#{method.to_s.upcase} #{args.first}")
        resp = @conn.send(method, *args)
        if resp.respond_to?(:status) && resp.status > 299
          raise resp.body
        end
        resp
      end

      def method_missing(m, *args)
        if %w(get put post delete patch).include?(m.to_s)
          make_request(m, *args)
        else
          super
        end
      end
    end

    class DefaultOutput
      def initialize
        @hl = HighLine.new
        HighLine.color_scheme = HighLine::SampleColorScheme.new
      end

      def monitor(msg)
        @hl.say("<%= color('*', :info) %> #{msg}")
        begin
          yield
          nil
        rescue => e
          @hl.say("<%= color(' -', :error) %> #{e}")
          false
        end
      end

      def trace(msg)
        @hl.say("<%= color(' +', :info) %> #{msg}")
      end
    end
  end

  def self.xml(root_name, root_attributes={}, &block)
    root_attributes['xmlns'] = 'http://bioinformatics.northwestern.edu/ns/psc'
    Builder::XmlMarkup.new(:indent => 2).tag!(root_name, root_attributes, &block)
  end
end
