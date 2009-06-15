require "buildr"
require "buildr/jetty"
require "buildr/emma" if emma?
require "shenandoah/buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5.1.DEV"
APPLICATION_SHORT_NAME = 'psc'

###### Jetty config

# enable JSP support in Jetty
Java.classpath.concat([
  "org.mortbay.jetty:jsp-api-2.1:jar:#{Buildr::Jetty::VERSION}",
  "org.mortbay.jetty:jsp-2.1:jar:#{Buildr::Jetty::VERSION}"
])
jetty.url = "http://localhost:7200"

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics"

  compile.options.target = "1.5"
  compile.options.source = "1.5"
  compile.options.other = %w(-encoding UTF-8)
  
  test.using(:properties => { "psc.config.datasource" => db_name })
  test.enhance [:check_module_packages]
  
  ipr.add_component("CompilerConfiguration") do |component|
    component.option :name => 'DEFAULT_COMPILER', :value => 'Javac'
    component.option :name => 'DEPLOY_AFTER_MAKE', :value => '0'
    component.resourceExtensions do |xml|
      xml.entry :name => '.+\.nonexistent'
    end
    component.wildcardResourceExtensions do |xml|
      xml.entry :name => '?*.nonexistent'
    end
  end
  
  task :public_demo_deploy do
    cp FileList[_("test/public/*")], "/opt/tomcat/webapps-vera/studycalendar/"
  end
  
  desc "Pure utility code"
  define "utility" do
    bnd.wrap!
    bnd.name = "PSC Utility Module"

    compile.with SLF4J.api, SPRING, JAKARTA_COMMONS.collections, 
      CTMS_COMMONS.lang, CTMS_COMMONS.core, CONTAINER_PROVIDED
    test.with(UNIT_TESTING)
    
    package(:jar)
    package(:sources)

    desc "Bidirectional object bridge for sharing object instances across the membrane between the OSGi classloader and the main application classloader"
    define "osgimosis" do
      compile.with SLF4J.api, CGLIB
      test.using(:junit).with(UNIT_TESTING, JAKARTA_COMMONS.io)
      package(:jar)
      package(:sources)
    end
    
    desc "Psc own implementations for the da-launcher"
    define "da-launcher" do
      project.version = "1.1.1"
      compile.with PSC_DA_LAUNCHER, OSGI, FELIX, EQUINOX, KNOPFLERFISH, GLOBUS.servlet
      package(:jar)
      package(:sources)
    end
  end

  desc "The domain classes for PSC"
  define "domain" do
    bnd.wrap!
    bnd.name = "PSC Domain Model"
    bnd.import_packages <<
      "org.hibernate;version=3.3" <<
      "org.hibernate.type;version=3.3" <<
      "org.hibernate.cfg;version=3.3"

    compile.with project('utility'), SLF4J.api, 
      CTMS_COMMONS.lang, CTMS_COMMONS.core,
      JAKARTA_COMMONS.beanutils, JAKARTA_COMMONS.collections, 
      JAKARTA_COMMONS.lang, JAKARTA_COMMONS.collections_generic,
      SPRING, SECURITY.acegi, SECURITY.csm, HIBERNATE
    test.with(UNIT_TESTING)

    package(:jar)
    package(:sources)
  end

  desc "Database configuration and testing"
  define "database" do
    # Migrations are resources, too
    resources.enhance([_("src/main/db/migrate")]) do
      filter.from(_("src/main/db/migrate")).
        into(resources.target.to_s + "/db/migrate").run
    end    
    compile.with BERING, SLF4J.api, SLF4J.jcl, SPRING, CORE_COMMONS, 
      CTMS_COMMONS.core, JAKARTA_COMMONS, DB, HIBERNATE, EHCACHE
    test.with UNIT_TESTING
    
    # Automatically generate the HSQLDB when the migrations change
    # if using hsqldb.
    test.enhance hsqldb[:files]
    hsqldb[:files].each do |f|
      file(f => Dir[_('src/main/db/migrate/**/*')]) do
        if hsqldb?
          task(:create_hsqldb).invoke
        end
      end
    end
    
    task :migrate do
      ant('bering') do |ant|
        # Load DS properties from /etc/psc or ~/.psc
        datasource_properties(ant)
        ant.echo :message => "Migrating ${datasource.url}"
        
        # default values
        ant.property :name => 'migrate.version', :value => ENV['MIGRATE_VERSION'] || ""
        ant.property :name => 'bering.dialect', :value => ""
        
        ant.taskdef :resource => "edu/northwestern/bioinformatics/bering/antlib.xml",
          :classpath => ant_classpath(project('database'))
        ant.migrate :driver => '${datasource.driver}',
          :dialect => "${bering.dialect}",
          :url => "${datasource.url}",
          :userid => "${datasource.username}",
          :password => "${datasource.password}",
          :targetVersion => "${migrate.version}",
          :migrationsDir => _("src/main/db/migrate"),
          :classpath => ant_classpath(project('database'))
        if hsqldb?
          # database must be explicitly shutdown in HSQLDB >=1.7.2, so that the lock is
          # released and the tests can reopen it
          
          # TODO: this sometimes (why not in all situations?) kills the buildr
          #  process.  Using CHECKPOINT instead also kills the process, 
          #  as do plain SHUTDOWN and SHUTDOWN COMPACT.
          ant.sql :driver => "${datasource.driver}", 
            :url => "${datasource.url}",
            :userid => "${datasource.username}",
            :password => "${datasource.password}",
            :autocommit => "true",
            :classpath => ant_classpath(project('database')),
            :pcdata => "SHUTDOWN SCRIPT;"
        end
      end
    end
    
    task :create_hsqldb => :clean_hsqldb do |t|
       psc_dir = "#{ENV['HOME']}/.psc"
       mkdir_p psc_dir
       File.open("#{psc_dir}/#{db_name}.properties", 'w') do |f|
         f.puts( (<<-PROPERTIES).split(/\n/).collect { |row| row.strip }.join("\n") )
           # Generated by PSC's psc:database:create_hsqldb task
           datasource.url=#{hsqldb[:url]}
           datasource.username=sa
           datasource.password=
           datasource.driver=org.hsqldb.jdbcDriver
         PROPERTIES
       end

       # Apply the bering migrations to build the HSQLDB schema
       mkdir_p hsqldb[:dir]
       task(:migrate).invoke

       # Mark read-only
       File.open("#{hsqldb[:dir]}/#{db_name}.properties", 'a') do |f|
         f.puts "hsqldb.files_readonly=true"
       end

       info "Read-only HSQLB instance named #{db_name} generated in #{hsqldb[:dir]}"
    end
    
    task :clean_hsqldb do
       rm_rf hsqldb[:dir]
    end
    
    package(:jar)
    package(:sources)
  end # database
  
  desc "Pluggable authentication definition and included plugins"
  define "authentication" do
    desc "PSC's framework for using the authentication plugins"
    define "socket" do
      bnd.wrap!
      bnd.name = "PSC Pluggable Auth Socket"
      bnd.import_packages <<
        "org.acegisecurity.context" <<
        "org.acegisecurity.providers.anonymous" <<
        "org.acegisecurity.providers.dao.cache" <<
        "org.acegisecurity.intercept.web" <<
        "org.acegisecurity.ui.logout" <<
        "org.acegisecurity.wrapper" <<
        "org.acegisecurity.vote" <<
        "org.springframework.cache.ehcache"
      compile.with project('plugin-api').and_dependencies, SPRING_OSGI,
        project('domain').and_dependencies, EHCACHE
      test.with UNIT_TESTING,
        project('authentication:local-plugin'),
        project('plugin-api').test_dependencies,
        project('test-infrastructure').and_dependencies, 
        project('test-infrastructure').test_dependencies
      package(:jar)
    end
    
    desc "Interfaces and base classes for PSC's pluggable authentication system"
    define "plugin-api" do
      bnd.wrap!
      bnd.name = "PSC Pluggable Auth API"
      compile.with project('utility'), SLF4J.api, OSGI,
        CONTAINER_PROVIDED, SPRING, SECURITY.acegi, CTMS_COMMONS.core, 
        JAKARTA_COMMONS.lang
      test.with UNIT_TESTING, EHCACHE,
        project('test-infrastructure').and_dependencies,
        project('test-infrastructure').test_dependencies
      package(:jar)
    end
    
    desc "Authentication using PSC's local CSM instance"
    define "local-plugin" do
      bnd.wrap!
      bnd.name = "PSC Local Auth Plugin"
      bnd['Bundle-Activator'] = 
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.local.Activator"
      compile.with project('plugin-api').and_dependencies, SECURITY.csm
      test.with project('plugin-api').test_dependencies,
        project('domain').and_dependencies, project('domain').test_dependencies,
        project('database').and_dependencies, project('database').test_dependencies, DB
      test.resources.filter.using(:ant, 'application-short-name'  => APPLICATION_SHORT_NAME)
      package(:jar)
    end
    
    desc "Authentication via an enterprise-wide CAS server"
    define "cas-plugin" do
      bnd.wrap!
      bnd.name = "PSC CAS Auth Plugin"
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.Activator"
      bnd.import_packages << 
        "org.springframework.beans.factory.config;version=2.5" <<
        "org.springframework.cache.ehcache;version=2.5" <<
        "org.acegisecurity.providers.cas" <<
        "org.acegisecurity.providers.cas.cache" <<
        "org.acegisecurity.providers.cas.populator" <<
        "org.acegisecurity.providers.cas.proxy" <<
        "org.acegisecurity.providers.cas.ticketvalidator" <<
        "org.acegisecurity.ui.cas" <<
        "org.acegisecurity.ui.logout"
      compile.with project('plugin-api').and_dependencies, SECURITY.cas, EHCACHE
      test.with project('plugin-api').test_dependencies
      package(:jar)
    end
    
    desc "Authentication via caGrid's customized version of CAS"
    define "websso-plugin" do
      bnd.wrap!
      bnd.name = "PSC caGrid WebSSO Auth Plugin"
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.Activator"
      bnd.import_packages << 
        "org.springframework.beans.factory.config;version=2.5" <<
        "org.springframework.cache.ehcache;version=2.5" <<
        "org.acegisecurity.providers.cas" <<
        "org.acegisecurity.providers.cas.cache" <<
        "org.acegisecurity.providers.cas.populator" <<
        "org.acegisecurity.providers.cas.proxy" <<
        "org.acegisecurity.providers.cas.ticketvalidator" <<
        "org.acegisecurity.ui.cas" <<
        "org.acegisecurity.ui.logout" <<
        "gov.nih.nci.cabig.caaers.web.security.cas"
      compile.with project('plugin-api').and_dependencies,
        project('cas-plugin').and_dependencies, SECURITY.caaers_cas
      test.with project('plugin-api').test_dependencies,
        project('cas-plugin').test_dependencies, 
        project('domain').and_dependencies, 
        project('domain').test_dependencies
      package(:jar)
    end
    
    desc "A completely insecure implementation for integrated tests and the like"
    define "insecure-plugin" do
      bnd.wrap!
      bnd.name = "PSC Insecure Auth Plugin"
      bnd['Bundle-Activator'] = 
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure.Activator"
      compile.with project('plugin-api').and_dependencies
      test.with project('plugin-api').test_dependencies, 
        project('domain').and_dependencies, project('domain').test_dependencies
      package(:jar)
    end
  end

  desc "External data-providing plugins" 
  define "providers" do
    desc "The interfaces under which data providers expose data"
    define "api" do
      bnd.wrap!
      bnd.name = "PSC Data Providers API"
      compile.with project('domain').and_dependencies
      package(:jar)
    end
    
    desc "Mock data providers with static data"
    define "mock" do
      bnd.wrap!
      bnd.name = "PSC Mock Data Providers"
      bnd.autostart = false
      compile.with project('providers:api').and_dependencies, SPRING
      test.with UNIT_TESTING
      package(:jar)
    end
    
    desc "Data providers which talk directly to COPPA"
    define "coppa-direct" do
      bnd.wrap!
      bnd.name = "PSC COPPA-based Data Providers"
      bnd.autostart = false
      bnd['Bundle-Activator'] = 
        "edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct.Activator"
      bnd.import_packages <<
        "org.apache.axis.types" <<
        "org.apache.axis.message.addressing"
      
      compile.with project('providers:api').and_dependencies, SPRING, OSGI, 
        GLOBUS, COPPA
      test.using(:junit).with UNIT_TESTING
      package(:jar)
    end
    
    desc "Commands for interacting with the providers from the felix console"
    define "felix-commands" do
      compile.with FELIX.shell, OSGI.core,
        project('providers:api').and_dependencies
      test.with UNIT_TESTING, project('domain').test_dependencies

      bnd.wrap!
      bnd.name = "PSC Data Provider Felix Shell Commands"
      bnd['Bundle-Activator'] = 
        "edu.northwestern.bioinformatics.studycalendar.dataproviders.commands.Activator"
      package(:jar)
    end
  end
  
  desc "Core data access, serialization and non-substitutable business logic"
  define "core" do
    project('providers') # Have to reference this before refing project('providers:mock') in buildr 1.3.3 for some reason.  Investigate later.  RMS20090331.
    
    resources.filter.using(:ant, 
      'application-short-name'  => APPLICATION_SHORT_NAME,
      "buildInfo.versionNumber" => project.version,
      "buildInfo.username"      => ENV['USER'],
      "buildInfo.hostname"      => `hostname`.chomp,
      "buildInfo.timestamp"     => Time.now.strftime("%Y-%m-%d %H:%M:%S")
    )
    
    compile.with project('domain').and_dependencies,
      project('providers:api').and_dependencies,
      project('database').and_dependencies,
      project('utility:osgimosis').and_dependencies,
      XML, RESTLET.framework, FREEMARKER, CSV,
      QUARTZ, SECURITY, OSGI, SLF4J.jcl,
      CONTAINER_PROVIDED, SPRING_WEB # tmp for mail

    test.with UNIT_TESTING, project('domain').test.compile.target, 
      project('database').test_dependencies

    package(:jar)
    package(:sources)
    
    check do
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))
      
      acSetup.should include(`hostname`.chomp)
      acSetup.should include(project.version)
    end
  end # core
  
  desc "Submodules related to building and deploying PSC's embedded plugin layer"
  define "osgi-layer" do
    task :da_launcher_artifacts do |task|
      class << task; attr_accessor :values; end
      knopflerfish_main = artifact(KNOPFLERFISH.framework)
      felix_main = artifact(FELIX.main)
      equinox_main = artifact(EQUINOX.osgi)

      if true # knopflerfish?
        system_optional = [FELIX.shell_remote, KNOPFLERFISH.consoletelnet]
        system_bundles = KNOPFLERFISH.values.reject { |a| a.to_s =~ /framework-/ } - system_optional + [FELIX.shell]
        osgi_framework = { "osgi-framework/knopflerfish/#{knopflerfish_main.version}" => [knopflerfish_main] }
      elsif false # felix?
        system_optional = [FELIX.shell_remote]
        system_bundles = FELIX.values - [FELIX.main] - system_optional
        osgi_framework = { "osgi-framework/felix/#{felix_main.version}" => [felix_main] }
      else
        system_optional = [FELIX.shell_remote]
        system_bundles = EQUINOX.values - [EQUINOX.osgi] - system_optional + [FELIX.shell]
        osgi_framework = { "osgi-framework/equinox/#{equinox_main.version.split('.')[0 .. 2].join('.')}" => [equinox_main] }
      end
      
      system_bundles += (LOG4J.values + [SLF4J.api, SLF4J.jcl]).collect { |spec| artifact(spec) } + 
        [ project('osgi-layer:log4j-configuration').packages.first ]

      bundle_projects = Buildr::projects.select { |p| p.bnd.wrap? }
      application_bundles = 
        bundle_projects.select { |p| p.bnd.autostart? }.collect { |p| p.package(:jar) } - system_bundles
      application_optional = 
        bundle_projects.select { |p| !p.bnd.autostart? }.collect { |p| p.package(:jar) } - system_bundles
      application_infrastructure = 
        [ SPRING_OSGI.extender, GLOBUS.core, GLOBUS.jaxb_api, STAX_API ].collect { |a| artifact(a) }
      application_libraries = bundle_projects.
        collect { |p| p.and_dependencies }.flatten.uniq.
        select { |a| Buildr::Artifact === a }.
        reject { |a| a.to_s =~ /osgi_R4/ }.reject { |a| a.to_s =~ /sources/ } -
        system_bundles - application_bundles - application_infrastructure

      task.values = osgi_framework.merge(
        "bundles/system-bundles" => system_bundles,
        "bundles/system-optional" => system_optional,
        "bundles/application-bundles" => application_bundles,
        "bundles/application-optional" => application_optional,
        "bundles/application-infrastructure" => application_infrastructure,
        "bundles/application-libraries" => application_libraries
      )
    end
    
    task :build_test_da_launcher => ["psc:osgi-layer:da_launcher_artifacts"] do |task|
      mkdir_p _('target', 'test')
      rm_rf _('target', 'test', 'da-launcher')
      cp_r project('web')._('src', 'main', 'webapp', 'WEB-INF', 'da-launcher'), _('target', 'test')
      task("psc:osgi-layer:da_launcher_artifacts").values.each do |path, artifacts|
        dadir = _("target/test/da-launcher/#{path}")
        mkdir_p dadir
        artifacts.each { |a| 
          trace "Putting #{a} in #{path}"
          a.invoke; 
          cp a.to_s, dadir 
        }
      end
    end
    
    task :examine => [:build_test_da_launcher, 'psc:osgi-layer:compile'] do
      cd _("target/classes") do
        mkdir_p _('tmp/logs')
        deps = project.test.dependencies.collect { |p| p.to_s }
        if ENV['WEBAPP_SIM']
          deps = [deps, project('psc:web').and_dependencies].flatten.uniq
        end
        classpath = deps.collect { |d| d.to_s }.join(':')
        puts "Classpath:\n- #{deps.join("\n- ")}"
        system("java -Dcatalina.home=#{_('tmp')} -cp #{classpath} edu.northwestern.bioinformatics.studycalendar.osgi.DaLauncherConsole #{_('target', 'test', 'da-launcher')}")
      end
    end
    
    compile.with project('utility:da-launcher').and_dependencies, OSGI
    
    desc "Advertises host-configured services to the OSGi layer"
    define "host-services" do
      bnd.wrap!
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.Activator"
      bnd.name = "PSC OSGi Layer Access to Host Services"
      
      compile.with project('utility').and_dependencies, SECURITY.acegi, OSGI
      test.using(:junit).with UNIT_TESTING, 
        project('domain').and_dependencies, project('domain').test_dependencies
      
      package(:jar)
    end
    
    define "log4j-configuration" do
      bnd.wrap!
      bnd.name = "PSC OSGi Layer log4j Configuration"
      bnd['Fragment-Host'] = 'com.springsource.org.apache.log4j'
      
      package(:jar)
    end
    
    define "integrated-tests" do
      test.using(:junit).with UNIT_TESTING, project('utility:da-launcher').and_dependencies, 
        project('authentication:socket').and_dependencies,
        project('authentication:cas-plugin').and_dependencies,
        project('web').and_dependencies,
        project('web').test_dependencies,
        project('authentication:plugin-api').test_dependencies
      test.enhance([:build_test_da_launcher])
    end
  end
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    compile.with SLF4J, LOGBACK, CTMS_COMMONS.web,
      project('core').and_dependencies,
      project('authentication:plugin-api').and_dependencies,
      project('authentication:socket').and_dependencies,
      project('osgi-layer:host-services').and_dependencies,
      SPRING_WEB, RESTLET, WEB, project('utility:da-launcher').and_dependencies

    test.with project('test-infrastructure').and_dependencies, 
      project('test-infrastructure').test_dependencies,
      project('authentication:socket').test_dependencies

    package(:war, :file => _('target/psc.war')).tap do |war|
      war.libs -= artifacts(CONTAINER_PROVIDED)
      war.libs -= war.libs.select { |artifact| artifact.respond_to?(:classifier) && artifact.classifier == 'sources' }
      war.enhance ["psc:osgi-layer:da_launcher_artifacts"] do
        task("psc:osgi-layer:da_launcher_artifacts").values.each do |path, artifacts|
          war.path("WEB-INF/da-launcher").path(path).include(artifacts.collect { |a| a.invoke; a.name })
        end
      end
    end
    package(:sources)
    
    iml.add_component("FacetManager") do |component|
      component.facet :type => 'web', :name => 'Web' do |facet|
        facet.configuration do |conf|
          conf.descriptors do |desc|
            desc.deploymentDescriptor :name => 'web.xml', 
              :url => "file://$MODULE_DIR$/src/main/webapp/WEB-INF/web.xml", 
              :optional => "false", :version => "2.4"
          end
          conf.webroots do |webroots|
            webroots.root :url => "file://$MODULE_DIR$/src/main/webapp", :relative => "/"
          end
        end
      end
    end
    
    task :explode => [compile, "psc:osgi-layer:da_launcher_artifacts"] do
      packages.detect { |pkg| pkg.to_s =~ /war$/ }.tap do |war_package|
        war_package.classes.each do |clz_src|
          filter.from(clz_src).into(_('src/main/webapp/WEB-INF/classes')).run
        end
        libdir = _('src/main/webapp/WEB-INF/lib')
        mkdir_p libdir
        war_package.libs.each do |lib|
          cp lib.to_s, libdir
        end
        task('psc:osgi-layer:da_launcher_artifacts').values.each do |path, artifacts|
          dadir = _("src/main/webapp/WEB-INF/da-launcher/#{path}")
          mkdir_p dadir
          artifacts.each { |a| a.invoke; cp a.to_s, dadir }
        end
      end
    end
    
    task :local_jetty do
      ENV['test'] = 'no'
      set_db_name 'datasource'
      
      task(:jetty_deploy_exploded).invoke
      
      msg = "PSC deployed at #{jetty.url}/psc.  Press ^C to stop."
      info "=" * msg.size
      info msg
      info "=" * msg.size

      # Keep the script running until interrupted
      while(true)
        sleep(1)
      end
    end
    
    directory _('tmp/logs')
    
    task :jetty_deploy_exploded => ['psc:web:explode', _('tmp/logs')] do
      logconfig = _('src/main/webapp/WEB-INF/classes/logback.xml')
      rm _('src/main/webapp/WEB-INF/classes/logback.xml')
      filter(_('src/main/java')).
        using(:maven, 'catalina.home' => _('tmp').to_s).
        include(File.basename(logconfig)).
        into(File.dirname(logconfig)).
        run
      Java.java.lang.System.setProperty("logback.configurationFile", logconfig)
      Java.java.lang.System.setProperty("catalina.home", _('tmp').to_s)

      jetty.deploy "#{jetty.url}/psc", _('src/main/webapp').to_s
    end
    
    # exclude exploded files from IDEA
    iml.excluded_directories << 
      _('src/main/webapp/WEB-INF/da-launcher/bundles') << 
      _('src/main/webapp/WEB-INF/da-launcher/runtime') << 
      _('src/main/webapp/WEB-INF/da-launcher/logs') << 
      _('src/main/webapp/WEB-INF/da-launcher/osgi-framework') << 
      _('src/main/webapp/WEB-INF/lib') << 
      _('src/main/webapp/WEB-INF/classes')
    
    # clean exploded files, too
    clean(["psc:osgi-layer:da_launcher_artifacts"]) {
      dal_paths = [task('psc:osgi-layer:da_launcher_artifacts').values.keys + %w(logs runtime osgi-framework/felix osgi-framework/knopflerfish osgi-framework/equinox)].
        flatten.collect { |path| "da-launcher/#{path}" }
      (%w(lib classes) + dal_paths).each do |exploded_path|
        rm_rf _("src/main/webapp/WEB-INF/#{exploded_path}")
      end
    }
    
    desc "Specs for client-side javascript"
    define "js-spec" do
      # using project('psc:web')._(:source, :main, :webapp, "js") causes a bogus
      # circular dependency failure
      test.using :shenandoah, :main_path => _("../src/main/webapp/js")
    end
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB, OSGI,
      project('core').and_dependencies
    test.with project('core').test_dependencies
    package(:jar)
    package(:sources)
  end
  
  desc "Integrated tests for the RESTful API"
  define "restful-api-test", :base_dir => _('test/restful-api') do
    # Only set_db after everything is built
    task :set_db => project('psc:web').task(:explode) do
      set_db_name(ENV['INTEGRATION_DB'] || 'rest-test')
      test.options[:properties]['psc.config.datasource'] = db_name
    end
    
    compile.with(project('web').and_dependencies)
    test.using(:integration, :rspec).
      with(
        project('test-infrastructure'), 
        project('test-infrastructure').compile.dependencies, 
        project('test-infrastructure').test.compile.dependencies
      ).using(
        :gems => { 'rest-open-uri' => '1.0.0', 'builder' => '2.1.2', 'json_pure' => '1.1.3' },
        :requires => %w(spec http static_data).collect { |help| _("src/spec/ruby/#{help}_helper.rb") },
        :properties => { 
          'applicationContext.path' => File.join(test.resources.target.to_s, "applicationContext.xml"),
        }
      )
    test.resources.filter.using(:ant, :'resources.target' => test.resources.target.to_s)

    integration.setup {
      task(:set_db).invoke 
      task('psc:web:jetty_deploy_exploded').invoke
    }
    
    desc "One-time setup for the RESTful API integrated tests"
    task :setup => [:set_db, :'test:compile', project('psc:database').task('migrate')] do
      Java::Commands.java(
        'edu.northwestern.bioinformatics.studycalendar.test.restfulapi.OneTimeSetup', project('psc')._, 
        :classpath => test.compile.dependencies,
        :properties => { "psc.config.datasource" => db_name })
    end
  end
  
  # This is just a direct port from ant -- might be possible to do something better with buildr
  desc "Build the binary distribution package"
  task :dist do |task|
    class << task; attr_accessor :filename; end
    ENV['test'] = 'no'
    task('psc:web:package').invoke

    dist_dir = "target/dist/bin"
    mkdir_p _("#{dist_dir}/conf-samples")
    cp _("db/datasource.properties.example"), _("#{dist_dir}/conf-samples/datasource.properties")
    cp project('web').packages.select { |p| p.type == :war }.to_s, _("#{dist_dir}/psc.war")
    puts `svn export https://svn.bioinformatics.northwestern.edu/studycalendar/documents/PSC_Install_Guide.doc #{_("#{dist_dir}/psc_install.doc")}`

    task.filename = _("target/dist/psc-#{VERSION_NUMBER}-bin.zip")
    zip(task.filename).path("psc-#{VERSION_NUMBER}").include("#{dist_dir}/*").root.invoke
  end
end

###### Shared configuration

projects.each do |p|
  if File.exist?(p._("src/test/java"))
    # Use same logback test config for all modules
    logback_test_src = project("psc")._("src/test/resources/logback-test.xml")
    logback_test_dst = File.join(p.test.resources.target.to_s, "logback-test.xml")
    file logback_test_dst => logback_test_src do |t|
      filter.clear.from(project("psc")._("src/test/resources")).
        include("logback-test.xml").
        into(p.test.resources.target.to_s).
        using(:project_root => p._).
        run
    end
    p.test.resources.enhance [logback_test_dst.to_sym]
  end
end

###### Top-level aliases for commonly-used tasks

desc "Update the core database schema via bering.  Override the target version with MIGRATE_VERSION=X-Y."
task :migrate => 'psc:database:migrate'

desc "Manually create the HSQLDB instance for unit testing"
task :create_hsqldb => 'psc:database:create_hsqldb'

desc "Run PSC from #{jetty.url}/psc"
task :server => 'psc:web:local_jetty'

###### Continuous integration

namespace :ci do
  desc "Continuous unit test build"
  task :unit => [:clean, :'psc:database:clean_hsqldb'] do
    task('psc:database:migrate').invoke unless hsqldb?
    task(:test).invoke
    if emma?
      [:'emma:html', :'emma:xml'].each { |t| task(t).invoke }
    end
  end
  
  task :nightly => [:unit, 'psc:dist', :artifacts_dir] do
    now = Time.now.strftime "%Y%m%d-%H%M%S"
    cp task('psc:dist').filename, "#{task('ci:artifacts_dir').dir}/psc-#{VERSION_NUMBER}-#{now}.zip"
  end
  
  directory project('psc')._('target/artifacts')
  task :artifacts_dir => project('psc')._('target/artifacts') do |task|
    class << task; attr_accessor :dir; end
    task.dir = task.prerequisites.first.to_s
  end
end