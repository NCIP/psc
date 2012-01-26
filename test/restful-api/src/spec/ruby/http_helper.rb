require 'rest-open-uri'
require 'builder'
require 'rexml/document'
require 'json/pure'
require 'cgi'
require 'icalendar'
require 'timeout'

module HttpHelper
  attr_reader :response

  def get(relative_uri, options={})
    process_options!(options)

    # Default to XML if an explicit Accept header or extension is not
    # present.  This was the default in Restlet 1.1, so many of the
    # specs expect it.
    unless options["Accept"] || relative_uri =~ /\.\w+$/
      options["Accept"] = "text/xml"
    end

    options[:method] = :get
    execute_request!(relative_uri, options)
  end

  def delete(relative_uri, options={})
    process_options!(options)
    options[:method] = :delete
    execute_request!(relative_uri, options)
  end

  def post(relative_uri, entity, options={})
    process_options!(options, entity)
    options[:method] = :post
    execute_request!(relative_uri, options)
  end

  def put(relative_uri, entity, options={})
    process_options!(options, entity)
    options[:method] = :put
    execute_request!(relative_uri, options)
  end

  def psc_url
    "http://localhost:#{ENV['JETTY_PORT'] || 7200}/psc"
  end
  module_function(:psc_url)

  # TODO: parameterize
  def full_uri(relative, params=nil)
    qs =
      if params
        params.map { |k, v| "#{CGI.escape k}=#{CGI.escape v}" }.join("&")
      end

    "#{psc_url}/api/v1#{relative}" + (qs ? "?#{qs}" : "")
  end

  def psc_xml(root_name, root_attributes={}, &block)
    root_attributes['xmlns'] = 'http://bioinformatics.northwestern.edu/ns/psc'
    Builder::XmlMarkup.new(:indent => 2).tag!(root_name, root_attributes, &block)
  end

  STARTUP_WAIT_TIME = 60 # seconds

  def wait_until_available
    begin
      Timeout::timeout(STARTUP_WAIT_TIME) do
        until psc_available?
          sleep 5
        end
      end
    rescue Timeout::Error => e
      fail "Integration tests waited #{STARTUP_WAIT_TIME} seconds for PSC to start"
    end
  end
  module_function(:wait_until_available)

  private

  def process_options!(options, entity=nil)
    user = options.delete(:as)
    options['Authorization'] = "psc_token #{user}" if user
    if entity
      options[:body] = entity
      options['Content-Type'] = 'text/xml' unless options['Content-Type']
    end
  end

  def execute_request!(relative_uri, options)
    uri = full_uri(relative_uri, options.delete(:params))
    options[:proxy] = RequestLoggerFormatter.proxy_url
    begin
      @hibernate.interrupt_session # force flush of setup data
      PscTest.log("#{options[:method]} #{uri} with options #{options.inspect}")
      OpenURI.open_uri uri, options do |f|
        @response = Response.new(f)
      end
    rescue OpenURI::HTTPError => e
      @response = Response.new(e.io)
    end
  end

  def psc_available?
    url = URI.parse(psc_url)
    begin
      session = Net::HTTP.new(url.host, url.port)
      session.start do |http|
        status = http.get(url.request_uri).code
        # anything indicating a functioning server
        return status =~ /[1234]\d\d/
      end
    rescue => e
      false
    end
  end
  module_function(:psc_available?)

  class Response
    attr_accessor :entity
    attr_accessor :status, :content_type, :meta

    alias :body :entity

    def initialize(io)
      self.entity = io.read
      io.methods.reject { |m| m =~ /^[_=]/ }.each do |m|
        setter = "#{m}="
        if self.respond_to?(setter)
          self.send(setter, io.send(m))
        end
      end
    end

    def status_code
      if status
        status.first.to_i
      end
    end

    def status_message
      (status || []).last
    end

    def success?
      199 < status_code && status_code < 300
    end

    def redirect?
      299 < status_code && status_code < 400
    end

    def client_error?
      399 < status_code && status_code < 500
    end

    def server_error?
      499 < status_code && status_code < 600
    end

    def xml_elements(xpath)
      xml_doc.root.elements.to_a(xpath)
    end

    # Finds the value for the named attribute on every instance of the named
    # element in the response document
    def xml_attributes(element_name, attribute_name)
      xml_elements("//#{element_name}").collect { |s| s.attributes[attribute_name] }
    end

    def xml_doc
      content_type.should == 'text/xml'
      @rexml_doc ||= REXML::Document.new(entity)
    end

    def json
      content_type.should == 'application/json'
      @json ||= JSON.parse(entity)
    end

    def ics
      content_type.should == 'text/calendar'
      @ics ||= Icalendar.parse(entity)
    end

    def calendar
      ics.first
    end
  end
end

class Spec::Example::ExampleGroup
  include HttpHelper
end
