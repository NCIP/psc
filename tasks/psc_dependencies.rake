# Dependency groups used by PSC's buildfile

###### REPOS

# NU m2 repo (for ctms-commons, caGrid, other non-repo dependencies)
repositories.remote << "http://download.bioinformatics.northwestern.edu/download/maven2"
# restlet repo
repositories.remote << "http://maven.restlet.org"
# ical4j repo
repositories.remote << "http://m2.modularity.net.au/releases"
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
CTMS_COMMONS_VERSION = "0.9-SNAPSHOT"
CORE_COMMONS_VERSION = "77"
SPRING_VERSION = "2.5.6"
RESTLET_VERSION = "1.1.1"

CTMS_COMMONS = struct(
  %w{base core laf lang web}.inject({}) do |h, a|
    h[a.to_sym] = "gov.nih.nci.cabig.ctms:ctms-commons-#{a}:jar:#{CTMS_COMMONS_VERSION}"
    h
  end
)
CORE_COMMONS = "edu.northwestern.bioinformatics:core-commons:jar:#{CORE_COMMONS_VERSION}"

XML = [
  eponym("dom4j", "1.6.1"),
  eponym("jdom", "1.0b8")
]

CSV = [
  "net.sourceforge.javacsv:javacsv:jar:2.0"
]

LOGBACK = group(%w{log4j-bridge logback-core logback-classic},
  :under => "ch.qos.logback", :version => "0.9.7")
SLF4J = group('slf4j-api', 'jcl-over-slf4j', 'jul-to-slf4j',
  :under => "org.slf4j", :version => "1.5.2")

JAKARTA_COMMONS = struct({
  :beanutils  => eponym("commons-beanutils", "1.7.0"),
  :collections => eponym("commons-collections", "3.2"),
  :dbcp       => eponym("commons-dbcp", "1.2.1"),
  :digester   => eponym("commons-digester", "1.7"),
  :discovery  => eponym("commons-discovery", "0.4"),
  :io         => "org.apache.commons:commons-io:jar:1.3.2",
  :lang       => eponym("commons-lang", "2.1"),
  :pool       => eponym("commons-pool", "1.2"),
  :fileupload => eponym("commons-fileupload", "1.2"),
  :collections_generic => "net.sourceforge.collections:collections-generic:jar:4.01",
  :validator  => eponym("commons-validator", "1.1.4")
})

SPRING = [
  "org.springframework:spring:jar:#{SPRING_VERSION}",
]

SPRING_WEB = [
  "org.springframework:spring-webmvc:jar:#{SPRING_VERSION}",
  "org.springframework:spring-webflow:jar:1.0.5",
  "org.springframework:spring-binding:jar:1.0.5",
  "javax.activation:activation:jar:1.0.2",
  "javax.mail:mail:jar:1.3.2",
  eponym("oro", "2.0.8"),
  eponym("ognl", "2.6.9") # For webflow
]

HIBERNATE = [
  "org.hibernate:hibernate:jar:3.2.5.ga",
  "org.hibernate:hibernate-annotations:jar:3.2.0.ga",
  eponym("antlr", "2.7.6"),
  eponym("c3p0", "0.9.1"),
  "cglib:cglib-nodep:jar:2.1_3",
  "net.sf.ehcache:ehcache:jar:1.5.0",
  "net.sf.jsr107cache:jsr107cache:jar:1.0",
  "javax.transaction:jta:jar:1.0.1B",
  "javax.persistence:persistence-api:jar:1.0",
  eponym('backport-util-concurrent', '3.0')
]

SECURITY = [
  group('acegi-csm', 'acegi-grid',
    :under => "gov.nih.nci.security.acegi", :version => CTMS_COMMONS_VERSION),
  "gov.nih.nci.security:clm:jar:3.2.1-ctms00",
  "gov.nih.nci.security:csmapi:jar:3.2.1-ctms00",
  "org.acegisecurity:acegi-security:jar:1.0.3",
  "cas:casclient:jar:2.0.11",
  artifact("gov.nih.nci.cabig.caaers:cas-patch:jar:1.1.3").from(static_lib('caaers-1.1.3-cas-patch.jar'))
]

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
  eponym("groovy", "1.0-jsr-06"),
  eponym("asm", "2.2.3")
]

QUARTZ = [
  'opensymphony:quartz:jar:1.6.0'
]

FREEMARKER = [
  "org.freemarker:freemarker:jar:2.3.10"
]

WEB = [
  "itext:itext:jar:1.3.1",
  "opensymphony:sitemesh:jar:2.2.1",
  "poi:poi-2.5.1-final:jar:20040804",
  "taglibs:standard:jar:1.1.2",
  eponym("jstl", "1.1.2"),
  "net.fortuna:ical4j:jar:1.0-beta4",
  artifact("gov.nih.nci.ccts:smoketest-client:jar:1.1").from(static_lib("SmokeTestService-client.jar")),
  eponym('displaytag', '1.1.1'),
]

RESTLET = struct({
  :framework        => "org.restlet:org.restlet:jar:#{RESTLET_VERSION}",
  :spring_ext       => "org.restlet:org.restlet.ext.spring:jar:#{RESTLET_VERSION}",
  :freemarker_ext   => "org.restlet:org.restlet.ext.freemarker:jar:#{RESTLET_VERSION}",
  :nre              => "com.noelios.restlet:com.noelios.restlet:jar:#{RESTLET_VERSION}",
  :servlet_nre_ext  => "com.noelios.restlet:com.noelios.restlet.ext.servlet:jar:#{RESTLET_VERSION}",
  :spring_nre_ext   => "com.noelios.restlet:com.noelios.restlet.ext.spring:jar:#{RESTLET_VERSION}"
})

CONTAINER_PROVIDED = [
  "javax.servlet:servlet-api:jar:2.5",
  "javax.servlet:jsp-api:jar:2.0"
]

UNIT_TESTING = [
  "edu.northwestern.bioinformatics:core-commons-testing:jar:#{CORE_COMMONS_VERSION}",
  "gov.nih.nci.cabig.ctms:ctms-commons-testing:jar:#{CTMS_COMMONS_VERSION}",
  eponym("dbunit", "2.1"),
  "org.easymock:easymock:jar:2.2",
  "org.easymock:easymockclassextension:jar:2.2.2",
  "org.springframework:spring-test:jar:#{SPRING_VERSION}",
  eponym("xmlunit", "1.1"),
  LOGBACK
].flatten

INTEGRATED_TESTING = [
  "org.jgrapht:jgrapht-jdk1.5:jar:0.7.3",
  "net.java.dev:jvyaml:jar:0.2.1"
]

DB = struct(
  :hsqldb => eponym("hsqldb", "1.8.0.7"),
  :postgresql => eponym("postgresql", "8.2-504.jdbc3"),
  :oracle => "com.oracle:ojdbc14:jar:10.2.0.2.0"
)
