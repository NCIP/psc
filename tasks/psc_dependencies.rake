# Dependency groups used by PSC's buildfile

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
# main m2 repo
repositories.remote << "http://repo1.maven.org/maven2"

###### HELPERS

# Define an artifact whose group is the same as its id
def eponym(artifact, version)
  "#{artifact}:#{artifact}:jar:#{version}"
end

def static_lib(filename)
  File.expand_path(filename, [File.dirname(__FILE__), '..', 'static-lib'].join('/'))
end

###### DEPS

# Only list versions which appear in more than one artifact here
CTMS_COMMONS_VERSION = "1.0.0-SNAPSHOT"
CORE_COMMONS_VERSION = "77"
SPRING_VERSION = "2.5.6"
RESTLET_VERSION = "1.1.1"
SLF4J_VERSION = "1.5.0"

CTMS_COMMONS = struct(
  %w{base core laf lang web}.inject({}) do |h, a|
    h[a.to_sym] = "gov.nih.nci.cabig.ctms:ctms-commons-#{a}:jar:#{CTMS_COMMONS_VERSION}"
    h
  end
)
CORE_COMMONS = "edu.northwestern.bioinformatics:core-commons:jar:#{CORE_COMMONS_VERSION}"

XML = [
  "org.dom4j:com.springsource.org.dom4j:jar:1.5.2",
  # "org.jdom:com.springsource.org.jdom:jar:1.0.0", # unused?
  # Saxon 9 isn't in the maven repo for some reason
  artifact("net.sf.saxon:saxon:jar:9").from(static_lib('saxon9.jar')),
  artifact("net.sf.saxon:saxon-dom:jar:9").from(static_lib('saxon9-dom.jar'))
]

CSV = [
  "net.sourceforge.javacsv:javacsv:jar:2.0"
]

LOGBACK = struct(
  :core    => "ch.qos.logback:com.springsource.ch.qos.logback.core:jar:0.9.9",
  :classic => "ch.qos.logback:com.springsource.ch.qos.logback.classic:jar:0.9.9",
  # not technically part of logback, but exclusively used by it
  :janino  => "org.codehaus.janino:com.springsource.org.codehaus.janino:jar:2.5.15"
)

SLF4J = struct(
  :api   => "org.slf4j:com.springsource.slf4j.api:jar:#{SLF4J_VERSION}",
  :jcl   => "org.slf4j:com.springsource.slf4j.org.apache.commons.logging:jar:#{SLF4J_VERSION}",
  :log4j => "org.slf4j:com.springsource.slf4j.org.apache.log4j:jar:#{SLF4J_VERSION}"
  # SpringSource doesn't have this package for SLF4J 1.5.0, so it is disabled until
  # they release a version of logback for SLF4J 1.5.6
  # :jul   => "org.slf4j:com.springsource.slf4j.bridge:jar:#{SLF4J_VERSION}"
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
  :lang       => spring_osgi_apache_commons("lang", "2.1.0"),
  :pool       => spring_osgi_apache_commons("pool", "1.4.0"),
  :fileupload => spring_osgi_apache_commons("fileupload", "1.2.0"),
  :collections_generic => "net.sourceforge.collections:collections-generic:jar:4.01",
  :validator  => spring_osgi_apache_commons("validator", "1.1.4")
})

SPRING = [
  "org.springframework:spring:jar:#{SPRING_VERSION}",
]

SPRING_WEB = [
  "org.springframework:spring-webmvc:jar:#{SPRING_VERSION}",
  "org.springframework:spring-webflow:jar:1.0.5",
  "org.springframework:spring-binding:jar:1.0.5",
  "javax.activation:com.springsource.javax.activation:jar:1.1.1",
  "javax.mail:com.springsource.javax.mail:jar:1.4.1",
  "org.apache.oro:com.springsource.org.apache.oro:jar:2.0.8",
  "org.ognl:com.springsource.org.ognl:jar:2.6.9" # For webflow
]

CGLIB = "net.sourceforge.cglib:com.springsource.net.sf.cglib:jar:2.1.3"

HIBERNATE = struct(
  :main => "org.hibernate:com.springsource.org.hibernate:jar:3.3.1.GA",
  :annotations => "org.hibernate:com.springsource.org.hibernate.annotations:jar:3.4.0.GA",
  :annotations_common => "org.hibernate:com.springsource.org.hibernate.annotations.common:jar:3.3.0.ga",
  :validator => "org.hibernate:com.springsource.org.hibernate.validator:jar:3.1.0.GA",
  :antlr => "org.antlr:com.springsource.antlr:jar:2.7.7",
  :c3p0 => "com.mchange.c3p0:com.springsource.com.mchange.v2.c3p0:jar:0.9.1.2",
  :cglib => CGLIB,
  :javax_transaction => "javax.transaction:com.springsource.javax.transaction:jar:1.1.0",
  :javax_persistence => "javax.persistence:com.springsource.javax.persistence:jar:1.0.0",
  :javassist => "org.jboss.javassist:com.springsource.javassist:jar:3.3.0.ga"
)

EHCACHE = struct(
  :ehcache => "net.sourceforge.ehcache:com.springsource.net.sf.ehcache:jar:1.5.0",
  :jsr107 => "net.sourceforge.jsr107cache:com.springsource.net.sf.jsr107cache:jar:1.0.0",
  :backport => "edu.emory.mathcs.backport:com.springsource.edu.emory.mathcs.backport:jar:3.0.0"
)

SECURITY = struct(
  :acegi_csm  => "gov.nih.nci.security.acegi:acegi-csm:jar:#{CTMS_COMMONS_VERSION}",
  :acegi_grid => "gov.nih.nci.security.acegi:acegi-grid:jar:#{CTMS_COMMONS_VERSION}",
  :clm => "gov.nih.nci.security:clm:jar:3.2.1-ctms00",
  :csm => psc_osgi_artifact(
    "gov.nih.nci.security:csmapi:jar:3.2.1-ctms00", 
    "Private-Package" => "test.*", "Export-Package" => "gov.nih.nci.*"
  ),
  :acegi => psc_osgi_artifact("org.acegisecurity:acegi-security:jar:1.0.3"),
  :cas => psc_osgi_artifact("cas:casclient:jar:2.0.11"),
  :caaers_cas => psc_osgi_artifact(artifact("gov.nih.nci.cabig.caaers:cas-patch:jar:1.1.3").from(static_lib('caaers-1.1.3-cas-patch.jar')))
)

# This is out of date, probably
CAGRID = [
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
  "org.apache.axis:axis:jar:1.4"
]

BERING = [
  "edu.northwestern.bioinformatics:bering:jar:0.7",
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
  artifact("org.johaml:johaml-bridge:jar:0.0").from(static_lib('johaml-bridge-0.0.jar')),
  artifact("org.johaml:patched-jruby:jar:1.1.3").from(static_lib('patched-jruby-complete-1.1.3.jar')),
  eponym("bsf", "2.4.0")
]

RESTLET = struct({
  :framework        => "org.restlet:org.restlet:jar:#{RESTLET_VERSION}",
  :spring_ext       => "org.restlet:org.restlet.ext.spring:jar:#{RESTLET_VERSION}",
  :freemarker_ext   => "org.restlet:org.restlet.ext.freemarker:jar:#{RESTLET_VERSION}",
  :json_ext         => "org.restlet:org.restlet.ext.json:jar:#{RESTLET_VERSION}",
  :nre              => "com.noelios.restlet:com.noelios.restlet:jar:#{RESTLET_VERSION}",
  :servlet_nre_ext  => "com.noelios.restlet:com.noelios.restlet.ext.servlet:jar:#{RESTLET_VERSION}",
  :spring_nre_ext   => "com.noelios.restlet:com.noelios.restlet.ext.spring:jar:#{RESTLET_VERSION}"
})

CONTAINER_PROVIDED = [
  "javax.servlet:com.springsource.javax.servlet:jar:2.5.0",
  "javax.servlet:com.springsource.javax.servlet.jsp:jar:2.0.0"
]

OSGI = struct(
  :core => 'org.osgi:osgi_R4_core:jar:1.0',
  :compendium => 'org.osgi:osgi_R4_compendium:jar:1.0'
)

KNOPFLERFISH = struct(
  Dir[static_lib('knopflerfish-2.2.0/**/*.jar')].inject({}) do |map, jar|
    group, name, version = jar.scan(%r{.*/(\w+)/(\w+)-([\d\.]+)\.jar$}).first
    map[name.to_sym] = artifact("org.knopflerfish.#{group}:knopflerfish-#{name}:jar:#{version}").from(jar)
    map
  end
)

FELIX = struct(
  Dir[static_lib('felix-1.4.1/*.jar')].inject({}) do |map, jar|
    group, name, version = jar.scan(%r{.*/(org.apache.felix)\.([\w\.]+)-([\d\.]+)\.jar$}).first
    map[name.gsub('.', '_').to_sym] = artifact("#{group}:#{group}.#{name}:jar:#{version}").from(jar)
    map
  end
)

EQUINOX = struct(
  Dir[static_lib('equinox-3.4.0/*.jar')].inject({}) do |map, jar|
    group, _, name, version = jar.scan(%r{.*/(org\.eclipse(\.equinox)?)\.([\w\.]+)_([\w\d\.-]+)\.jar$}).first
    map[name.gsub('.', '_').to_sym] = artifact("#{group}:#{group}.#{name}:jar:#{version}").from(jar)
    map
  end
)

DYNAMIC_JAVA = struct(
  # TODO: there's a dynamicjava.org maven repo now
  :da_launcher => artifact("org.dynamicjava:da-launcher:jar:1.1.1").from(static_lib('da-launcher-1.1.1.jar')),
  :osgi_commons => artifact("org.dynamicjava:osgi-commons:jar:1.1.2").from(static_lib('osgi-commons-1.1.2.jar'))
)

SPRING_OSGI = struct(
  :core => "org.springframework.osgi:org.springframework.osgi.core:jar:1.1.3.RELEASE",
  :io => "org.springframework.osgi:org.springframework.osgi.io:jar:1.1.3.RELEASE",
  :extender => "org.springframework.osgi:org.springframework.osgi.extender:jar:1.1.3.RELEASE"
)

UNIT_TESTING = [
  "edu.northwestern.bioinformatics:core-commons-testing:jar:#{CORE_COMMONS_VERSION}",
  "gov.nih.nci.cabig.ctms:ctms-commons-testing:jar:#{CTMS_COMMONS_VERSION}",
  CTMS_COMMONS.base,
  eponym("dbunit", "2.1"),
  "org.easymock:easymock:jar:2.2",
  "org.easymock:easymockclassextension:jar:2.2.2",
  CGLIB,
  "org.springframework:spring-test:jar:#{SPRING_VERSION}",
  eponym("xmlunit", "1.1"),
  "org.springframework.osgi:org.springframework.osgi.mock:jar:1.1.3.RELEASE",
  LOGBACK
].flatten

INTEGRATED_TESTING = [
  "org.jgrapht:jgrapht-jdk1.5:jar:0.7.3",
  "net.java.dev:jvyaml:jar:0.2.1"
]

BND = artifact("biz.aQute:bnd:jar:0.0.313").from(static_lib('bnd-0.0.313.jar'))

DB = struct(
  :hsqldb => "org.hsqldb:com.springsource.org.hsqldb:jar:1.8.0.9",
  :postgresql => eponym("postgresql", "8.2-504.jdbc3"),
  :oracle => "com.oracle:ojdbc14:jar:10.2.0.2.0"
)
