# General test environment setup
################################

require 'fileutils'
require 'jruby'

# Direct Spring to the correct classloader
# a la http://www.ruby-forum.com/topic/153160
Java::JavaLang::Thread.current_thread.context_class_loader = JRuby.runtime.getJRubyClassLoader

module PscTest
  include_package "edu.northwestern.bioinformatics.studycalendar.test"
  
  def self.createDate(year, month, day)
    # DateTools expects month as java.util.Calendar constant, which starts with 0
    Java::GovNihNciCabigCtmsLang::DateTools.createDate(year, month - 1, day)
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
  end

  config.after(:each) do
    application_context['databaseInitializer'].afterEach
  end
end

application_context['databaseInitializer'].beforeAll
# afterAll is invoked via a jtestr after block (in jtestr_config.rb)