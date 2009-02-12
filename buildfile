require "buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.rubyforge.org/ .

VERSION_NUMBER="2.5-SNAPSHOT"

###### PROJECT

desc "The main Study Calendar application, including core data access, business logic, and the web interface"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "gov.nih.nci.cabig.psc"

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
  
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    
  end
end
