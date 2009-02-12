# General test environment setup
################################

require 'fileutils'
require 'jruby'

# Direct Spring to the correct classloader
# a la http://www.ruby-forum.com/topic/153160
Java::JavaLang::Thread.current_thread.context_class_loader = JRuby.runtime.getJRubyClassLoader

module PscTest
  include_package "edu.northwestern.bioinformatics.studycalendar.test"

  Fixtures = ServicedFixtures

  def self.createDate(year, month, day)
    # DateTools expects month as java.util.Calendar constant. They start with 0.
    Java::GovNihNciCabigCtmsLang::DateTools.createDate(year, month - 1, day)
  end
  
  def self.createDeltaFor(node, *changes)
    Psc::Domain::Delta::Delta.createDeltaFor(
      node, changes.to_java(Psc::Domain::Delta::Change)
    )
  end
  
  class HibernateOpenSession
    def initialize
      mock_request = Java::OrgSpringframeworkMockWeb::MockHttpServletRequest.new
      @web_request = Java::OrgSpringframeworkWebContextRequest::ServletWebRequest.new(mock_request)
      @flush = true
    end
    
    def begin_session
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
    end
  
    def open_session_interceptors
      %w(auditOpenSessionInViewInterceptor openSessionInViewInterceptor).collect do |bean_name|
        application_context[bean_name]
      end
    end
  end
end

Role = Java::EduNorthwesternBioinformaticsStudycalendarDomain::Role

# Shortcuts for PSC java packages
module Psc
  %w(domain domain.delta).each do |pkg|
    class_eval <<-RUBY
      module #{pkg.split('.').map(&:capitalize).join('::')}
        include_package 'edu.northwestern.bioinformatics.studycalendar.#{pkg}'
      end
    RUBY
  end
end

def application_context
  $application_context ||= Class.new do
    def initialize
      FileUtils.cd(File.expand_path("../..", File.dirname(__FILE__))) do
        @context = Java::OrgSpringframeworkContextSupport::GenericApplicationContext.new(
          Java::OrgSpringframeworkBeansFactoryXml::XmlBeanFactory.new(
            Java::OrgSpringframeworkCoreIo::FileSystemResource.new(
              File.expand_path("static-data/applicationContext.xml", File.dirname(__FILE__))
            ),
            PscTest::StudyCalendarTestHelper.getDeployedApplicationContext()
          )
        )
      end
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
    @hibernate = PscTest::HibernateOpenSession.new
    @hibernate.begin_session
  end

  config.after(:each) do
    @hibernate.end_session
    application_context['databaseInitializer'].afterEach
  end
end

application_context['databaseInitializer'].beforeAll
# afterAll is invoked via a jtestr after block (in jtestr_config.rb)