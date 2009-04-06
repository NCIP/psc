#!/usr/bin/env ruby

# This script uses PSC's OSGi API resources to turn on and off the telnet
# console for the dev-deployed PSC instance.
#
# Usage:
#   $ osgi-telnet.rb            # Get the current state
#   {"state":"INSTALLED"}
#   $ osgi-telnet.rb STARTING   # Start the bundle
#   {"state":"ACTIVE"}
#   $ osgi-telnet.rb STOPPING   # Stop the bundle
#   {"state":"RESOLVED"}
#
# Fairly rough around the edges -- user/pass/hostname are all hard coded.

require 'rubygems'
gem 'rest-open-uri'
require 'rest-open-uri'
require 'json'

SYSADMIN_USER = "superuser"
SYSADMIN_PASS = "superuser"
PSC_HOST = "http://localhost:7200/psc"

def http(method, resource, entity=nil)
  options = {
    :method => method,
    :body => entity,
    :http_basic_authentication => [SYSADMIN_USER, SYSADMIN_PASS],
    'Content-Type' => 'application/json'
  }
  OpenURI.open_uri "#{PSC_HOST}/api/v1/#{resource}", options do |f|
    f.read
  end
end

new_state = ARGV.first

bundles = JSON.parse(http(:get, 'osgi/bundles'))
telnet_bundle = bundles.detect { |b| b['symbolic-name'] =~ /consoletelnet/ }
raise "Telnet bundle not present: #{bundles.collect { |b| b['symbolic-name'] }.inspect}" unless telnet_bundle
method, entity = 
  if new_state
    [:put, "{ state: '#{new_state}' }"]
  else
    [:get]
  end
puts http(method, "osgi/bundles/#{telnet_bundle['id']}/state", entity)
