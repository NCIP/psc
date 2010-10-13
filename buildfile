ENV['JAVA_OPTS'] ||= "-Xmx512M -XX:MaxPermSize=256M -Dcom.sun.management.jmxremote"
puts "Using JAVA_OPTS=#{ENV['JAVA_OPTS'].inspect}"

require "buildr"
require "buildr/jetty"
require "buildr/emma" if emma?
require "shenandoah/buildr"
require "buildr/core/filter"
require 'fileutils'
require 'rexml/document'
require 'buildr_iidea'

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

# Version number is set in build/project.properties for BDA compatibility
VERSION_NUMBER=File.read(File.expand_path("../build/project.properties", __FILE__)).
  scan(/^psc-webapp.version=(.*?)$/).first.first
APPLICATION_SHORT_NAME = 'psc'

###### Jetty config

# enable JSP support in Jetty
Java.classpath.concat([
  "org.mortbay.jetty:jsp-api-2.1:jar:#{Buildr::Jetty::VERSION}",
  "org.mortbay.jetty:jsp-2.1:jar:#{Buildr::Jetty::VERSION}"
])
jetty.url = jetty_url

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

  iml.local_repository_env_override = nil

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

  desc "Ensures that the BDA build includes an explicit install for each gem mentioned in build.yml"
  task :verify_bda_gems do
    requirements = *Buildr.settings.build["gems"].
      collect { |line| line.split(/\s+/, 2) }.
      collect { |name, version| [name, (Gem::Requirement.new(version) if version)] }

    bda_installs = {}
    REXML::Document.new(File.new _("build-adapter.xml")).elements.each("project/property") do |prop|
      if prop.attributes["name"] =~ /gems.(\S+).version/
        bda_installs[$1] = Gem::Version.new(prop.attributes["value"])
      end
    end

    missing = requirements.reject { |gem, req|
      (req.nil? && bda_installs.keys.include?(gem)) ||
      (bda_installs[gem] && req.satisfied_by?(bda_installs[gem]))
    }
    unless missing.empty?
      fail "The BDA installer is missing the following gem#{'s' unless missing.size == 1}:\n" <<
        "- #{missing.collect { |pair| pair.join(' ') }.join("\n- ")}"
    end
  end

  desc "Pure utility code"
  define "utility" do
    bnd.wrap!
    bnd.name = "PSC Utility Module"

    compile.with SLF4J.api, SPRING, JAKARTA_COMMONS.collections,
      CTMS_COMMONS.base, CTMS_COMMONS.lang, CTMS_COMMONS.core, CONTAINER_PROVIDED
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
      CTMS_COMMONS.base, CTMS_COMMONS.lang, CTMS_COMMONS.core,
      JAKARTA_COMMONS.beanutils, JAKARTA_COMMONS.collections,
      JAKARTA_COMMONS.lang, JAKARTA_COMMONS.collections_generic,
      SPRING, SECURITY.acegi, SECURITY.csm, HIBERNATE, HIBERNATE_ANNOTATIONS
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
      CTMS_COMMONS.base, CTMS_COMMONS.core, JAKARTA_COMMONS, db_deps,
      HIBERNATE, EHCACHE, HIBERNATE_ANNOTATIONS
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
       psc_dir = "#{Java.java.lang.System.getProperty("user.home")}/.psc"
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

  define "authorization" do
    bnd.wrap!
    bnd.name = "PSC Authorization Implementation"
    bnd.import_packages <<
      "org.acegisecurity" <<
      "org.springframework.core" <<
      "gov.nih.nci.cabig.ctms.domain"
    compile.with SECURITY.acegi, project('domain').and_dependencies, SECURITY.csm, SECURITY.clm,
      SECURITY.suite_authorization
    test.with UNIT_TESTING, project('domain').test_dependencies
    package(:jar)
  end

  desc "Pluggable authentication definition and included plugins"
  define "authentication" do
    desc "Interfaces and base classes for PSC's pluggable authentication system"
    define "plugin-api" do
      bnd.wrap!
      bnd.name = "PSC Pluggable Auth API"
      compile.with project('utility'), project('authorization'),
        SLF4J.api, OSGI, CONTAINER_PROVIDED, SPRING, SECURITY.acegi,
        CTMS_COMMONS.core, JAKARTA_COMMONS.lang, SPRING_OSGI,
        SECURITY.suite_authorization
      test.with UNIT_TESTING, EHCACHE,
        project('mocks').and_dependencies,
        project('domain').and_dependencies,
        project('domain').test_dependencies
      package(:jar)
    end

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
        project.parent.project('local-plugin'),
        project('plugin-api').test_dependencies,
        project('domain').test_dependencies,
        SECURITY.suite_authorization
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
        project('database').and_dependencies, project('database').test_dependencies, db_deps
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
      compile.with project('plugin-api').and_dependencies, SECURITY.cas,
        EHCACHE, JAKARTA_COMMONS.httpclient, HTMLPARSER
      test.with project('plugin-api').test_dependencies, JAKARTA_COMMONS.io
      package(:jar)
    end

    desc "Authentication via caGrid's customized version of CAS"
    define "websso-plugin" do
      bnd.wrap!
      bnd.name = "PSC caGrid WebSSO Auth Plugin"
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.Activator"
      bnd.import_packages.clear
      bnd.import_packages <<
        "!org.globus.gsi" << "*" <<
        "org.springframework.beans.factory.config;version=2.5" <<
        "org.springframework.cache.ehcache;version=2.5" <<
        "org.acegisecurity.providers.cas" <<
        "org.acegisecurity.providers.cas.cache" <<
        "org.acegisecurity.providers.cas.populator" <<
        "org.acegisecurity.providers.cas.proxy" <<
        "org.acegisecurity.providers.cas.ticketvalidator" <<
        "org.acegisecurity.ui.cas" <<
        "org.acegisecurity.ui.logout" <<
        "gov.nih.nci.cabig.caaers.web.security.cas" <<
        "org.apache.commons.httpclient" << # an instance is directly created in cas-authentication-beans.xml, so it needs to be visible
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct" <<
        "gov.nih.nci.cagrid.metadata"
      compile.with project('plugin-api').and_dependencies,
        project('cas-plugin').and_dependencies,
        project('domain').and_dependencies,
        SECURITY.caaers_cas, CAGRID,
        GLOBUS.core, GLOBUS_UNDUPLICABLE

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
      bnd.import_packages <<
        "edu.northwestern.bioinformatics.studycalendar.domain.delta" <<
        "edu.northwestern.bioinformatics.studycalendar.domain.tools" <<
        "gov.nih.nci.cabig.ctms.domain"
      iml.id = "providers-mock"
      compile.with parent.project('api').and_dependencies, SPRING
      test.with UNIT_TESTING, project('domain').test_dependencies
      package(:jar)
    end

    desc "Data providers which talk to COPPA"
    define "coppa" do
      define "common" do
        bnd.wrap!
        bnd.name = "PSC COPPA Data Providers Common Library"
        bnd.autostart = false
        bnd.import_packages <<
          "edu.northwestern.bioinformatics.studycalendar.domain.delta" <<
          "edu.northwestern.bioinformatics.studycalendar.domain.tools" <<
          "gov.nih.nci.cabig.ctms.domain"

        compile.with parent.project('api').and_dependencies, SPRING, OSGI,
          GLOBUS, CAGRID, COPPA
        test.using(:junit).with UNIT_TESTING, project('domain').test_dependencies
        package(:jar)
      end

      define "ihub" do
        bnd.wrap!
        bnd.name = "PSC COPPA Integration Hub Data Providers"
        bnd.description = "A suite of data providers which communicate with COPPA via caBIG Integration Hub"
        bnd.autostart = false
        bnd['Bundle-Activator'] =
          "edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub.Activator"
        bnd.import_packages.clear
        bnd.import_packages <<
          "!org.globus.gsi" << "*" <<
          "edu.northwestern.bioinformatics.studycalendar.domain.delta" <<
          "edu.northwestern.bioinformatics.studycalendar.domain.tools" <<
          "gov.nih.nci.cabig.ctms.domain"

        compile.with project('psc:providers:coppa:common').and_dependencies,
          CIH, GLOBUS_UNDUPLICABLE, project('authorization')
        test.using(:junit).with UNIT_TESTING
        package(:jar)
      end
    end

    desc "Commands for interacting with the providers from the felix console"
    define "felix-commands" do
      compile.with FELIX.shell, OSGI.core,
        parent.project('api').and_dependencies
      test.with UNIT_TESTING, project('domain').test_dependencies
      iml.id = "providers-felix-commands"

      bnd.wrap!
      bnd.name = "PSC Data Provider Felix Shell Commands"
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.dataproviders.commands.Activator"
      package(:jar)
    end
  end

  desc "Submodules related to building and deploying PSC's embedded plugin layer"
  define "osgi-layer" do
    task :da_launcher_artifacts do |task|
      class << task; attr_accessor :values; end
      knopflerfish_main = artifact(KNOPFLERFISH.framework)
      felix_main = artifact(FELIX.main)
      equinox_main = artifact(EQUINOX.osgi)

      system_optional = [(FELIX.shell_remote unless ENV['OSGI_TELNET'] == 'yes')].compact
      system_bundles = FELIX.values - [FELIX.main, FELIX.framework] - system_optional
      osgi_framework = { "osgi-framework/felix/#{felix_main.version}" => [felix_main] }

      system_bundles += (LOGBACK.values + [SLF4J.api, SLF4J.jcl]).collect { |spec| artifact(spec) } +
        [ project('osgi-layer:log-configuration').packages.first ]

      bundle_projects = Buildr::projects.select { |p| p.bnd.wrap? }
      application_bundles =
        bundle_projects.select { |p| p.bnd.autostart? }.collect { |p| p.package(:jar) } - system_bundles
      application_optional =
        bundle_projects.select { |p| !p.bnd.autostart? }.collect { |p| p.package(:jar) } - system_bundles
      application_infrastructure =
        [ SPRING_OSGI.extender, GLOBUS.jaxb_api, STAX_API ].collect { |a| artifact(a) }
      application_libraries = bundle_projects.
        collect { |p| p.and_dependencies }.flatten.uniq.
        select { |a| Buildr::Artifact === a }.
        reject { |a| a.to_s =~ /org.osgi/ }.reject { |a| a.to_s =~ /sources/ } -
        system_bundles - application_bundles - application_infrastructure - [FELIX.shell] - GLOBUS_UNDUPLICABLE.values

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

    task :examine => :'psc:osgi-layer:console:run'

    task :analyze_package_consistency => [:build_test_da_launcher] do
      test_dal = _('target', 'test', 'da-launcher')
      boot_packages = File.read("#{test_dal}/config/osgi-framework.xml").
        grep(/org.osgi.framework.bootdelegation/).first.gsub(/<.*?>/, "").strip.split(',') +
        ['java.*', 'javax.*', 'org.xml.sax.*', 'org.w3c.dom.*', 'org.omg.*']
      AnalyzeOsgiConsistency.analyze_packages(Dir["#{test_dal}/**/*.jar"], boot_packages)
    end

    # Finds packages which contain classes in different bundles.
    # Note that these are not all necessarily errors -- fragment bundles
    # may extend packages from their associated bundles and internal-only
    # packages may be repeated across bundles
    task :find_duplicate_packages => [:build_test_da_launcher] do
      Dir[_('target', 'test', 'da-launcher') + "/**/*.jar"].inject({}) { |h, jar|
        `jar tf #{jar}`.split(/\n/).grep(/.class$/).collect { |path|
          path.sub(/\/[^\/]+$/, '').gsub('/', '.')
        }.uniq.each { |package|
          h[package] ||= []
          h[package] << jar
        }
        h
      }.each_pair { |package, jars|
        if jars.size > 1
          puts package
          puts '=' * package.size
          jars.each { |jar| puts "- #{jar}"}
          puts
        end
      }
    end

    define "console" do
      compile.with SLF4J, LOGBACK, DYNAMIC_JAVA, FELIX.main,
        project('core').and_dependencies

      task :run => [:build_test_da_launcher, 'psc:osgi-layer:console:compile'] do
        cd _("target/classes") do
          mkdir_p _('tmp/logs')
          deps = project.test.dependencies.collect { |p| p.to_s }
          if ENV['WEBAPP_SIM']
            deps = [deps, project('psc:web').and_dependencies].flatten.uniq
            deps += GLOBUS_UNDUPLICABLE.values if (env_true?('WEBSSO') || env_true?('GLOBUS'))
          end
          classpath = deps.collect { |d| d.to_s }.join(':')
          puts "Classpath:\n- #{deps.join("\n- ")}"
          system("java -Dcatalina.home=#{_('tmp')} -cp #{classpath} edu.northwestern.bioinformatics.studycalendar.osgi.console.DaLauncherConsole #{project('osgi-layer')._('target', 'test', 'da-launcher')}")
        end
      end
    end

    desc "Advertises host-configured services to the OSGi layer"
    define "host-services" do
      bnd.wrap!
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.Activator"
      bnd.name = "PSC OSGi Layer Access to Host Services"
      bnd.import_packages <<
        "org.acegisecurity.userdetails" <<
        "edu.northwestern.bioinformatics.studycalendar.domain"

      compile.with project('utility').and_dependencies,
        project('authorization'), SECURITY.acegi, OSGI, FELIX.configadmin
      test.using(:junit).with UNIT_TESTING,
        project('domain').and_dependencies, project('domain').test_dependencies

      package(:jar)
    end

    desc "Routes from the OSGi Log Service to SLF4J"
    define 'log-adapter' do
      bnd.wrap!
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.log.Adapter"
      bnd.name = "PSC OSGi Log to SLF4J Adapter"

      compile.with project('utility').and_dependencies, OSGI

      package(:jar)
    end

    define "log-configuration" do
      bnd.wrap!
      bnd.name = "PSC OSGi Layer Log Configuration"
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.logback.LogbackConfigurator"

      compile.with SLF4J.api, LOGBACK, OSGI.core

      package(:jar)
    end

    desc "A bundle which exports services for testing OSGi config interfaces"
    define "mock" do
      bnd.wrap!
      bnd.autostart = false
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.mock.Activator"
      bnd.name = "PSC OSGi Layer Mock Services"
      iml.id = "osgi-layer-mock"

      compile.with project('utility').and_dependencies, OSGI
      package(:jar)
    end

    desc "Non-PSC-specific commands for the felix shell"
    define "felix-commands" do
      bnd.wrap!
      bnd['Bundle-Activator'] =
        "edu.northwestern.bioinformatics.studycalendar.osgi.commands.Activator"
      bnd.name = "PSC Utility Felix Commands"
      iml.id = "osgi-layer-felix-commands"

      compile.with FELIX.shell, OSGI.core, JAKARTA_COMMONS.lang
      test.with UNIT_TESTING, project('utility').and_dependencies
      package(:jar)
    end

    define "integrated-tests" do
      directory _('tmp/logs')

      test.using(:junit).with UNIT_TESTING, DYNAMIC_JAVA,
        project('authentication:socket').and_dependencies,
        project('authentication:cas-plugin').and_dependencies,
        project('web').and_dependencies,
        project('web').test_dependencies,
        project('authentication:plugin-api').test_dependencies,
        project('providers:mock')
      test.enhance([:build_test_da_launcher, _('tmp/logs')])
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
    ).exclude "**/.DS_Store"

    compile.with project('domain').and_dependencies,
      project('authorization').and_dependencies,
      project('providers:api').and_dependencies,
      project('database').and_dependencies,
      project('utility:osgimosis').and_dependencies,
      project('psc:osgi-layer:host-services').and_dependencies,
      XML, RESTLET.framework, FREEMARKER, CSV,
      QUARTZ, SECURITY, OSGI, SLF4J.jcl, FELIX.configadmin,
      CONTAINER_PROVIDED, SPRING_WEB # tmp for mail

    test.with UNIT_TESTING, project('domain').test.compile.target,
      project('database').test_dependencies,
      project('mocks').and_dependencies

    package(:jar)
    package(:sources)

    iml.add_facet("Spring", "Spring") do |facet|
      facet.configuration do |conf|
        conf.fileset(:id => 'core-common', :name => 'Core Common ApplicationContext') do |fs|
          Dir[_(:source, :main, :java) + "/applicationContext*xml"].reject { |f| f =~ /osgi/ }.each do |f|
            fs.file "file://$MODULE_DIR$/#{f.sub(_(), '')}"
          end
        end
        conf.fileset(:id => 'core-prod', :name => 'Core Production ApplicationContext') do |fs|
          fs.dependency "core-common"
          fs.file "file://$MODULE_DIR$/src/main/java/applicationContext-core-osgi.xml"
        end
        conf.fileset(:id => 'core-testing', :name => 'Core Testing ApplicationContext') do |fs|
          fs.dependency "core-common"
          fs.file "file://$MODULE_DIR$/src/main/java/applicationContext-core-testing-osgi.xml"
        end
      end
    end

    check do
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))

      acSetup.should include(`hostname`.chomp)
      acSetup.should include(project.version)
    end
  end # core

  ##Adding the grid module.
  desc "Grid Services, includes Registration Consumer, Study Consumer and AE Service"
  define "grid" do
    project.no_iml

    ##creating work folders for mimicking the tomcat directory structure.
    rm_rf _('target/work-tomcat')
    mkdir_p _('target/work-tomcat/webapps/wsrf')
    mkdir_p _('target/work-tomcat/common/lib')

    task :check_globus do |task|
      raise "GLOBUS_LOCATION not set. Cannot build grid services without globus" unless ENV['GLOBUS_LOCATION']
    end

    task :check_ccts do |task|
      raise "CCTS_HOME not set. Cannot deploy grid service without CAAERS" unless ENV['CCTS_HOME']
    end

    task :check_grid_tomcat do |task|
      raise "CATALINA_HOME not set. Cannot deploy grid service without TOMCAT" unless ENV['CATALINA_HOME']
    end

    task :check_wsrf do |task|
      raise "#{wsrf_dir} not found. Cannot deploy grid service" unless File.directory? wsrf_dir
    end

    task :deploy_globus do |task|
      raise "GLOBUS_LOCATION not found. Cannot deploy globus" unless ENV['GLOBUS_LOCATION']
      ant('deploy-globus') do |ant|
        ant.echo :message => "deploying secured globus on tomcat, #{ENV['tomcat.dir']}"
        ant.subant :buildpath => ENV['GLOBUS_LOCATION'], :antfile => "share/globus_wsrf_common/tomcat/tomcat.xml", :target => "deploySecureTomcat", :inheritAll => "false"  do |subant|
          subant.property :name => "tomcat.dir", :value => ENV['CATALINA_HOME']
          subant.property :name => "webapp.name", :value => wsrf_dir_name
        end
      end
      ##using filtertask to filter the server-config.wsdd. Migrated the update-wsdd ant task to buildr. Added a custom mapper for filters
      ##Wasnt able to do an inplace filtering hence creating a work directory and then deleting it.
      ##Tested and working
      ## 1093 task
       FileUtils.mkdir_p _('target/work')
       filter.from(wsrf_dir+"/WEB-INF/etc/globus_wsrf_core").into(_('target/work')).include("server-config.wsdd").using(
           :xml, :xpath => "/deployment/globalConfiguration", 
	   :insert_type => :under, 
	   :xml_content => "<parameter name=\"disableDNS\" value=\"true\"/>
                            <parameter name=\"logicalHost\" value=\"" + tomcat_hostname + "\"/>"
       ).run
       FileUtils.rm wsrf_dir+"/WEB-INF/etc/globus_wsrf_core/server-config.wsdd"
       filter.from(_('target/work')).into(wsrf_dir+"/WEB-INF/etc/globus_wsrf_core").include("server-config.wsdd").run
       FileUtils.remove_dir _('target/work')
    end

    ##this will deploy all the psc implementations together with the grid services provided PSC. Consider this for CCTS deployments since the
    ##container will most likely be secured before deploy the PSC specific implementations. Also check the "deploy_with_globus" task.
    task :deploy => ['psc:grid:adverse-event-consumer-impl:deploy' , 'psc:grid:registration-consumer-impl:deploy' , 'psc:grid:study-consumer-impl:deploy']

    ##this will grid secure the tomcat and then deploy all the implementation.
    task :deploy_with_globus => [:deploy_globus , :deploy]

    ## packaging the war file by deploying the grid services on the work tomcat folder.
    package(:war, :file => _('target/'+wsrf_dir_name+'.war')).clean.include(:from=>_('target/work-tomcat/webapps/wsrf')).enhance do
      ENV['CATALINA_HOME']=_('target/work-tomcat').to_s
      ENV['WSRF_DIR_NAME']='wsrf'
      task(:deploy_with_globus).invoke
    end

    ##Project src and test compiling successfully but test cases are failing.
    desc "AdverseEvent Grid Service"
    define "adverse-event-consumer-impl", :base_dir => _('adverse-event-consumer') do
      compile.from(_('src/java')).with project('core').and_dependencies, GLOBUS, ADVERSE_EVENT_CONSUMER_GRID, SLF4J, LOGBACK
      resources.from(_('src/java')).include('*.xml')
      package(:jar)
      package(:sources)

      #Test cases are written with DBUnit 2.2, hence its added as a seperate dependency
      test.with(UNIT_TESTING, project('core').test_dependencies, project('database').test_dependencies, CAGRID_1, DBUNIT_GRID).compile.from(_('test/src/java'))

      test.resources.from(_('test/resources')).include('*')
      test.resources.from('src/test/resources').include('logback-test.xml')

      #removing the DBUNIT 2.1 from the test dependencies
      test.dependencies.reject! do |dep|
        dep == artifact(eponym("dbunit", "2.1"))
      end

      task :deploy => ['psc:grid:check_globus', 'psc:grid:check_grid_tomcat'] do |task|
        ##Delegating to caaers.
        ##Not Tested therefore commented temporarily
        ant('deploy-adverse-event-consumer-service') do |ant|
          ant.echo :message => "delegating the adverse event consumer service deployment to caaers"
          ant.subant :buildpath =>  ENV['CCTS_HOME']+"/AdverseEventConsumerService-caGrid13", :antfile => "build.xml", :target => "deployTomcat", :inheritAll => "false" do |subant|
            subant.property :name => "tomcat.dir", :value => ENV['CATALINA_HOME']
            subant.property :name => "globus.webapp", :value => wsrf_dir_name
          end
        end

        task(:deploy_impl).invoke

        ##using filtertask to filter the server-config.wsdd. Migrated the update-wsdd ant task to buildr. Added a custom mapper for filters
        ##Wasnt able to do an inplace filtering hence creating a work directory and then deleting it.
        ##Tested and working
        FileUtils.mkdir_p _('target/work')
        filter.from(wsrf_dir+"/WEB-INF/etc/cagrid_AdverseEventConsumer").into(_('target/work')).include("server-config.wsdd").using(
           :xml, :xpath => "/deployment/service", :insert_type => :before, :xml_content => "<handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoRequestHandler\"
           type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoRequestHandler\"/>

        <handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoResponseHandler\"
            type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoResponseHandler\"/>"
        ).run
        FileUtils.rm wsrf_dir+"/WEB-INF/etc/cagrid_AdverseEventConsumer/server-config.wsdd"
        filter.from(_('target/work')).into(
            wsrf_dir+"/WEB-INF/etc/cagrid_AdverseEventConsumer").include("server-config.wsdd").using(
            :xml, :xpath => "/deployment/service", :insert_type => :under, :xml_content => "<requestFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoRequestHandler\"/>
            </requestFlow>
            <responseFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoResponseHandler\"/>
            </responseFlow>"
        ).run
        FileUtils.remove_dir _('target/work')
      end

      task :deploy_impl => ['psc:grid:check_wsrf', package]  do |task|
        cp package.name, wsrf_dir+"/WEB-INF/lib"

        #removing the AdverseEventConsumer jars from the compile dependencies since these jars will be copied by the Grid Service
        compile.dependencies.reject! do |dep|
          ADVERSE_EVENT_CONSUMER_GRID.member?(dep) || GLOBUS.member?(dep)
        end

        compile.dependencies.each do |lib|
          cp lib.to_s, wsrf_dir+"/WEB-INF/lib"
        end
      end

      task :deploy_with_globus => ['psc:grid:deploy_globus', :deploy]
    end

    desc "Registration consumer Grid Service"
    define "registration-consumer-impl", :base_dir => _('registration-consumer') do
      compile.from(_('src/java')).with project('core').and_dependencies, GLOBUS, REGISTRATION_CONSUMER_GRID, SLF4J, LOGBACK
      resources.from(_('src/java')).include('*.xml')
      package(:jar)
      package(:sources)

      # TODO: temporarily disabled testing until it works
      #Test cases are written with DBUnit 2.2, hence its added as a seperate dependency
      test.with(UNIT_TESTING, project('core').test.compile.target, project('database').test_dependencies, CAGRID_1, DBUNIT_GRID).
      compile.from(_('test/src/java'));

      test.resources.from(_('test/resources')).include('*')
      test.resources.from('src/test/resources').include('logback-test.xml')

      #removing the DBUNIT 2.1 from the test dependencies
      test.dependencies.reject! do |dep|
        dep == artifact(eponym("dbunit", "2.1"))
      end

      task :deploy => ['psc:grid:check_globus', 'psc:grid:check_ccts', 'psc:grid:check_grid_tomcat'] do |task|
        ##Delegating to ccts.
        ##Not Tested therefore commented temporarily
        ant('deploy-registration-consumer-service') do |ant|
          ant.echo :message => "delegating the registration consumer service deployment to ccts"
          ant.subant :buildpath => ENV['CCTS_HOME']+"/RegistrationConsumerGridService-caGrid1.3.1", :antfile => "build.xml", :target => "deployTomcat", :inheritAll => "false" do |subant|
            subant.property :name => "tomcat.dir", :value => ENV['CATALINA_HOME']
            subant.property :name => "globus.webapp", :value => wsrf_dir_name
          end
        end
        task(:deploy_impl).invoke

        ##using filtertask to filter the server-config.wsdd. Migrated the update-wsdd ant task to buildr. Added a custom mapper for filters
        ##Wasnt able to do an inplace filtering hence creating a work directory and then deleting it.
        ##Tested and working
        FileUtils.mkdir_p _('target/work')
        filter.from(wsrf_dir+"/WEB-INF/etc/cagrid_RegistrationConsumer").into(_('target/work')).include("server-config.wsdd").using(
           :xml, :xpath => "/deployment/service", :insert_type => :before, :xml_content => "<handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoRequestHandler\"
           type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoRequestHandler\"/>

        <handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoResponseHandler\"
            type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoResponseHandler\"/>"
        ).run
        FileUtils.rm wsrf_dir+"/WEB-INF/etc/cagrid_RegistrationConsumer/server-config.wsdd"
        filter.from(_('target/work')).into(
            wsrf_dir+"/WEB-INF/etc/cagrid_RegistrationConsumer").include("server-config.wsdd").using(
            :xml, :xpath => "/deployment/service", :insert_type => :under, :xml_content => "<requestFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoRequestHandler\"/>
            </requestFlow>
            <responseFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoResponseHandler\"/>
            </responseFlow>"
        ).run
        FileUtils.remove_dir _('target/work')
      end

      task :deploy_impl => ['psc:grid:check_wsrf', package]  do |task|
        cp package.name, wsrf_dir+"/WEB-INF/lib"

        #removing the RegistrationConsumer jars from the compile dependencies since these jars will be copied by the Grid Service
        compile.dependencies.reject! do |dep|
          REGISTRATION_CONSUMER_GRID.member?(dep) || GLOBUS.member?(dep)
        end

        compile.dependencies.each do |lib|
          cp lib.to_s, wsrf_dir+"/WEB-INF/lib"
        end
      end

      task :deploy_with_globus => ['psc:grid:deploy_globus', :deploy]
    end

    desc "Study consumer Grid Service"
    define "study-consumer-impl", :base_dir => _('study-consumer') do
      compile.from(_('src/java')).with project('core').and_dependencies, project('psc:providers:coppa:common').and_dependencies, GLOBUS, STUDY_CONSUMER_GRID, SLF4J, LOGBACK
      resources.from(_('src/java')).include('*.xml')
      package(:jar)
      package(:sources)

      #Test cases are written with DBUnit 2.2, hence its added as a seperate dependency
      test.with(UNIT_TESTING, project('core').test.compile.target, project('database').test_dependencies, CAGRID_1, DBUNIT_GRID).
        compile.from(_('test/src/java'))

      test.resources.from(_('test/resources')).include('*')
      test.resources.from('src/test/resources').include('logback-test.xml')

      #removing the DBUNIT 2.1 from the test dependencies
      test.dependencies.reject! do |dep|
        dep == artifact(eponym("dbunit", "2.1"))
      end

      task :deploy => ['psc:grid:check_globus', 'psc:grid:check_ccts', 'psc:grid:check_grid_tomcat'] do |task|
        ##Delegating to ccts.
        ##Not Tested therefore commented temporarily
        ant('deploy-study-consumer-service') do |ant|
          ant.echo :message => "delegating the study consumer service deployment to ccts"
          ant.subant :buildpath => ENV['CCTS_HOME']+"/StudyConsumerGridService-caGrid1.3.1", :antfile => "build.xml", :target => "deployTomcat", :inheritAll => "false" do |subant|
            subant.property :name => "tomcat.dir", :value => ENV['CATALINA_HOME']
            subant.property :name => "globus.webapp", :value => wsrf_dir_name
          end
        end
        task(:deploy_impl).invoke

        ##using filtertask to filter the server-config.wsdd. Migrated the update-wsdd ant task to buildr. Added a custom mapper for filters
        ##Wasnt able to do an inplace filtering hence creating a work directory and then deleting it.
        ##Tested and working
        FileUtils.mkdir_p _('target/work')
        filter.from(wsrf_dir+"/WEB-INF/etc/cagrid_StudyConsumer").into(_('target/work')).include("server-config.wsdd").using(
           :xml, :xpath => "/deployment/service", :insert_type => :before, :xml_content => "<handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoRequestHandler\"
           type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoRequestHandler\"/>

        <handler xmlns=\"http://xml.apache.org/axis/wsdd/\"  name=\"auditInfoResponseHandler\"
            type=\"java:edu.northwestern.bioinformatics.studycalendar.grid.AuditInfoResponseHandler\"/>"
        ).run
        FileUtils.rm wsrf_dir+"/WEB-INF/etc/cagrid_StudyConsumer/server-config.wsdd"
        filter.from(_('target/work')).into(
            wsrf_dir+"/WEB-INF/etc/cagrid_StudyConsumer").include("server-config.wsdd").using(
            :xml, :xpath => "/deployment/service", :insert_type => :under, :xml_content => "<requestFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoRequestHandler\"/>
            </requestFlow>
            <responseFlow xmlns=\"http://xml.apache.org/axis/wsdd/\" >
                <handler type=\"auditInfoResponseHandler\"/>
            </responseFlow>"
        ).run
        FileUtils.remove_dir _('target/work')
      end

      task :deploy_impl => ['psc:grid:check_wsrf', package]  do |task|
        cp package.name, wsrf_dir+"/WEB-INF/lib"

        #removing the StudyConsumer jars from the compile dependencies since these jars will be copied by the Grid Service
        compile.dependencies.reject! do |dep|
          STUDY_CONSUMER_GRID.member?(dep) || GLOBUS.member?(dep)
        end

        compile.dependencies.each do |lib|
          cp lib.to_s, wsrf_dir+"/WEB-INF/lib"
        end
      end

      task :deploy_with_globus => ['psc:grid:deploy_globus', :deploy]
    end
  end

  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    COMPILED_SASS_TARGET = _(:target, :'compiled-sass')
    COMPILED_SASS_PKG_DIR = "sass-css"

    compile.with SLF4J, LOGBACK, CTMS_COMMONS.web,
      project('core').and_dependencies,
      project('authentication:plugin-api').and_dependencies,
      project('authentication:socket').and_dependencies,
      project('psc:osgi-layer:host-services').and_dependencies,
      SPRING_WEB, RESTLET, WEB, DYNAMIC_JAVA,
      FELIX.main

    test.with project('test-infrastructure').and_dependencies,
      project('test-infrastructure').test_dependencies,
      project('authentication:socket').test_dependencies
    test.using :java_args => [ '-Xmx512M', '-Dcom.sun.management.jmxremote' ]

    package(:war, :file => _('target/psc.war')).tap do |war|
      war.libs -= artifacts(CONTAINER_PROVIDED)
      war.libs -= war.libs.select { |artifact| artifact.respond_to?(:classifier) && artifact.classifier == 'sources' }
      war.enhance ["psc:osgi-layer:da_launcher_artifacts", "psc:web:compile_sass"] do
        task("psc:osgi-layer:da_launcher_artifacts").values.each do |path, artifacts|
          war.path("WEB-INF/da-launcher").path(path).include(artifacts.collect { |a| a.invoke; a.name })
        end
        war.path(COMPILED_SASS_PKG_DIR).include(Dir[COMPILED_SASS_TARGET + "/**/*.css"])
      end
    end
    package(:sources)

    iml.add_facet('Web', 'web') do |facet|
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

    iml.add_facet('Spring', 'Spring') do |facet|
      facet.configuration do |conf|
        conf.fileset(:id => 'web-common', :name => 'Web Common ApplicationContext') do |fs|
          Dir[project('psc:core')._(:source, :main, :java) + "/applicationContext*xml"].reject { |f| f =~ /osgi/ }.each do |f|
            fs.file "file://$MODULE_DIR$/../core#{f.sub(project('psc:core')._(), '')}"
          end
          Dir[_(:source, :main, :java) + "/applicationContext*xml"].reject { |f| f =~ /osgi/ }.each do |f|
            fs.file "file://$MODULE_DIR$#{f.sub(_(), '')}"
          end
        end
        conf.fileset(:id => 'web: application context', :name => 'Web Production ApplicationContext') do |fs|
          fs.dependency('web-common')
          fs.file "file://$MODULE_DIR$/../core/src/main/java/applicationContext-core-osgi.xml"
          fs.file "file://$MODULE_DIR$/src/main/java/applicationContext-web-osgi.xml"
        end
        conf.fileset(:id => 'web-testing', :name => 'Web Testing ApplicationContext') do |fs|
          fs.dependency('web-common')
          fs.file "file://$MODULE_DIR$/../core/src/test/java/applicationContext-core-testing-osgi.xml"
          fs.file "file://$MODULE_DIR$/src/test/java/applicationContext-web-testing-osgi.xml"
        end
        conf.fileset(:id => 'web: spring servlet context', :name => 'MVC spring servlet context') do |fs|
          fs.dependency('web: application context')
          fs.file "file://$MODULE_DIR$/src/main/webapp/WEB-INF/spring-servlet.xml"
        end
        conf.fileset(:id => 'web: setup servlet context', :name => 'MVC setup servlet context') do |fs|
          fs.dependency('web: application context')
          fs.file "file://$MODULE_DIR$/src/main/webapp/WEB-INF/setup-servlet.xml"
        end
        conf.fileset(:id => 'web: public servlet context', :name => 'MVC public servlet context') do |fs|
          fs.dependency('web: application context')
          fs.file "file://$MODULE_DIR$/src/main/webapp/WEB-INF/public-servlet.xml"
        end
        conf.fileset(:id => 'api-servlet', :name => 'Restlet API servlet context') do |fs|
          fs.dependency('web: application context')
          fs.file "file://$MODULE_DIR$/src/main/webapp/WEB-INF/restful-api-servlet.xml"
        end
      end
    end

    def compile_sass(src, dst)
      trace "Compiling Sass #{src} => #{dst}"
      require 'sass'
      mkdir_p File.dirname(dst)
      File.open(dst, 'w') do |f|
        f.write(Sass::Engine.new(File.read(src)).render)
      end
    end

    def sass_dst(src)
      src.
        sub(_(:source, :main, 'sass'), COMPILED_SASS_TARGET).
        sub(/sass$/, 'css')
    end

    def sass_src
      Dir[_(:source, :main, 'sass') + "/**/*.sass"]
    end

    def watch_sass
      sass_src.each do |src|
        dst = sass_dst(src)
        if File.stat(dst) < File.stat(src)
          info "Recompiling #{src} into #{dst}"
          compile_sass(src, dst)
        end
      end
    end

    task :compile_sass
    sass_src.each do |src|
      dst = sass_dst(src)
      file dst => src do
        compile_sass(src, dst)
      end
      task(:compile_sass).enhance [dst]
    end

    def link_unique_nodes(src, dst)
      Dir["#{src}/*"].each do |src_path|
        dst_path = src_path.sub(src, dst)
        if File.exist? dst_path
          link_unique_nodes(src_path, dst_path)
        else
          ln_s(src_path, dst_path)
        end
      end
    end

    task :explode => [compile, :compile_sass, "psc:osgi-layer:da_launcher_artifacts"] do |t|
      class << t; attr_accessor :target; end
      t.target = _(:target, 'dev-webapp')
      rm_rf t.target
      mkdir_p t.target

      packages.detect { |pkg| pkg.to_s =~ /war$/ }.tap do |war_package|
        info "Exploding into #{t.target}"
        # Explicitly copied (i.e., built) pieces
        info "- Exploding classes"
        war_package.classes.each do |clz_src|
          filter.from(clz_src).into(t.target + '/WEB-INF/classes').run
        end
        info "- Exploding libs"
        libdir = t.target + '/WEB-INF/lib'
        mkdir_p libdir
        main_libs = war_package.libs
        main_libs += GLOBUS_UNDUPLICABLE.values if (env_true?('WEBSSO') || env_true?('GLOBUS'))
        main_libs.each do |lib|
          cp lib.to_s, libdir
        end
        info "- Exploding DA Launcher libs"
        task('psc:osgi-layer:da_launcher_artifacts').values.each do |path, artifacts|
          dadir = t.target + "/WEB-INF/da-launcher/#{path}"
          mkdir_p dadir
          artifacts.each { |a| a.invoke; cp a.to_s, dadir }
        end
        info "- Linking Sass"
        ln_s COMPILED_SASS_TARGET, t.target + "/" + COMPILED_SASS_PKG_DIR

        # Symlinked (i.e., source) pieces.  Must come 2nd.
        # Approach: walk src/main/webapp
        # For each node, if it appears in src and target, traverse down
        #                if it appears in src only, link
        info "- Linking static files"
        link_unique_nodes(_('src/main/webapp'), t.target)
      end
    end

    task :local_jetty do
      ENV['test'] = 'no'
      set_db_name 'datasource' unless ENV['DB']

      # set temp directory to something without +++ in it
      tmpdir = _('target/java-tmp')
      mkdir_p tmpdir
      Java.java.lang.System.setProperty('java.io.tmpdir', tmpdir)

      task(:jetty_deploy_exploded).invoke

      msg = "PSC deployed at #{jetty.url}/psc.  Press ^C to stop.  PID: #{Process.pid || "?"}"
      info "=" * msg.size
      info msg
      info "=" * msg.size

      # enable non-default OSGi bundles
      unless ENV['MOCK_PROVIDER'] == 'no'
        if start_bundle('edu.northwestern.bioinformatics.psc-providers-mock')
          info "Started mock providers bundle"
        else
          warn "Could not start mock providers bundle"
        end
      end

      if start_bundle("edu.northwestern.bioinformatics.psc-osgi-layer-mock")
        info "Started mock OSGi service bundle"
      else
        warn "Could not start mock OSGi service bundle"
      end

      if ENV['OSGI_TELNET']
        if start_bundle(/telnet|shell\.remote/)
          info "Started telnet bundle(s)"
        else
          warn "Could not start telnet bundle(s)"
        end
      end

      # Keep the script running until interrupted
      while(true)
        sleep(1)
        watch_sass
      end
    end

    directory _('tmp/logs')

    task :jetty_deploy_exploded => ['psc:web:explode', _('tmp/logs')] do
      Java.java.lang.System.setProperty("catalina.home", _('tmp').to_s)
      Java.java.lang.System.setProperty("org.mortbay.util.FileResource.checkAliases", "false")
      Java.java.lang.System.setProperty("psc.logging.debug", ENV['PSC_DEBUG'] || "true")

      jetty.deploy "#{jetty.url}/psc", task(:explode).target
    end

    desc "Prints a list of the bundles in the dev webapp with their IDs"
    task :'list-dev-bundles' do
      felix_runtime_dir = _('target/dev-webapp/WEB-INF/da-launcher/runtime/cache')
      if File.exist?(felix_runtime_dir)
        Dir["#{felix_runtime_dir}/bundle*"].collect { |b| b.scan(/bundle(\d+)/)[0][0].to_i }.sort.each do |bundle_id|
          location_file = "#{felix_runtime_dir}/bundle#{bundle_id}/bundle.location"
          bundle_info =
            if File.exist? location_file
              loc = File.read("#{felix_runtime_dir}/bundle#{bundle_id}/bundle.location")
              if application.options.trace
                loc
              else
                loc.split(%r{/})[-1]
              end
            else
              "<No location available>"
            end
          puts "%3d %s" % [bundle_id, bundle_info]
        end
      else
        puts "The dev webapp is not deployed (#{felix_runtime_dir} does not exist)"
      end
    end

    unless ENV['SHEN'] == 'no'
      desc "Specs for client-side javascript"
      define "js-spec" do
        # using project('psc:web')._(:source, :main, :webapp, "js") causes a bogus
        # circular dependency failure
        test.using :shenandoah, :main_path => _("../src/main/webapp/js")
      end
    end
  end

  desc "Shared mocks for external libraries (exc. JDBC)"
  define "mocks", :base_dir => _('test/mock') do
    compile.using(:javac).with OSGI, SPRING_OSGI_MOCKS
    package(:jar)
  end

  desc "Empty base mocks for JDBC classes"
  define "jdbc-mock", :base_dir => _('test/jdbc-mock') do
    compile.using(:javac).from(_("src/main/java#{java6? ? '6' : '5'}"))
    package(:jar)
  end

  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB, OSGI,
      project('core').and_dependencies, project('jdbc-mock'),
      project('mocks').and_dependencies
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
        :gems => { 'rest-open-uri' => '1.0.0', 'builder' => '2.1.2', 'json_pure' => '>1.1.3', 'icalendar' => '1.1.0', 'haml' => '~>2.2.0' },
        :requires =>
          %w(spec http static_data template).collect { |help| _("src/spec/ruby/#{help}_helper.rb") } +
          [_("src/spec/ruby/request_logger_formatter.rb")],
        :properties => {
          'applicationContext.path' => File.join(test.resources.target.to_s, "applicationContext.xml"),
          'logback.configurationFile' => File.join(test.resources.target.to_s, "logback-test.xml")
        },
        :format => [
          "RequestLoggerFormatter:#{_('reports/rspec/requests.html')}"
        ]
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

    cp project('web').packages.select { |p| p.type == :war }.to_s, _("#{dist_dir}/psc.war")
    # Ensure oracle driver is present in war
    unless `jar tf '#{_(dist_dir, 'psc.war')}'` =~ /ojdbc/
      fail "Oracle JDBC driver not present in war.  Distributions must be built with ORACLE=yes."
    end

    cp _("datasource.properties.example"), _("#{dist_dir}/conf-samples/datasource.properties")
    puts `svn export https://ncisvn.nci.nih.gov/svn/psc/documents/PSC_Install_Guide.doc '#{_("#{dist_dir}/psc_install.doc")}'`

    pkg_name = "psc-#{VERSION_NUMBER.sub(/.RELEASE/, '')}"

    task.filename = _("target/dist/#{pkg_name}-bin.zip")
    zip(task.filename).path(pkg_name).include("#{dist_dir}/*").root.invoke
  end

  desc "Purge any .DEV artifacts in the local m2 repo"
  task :purge_dev_artifacts do
    Dir["#{ENV['HOME']}/.m2/repository/**/*.DEV"].each do |dir|
      rm_r dir
    end
  end
end

###### Shared configuration

projects.each do |p|
  if File.exist?(p._("src/test/java"))
    # Use same logback test config for all modules
    p.test.resources.enhance do
      Buildr::Filter.new.
        from(project("psc")._("src/test/resources")).
        into(p.test.resources.target).
        using(:project_root => p._).
        run
    end
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
  task :unit => [:clean, :'psc:database:clean_hsqldb', :'psc:verify_bda_gems'] do
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

  desc "Fakes that the unit tests where already run"
  task :fake_unit_tests_already_run do
    timestamp = Time.now + (60 * 60 * 3)  # Fake out must be well in the future because when a task is invoked, it's
                                          # dependencies are compiled too, which sets their modified timestamp.
    Project.projects.each do |p|
      report_to = File.join(p.base_dir, 'reports', 'junit')
      FileUtils.mkdir_p(report_to)
      last_successful_run = File.join(report_to, 'last_successful_run')
      FileUtils.touch(last_successful_run)
      File.utime(timestamp, timestamp, last_successful_run)
    end
  end

  desc "Continuous integration test build"
  task :integration => ['fake_unit_tests_already_run', 'psc:restful-api-test:setup'] do
    task('integration').invoke
  end
end
