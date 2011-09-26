#!/usr/bin/env ruby

require 'rubygems'
require 'highline'

######
# Repeatedly invokes port-one.rb for a series of commits.
# Example:
#
#   ./port-many.rb 6700-6703 6811
#
# This is equivalent to:
#
#   ./port-one.rb 6700
#   ./port-one.rb 6701
#   ./port-one.rb 6702
#   ./port-one.rb 6703
#   ./port-one.rb 6811

HL=HighLine.new
PORT_ONE = File.expand_path('../port-one.rb', __FILE__)

commits = ARGV.collect { |entered|
  if entered =~ /-/
    start, stop = entered.split('-', 2).collect(&:to_i)
    (start..stop).to_a
  else
    entered.to_i
  end
}.flatten

unless HL.agree("About to port #{commits.join(", ")}, one at a time. Continue?")
  exit(0)
end

until commits.empty?
  HL.say("#{commits.size} left")
  commit = commits.shift
  unless system("#{PORT_ONE} #{commit}")
    puts "#{commit} failed; #{commits.join(', ')} not attempted"
    exit(1)
  end
end
