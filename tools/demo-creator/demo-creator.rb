#!/usr/bin/env ruby

#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

$LOAD_PATH << File.expand_path('../lib', __FILE__)

require 'psc/state'
require 'faraday'

demo_state = Psc::State.from_file(File.join(File.dirname(__FILE__), 'demo', 'demo-state.xml'))

connection = Faraday.new('http://localhost:7200/psc/api/v1') do |builder|
  #  builder.response :logger
  builder.adapter :net_http
end
connection.basic_auth 'superuser', 'superuser'

demo_state.apply(connection)
