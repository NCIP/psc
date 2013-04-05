#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# Uses before_define to default all projects to including their resources from
# src/main/java instead of src/main/resources (& similar for test) if
# those source directories exist
module InlineResources
  include Buildr::Extension
  
  before_define do |p|
    [
      [p.resources, p._("src/main/java")],
      [p.test.resources, p._("src/test/java")]
    ].each do |res, path|
      if File.exist?(path)
        res.from(path).exclude("**/*.java")
      end
    end
  end
end

class Buildr::Project
  include InlineResources
end