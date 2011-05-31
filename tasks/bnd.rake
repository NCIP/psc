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

  def self.libraries
    ["biz.aQute:bnd:jar:0.0.313"]
  end

  first_time do
    Java.classpath << Bnd.libraries
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
      # This replicates the logic in bnd 0.0.313's WrapTask
      bndfile = jar.name.sub /(\.jar)?$/, '.bnd'
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
      bndjar = jar.name.sub(/jar$/, 'bndjar')

      jar.enhance [bndfile] do |task|
        # No, I want to be last -- see Buildr::ArchiveTask#initialize
        task.enhance do
          project.ant('bnd') do |ant|
            ant.taskdef :resource => 'aQute/bnd/ant/taskdef.properties'
            trace "Wrapping #{jar.name} into #{bndjar}"
            definitions_dir = File.dirname(bndfile)
            trace "Telling bnd to look in #{definitions_dir} for directions."
            ant.bndwrap :jars => jar.name,
              :output => bndjar,
              :definitions => definitions_dir
          end
          raise "bnd failed" unless File.exist?(bndjar)

          info "Replacing #{File.basename(jar.name)} with bnd wrapped version"
          mv bndjar, jar.name
        end
      end
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
      'Bundle-Name' => :name,
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
    attr_writer :autostart

    def autostart?
      @autostart.nil? ? true : @autostart
    end

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
          # XXX: this does not account for quotes
          @#{attribute} = s.split(/\\s*,\\s*/)
        end
      RUBY
    end

    def write(f)
      f.print self.to_hash.collect { |k, v| "#{k}=#{v}" }.join("\n")
    end

    def to_hash
      Hash[
        *BND_TO_ATTR.keys.
        collect { |k| [ k, self[k] ] }.
        reject { |k, v| v.nil? || v.empty? }.
        flatten
      ].merge(other)
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

    def merge!(other)
      other.each do |k, v|
        self[k] = v
      end
      self
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
      ["*;version=#{version}"]
    end

    protected

    def project
      @project
    end
  end
end unless Object.const_defined?(:Bnd)

class Buildr::Project
  include Bnd
end
