require "buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.rubyforge.org/ .

VERSION_NUMBER="1.2-SNAPSHOT"

###### PROJECT

desc "The main Study Calendar application, including core data access, business logic, and the web interface"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "gov.nih.nci.cabig.psc"

  resources.from(_("src/main/java")).exclude("**/*.java")
  compile.options.target = "1.5"
  compile.with CTMS_COMMONS, CORE_COMMONS, SECURITY, XML, SPRING, HIBERNATE, 
    LOGBACK, SLF4J, JAKARTA_COMMONS, CAGRID, WEB, DB, CONTAINER_PROVIDED
  
  test.resources.from(_("src/test/java")).exclude("**/*.java")
  test.with(UNIT_TESTING).include("*Test")

  package(:war).exclude(CONTAINER_PROVIDED)
  package(:sources)
  
  resources task(:init)
  
  db = ENV['DB'] || 'studycalendar'
  dbprops = { } # Filled in by :init
  
  dbfile = file("db/#{db}.properties") do |f|
    # If we get here, the file doesn't exist, therefore:
    fail "Database not configured (could not read #{f}).  See db/readme.txt."
  end
  
  task :init => dbfile do
    loaded = Hash.from_java_properties(read(dbfile.to_s)).inject({}) do |h, (k, v)|
      h[k] = v.nil? ? '' : v ; h # nils don't get filtered in
    end
    dbprops.merge!(loaded)
    dbprops['datasource.dialect.upt'] ||= dbprops['datasource.dialect']
    puts "All database ops for this build will use #{dbprops['datasource.url']}"
  end
  
  resources.enhance do
    # always overwrite
    cp dbfile.to_s, "target/classes/datasource.properties"
  end
  
  test.resources task(:test_csm_config)
  
  task :test_csm_config => :init do
    filter(_("conf/upt")).include('*.xml').into(_("target/test-classes")).
      using(:ant, {'tomcat.security.dir' => _("target/test-classes")}.merge(dbprops)).run
  end

  # TODO: this is hardcoded to the particular file locations appropriate for vera (at NU)
  # Note that namespaces embedded into projects don't seem to work with buildr at the moment
  task :public_demo_build do
    filter(_("test/public")).include('*.html').into(_("target/public")).
      using(:ant, {'public-demo.base-url' => 'https://vera.bioinformatics.northwestern.edu/studycalendar/'}).run
  end

  task :public_demo_deploy => :public_demo_build do
    cp _("target/public/*.html"), "/opt/tomcat/webapps-vera/studycalendar/"
  end
end
