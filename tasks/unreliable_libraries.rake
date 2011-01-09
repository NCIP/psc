# Tasks for clearing the local cache of libraries that may change
# upstream.

namespace :local_repo do
  desc "Clear the SpringSource-wrapped OSGi bundles from the local M2 repo"
  task :clear_springsource do
    unreliable = Dir["#{repositories.local}/**/com.springsource*/"]
    rm_rf unreliable
    info "Removed #{unreliable.size} SpringSource bundle(s) from #{repositories.local}"
  end

  # With the changes for #1310, there should never be any of these any
  # more.  Might as well make sure, though.
  desc "Clear any PSC-bundled artifacts from the local M2 cache"
  task :clear_psc_bundled do
    unreliable = Dir["#{repositories.local}/**/edu.northwestern.bioinformatics.osgi*/"]
    rm_rf unreliable
    info "Removed #{unreliable.size} PSC-created bundle(s) from #{repositories.local}"
  end

  task :clear_unreliable => [:clear_springsource, :clear_psc_bundled]
end
