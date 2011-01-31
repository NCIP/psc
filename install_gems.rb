#!/usr/bin/env ruby

####
# Installs the gems that PSC's build depends on, as expressed in build.yaml.

require 'yaml'

require 'rubygems'
require 'rubygems/user_interaction'
require 'rubygems/dependency_installer'

BUILD_YAML = File.expand_path("../build.yaml", __FILE__)

YAML.load(File.read(BUILD_YAML))["gems"].
  collect { |gem_entry| gem_entry.split(/\s+/, 2) }.
  each do |gem, version|
    if Gem.available?(gem, version)
      $stderr.puts "Already installed #{[gem, version].compact.join(' ')}"
    else
      inst = Gem::DependencyInstaller.new
      inst.install gem, version

      inst.installed_gems.each do |spec|
        $stderr.puts "Successfully installed #{spec.full_name}"
      end
    end
  end
