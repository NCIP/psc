#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

require 'fileutils'
include FileUtils::Verbose

# CAGRID_MIRROR should point to a local file path designating a mirror
# of caGrid's ivy repo for the version of caGrid you're targeting. You
# can build such a mirror with wget, e.g.:
#
#   $ wget -nH -nc -np -l 4 -r http://software.cagrid.org/repository-1.3/caGrid/
#
CAGRID_LOCATION = ENV['CAGRID_MIRROR'] or raise "No CAGRID_MIRROR"
# Exclude test, GUI, and sample code
CAGRID_EXCLUDE = [
  /-ui/, /-deprecated/, /installer/, /-graph/, /test/, /wizard/
]
CAGRID_VERSION = "1.4.0"
BUNDLE_VERSION = "#{CAGRID_VERSION}.PSC000"
JAR_NAME = "psc-cagrid-all_#{BUNDLE_VERSION}.jar"
BND_NAME = JAR_NAME.sub /jar^/, 'bnd'

tmpdir = "/tmp/cagrid-all"
mkdir_p tmpdir

FileUtils.cd(tmpdir) do
  Dir["#{CAGRID_LOCATION}/**/*.jar"].reject { |jar|
    CAGRID_EXCLUDE.detect { |re| re =~ File.basename(jar) }
  }.each { |jar|
    puts "Unpacking #{File.basename jar}"
    system("jar xf #{jar}")
  }
  rm_rf 'META-INF'
end

File.open("#{tmpdir}/#{BND_NAME}", 'w') do |f|
  f.puts DATA.read
  f.puts "Bundle-Version: #{BUNDLE_VERSION}"
end

puts "Invoking bnd to create #{JAR_NAME}"
system("java -jar bnd-0.0.313.jar build -classpath #{tmpdir} -output #{JAR_NAME} #{tmpdir}/#{BND_NAME}")

__END__
Export-Package: *
Import-Package: *;resolution:=optional
Bundle-Name: PSC's caGrid composite bundle
Bundle-SymbolicName: edu.northwestern.bioinformatics.osgi.gov.nih.nci.cagrid.all
Bundle-Description: Contains all the runtime classes in the corresponding version of caGrid, excluding tests, GUI code, and full webapps
