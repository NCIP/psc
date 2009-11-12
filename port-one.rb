#!/usr/bin/env ruby

require 'rubygems'
require 'nokogiri'
require 'highline'

# Ports a single commit from one branch to the branch of the current subversion 
# WC. (Must be either a release branch or trunk.)
# The source branch is automatically determined from the specified revision
# number.

REV_N=ARGV.first or raise "Please specify a revision number"
HL = HighLine.new

class Revision
  def initialize(number)
    cmd = "svn log --xml -v -r #{number} ^/"
    $stderr.puts "Executing `#{cmd}` to get revision metadata" if ENV['VERBOSE']
    @xml = Nokogiri::XML(`#{cmd}`)
  end

  def number
    @xml.css('logentry').first['revision']
  end

  def paths
    @paths ||= @xml.css('path').collect { |p| p.content }
  end

  def base_path
    if paths.first =~ %r{^/trunk}
      '^/trunk'
    elsif paths.first =~ %r{^(/branches/releases/[^/]+)}
      $1
    else
      raise "Could not extract a source branch from #{paths.inspect}"
    end
  end

  def message
    @xml.css('msg').first.content
  end

  def port_message
    if base_path =~ /trunk/
      "Backport r#{number}: #{message}"
    else
      "Port r#{number}: #{message}"
    end
  end

  def merge_cmd
    "svn merge -c #{number} ^#{base_path}"
  end

  def commit_cmd
    "svn commit -m \"#{port_message.gsub(/"/, '\\"')}\""
  end
end

class WorkingCopy
  def initialize
    cmd = "svn info --xml"
    $stderr.puts "Executing `#{cmd}` to get working copy info" if ENV['VERBOSE']
    @xml = Nokogiri::XML(`#{cmd}`)
  end

  def url
    @xml.css('url').first.content
  end

  def root
    @xml.css('root').first.content
  end

  def local_path
    url.gsub(%r{^#{root}}, '')
  end

  def at_root?
    local_path =~ /trunk$|-stable$/
  end
end

wc = WorkingCopy.new
unless wc.at_root?
  raise "This script must be run from the root of a PSC trunk or branch checkout; not #{wc.local_path}."
end

rev = Revision.new(REV_N)
unless HL.agree("About to run:\n  #{rev.merge_cmd}\n  #{rev.commit_cmd}\nContinue?")
  exit(0)
end

puts rev.merge_cmd
system(rev.merge_cmd)
if $? != 0
  puts "Merge failed."
  exit(1)
end

puts rev.commit_cmd
system(rev.commit_cmd)
if $? != 0
  puts "Commit failed.  Attempting to revert."
  system("svn revert -R .")
  exit(2)
end
