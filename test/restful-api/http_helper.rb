puts "Loading #{__FILE__}"

require File.expand_path("lib/rest-open-uri", File.dirname(__FILE__))

def tomcat_properties
  $tomcat_properties ||= Class.new do
    def initialize
      @properties = Java::JavaUtil::Properties.new()
      @properties.load(Java::JavaIo::FileInputStream.new(
        File.expand_path("../../tools/tomcat/tomcat.properties", File.dirname(__FILE__))))
    end

    def [](k)
      @properties.getProperty(k)
    end
  end.new
end

helper_for Spec::Example::ExampleGroup do
  attr_reader :response

  def get(relative_uri, options={})
    user = options.delete(:as)
    options['Authorization'] = "psc_token #{user}" if user
    options[:method] = :get
    begin
      OpenURI.open_uri full_uri(relative_uri), options do |f|
        @response = Response.new(f)
      end
    rescue OpenURI::HTTPError => e
      @response = Response.new(e.io)
    end
  end

  def full_uri(relative)
    "http://#{tomcat_properties['tomcat.server']}:8080#{tomcat_properties['tomcat.deploy-path']}/api/v1#{relative}"
  end

  class Response
    attr_accessor :entity
    attr_accessor :status, :content_type, :meta

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
  end
end