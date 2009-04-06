# Provides tasks to use bnd with buildr.  Of particular interest is 
# bnd:wrap, which implements automatic bundling of buildr-packaged jars
# if enabled for a project.

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
    @bnd ||= ProjectBndProperties.new(self)
  end
  
  module BndProperties
    BND_TO_ATTR = {
      '-classpath' => :classpath,
      'Bundle-Version' => :version,
      'Bundle-SymbolicName' => :symbolic_name,
      'Bundle-Description' => :description,
      'Import-Package' => :import_packages_serialized,
      'Export-Package' => :export_packages_serialized
    }
    LIST_ATTR = BND_TO_ATTR.values.select { |a| a.to_s =~ /_serialized$/ }
    SCALAR_ATTR = BND_TO_ATTR.values - LIST_ATTR
    
    # Scalar properties are deliberately not memoized to allow
    # the default values to be evaluated as late as possible.
    
    SCALAR_ATTR.each do |attribute|
      class_eval <<-RUBY
        def #{attribute}
          @#{attribute} || (default_#{attribute} if respond_to? :default_#{attribute})
        end
      RUBY
    end
    
    attr_writer(*SCALAR_ATTR)
    
    # List properties are memoized to allow for concatenation via the 
    # read accessor.
    
    LIST_ATTR.each do |attribute_ser|
      attribute = attribute_ser.to_s.sub(/_serialized$/, '')
      class_eval <<-RUBY
        def #{attribute}
          @#{attribute} ||= (self.respond_to?(:default_#{attribute}) ? default_#{attribute} : [])
        end
        
        def #{attribute_ser}
          #{attribute}.join(', ')
        end
        
        def #{attribute_ser}=(s)
          @#{attribute} = s.split(/\\s*,\\s*/)
        end
      RUBY
    end
    
    def write(f)
      f.print self.to_hash.collect { |k, v| "#{k}=#{v}" }.join("\n")
    end
    
    def to_hash
      Hash[ *BND_TO_ATTR.collect { |k, v| [ k, self[k] ] }.flatten ].merge(other)
    end
    
    def [](k)
      if BND_TO_ATTR.keys.include?(k)
        self.send BND_TO_ATTR[k]
      else
        other[k]
      end
    end
    
    def []=(k, v)
      if BND_TO_ATTR.keys.include?(k)
        self.send :"#{BND_TO_ATTR[k]}=", v
      else
        other[k] = v
      end
    end
    
    protected
    
    def other
      @other ||= { }
    end
  end
  
  class ProjectBndProperties
    include BndProperties
    
    def initialize(project)
      @project = project
      @wrap = false # eventually, change this to default true
    end
    
    def wrap!
      @wrap = true
    end
    
    def wrap?
      @wrap
    end
    
    def default_version
      project.version
    end
    
    def default_classpath
      project.compile.dependencies.collect(&:to_s).join(", ")
    end
    
    def default_symbolic_name
      [project.group, project.id].join('.')
    end
    
    def default_description
      project.full_comment
    end
    
    def default_import_packages
      ['*']
    end
    
    def default_export_packages
      ['*']
    end
    
    protected
    
    def project
      @project
    end
  end
end

class Buildr::Project
  include Bnd
end