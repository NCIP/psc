#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

desc "Check the java source to ensure that no two modules define the same package"
task :check_module_packages do
  project_packages = projects.inject({}) do |h, proj|
    h[proj.name] = Dir[proj._(:source, :main, :java) + "/**/*.java"].collect do |java|
      if File.read(java) =~ /package\s+([a-zA-Z.]*);/
        $1
      end
    end.compact.uniq.sort
    h
  end
  pairs = []
  (0 ... (project_packages.size)).each do |i|
    pairs.concat(
      ((i + 1) ... (project_packages.size)).collect do |j|
        [project_packages.keys[i], project_packages.keys[j]]
      end
    )
  end
  errors = pairs.collect do |a, b|
    overlap = project_packages[a] & project_packages[b]
    unless overlap.empty?
      "#{a} and #{b} both include the following package#{'s' if overlap.size != 1}:\n - #{overlap.join("\n - ")}"
    end
  end.compact
  unless errors.empty?
    error errors.join("\n")
    raise "There are repeated packages"
  end
end