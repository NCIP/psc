module Bnd
  class << self
    def bnd_main(*args)
      Rjb.import("aQute.bnd.main.bnd").main([*args].flatten)
    end
  end
  
  include Buildr::Extension
  
  first_time do
    Java.classpath << "biz.aQute:bnd:jar:0.0.249"
    desc "Does `bnd print` on the packaged jar and stdouts the output for inspection"
    Project.local_task("bnd:print")
    desc "Generates a bnd properties file from the project metadata"
    Project.local_task("bnd:generate")
  end
  
  after_define do |project|
    jar = project.packages.detect { |pkg| pkg.type == :jar }
    
    project.task('bnd:print' => project.task('package')) do
      if jar
        Bnd.bnd_main(jar.to_s)
      else
        fail "#{project.name} does not have a jar to inspect"
      end
    end
    
    project.recursive_task('bnd:generate')
    if jar
      bndfile = jar.to_s.sub /jar$/, 'bnd'
      project.task('bnd:generate').enhance [bndfile]
      directory(File.dirname(bndfile))
      project.file(bndfile => [Buildr.application.buildfile, File.dirname(bndfile)]) do |task|
        File.open(task.name, 'w') do |f|
          project.bnd.write(f)
        end
      end
    end
    
    if jar && project.bnd.wrap?
      bndfile = project.task('bnd:generate').prerequisites.first.to_s
      bndjar = jar.to_s.sub(/jar$/, 'bndjar')
      
      project.task('bnd:wrap' => [jar, bndfile]) do |task|
        project.ant('bnd') do |ant|
          ant.taskdef :resource => 'aQute/bnd/ant/taskdef.properties',
            :classpath => Java.classpath.flatten.join(':')
          ant.bndwrap :jars => jar.to_s, 
            :output => bndjar,
            :definitions => File.dirname(bndfile)
        end
        info "Replacing #{File.basename(jar.to_s)} with bnd wrapped version"
        mv bndjar, jar.to_s
      end
      
      project.task('package').enhance [project.task('bnd:wrap')]
    end
  end
  
  def bnd
    @bnd ||= BndProperties.new(self)
  end
  
  private
  
  class BndProperties
    attr_writer :classpath, :symbolic_name, :version
    
    BND_TO_ATTR = {
      '-classpath' => :classpath,
      'Bundle-Version' => :version,
      'Bundle-SymbolicName' => :symbolic_name,
      'Import-Package' => :import_packages_serialized,
      'Export-Package' => :export_packages_serialized
    }
    
    def initialize(project)
      @project = project
      @other = { }
      @wrap = false # eventually, change this to default true
    end
    
    def wrap!
      @wrap = true
    end
    
    def wrap?
      @wrap
    end
    
    # These properties are deliberately not memoized
    def version
      @version || project.version
    end
    
    def classpath
      @classpath || project.compile.dependencies.collect(&:to_s).join(", ")
    end
    
    def symbolic_name
      @symbolic_name || [project.group, project.id].join('.')
    end
    
    def import_packages
      @import_packages || ['*']
    end
    
    def export_packages
      @export_packages || ['*']
    end
    
    def write(f)
      f.print self.to_hash.collect { |k, v| "#{k}=#{v}" }.join("\n")
    end
    
    def to_hash
      Hash[ *BND_TO_ATTR.collect { |k, v| [ k, self[k] ] }.flatten ].merge(@other)
    end
    
    def [](k)
      if BND_TO_ATTR.keys.include?(k)
        case BND_TO_ATTR[k]
        when Symbol
          self.send BND_TO_ATTR[k]
        when Proc
          BND_TO_ATTR[k].call
        else
          raise "Unexpected value in BND_TO_ATTR for #{k}"
        end
      else
        @other[k]
      end
    end
    
    def []=(k, v)
      if BND_TO_ATTR.keys.include?(k)
        self.send :"#{BND_TO_ATTR[k]}=", v
      else
        @other[k] = v
      end
    end
    
    protected
    
    def project
      @project
    end
    
    %w(import_packages export_packages).each do |kind|
      class_eval <<-RUBY
        def #{kind}_serialized
          #{kind}.join(', ')
        end
        
        def #{kind}_serialized=(s)
          @#{kind} = s.split(/\\s*,\\s*/)
        end
      RUBY
    end
  end
end

class Buildr::Project
  include Bnd
end