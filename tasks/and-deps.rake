# Adds some methods for cleaner dependency on another project and its deps
class Buildr::Project
  def and_dependencies
    [self, self.compile.dependencies].compact
  end
  
  def test_dependencies
    [self.test.compile.target, self.test.resources.target, self.test.compile.dependencies].compact
  end
end