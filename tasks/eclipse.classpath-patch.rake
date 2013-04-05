#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# A patch for the Eclipse file generation task.
# Details
#   classpath- : Buildr generates the lib classpath of dependent project using the
#                absolute file path, whereas Eclipse expects the project dependencies using
#                the relative path of projects in workspace

require 'buildr/ide/eclipse'
module Buildr
  module Eclipse
    class ClasspathEntryWriter
      def lib libs
        libs.map(&:to_s).sort.uniq.collect! { |path|
          project = Buildr.projects.select { |prj| path.to_s.index(prj.base_dir) == 0 }.last 
          path = project.nil??path:path.to_s.gsub(project.base_dir,"/#{project.id}")
          path
        }.each do |final_path|
          @xml.classpathentry :kind=>'lib', :path=>final_path
        end
      end
    end
  end
end
