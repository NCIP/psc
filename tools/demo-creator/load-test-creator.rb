#!/usr/bin/env ruby

##
# This script will populate the targeted PSC instance with many, many
# subjects and a variety of sites and SSCMs. It expects that the PSC
# you're targeting was initialized using the RESTful API test setup
# scripts. (In particular, it relies on the users set up by those
# scripts.) E.g., to __WIPE__ your main PSC database and prepare it to
# run this script, you can do something like this:
#
# $ buildr psc:test-restful-api:setup INTEGRATION_DB=datasource
#
# Then run this script.

$LOAD_PATH << File.expand_path('../lib', __FILE__)

require 'psc/state'
require 'faraday'

state = Psc::State.from_file(File.join(File.dirname(__FILE__), 'load-test', 'load-test.xml'))

connection = Faraday.new('http://localhost:7200/psc/api/v1') do |builder|
  # builder.response :logger
  builder.adapter :net_http
end
connection.basic_auth 'juno', 'juno'

state.apply(connection)
