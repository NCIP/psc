#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

require 'fileutils'
include FileUtils::Verbose

GLOBUS_LOCATION = ENV['GLOBUS_LOCATION'] or raise "No GLOBUS_LOCATION"
GLOBUS_EXCLUDE = [
  # Exclude shared libraries, test & sample code
  /^commons-/, /^x(a|e|ml-)/, /junit/, /samples/, /test/, /jaxrpc/, /servlet/, /log4j/, /saaj/, /wsdl/,
  # Exclude JCE-dependent security code per http://cagrid.org/display/knowledgebase/GSSAPI+-+Bad+Certificate+Error+Solution
  /^cog-jglobus/, /^cryptix/, /^jce-jdk13-125/, /^jgss/, /^puretls/
]
GLOBUS_VERSION = "4.0.3"
BUNDLE_VERSION = "#{GLOBUS_VERSION}.WS-CORE-PSC003"
JAR_NAME = "psc-globus-all_#{BUNDLE_VERSION}.jar"
BND_NAME = JAR_NAME.sub /jar^/, 'bnd'

tmpdir = "/tmp/globus-all"
mkdir_p tmpdir

FileUtils.cd(tmpdir) do
  Dir["#{GLOBUS_LOCATION}/lib/*.jar"].reject { |jar|
    GLOBUS_EXCLUDE.detect { |re| re =~ File.basename(jar) }
  }.each { |jar|
    puts "Unpacking #{File.basename jar}"
    system("jar xf #{jar}")
  }
  rm_rf 'META-INF'
  rm_rf 'javax/crypto' # Included in JRE
end

File.open("#{tmpdir}/#{BND_NAME}", 'w') do |f|
  f.puts DATA.read
  f.puts "Bundle-Version: #{BUNDLE_VERSION}"
end

puts "Invoking bnd to create #{JAR_NAME}"
system("java -jar bnd-0.0.313.jar build -classpath #{tmpdir} -output #{JAR_NAME} #{tmpdir}/#{BND_NAME}")

# The explicit imports there are mostly the dependencies of Axis, according to SpringSource
__END__
Export-Package: *
Import-Package: \
 com.ibm.wsdl.extensions.soap;version="[1.6.1, 2.0.0)", \
 com.sun.jimi.core;resolution:=optional, \
 com.sun.net.ssl;resolution:=optional, \
 com.sun.net.ssl.internal.ssl;resolution:=optional, \
 javax.activation;version="[1.1.0, 2.0.0)", \
 javax.imageio, \
 javax.imageio.metadata, \
 javax.imageio.stream, \
 javax.jms;version="[1.1.0, 2.0.0)", \
 javax.mail;version="[1.4.0, 2.0.0)", \
 javax.mail.internet;version="[1.4.0, 2.0.0)", \
 javax.naming, \
 javax.naming.spi, \
 javax.net, \
 javax.net.ssl, \
 javax.rmi, \
 javax.servlet;version="[2.4.0, 3.0.0)", \
 javax.servlet.http;version="[2.4.0, 3.0.0)", \
 javax.swing, \
 javax.swing.border, \
 javax.swing.event, \
 javax.swing.plaf.basic, \
 javax.swing.table, \
 javax.swing.text, \
 javax.wsdl;version="[1.6.1, 2.0.0)", \
 javax.wsdl.extensions;version="[1.6.1, 2.0.0)", \
 javax.wsdl.extensions.http;version="[1.6.1, 2.0.0)", \
 javax.wsdl.extensions.mime;version="[1.6.1, 2.0.0)", \
 javax.wsdl.extensions.soap;version="[1.6.1, 2.0.0)", \
 javax.wsdl.factory;version="[1.6.1, 2.0.0)", \
 javax.wsdl.xml;version="[1.6.1, 2.0.0)", \
 javax.xml.namespace, \
 javax.xml.parsers, \
 javax.xml.rpc, \
 javax.xml.rpc.encoding, \
 javax.xml.rpc.handler, \
 javax.xml.rpc.handler.soap, \
 javax.xml.rpc.holders, \
 javax.xml.rpc.server, \
 javax.xml.rpc.soap, \
 javax.xml.soap, \
 javax.xml.transform, \
 javax.xml.transform.dom, \
 javax.xml.transform.sax, \
 javax.xml.transform.stream, \
 org.apache.bsf;version="[2.4.0, 3.0.0)";resolution:=optional, \
 org.apache.commons.discovery;version="[0.4.0, 1.0.0)", \
 org.apache.commons.discovery.resource;version="[0.4.0, 1.0.0)", \
 org.apache.commons.discovery.resource.classes;version="[0.4.0, 1.0.0)", \
 org.apache.commons.discovery.resource.names;version="[0.4.0, 1.0.0)", \
 org.apache.commons.discovery.tools;version="[0.4.0, 1.0.0)", \
 org.apache.commons.httpclient;version="[3.1.0, 4.0.0)", \
 org.apache.commons.httpclient.auth;version="[3.1.0, 4.0.0)", \
 org.apache.commons.httpclient.methods;version="[3.1.0, 4.0.0)", \
 org.apache.commons.httpclient.params;version="[3.1.0, 4.0.0)", \
 org.apache.commons.httpclient.protocol;version="[3.1.0, 4.0.0)", \
 org.apache.commons.logging;version="[1.1.1, 2.0.0)", \
 org.apache.commons.net.pop3;version="[1.4.1, 2.0.0)", \
 org.apache.commons.net.smtp;version="[1.4.1, 2.0.0)", \
 org.exolab.castor.xml;version="[1.1.2, 2.0.0)";resolution:=optional, \
 org.omg.CORBA, \
 org.omg.CosNaming, \
 org.w3c.dom, \
 org.xml.sax, \
 org.xml.sax.ext, \
 org.xml.sax.helpers, \
 *;resolution:=optional
Bundle-Name: PSC's Globus composite bundle
Bundle-SymbolicName: edu.northwestern.bioinformatics.osgi.org.globus.all
Bundle-Description: Contains the contents of the lib directory for the corresponding Globus version, except for 3rd-party libs, tests, samples, and JCE-using security code