#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

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
# Somewhat rough around the edges -- hostname is hard coded.

require 'rubygems'
require 'restclient'
require 'json'
require 'yaml'

PSC_HOST = "localhost:#{ENV['JETTY_PORT'] || 7200}"
RestClient.log = 'stdout'

def user_settings
  @settings ||= begin
    specific_file = "#{ENV['HOME']}/.buildr/settings.yaml"
    default_file = "#{File.dirname(__FILE__)}/buildr-user-settings.yaml.default"
    YAML.load(open(File.exist?(specific_file) ? specific_file : default_file))
  end
end

def psc_url(resource)
  "http://#{user_settings['psc']['dev_admin']['username']}:#{user_settings['psc']['dev_admin']['password']}@#{PSC_HOST}/psc/api/v1/#{resource}"
end

new_state = ARGV.first

bundles = JSON.parse(RestClient.get(psc_url('osgi/bundles'), :accept => '*/*'))
telnet_bundles = bundles.select { |b| (b['symbolic_name'] =~ /consoletelnet/) || (b['symbolic_name'] =~ /shell.remote/) }
raise "Telnet bundle not present: #{bundles.collect { |b| b['symbolic_name'] }.inspect}" if telnet_bundles.empty?
telnet_bundles.each do |b|
  url = psc_url("osgi/bundles/#{b['id']}/state")
  result = 
    if new_state
      RestClient.put(url, "{ state: '#{new_state}' }", :content_type => 'application/json')
    else
      RestClient.get(url, :accept => '*/*')
    end
  puts "#{b['symbolic_name']}: #{result}"
end
