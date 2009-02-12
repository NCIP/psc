# Dependency groups used by PSC's buildfile

###### REPOS

# NU m2 repo (for ctms-commons, caGrid, other non-repo dependencies)
repositories.remote << "http://download.bioinformatics.northwestern.edu/maven2/"
# ical4j repo
repositories.remote << "http://m2.modularity.net.au/releases/"
# main m2 repo
repositories.remote << "http://repo1.maven.org/maven2/"

###### HELPERS

# Define an artifact whose group is the same as its id
def eponym(artifact, version)
  "#{artifact}:#{artifact}:jar:#{version}"
end

###### DEPS

# Only list versions which appear in more than one artifact here
CTMS_COMMONS_VERSION = "0.9-SNAPSHOT"
CORE_COMMONS_VERSION = "77"
SPRING_VERSION = "2.0.7"

CTMS_COMMONS = group(%w{base core laf lang web}.map { |a| "ctms-commons-#{a}"}, 
  :under => "gov.nih.nci.cabig.ctms", :version => CTMS_COMMONS_VERSION)

CORE_COMMONS = "edu.northwestern.bioinformatics:core-commons:jar:#{CORE_COMMONS_VERSION}"

XML = [
  eponym("dom4j", "1.6.1"),
  eponym("jdom", "1.0b8")
]

LOGBACK = group(%w{log4j-bridge logback-core logback-classic},
  :under => "ch.qos.logback", :version => "0.9.7")
SLF4J = group('slf4j-api', 'jcl104-over-slf4j', 'jul-to-slf4j',
  :under => "org.slf4j", :version => "1.5.2")

JAKARTA_COMMONS = [
  eponym("commons-beanutils", "1.7.0"),
  eponym("commons-collections", "3.2"),
  eponym("commons-dbcp", "1.2.1"),
  eponym("commons-digester", "1.7"),
  eponym("commons-discovery", "0.4"),
  "org.apache.commons:commons-io:jar:1.3.2",
  eponym("commons-lang", "2.1"),
  eponym("commons-pool", "1.2"),
  eponym("commons-fileupload", "1.2")
]

SPRING = [
  "org.springframework:spring:jar:#{SPRING_VERSION}",
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
  "net.sf.ehcache:ehcache:jar:1.2.3",
  "javax.transaction:jta:jar:1.0.1B",
  "javax.persistence:persistence-api:jar:1.0"
]

SECURITY = [
  group('acegi-csm', 'acegi-grid',
    :under => "gov.nih.nci.security.acegi", :version => CTMS_COMMONS_VERSION),
  "gov.nih.nci.security:clm:jar:3.2.1-ctms00",
  "gov.nih.nci.security:csmapi:jar:3.2.1-ctms00",
  "org.acegisecurity:acegi-security:jar:1.0.3",
  "cas:casclient:jar:2.0.11"
]

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
  "org.globus:cog-url:jar:1.2"
]

BERING = [
  "edu.northwestern.bioinformatics:bering:jar:0.6.1",
  eponym("groovy", "1.0-jsr-06"),
  eponym("asm", "2.2.3")
]

WEB = [
  "itext:itext:jar:1.3.1",
  "opensymphony:sitemesh:jar:2.2.1",
  "poi:poi-2.5.1-final:jar:20040804",
  "org.freemarker:freemarker:jar:2.3.10",
  "taglibs:standard:jar:1.1.2",
  eponym("jstl", "1.1.2"),
  "net.fortuna:ical4j:jar:1.0-beta4"
]

CONTAINER_PROVIDED = [
  "javax.servlet:servlet-api:jar:2.5",
  "javax.servlet:jsp-api:jar:2.0"
]

UNIT_TESTING = [
  "edu.northwestern.bioinformatics:core-commons-testing:jar:#{CORE_COMMONS_VERSION}",
  eponym("dbunit", "2.1"),
  "org.easymock:easymock:jar:2.2",
  "org.easymock:easymockclassextension:jar:2.2.2",
  "org.springframework:spring-mock:jar:#{SPRING_VERSION}"
]

DB = struct(
  :hsqldb => eponym("hsqldb", "1.8.0.7"),
  :postgresql => eponym("postgresql", "8.2-504.jdbc3"),
  :oracle => "com.oracle:ojdbc14:jar:10.2.0.2.0"
)
