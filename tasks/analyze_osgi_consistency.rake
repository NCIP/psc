#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

begin
  require 'md5'
rescue LoadError
  require 'digest/md5'
end

# Takes a set of OSGi bundle jars and looks for unresolved dependencies.
# The implementation is somewhat naive.
module AnalyzeOsgiConsistency
  def self.analyze_packages(bundle_filenames, boot_packages)
    boot_patterns = boot_packages.collect { |spec| /^#{spec}$/ }
    bundles = bundle_filenames.collect { |fn| Bundle.new(fn) }
    unresolvable = bundles.select do |bundle|
      unresolved = bundle.imports.
        reject { |import| import.optional? }.
        reject { |import| boot_patterns.find { |boot| import.package =~ boot } }.
        select { |import| import.resolve(bundles).empty? }
      unless unresolved.empty?
        puts "Unresolved imports in #{bundle.symbolic_name} (#{bundle.filename}):"
        unresolved.each do |ui|
          puts "- #{ui.package.inspect} #{ui.version_range}"
        end
        puts ""
        true
      else
        false
      end
    end
    if unresolvable
      raise "#{unresolvable.size} unresolveable bundle#{unresolvable.size == 1 ? '' : 's'}"
    else
      puts "No unresolveable bundles"
    end
  end

  class Bundle
    attr_reader :filename

    def initialize(bundle_filename)
      @filename = bundle_filename
    end

    def symbolic_name
      manifest.main['Bundle-SymbolicName']
    end

    def manifest
      @manifest ||= Buildr::Packaging::Java::Manifest.from_zip(filename)
    end

    def imports
      @imports ||= 
        AnalyzeOsgiConsistency.split_ignoring_quotes(manifest.main['Import-Package']).
          collect { |clause| PackageImport.new(clause) }
    end

    def exports
      @exports ||= 
        AnalyzeOsgiConsistency.split_ignoring_quotes(manifest.main['Export-Package']).
          collect { |clause| PackageExport.new(clause) }
    end
  end

  module Clause
    def extract_directives(params)
      (params.select { |p| p =~ /:=/ } || []).
        collect { |s| s.split(':=') }.
        inject({}) { |d, (k, v)| d[k] = v.gsub('"', ''); d }
    end

    def extract_attributes(params)
      (params.select { |p| p =~ /[^:]=/ } || []).
        collect { |s| s.split('=') }.
        inject({}) { |d, (k, v)| d[k] = v.gsub('"', ''); d }
    end
  end

  class PackageImport
    include Clause
    attr_accessor :package, :version_range, :attributes, :directives

    def initialize(clause)
      @package, *params = AnalyzeOsgiConsistency.split_ignoring_quotes(clause, ';')
      @directives = extract_directives(params)
      @attributes = extract_attributes(params)
      @version_range = BundleVersionRange.new(attributes['version'])
    end

    # Selects out the bundles which can satisfy this import
    def resolve(bundles)
      bundles.select { |b|
        by_pkg = b.exports.find { |ex| ex.package == self.package }
        if by_pkg
          version_range.includes?(by_pkg.version)
        end
      }
    end

    def optional?
      directives['resolution'] == 'optional'
    end
  end

  class PackageExport
    include Clause
    attr_accessor :package, :version, :attributes, :directives

    def initialize(clause)
      @package, *params = AnalyzeOsgiConsistency.split_ignoring_quotes(clause, ';')
      @directives = extract_directives(params)
      @attributes = extract_attributes(params)
      @version = BundleVersion.new(attributes['version'])
    end
  end

  class BundleVersionRange
    def initialize(s)
      unless s
        @lo = BundleVersion.new("0"); 
        @lo_inclusive = true
        @hi = nil
        @hi_inclusive = false
      else
        lo, hi = s.split(/\s*,\s*/, 2)
        lo ||= "0"
        @lo = BundleVersion.new(lo.sub(%r{[\[\(]}, ''))
        if hi
          @lo_inclusive = lo[0, 1] == '['
          @hi_inclusive = hi[-1, 1] == ']'
          @hi = BundleVersion.new(hi.sub(%r{[\]\)]}, ''))
        else
          @lo_inclusive = true
          @hi_inclusive = false
          @hi = nil
        end
      end
    end

    def includes?(version)
      result = (@lo < version || (@lo_inclusive && @lo == version)) &&
        (@hi.nil? || (@hi > version || (@hi_inclusive && @hi == version)))
      result
    end

    def to_s
      hi_s = @hi ? "#{@hi}#{@hi_inclusive ? ']' : ')'}" : 'Inf)'
      "#{@lo_inclusive ? '[' : '('}#{@lo}, #{hi_s}"
    end
  end

  class BundleVersion
    include Comparable

    def initialize(s)
      @s = s || "0"
    end

    def <=>(other)
      [:major, :minor, :micro, :qualifier].collect { |part| 
        self.send(part) <=> other.send(part) 
      }.find { |result| result != 0 } || 0
    end

    def major
      @major ||= @s.split('.')[0].to_i
    end

    def minor
      @minor ||= @s.split('.')[1].to_i
    end

    def micro
      @micro ||= @s.split('.')[2].to_i
    end

    def qualifier
      @qualifier ||= @s.split('.')[3] || ""
    end

    def to_s
      "#{major}.#{minor}.#{micro}#{qualifier.empty? ? '' : '.' + qualifier }"
    end
  end

  private

  def self.split_ignoring_quotes(value, sep=',')
    return [] unless value
    masks = {}
    value.gsub(/"[^\"]*"/) { |match|
      repl = Digest::MD5.hexdigest(match); masks[repl] = match; repl
    }.split(/\s*#{sep}\s*/).collect { |e|
      masks.each_pair { |repl, original| e.gsub!(repl, original) }; e
    }
  end
end
