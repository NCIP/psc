require "buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5-SNAPSHOT"
APPLICATION_SHORT_NAME = 'psc'

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics.studycalendar"

  # resources.from(_("src/main/java")).exclude("**/*.java")
  compile.options.target = "1.5"
  # compile.with CTMS_COMMONS, CORE_COMMONS, SECURITY, XML, SPRING, HIBERNATE, 
  #   LOGBACK, SLF4J, JAKARTA_COMMONS, CAGRID, BERING, WEB, DB, CONTAINER_PROVIDED
  
  # test.resources.from(_("src/test/java")).exclude("**/*.java")
  # test.with(UNIT_TESTING, 'psc:test-infrastructure').include("*Test")

  # package(:war).exclude(CONTAINER_PROVIDED)
  # package(:sources)
  
  # resources task(:init)
  
  # db = ENV['DB'] || 'studycalendar'
  # dbprops = { } # Filled in by :init
  
  # test.resources task(:test_csm_config)
  
  # task :test_csm_config => :init do
  #   filter(_("conf/upt")).include('*.xml').into(_("target/test-classes")).
  #     using(:ant, {'tomcat.security.dir' => _("target/test-classes")}.merge(dbprops)).run
  # end

  task :public_demo_deploy do
    cp FileList[_("test/public/*")], "/opt/tomcat/webapps-vera/studycalendar/"
  end
  
  define "Pure utility code"
  define "utility" do
    compile.with SLF4J, SPRING, JAKARTA_COMMONS.collections, 
      JAKARTA_COMMONS.collections_generic, CTMS_COMMONS.lang
    test.with(UNIT_TESTING)
    package(:jar)
    package(:sources)
  end
  
  desc "The domain classes for PSC"
  define "domain" do
    compile.with project('utility'), SLF4J, CTMS_COMMONS, CORE_COMMONS, 
      JAKARTA_COMMONS, SPRING, HIBERNATE, SECURITY
    test.with(UNIT_TESTING) #.include("*Test")
    package(:jar)
    package(:sources)
  end
  
  desc "Core data access, serialization and non-substitutable business logic"
  define "core" do
    task :refilter do
      rm_rf Dir[_(resources.target.to_s, "applicationContext-{spring,setup}.xml")]
    end
    resources.enhance [:refilter]
    
    filter_tokens = {
      'application-short-name'  => APPLICATION_SHORT_NAME,
      'config.database'         => ENV['DB'] || 'datasource',
      "buildInfo.versionNumber" => project.version,
      "buildInfo.username"      => ENV['USER'],
      "buildInfo.hostname"      => `hostname`.chomp,
      "buildInfo.timestamp"     => Time.now.strftime("%Y-%m-%d %H:%M:%S")
    }
    
    resources.from(_("src/main/java")).exclude("**/*.java").
      filter.using(:ant, filter_tokens)
    compile.with project('domain'), project('domain').compile.dependencies, 
      BERING, DB, XML, RESTLET.framework, FREEMARKER, CSV, CONTAINER_PROVIDED,
      QUARTZ, 
      SPRING_WEB # tmp for mail

    test.resources.from(_("src/test/java")).exclude("**/*.java")
    test.with UNIT_TESTING, project('domain').test.compile.target
    
    package(:jar)
    package(:sources)
    
    check do
      acSpring = File.read(_('target/resources/applicationContext-spring.xml'))
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))
      
      acSpring.should include(filter_tokens['config.database'])
      acSetup.should include(filter_tokens['buildInfo.hostname'])
      acSetup.should include(project.version)
    end
  end
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    compile.with LOGBACK, project('core'), project('core').compile.dependencies, 
      SPRING_WEB, RESTLET, WEB
    test.with project('test-infrastructure'), 
      project('test-infrastructure').compile.dependencies
    package(:war).exclude(CONTAINER_PROVIDED)
    package(:sources)
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB,
      %w(domain core).collect { |n| project(n) }.collect { |p| [p, p.compile.dependencies, p.test.compile.target] }
    package(:jar)
    package(:sources)
  end
end
