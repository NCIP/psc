require "buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5-SNAPSHOT"

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics.studycalendar"

  resources.from(_("src/main/java")).exclude("**/*.java")
  compile.options.target = "1.5"
  compile.with CTMS_COMMONS, CORE_COMMONS, SECURITY, XML, SPRING, HIBERNATE, 
    LOGBACK, SLF4J, JAKARTA_COMMONS, CAGRID, BERING, WEB, DB, CONTAINER_PROVIDED
  
  test.resources.from(_("src/test/java")).exclude("**/*.java")
  test.with(UNIT_TESTING, 'psc:test-infrastructure').include("*Test")

  package(:war).exclude(CONTAINER_PROVIDED)
  package(:sources)
  
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
  
  desc "Pure utility code"
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
  
  desc "Core data access, XML serialization, and non-replaceable business logic"
  define "core" do
    compile.with project('domain'), project('domain').compile.dependencies, XML,
      RESTLET.framework, FREEMARKER, CSV, CONTAINER_PROVIDED
    test.with(UNIT_TESTING)
    package(:jar)
    package(:sources)
  end
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    compile.with LOGBACK, project('core'), project('core').compile.dependencies, 
      SPRING_WEB, RESTLET, WEB, FREEMARKER
    test.with project('test-infrastructure'), 
      project('test-infrastructure').compile.dependencies
    package(:war)
    package(:sources)
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING,
      project('domain'), project('domain').compile.dependencies, project('domain').test,
      project('core'), project('core').compile.dependencies, project('core').test,
    package(:jar)
    package(:sources)
  end
end
