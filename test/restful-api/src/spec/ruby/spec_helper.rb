# General test environment setup
################################

require 'fileutils'
require 'jruby'

# Manually configure logback since it isn't finding the configuration file on its own
begin
  lc = Java::OrgSlf4j::LoggerFactory.getILoggerFactory();
  conf = Java::ChQosLogbackClassicJoran::JoranConfigurator.new
  conf.context = lc
  lc.reset
  conf.doConfigure(Java::JavaLang::System.getProperty('logback.configurationFile'))
  # Uncomment for debugging:
  # Java::ChQosLogbackCoreUtil::StatusPrinter.print(lc);
end

# Direct Spring to the correct classloader
# a la http://www.ruby-forum.com/topic/153160
Java::JavaLang::Thread.current_thread.context_class_loader = JRuby.runtime.getJRubyClassLoader

# Shortcuts for PSC java packages
module Psc
  %w(domain domain.delta core service tools service.presenter).each do |pkg|
    class_eval <<-RUBY
      module #{pkg.split('.').map { |n| n.capitalize }.join('::')}
        include_package 'edu.northwestern.bioinformatics.studycalendar.#{pkg}'
      end
    RUBY
  end
end

module PscTest
  include_package "edu.northwestern.bioinformatics.studycalendar.test"

  Fixtures = Psc::Core::Fixtures

  def self.createDate(year, month, day)
    # DateTools expects month as java.util.Calendar constant. They start with 0.
    Java::GovNihNciCabigCtmsLang::DateTools.createDate(year, month - 1, day)
  end

  def self.createDeltaFor(node, *changes)
    Psc::Domain::Delta::Delta.createDeltaFor(
      node, changes.to_java(Psc::Domain::Delta::Change)
    )
  end

  def self.log(message)
    Java::OrgSlf4j::LoggerFactory.getLogger("RESTful API Specs").info(message.to_s)
  end

  class HibernateOpenSession
    def initialize
      mock_request = Java::OrgSpringframeworkMockWeb::MockHttpServletRequest.new
      @web_request = Java::OrgSpringframeworkWebContextRequest::ServletWebRequest.new(mock_request)
      @flush = true
    end

    def begin_session
      PscTest.log("-------- begin interceptor session --------")
      open_session_interceptors.each { |interceptor| interceptor.preHandle(@web_request) }
    end

    def interrupt_session
      end_session
      begin_session
    end

    def end_session
      open_session_interceptors.reverse.each { |interceptor|
        if @flush
          interceptor.postHandle(@web_request, nil)
        end
        interceptor.afterCompletion(@web_request, nil)
      }
      PscTest.log("---------- end interceptor session --------")
    end

    def open_session_interceptors
      %w(auditOpenSessionInViewInterceptor openSessionInViewInterceptor).collect do |bean_name|
        application_context[bean_name]
      end
    end
  end
end

def application_context
  $application_context ||= Class.new do
    def initialize
      path = Java::JavaLang::System.getProperty("applicationContext.path")
      puts "Loading framework application context from #{path}"
      @context = Java::OrgSpringframeworkContextSupport::GenericApplicationContext.new(
        Java::OrgSpringframeworkBeansFactoryXml::XmlBeanFactory.new(
          Java::OrgSpringframeworkCoreIo::FileSystemResource.new(path),
          Psc::Core::StudyCalendarTestCoreApplicationContextBuilder.getApplicationContext()
        )
      )
    end

    def [](beanName)
      @context.getBean(beanName.to_s)
    end

    def method_missing(m, *args)
      @context.send(m, *args)
    end
  end.new
end

Spec::Runner.configure do |config|
  config.before(:each) do
    application_context['databaseInitializer'].beforeEach
    Psc::Tools::FormatTools.setLocal(Psc::Tools::FormatTools.new("MM/dd/yyyy"))
    @hibernate = PscTest::HibernateOpenSession.new
    @hibernate.begin_session
  end

  config.after(:each) do
    @hibernate.end_session
    Psc::Tools::FormatTools.clearLocalInstance
    application_context['databaseInitializer'].afterEach
  end
end

application_context['databaseInitializer'].beforeAll
