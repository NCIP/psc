#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

# Remove the slf4j commons-logging adapter from the build classpath.
# When it is present, JCL loggers get bound to the simple logger used
# by jetty itself, making it impossible to get to DEBUG-level logging
# in deployed applications.
#
# It would be nice to patch buildr to allow this more cleanly, but I'm
# not sure how. -- RMS20090213

module Java
  class << self
    def classpath=(newcp)
      @classpath = newcp
    end
  end
end

require 'buildr/jetty'

Java.classpath = Java.classpath.flatten.reject { |a| a.to_s =~ /jcl104/ }