#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# Adds some methods for cleaner dependency on another project and its deps
class Buildr::Project
  def and_dependencies
    [self, self.compile.dependencies].compact
  end
  
  def test_dependencies
    [self.test.compile.target, self.test.resources.target, self.test.compile.dependencies].compact
  end
end