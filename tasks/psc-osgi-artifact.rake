# Provides extensions to Buildr's Artifact task to use locally-stored,
# bnd-wrapped versions of some libraries.
#
# The libs are stored in osgi/bundled-lib.  A bnd file to use can be placed
# in osgi/instructions.  The bnd file may be named, in order of preference,
# "#{group}-#{id}-#{type}-#{version}.bnd" or "#{group}-#{id}-#{type}.bnd"

def psc_osgi_artifact(spec)
  src_spec = Artifact.to_hash(spec)
  # wrapped id per SpringSource repo model
  dst_spec = src_spec.merge( :id => "edu.northwestern.bioinformatics.osgi.#{src_spec[:id]}" )
  unless task = Artifact.lookup(dst_spec)
    src = artifact(src_spec)
    task = Osgi::BundledArtifact.define_task(repositories.locate(dst_spec))
    task.send :apply_spec, dst_spec
    task.init(src)
    task.enhance [src]
    Rake::Task['rake:artifacts'].enhance [task]
    Artifact.register(task)
  end
end

module Osgi
  class BundledArtifact < Buildr::Artifact
    def init(src_artifact)
      @src_artifact = src_artifact
      @bnd_task = create_bnd_task
      if bnd_file
        trace "Using bnd instructions from #{bnd_file}."
        @bnd_task.enhance [bnd_file]
      else
        trace "No wrapping instructions.  Looked for one of #{bnd_file_possibilities.inspect}."
      end
      self.from(bundled_file)
    end
    
    
    def bundled_file
      "#{basedir}/osgi/bundled-lib/#{group_path}/#{id}/#{version}/#{Artifact.hash_to_file_name(self.to_spec_hash)}"
    end
    
    def bnd_file
      bnd_file_possibilities.detect { |f| File.exist? f }
    end
    
    protected
    
    def basedir
      File.expand_path("..", File.dirname(__FILE__))
    end
    
    def bnd_file_possibilities
      basename = "#{basedir}/osgi/instructions/#{group}-#{id}-#{type}"
      ["#{basename}-#{version}.bnd", "#{basename}.bnd"]
    end
    
    def create_bnd_task
      Rake::FileTask.define_task(bundled_file => [@src_artifact]) do |task|
        mkdir_p File.dirname(bundled_file)
        Buildr::ant("bnd") do |ant|
          bndargs = {
            :jars => @src_artifact.to_s, 
            :output => bundled_file
          }
          bndargs[:definitions] = bnd_file if bnd_file
          ant.taskdef :resource => 'aQute/bnd/ant/taskdef.properties',
            :classpath => Java.classpath.flatten.join(':')
          trace "Wrapping #{File.basename(@to_wrap.to_s)} into #{bundled_file}"
          ant.bndwrap bndargs
        end
      end
    end
  end
end