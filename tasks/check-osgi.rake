#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# Adds a task that checks a project's dependencies to figure out which
# ones lack OSGi's required Bundle-SymbolicName manifest entry.

module CheckNonOsgiDependencies
  include Buildr::Extension
  
  MANIFEST_PATH = "META-INF/MANIFEST.MF"
  
  first_time do
    Project.local_task('osgi:check')
  end
  
  before_define do |p|
    Rake::Task.define_task 'osgi:check' do
      trouble = p.compile.dependencies.reject do |art|
        if art.type == :jar
          puts "Checking #{art} manifest for OSGiness" if Buildr.application.options.trace
          art.invoke # ensure it has been downloaded
          Zip::ZipFile.open(art.to_s) do |jar|
            jar.find_entry(MANIFEST_PATH) && jar.read(MANIFEST_PATH) =~ /Bundle-SymbolicName/
          end
        else
          true # skip non-jar artifacts
        end
      end
      
      unless trouble.empty?
        error "The following artifacts are not OSGi bundles:\n - #{trouble.collect(&:to_spec).join("\n - ")}"
      else
        info "#{p.name} has no deps without Bundle-SymbolicName\n  manifest entries.  Woo hoo!"
      end
    end
  end
end

class Buildr::Project
  include CheckNonOsgiDependencies
end