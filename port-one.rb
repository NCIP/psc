#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

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

  def local_paths
    paths.collect { |p| p.sub(/^#{base_path}\//, '') }
  end

  def base_path
    if paths.first =~ %r{^/trunk}
      '/trunk'
    elsif paths.first =~ %r{^(/branches/releases/[^/]+)}
      $1
    else
      raise "Could not extract a source branch from #{paths.inspect}"
    end
  end

  def message
    @xml.css('msg').first.content.strip
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
    "svn commit -N -m $'#{port_message.gsub(/'/, "\\\\'")}' . '#{local_paths.join("' '")}'"
  end
end

class WorkingCopy
  def initialize
    cmd = "svn info --xml"
    $stderr.puts "Executing `#{cmd}` to get working copy info" if ENV['VERBOSE']
    @xml = Nokogiri::XML(`#{cmd}`)
  end

  def local_changes
    @local_changes ||= begin
      cmd = "svn status --xml"
      $stderr.puts "Executing `#{cmd}` to determine if the WC is clean" if ENV['VERBOSE']
      Nokogiri::XML(`#{cmd}`).css('entry').collect { |entry| entry['path'] }
    end
  end

  # Determines if there are any locally modified files which are also modified in the given revision
  def conflicts(revision)
    self.local_changes & revision.local_paths
  end

  def same_branch?(revision)
    self.local_path == revision.base_path
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
  puts "This script must be run from the root of a PSC trunk or branch checkout; not #{wc.local_path}."
  exit(3)
end

rev = Revision.new(REV_N)
conflicts = wc.conflicts(rev)
unless conflicts.empty?
  puts "The following file#{conflicts.size == 1 ? ' is' : 's are'} modified both locally and in the revision to be merged:\n- #{conflicts.join("\n- ")}\nCommit or revert the local modifications before proceeding."
  exit(3)
end
if wc.same_branch?(rev)
  puts "Revision #{REV_N} and this WC are both from the same branch (#{wc.local_path})."
  exit(3)
end

unless HL.agree("About to run:\n  #{rev.merge_cmd}\n  #{rev.commit_cmd}\nContinue?")
  exit(0)
end

puts rev.merge_cmd
system(rev.merge_cmd)
if $? != 0
  puts "Merge failed."
  exit(1)
end

unless HL.agree("Did the merge succeed?")
  puts "Correct the problems and then commit using"
  puts "  #{rev.commit_cmd}"
  exit(1)
end

puts rev.commit_cmd
system(rev.commit_cmd)
if $? != 0
  puts "Commit failed.  Attempting to revert."
  system("svn revert -R .")
  exit(2)
end

update_cmd = "svn update -N ."
puts update_cmd
system(update_cmd)
