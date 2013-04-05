#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# Dependency groups used by PSC's buildfile

load File.dirname(__FILE__) + "/psc-osgi-artifact.rake"
load File.dirname(__FILE__) + "/helper_functions.rake"

###### REPOS

# NU m2 repo (for ctms-commons, caGrid, other non-repo dependencies)
repositories.remote << "http://download.bioinformatics.northwestern.edu/download/maven2"
# restlet repo
repositories.remote << "http://maven.restlet.org"
# ical4j repo
repositories.remote << "http://m2.modularity.net.au/releases"
# SpringSource osgi-ified bundle repos
repositories.remote << "http://repository.springsource.com/maven/bundles/release"
repositories.remote << "http://repository.springsource.com/maven/bundles/external"
# codehaus repo
repositories.remote << "http://repository.codehaus.org/"
# NCI maven repo
repositories.remote << 'https://ncimvn.nci.nih.gov/nexus/content/groups/public/'
# main m2 repo
repositories.remote << "http://repo1.maven.org/maven2"
#Jboss repository added to fetch dbunit 2.2 jar.
repositories.remote << "http://repository.jboss.org/maven2"

###### HELPERS

# Define an artifact whose group is the same as its id
def eponym(artifact, version)
  "#{artifact}:#{artifact}:jar:#{version}"
end

def static_lib(filename)
  File.expand_path(filename, [File.dirname(__FILE__), '..', 'static-lib'].join('/'))
end

def ctms_commons_lib(mod)
  if ENV['LOCAL_CC_REV']
    rev = ENV['LOCAL_CC_REV']
    spec = "gov.nih.nci.cabig.ctms:#{mod}.LOCAL:jar:#{rev}"
    file = Dir[ "#{ENV['HOME']}/ctms-commons/git/**/target/#{mod}-#{rev}.jar" ].first
    fail "Cannot find locally built #{mod} rev #{rev}" unless file
    artifact(spec, file)
  else
    "gov.nih.nci.cabig.ctms:#{mod}:jar:#{CTMS_COMMONS_VERSION}"
  end
end

###### DEPS

# Only list versions which appear in more than one artifact here
CTMS_COMMONS_VERSION = "1.1.2.RELEASE"
CORE_COMMONS_VERSION = "77"
SPRING_VERSION = "3.0.7.RELEASE"
SPRING_WEBFLOW_VERSION = "2.3.0.RELEASE"
RESTLET_VERSION = "2.0.3"
SLF4J_VERSION = "1.6.4"
LOGBACK_VERSION = "1.0.0"

CTMS_COMMONS = struct(
  %w{base core laf lang web}.inject({}) do |h, a|
    h[a.to_sym] = ctms_commons_lib("ctms-commons-#{a}")
    h
  end
)
CORE_COMMONS = "edu.northwestern.bioinformatics:core-commons:jar:#{CORE_COMMONS_VERSION}"

XML = struct(
  # Note that DOM4J 1.6.1 has classloader incompatibilties with OSGi
  :dom4j => "org.dom4j:com.springsource.org.dom4j:jar:1.5.2",
  # "org.jdom:com.springsource.org.jdom:jar:1.0.0", # unused?
  # Saxon 9 isn't in the maven repo for some reason
  :saxon => artifact("net.sf.saxon:saxon:jar:9").from(static_lib('saxon9.jar')),
  :saxon_dom => artifact("net.sf.saxon:saxon-dom:jar:9").from(static_lib('saxon9-dom.jar'))
)

CSV = [
  "net.sourceforge.javacsv:javacsv:jar:2.0"
]

# While 0.9.18 is available in the springsource repo, it wants to work with SLF4J 1.5.8, which is not.
LOGBACK = struct(
  :core    => "ch.qos.logback:logback-core:jar:#{LOGBACK_VERSION}",
  :classic => "ch.qos.logback:logback-classic:jar:#{LOGBACK_VERSION}",
  # not technically part of logback, but exclusively used by it
  :janino  => "org.codehaus.janino:com.springsource.org.codehaus.janino:jar:2.5.15"
)

SLF4J = struct(
  :api   => "org.slf4j:slf4j-api:jar:#{SLF4J_VERSION}",
  :jcl   => "org.slf4j:jcl-over-slf4j:jar:#{SLF4J_VERSION}",
  :log4j => "org.slf4j:log4j-over-slf4j:jar:#{SLF4J_VERSION}",
  :jul   => "org.slf4j:jul-to-slf4j:jar:#{SLF4J_VERSION}"
)

def spring_osgi_apache_commons(name, version)
  "org.apache.commons:com.springsource.org.apache.commons.#{name}:jar:#{version}"
end

JAKARTA_COMMONS = struct({
  :beanutils  => spring_osgi_apache_commons("beanutils", "1.7.0"),
  :collections => spring_osgi_apache_commons("collections", "3.2.0"),
  :dbcp       => spring_osgi_apache_commons("dbcp", "1.2.2.osgi"),
  :digester   => spring_osgi_apache_commons("digester", "1.8.0"),
  :discovery  => spring_osgi_apache_commons("discovery", "0.4.0"),
  :io         => spring_osgi_apache_commons("io", "1.4.0"),
  :lang       => spring_osgi_apache_commons("lang", "2.4.0"),
  :pool       => spring_osgi_apache_commons("pool", "1.4.0"),
  :fileupload => spring_osgi_apache_commons("fileupload", "1.2.0"),
  :collections_generic => psc_osgi_artifact("net.sourceforge.collections:collections-generic:jar:4.01"),
  :validator  => spring_osgi_apache_commons("validator", "1.1.4"),
  :discovery  => spring_osgi_apache_commons("discovery", "0.4.0"),
  :httpclient => spring_osgi_apache_commons("httpclient", "3.1.0"),
  :codec      => spring_osgi_apache_commons("codec", "1.3.0"),
  :net        => spring_osgi_apache_commons("net", "1.4.1"),
})

HTMLPARSER = psc_osgi_artifact("nu.validator.htmlparser:htmlparser:jar:1.1.0") # 1.2.1 not in mvn repo

SPRING = struct(
  %w(aop asm beans context context-support core expression jdbc orm tx).inject({}) { |h, a|
    h[a] = "org.springframework:spring-#{a}:jar:#{SPRING_VERSION}"; h
  }.merge(
    :aopalliance => 'org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0'
  )
)

SPRING_WEB = struct(
  :web        => "org.springframework:spring-web:jar:#{SPRING_VERSION}",
  :webmvc     => "org.springframework:spring-webmvc:jar:#{SPRING_VERSION}",
  :webflow    => "org.springframework.webflow:spring-webflow:jar:#{SPRING_WEBFLOW_VERSION}",
  :binding    => "org.springframework.webflow:spring-binding:jar:#{SPRING_WEBFLOW_VERSION}",
  :spring_js  => "org.springframework.webflow:spring-js:jar:#{SPRING_WEBFLOW_VERSION}",
  :activation => "javax.activation:com.springsource.javax.activation:jar:1.1.1",
  :mail       => "javax.mail:com.springsource.javax.mail:jar:1.4.1",
  :oro        => "org.apache.oro:com.springsource.org.apache.oro:jar:2.0.8",
  :ognl       => "org.ognl:com.springsource.org.ognl:jar:2.6.9" # For webflow
)

CGLIB = "net.sourceforge.cglib:com.springsource.net.sf.cglib:jar:2.1.3"

# Alt: "javax.xml.stream:com.springsource.javax.xml.stream:jar:1.0.1"
STAX_API = artifact("org.dynamicjava.jsr:stax-api:jar:1.0.1").
  from(static_lib("org.dynamicjava.stax-api-1.0.1.jar"))

HIBERNATE = struct(
  :main => psc_osgi_artifact('org.hibernate:hibernate-core:jar:3.6.4.Final'),
  # Don't use HCA 3.3.0.ga. It's broken.
  :annotations_common => psc_osgi_artifact(
    'org.hibernate:hibernate-commons-annotations:jar:3.2.0.Final'
  ),
  :antlr => "org.antlr:com.springsource.antlr:jar:2.7.7",
  :cglib => CGLIB,
  :javax_transaction => "javax.transaction:com.springsource.javax.transaction:jar:1.1.0",
  :javax_persistence => psc_osgi_artifact(
    'org.hibernate.javax.persistence:hibernate-jpa-2.0-api:jar:1.0.0.Final'
  ),
  :javassist => "org.jboss.javassist:com.springsource.javassist:jar:3.3.0.ga",
  :dom4j => XML.dom4j
)

EHCACHE = struct(
  :ehcache => "net.sourceforge.ehcache:com.springsource.net.sf.ehcache:jar:1.5.0",
  :jsr107 => "net.sourceforge.jsr107cache:com.springsource.net.sf.jsr107cache:jar:1.0.0",
  :backport => "edu.emory.mathcs.backport:com.springsource.edu.emory.mathcs.backport:jar:3.0.0"
)

SECURITY = struct(
  :acegi_csm  => ctms_commons_lib("ctms-commons-acegi-csm"),
  :acegi_grid => ctms_commons_lib("ctms-commons-acegi-grid"),
  :suite_authorization => ctms_commons_lib('ctms-commons-suite-authorization'),
  :clm => psc_osgi_artifact(
    "gov.nih.nci.security:clm:jar:4.2.beta",
    "Export-Package" => "gov.nih.nci.*"
  ),
  :csm => psc_osgi_artifact(
    "csmapi:csmapi:jar:4.2",
    # -failok is necessary because CSM contains some classes that are
    # not in the correct directory according to their package
    # names. They are not actually used, however.
    'Export-Package' => 'gov.nih.nci.*', '-failok' => true
  ),
  :acegi => psc_osgi_artifact("org.acegisecurity:acegi-security:jar:1.0.7"),
  :cas => psc_osgi_artifact("cas:casclient:jar:2.0.11"),
  :caaers_cas => psc_osgi_artifact(artifact("gov.nih.nci.cabig.caaers:cas-patch:jar:1.1.3").from(static_lib('caaers-1.1.3-cas-patch.jar')))
)

# This is out of date, but for some reason still used in the grid
# service compilation phase.
CAGRID_1 = [
  group(%w{
    authentication-service-client
    authentication-service-common
    authentication-service-stubs
    authz-common
    core
    dorian-client
    dorian-common
    dorian-stubs
    gridca
    metadata-common
    metadata-data
    metadata-security
    opensaml
    ServiceSecurityProvider-client
    ServiceSecurityProvider-common
    ServiceSecurityProvider-stubs
  }.map {|a| "cagrid-#{a}"}, :under => 'gov.nih.nci.cagrid', :version => 1.0),
  "org.globus:wsrf-core:jar:4.0-cagrid1.0",
  "org.globus:wsrf-core-stubs:jar:4.0-cagrid1.0",
  "wsdl4j:wsdl4j:jar:1.6.1",
  "org.globus:wss4j:jar:4.0-cagrid1.0",
  "org.globus:puretls:jar:4.0.3-globus",
  "org.globus:saaj:jar:4.0.3-globus",
  "org.globus:cog-axis:jar:4.0.3-globus",
  "org.globus:cog-jglobus:jar:1.2",
  "org.globus:cog-tomcat:jar:4.0.3-globus",
  "org.globus:cog-url:jar:1.2",
  "net.sourceforge.addressing:addressing:jar:1.1",
  "org.apache.axis:axis:jar:1.4",
  "org.globus:axis:jar:4.0.3-globus"
]

CAGRID = struct(
  :all => Dir[static_lib("psc-cagrid-all*.jar")].sort.collect { |jar|
      version = File.basename(jar).split('_')[1].gsub(/.jar/, '')
      artifact("org.globus:edu.northwestern.bioinformatics.osgi.gov.nih.nci.cagrid.all:jar:#{version}", jar)
    }.last,
  :globus_adapter =>
    artifact(
      "gov.nih.nci.cagrid:edu.northwestern.bioinformatics.osgi.cagrid-globus-adapter:jar:1.3.0.PSC001",
      static_lib("cagrid/cagrid-globus-adapter-1.3.0.PSC001.jar"))
)

GLOBUS = struct(
  :core => Dir[static_lib("psc-globus-all*.jar")].sort.collect { |jar|
      version = File.basename(jar).split('_')[1].gsub(/.jar/, '')
      artifact("org.globus:edu.northwestern.bioinformatics.osgi.org.globus.all:jar:#{version}", jar)
    }.last,
  :discovery  => JAKARTA_COMMONS.discovery,
  :httpclient => JAKARTA_COMMONS.httpclient,
  :codec      => JAKARTA_COMMONS.codec,
  :net        => JAKARTA_COMMONS.net,
  :servlet    => "javax.servlet:com.springsource.javax.servlet:jar:2.4.0",
  :jms        => "javax.jms:com.springsource.javax.jms:jar:1.1.0",
  :mail       => "javax.mail:com.springsource.javax.mail:jar:1.4.1",
  :wsdl       => "javax.wsdl:com.springsource.javax.wsdl:jar:1.6.1",
  :soap       => "javax.xml.soap:com.springsource.javax.xml.soap:jar:1.3.0",
  :activation => "javax.activation:com.springsource.javax.activation:jar:1.1.1",
  :jaxrpc     => "javax.xml.rpc:com.springsource.javax.xml.rpc:jar:1.1.0",
  # :jaxb_api   => "javax.xml.bind:com.springsource.javax.xml.bind:jar:2.1.7",
  :jaxb_api   => artifact("org.dynamicjava.jsr:jaxb-api:jar:1.0.0.PSC0").from(static_lib("org.dynamicjava.jaxb-api-2.1.0.PSC0.jar")),
  :stax       => STAX_API,
  :jaxb_impl  => "com.sun.xml:com.springsource.com.sun.xml.bind:jar:2.1.7",
  :infoset    => "com.sun.xml:com.springsource.com.sun.xml.fastinfoset:jar:1.2.2",
  :staxex     => "org.jvnet.staxex:com.springsource.org.jvnet.staxex:jar:1.0.0",
  :castor     => "org.codehaus.castor:com.springsource.org.castor:jar:1.2.0",
  :bsf        => "org.apache.bsf:com.springsource.org.apache.bsf:jar:2.4.0"
)

# These are the globus dependencies which cannot be duplicated within the same JVM
# per http://cagrid.org/display/knowledgebase/GSSAPI+-+Bad+Certificate+Error+Solution .
# Therefore they are not in the OSGi layer and may or may not be bundled into the WAR.
GLOBUS_UNDUPLICABLE = struct(
  :cog_jglobus => artifact("org.globus:cog-jglobus:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/cog-jglobus.jar")),
  :cryptix_asn1 => artifact("cryptix:cryptix_asn1:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/cryptix-asn1.jar")),
  :cryptix => artifact("cryptix:cryptix:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/cryptix.jar")),
  :cryptix32 => artifact("cryptix:cryptix32:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/cryptix32.jar")),
  :bouncycastle => artifact("org.bouncycastle:jce-jdk13:jar:1.25.0").from(static_lib("globus-4.0.3/jce-jdk13-125.jar")),
  :jgss => artifact("org.ietf:jgss:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/jgss.jar")),
  :puretls => artifact("com.claymoresystems:puretls:jar:4.0.3.WS-CORE").from(static_lib("globus-4.0.3/puretls.jar"))
)

# Some of this is generic caGrid/introduce stuff -- split it out later
COPPA_VERSION = "3.1.0.PSC001"
# fragment client-config.wsdd (grid/globus) jars onto the globus metabundle
# client_config_bnd = { "Fragment-Host" => "edu.northwestern.bioinformatics.osgi.org.globus.all" }
COPPA = struct(
  # COPPA introduce-stubs
  :core_stubs => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-stubs:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-stubs.jar")),
      "Export-Package" => "!gov.nih.nci.cagrid.introduce.security.stubs, !gov.nih.nci.cagrid.metadata.common, !gov.nih.nci.cagrid.metadata.service, *"),
  :core_common => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-common:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-common.jar"))),
  :core_client => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-client:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-client.jar"))),
  :common => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-commons:jar:1.2.3").from(static_lib('coppa/coppa-commons-1.2.3.jar'))),

  :pa_stubs => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-pa-services-stubs:jar:#{COPPA_VERSION}").from(static_lib("coppa/PAServices-stubs.jar")),
      "Export-Package" => "!gov.nih.nci.cagrid.introduce.security.stubs, *"),
  :pa_common => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-pa-services-common:jar:#{COPPA_VERSION}").from(static_lib("coppa/PAServices-common.jar"))),
  :pa_client => psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-pa-services-client:jar:#{COPPA_VERSION}").from(static_lib("coppa/PAServices-client.jar"))),

  # artifact("gov.nih.nci.coppa:edu.northwestern.bioinformatics.osgi.coppa-jaxb-adapter:jar:0.0.0").
  #   from(static_lib('coppa/coppa-jaxb-adapter-0.0.0.jar')),

  # version changes separately from the others since it's manually created
  :globus_adapter => artifact(
    "gov.nih.nci.coppa:edu.northwestern.bioinformatics.osgi.coppa-globus-adapter:jar:3.0.0.PSC000",
    static_lib('coppa/coppa-globus-adapter-3.0.0.PSC000.jar'))
)

CIH_VERSION = "1.2.2.PSC000"
CIH = struct(
  [:client, :common, :service, :stubs].inject({}) { |s, q|
    s[q] = psc_osgi_artifact(
      artifact("gov.nih.nci.caxchange:caxchange-request-processor-#{q}:jar:#{CIH_VERSION}").
        from(static_lib("caxchange/CaXchangeRequestProcessor-#{q}-#{CIH_VERSION.split('.')[0, 3].join('.')}.jar")),
      "Export-Package" => "!gov.nih.nci.cagrid.introduce.security.stubs, *"
    ); s
  }
)

BERING = [
  "edu.northwestern.bioinformatics:bering:jar:0.8.2",
  "org.codehaus.groovy:com.springsource.org.codehaus.groovy:jar:1.5.7",
  "org.objectweb.asm:com.springsource.org.objectweb.asm:jar:2.2.3"
]

QUARTZ = [
  "com.opensymphony.quartz:com.springsource.org.quartz:jar:1.6.0"
]

FREEMARKER = [
  "org.freemarker:com.springsource.freemarker:jar:2.3.12"
]

WEB = [
  "itext:itext:jar:1.3.1",
  "opensymphony:sitemesh:jar:2.2.1",
  "poi:poi-2.5.1-final:jar:20040804",
  "taglibs:standard:jar:1.1.2",
  "org.json:json:jar:20080701",
  eponym("jstl", "1.1.2"),
  "net.fortuna:ical4j:jar:1.0-beta4",
  artifact("gov.nih.nci.ccts:smoketest-client:jar:1.1").from(static_lib("SmokeTestService-client.jar")),
  eponym('displaytag', '1.1.1'),
  "displaytag:displaytag-export-poi:jar:1.1.1",
  "org.codehaus.jackson:jackson-core-lgpl:jar:1.2.0",
  # Request mocks are used in production web-layer code
  "org.springframework:spring-test:jar:#{SPRING_VERSION}"
]

def restlet_lib(name)
  spec = "org.restlet.jee:#{name}:jar:#{RESTLET_VERSION}"
  if RESTLET_VERSION =~ /PSC/
    snapshot_base = "restlet-snapshot"
    version = Dir[File.join("static-lib", snapshot_base, "org.restlet", "*")].first.split('/').last
    artifact(spec.sub(":jar:", ":jar:sources:")).from(
      static_lib(File.join(snapshot_base, name, version, "#{name}-#{version}-sources.jar")))
    artifact(spec).from(
      static_lib(File.join(snapshot_base, name, version, "#{name}-#{version}.jar")))
  else
    spec
  end
end

def felix_lib(name, version)
  "org.apache.felix:org.apache.felix.#{name}:jar:#{version}"
end

RESTLET = struct({
  :framework        => restlet_lib("org.restlet"),
  :spring_ext       => restlet_lib("org.restlet.ext.spring"),
  :freemarker_ext   => restlet_lib("org.restlet.ext.freemarker"),
  :json_ext         => restlet_lib("org.restlet.ext.json"),
  :servlet_ext      => restlet_lib("org.restlet.ext.servlet"),
  :xml_ext          => restlet_lib("org.restlet.ext.xml")
})

CONTAINER_PROVIDED = [
  "javax.servlet:com.springsource.javax.servlet:jar:2.4.0",
  "javax.servlet:com.springsource.javax.servlet.jsp:jar:2.0.0"
]

OSGI = struct(
  :core => 'org.osgi:org.osgi.core:jar:4.2.0',
  :compendium => 'org.osgi:org.osgi.compendium:jar:4.2.0'
)

FELIX = struct(
  :framework    => felix_lib("framework",    "3.0.9"),

  :bundlerepo   => felix_lib("bundlerepository", "1.6.4"),
  :configadmin  => felix_lib("configadmin",      "1.2.8"),
  :eventadmin   => felix_lib("eventadmin",       "1.2.10"),
  :fileinstall  => felix_lib('fileinstall',      '3.1.10'),
  :log          => felix_lib("log",              "1.0.0"),
  :metatype     => felix_lib("metatype",         "1.0.0"),
  :scr          => felix_lib('scr',              '1.6.0'),
  :shell        => felix_lib("shell",            "1.4.2"),
  :shell_tui    => felix_lib("shell.tui",        "1.4.1"),
  :shell_remote => felix_lib("shell.remote",     "1.1.2")
)

SPRING_OSGI = struct(
  %w(core io extender).inject({}) { |h, a|
    h[a] = "org.springframework.osgi:org.springframework.osgi.#{a}:jar:1.2.1"; h
  }
)

SPRING_OSGI_MOCKS = "org.springframework.osgi:org.springframework.osgi.mock:jar:1.2.1"

UNIT_TESTING = [
  "edu.northwestern.bioinformatics:core-commons-testing:jar:#{CORE_COMMONS_VERSION}",
  ctms_commons_lib("ctms-commons-testing-unit"),
  CTMS_COMMONS.base,
  eponym("dbunit", "2.1"),
  "org.easymock:easymock:jar:2.2",
  "org.easymock:easymockclassextension:jar:2.2.2",
  CGLIB,
  "org.springframework:spring-test:jar:#{SPRING_VERSION}",
  eponym("xmlunit", "1.1"),
  "org.reflections:reflections:jar:0.9.5",
  "com.google.guava:guava:jar:r08",
  SPRING_OSGI_MOCKS,
  LOGBACK,
  SLF4J.jcl,
  SLF4J.jul,
  SLF4J.log4j
].flatten

INTEGRATED_TESTING = [
  "org.jgrapht:jgrapht-jdk1.5:jar:0.7.3",
  "net.java.dev:jvyaml:jar:0.2.1"
]

BND = artifact("biz.aQute:bnd:jar:0.0.313").from(static_lib('bnd-0.0.313.jar'))

DB = struct(
  :hsqldb => "org.hsqldb:com.springsource.org.hsqldb:jar:1.8.0.9",
  :postgresql => "org.postgresql:com.springsource.org.postgresql.jdbc3:jar:8.3.603",
  :oracle => if env_true?('ORACLE')
               psc_osgi_artifact("com.oracle:ojdbc14:jar:10.2.0.2.0", {},
                 :version => '10.2.020.PSC001')
             else
               "Set ORACLE=yes if you want to use Oracle"
             end
)

ADVERSE_EVENT_CONSUMER_GRID = struct(
  Dir[static_lib('grid-consumer/AdverseEventConsumer*.jar')].inject({}) do |map, jar|
    group, name = jar.scan(%r{.*/(AdverseEventConsumer)-(\w+)\.jar$}).first
    version= "1.0"
    map[name.gsub('.', '_').to_sym] = artifact("#{group}:#{group}.#{name}:jar:#{version}").from(jar)
    map
  end
)

REGISTRATION_CONSUMER_GRID = struct(
  Dir[static_lib('grid-consumer/RegistrationConsumer*.jar')].inject({}) do |map, jar|
    group, name = jar.scan(%r{.*/(RegistrationConsumer)-(\w+)\.jar$}).first
    version= "1.0"
    map[name.gsub('.', '_').to_sym] = artifact("#{group}:#{group}.#{name}:jar:#{version}").from(jar)
    map
  end
)

STUDY_CONSUMER_GRID = struct(
  Dir[static_lib('grid-consumer/StudyConsumer*.jar')].inject({}) do |map, jar|
    group, name = jar.scan(%r{.*/(StudyConsumer)-(\w+)\.jar$}).first
    version= "1.0"
    map[name.gsub('.', '_').to_sym] = artifact("#{group}:#{group}.#{name}:jar:#{version}").from(jar)
    map
  end
)

DBUNIT_GRID= "org.dbunit:dbunit:jar:2.2"
