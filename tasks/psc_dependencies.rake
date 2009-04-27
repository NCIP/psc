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
# dynamicjava.org repo
repositories.remote << "http://maven.dynamicjava.org"
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

def cagrid_lib(org, mod, art, bnd_props = nil)
  ivy_artifact(
    "http://software.cagrid.org/repository-#{CAGRID_VERSION}",
    "cagrid-ivy", org, mod, CAGRID_VERSION, art, bnd_props)
end

def ncicb_lib(org, mod, art, ver, bnd_props = nil)
  ivy_artifact(
    "https://gforge.nci.nih.gov/svnroot/commonlibrary/trunk/ivy-repo",
    "ncicb-common", org, mod, ver, art, bnd_props)
end

def ivy_artifact(repo_url, repo_group, org, mod, rev, art, bnd_props = nil)
  # TODO: base this on ivysettings.xml instead
  url = "#{repo_url}/#{org}/#{mod}/#{rev}/#{art}-#{rev}.jar"
  artifact_spec = "#{repo_group}.#{org}.#{mod}:#{art}:jar:#{rev}"
  if bnd_props.nil?
    download(artifact(artifact_spec) => url)
  else
    psc_osgi_artifact(artifact_spec, bnd_props) { |src|
      download(src => url)
    }
  end
end

###### DEPS

# Only list versions which appear in more than one artifact here
CTMS_COMMONS_VERSION = "1.0.0-SNAPSHOT"
CORE_COMMONS_VERSION = "77"
SPRING_VERSION = "2.5.6"
RESTLET_VERSION = "1.1.1"
SLF4J_VERSION = "1.5.6"
CAGRID_VERSION = "1.2"

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

# Note that this bundle of logback-0.9.9 is not compatible, OSGi-wise, with 
# slf4j-1.5.6 below.
LOGBACK = struct(
  :core    => "ch.qos.logback:com.springsource.ch.qos.logback.core:jar:0.9.9",
  :classic => "ch.qos.logback:com.springsource.ch.qos.logback.classic:jar:0.9.9",
  # not technically part of logback, but exclusively used by it
  :janino  => "org.codehaus.janino:com.springsource.org.codehaus.janino:jar:2.5.15"
)

SLF4J = struct(
  :api   => "org.slf4j:com.springsource.slf4j.api:jar:#{SLF4J_VERSION}",
  :jcl   => "org.slf4j:com.springsource.slf4j.org.apache.commons.logging:jar:#{SLF4J_VERSION}",
  :log4j => "org.slf4j:com.springsource.slf4j.org.apache.log4j:jar:#{SLF4J_VERSION}",
  :jul   => "org.slf4j:com.springsource.slf4j.bridge:jar:#{SLF4J_VERSION}"
)

# For use inside the osgi layer until SpringSource releases a bundle of logback
# that's compatible with slf4j-1.5.6
LOG4J = struct(
  :main  => "org.apache.log4j:com.springsource.org.apache.log4j:jar:1.2.15",
  :slf4j => "org.slf4j:com.springsource.slf4j.log4j:jar:1.5.6"
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
  :collections_generic => psc_osgi_artifact("net.sourceforge.collections:collections-generic:jar:4.01"),
  :validator  => spring_osgi_apache_commons("validator", "1.1.4"),
  :discovery  => spring_osgi_apache_commons("discovery", "0.4.0"),
  :httpclient => spring_osgi_apache_commons("httpclient", "3.1.0"),
  :codec      => spring_osgi_apache_commons("codec", "1.3.0"),
  :net        => spring_osgi_apache_commons("net", "1.4.1"),
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

# Alt: "javax.xml.stream:com.springsource.javax.xml.stream:jar:1.0.1"
STAX_API = artifact("org.dynamicjava.jsr:stax-api:jar:1.0.1").
  from(static_lib("org.dynamicjava.stax-api-1.0.1.jar"))

HIBERNATE = struct(
  :main => "org.hibernate:com.springsource.org.hibernate:jar:3.3.1.GA",
  :annotations => "org.hibernate:com.springsource.org.hibernate.annotations:jar:3.4.0.GA",
  :annotations_common => "org.hibernate:com.springsource.org.hibernate.annotations.common:jar:3.3.0.ga",
  :antlr => "org.antlr:com.springsource.antlr:jar:2.7.7",
  :c3p0 => "com.mchange.c3p0:com.springsource.com.mchange.v2.c3p0:jar:0.9.1.2",
  :cglib => CGLIB,
  :javax_transaction => "javax.transaction:com.springsource.javax.transaction:jar:1.1.0",
  :javax_persistence => "javax.persistence:com.springsource.javax.persistence:jar:1.0.0",
  :javassist => "org.jboss.javassist:com.springsource.javassist:jar:3.3.0.ga",
  :dom4j => "org.dom4j:com.springsource.org.dom4j:jar:1.6.1",
  :stax => STAX_API,
  :jgroups => "org.jgroups:com.springsource.org.jgroups:jar:2.5.1"
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
  :acegi => psc_osgi_artifact("org.acegisecurity:acegi-security:jar:1.0.7"),
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

GLOBUS = struct(
  :core => Dir[static_lib("psc-globus-all*.jar")].collect { |jar|
      version = File.basename(jar).split('_')[1].gsub(/.jar/, '')
      artifact("org.globus:edu.northwestern.bioinformatics.osgi.org.globus.all:jar:#{version}").from(jar)
    }.first,
  :discovery  => JAKARTA_COMMONS.discovery,
  :httpclient => JAKARTA_COMMONS.httpclient,
  :codec      => JAKARTA_COMMONS.codec,
  :net        => JAKARTA_COMMONS.net,
  :servlet    => "javax.servlet:com.springsource.javax.servlet:jar:2.5.0",
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
  :staxex     => "org.jvnet.staxex:com.springsource.org.jvnet.staxex:jar:1.0.0"
)

GLOBUS_AXIS_STUB_PACKAGES = %w(
  org.apache.axis org.apache.axis.client org.apache.axis.configuration 
  org.apache.axis.constants
  org.apache.axis.description org.apache.axis.utils org.apache.axis.types
  org.apache.axis.message.addressing org.apache.axis.soap
  org.globus.axis.util javax.xml.namespace org.w3c.dom
  org.ietf.jgss
)

# Some of this is generic caGrid/introduce stuff -- split it out later
COPPA_VERSION = "1.2.0.PSC000"
# fragment client-config.wsdd (grid/globus) jars onto the globus metabundle
# client_config_bnd = { "Fragment-Host" => "edu.northwestern.bioinformatics.osgi.org.globus.all" }
COPPA = [
  # COPPA introduce-stubs
  psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-stubs:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-stubs.jar")), 
      "Export-Package" => "!gov.nih.nci.cagrid.introduce.security.stubs, *"),
  psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-common:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-common.jar"))),
  psc_osgi_artifact(
      artifact("gov.nih.nci.coppa:coppa-core-services-client:jar:#{COPPA_VERSION}").from(static_lib("coppa/CoreServices-client.jar"))),
  # artifact("gov.nih.nci.coppa:edu.northwestern.bioinformatics.osgi.coppa-jaxb-adapter:jar:0.0.0").
  #   from(static_lib('coppa/coppa-jaxb-adapter-0.0.0.jar')),
  artifact("gov.nih.nci.coppa:edu.northwestern.bioinformatics.osgi.coppa-globus-adapter:jar:0.0.0").
    from(static_lib('coppa/coppa-globus-adapter-0.0.0.jar')),
  
  # Trial-and-error caGrid/globus deps
  cagrid_lib("caGrid", "service-security-provider", "caGrid-ServiceSecurityProvider-stubs", 
    { "Fragment-Host" => "edu.northwestern.bioinformatics.osgi.org.globus.all",
      "Import-Package" => (%w(javax.xml.rpc javax.xml.rpc.encoding gov.nih.nci.cagrid.metadata.security)).join(",") }),
    # { "Import-Package" => (%w(javax.xml.rpc javax.xml.rpc.encoding) + GLOBUS_AXIS_STUB_PACKAGES).join(",") }),
  cagrid_lib("caGrid", "service-security-provider", "caGrid-ServiceSecurityProvider-common", {}),
  cagrid_lib("caGrid", "service-security-provider", "caGrid-ServiceSecurityProvider-client", 
    { "Import-Package" => (%w(
        gov.nih.nci.cagrid.introduce.security.stubs gov.nih.nci.cagrid.introduce.security.stubs.service
        gov.nih.nci.cagrid.introduce.security.common gov.nih.nci.cagrid.metadata.security
        ) + GLOBUS_AXIS_STUB_PACKAGES).join(",") }),
  cagrid_lib("caGrid", "metadata", "caGrid-metadata-common", {}),
  cagrid_lib("caGrid", "metadata", "caGrid-metadata-data", {}),
  cagrid_lib("caGrid", "metadata", "caGrid-metadata-security", {}),
  artifact("gov.nih.nci.cagrid:edu.northwestern.bioinformatics.osgi.cagrid-globus-adapter:jar:0.0.0").
    from(static_lib('cagrid/cagrid-globus-adapter-0.0.0.jar'))
].flatten

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
  :postgresql => eponym("postgresql", "8.2-504.jdbc3"),
  :oracle => "com.oracle:ojdbc14:jar:10.2.0.2.0"
)
