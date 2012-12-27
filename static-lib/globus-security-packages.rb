#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

# Lists out the packages included in the unduplicable JARs in globus so that
# they can be added to OSGI's bootdelegation list

# Per http://cagrid.org/display/knowledgebase/GSSAPI+-+Bad+Certificate+Error+Solution
UNDUPLICABLE = [
  /cog-jglobus/, /cryptix/, /jce-jdk13-125/, /jgss/, /puretls/
]
GLOBUS_LOCATION = ENV['GLOBUS_LOCATION'] or raise "No GLOBUS_LOCATION"

jars = Dir["#{GLOBUS_LOCATION}/lib/*.jar"].select { |jar|
  UNDUPLICABLE.detect { |re| re =~ File.basename(jar) }
}

def packages(jarfile)
  `jar tf #{jarfile}`.split(/\n/).select { |entry| entry =~ /class$/ }.
    collect { |entry| entry.sub(/\/[^\/]+$/, '').gsub('/', '.') }.uniq.sort
end

jars.each do |jar|
  puts File.basename(jar)
  puts "- #{packages(jar).join("\n- ")}"
end
