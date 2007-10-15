THIS_DIR = File.dirname(__FILE__)
require THIS_DIR + '/test_helper'
require "test/unit"

# Temporarily disabled tests for changing functionality
DISABLED = %w{ test_assign_sites test_manage_sites test_holidays test_arm }

EXCLUDE = %w{ test_suite test_helper } + DISABLED
# `require` each test case not listed in EXCLUDE
Dir["#{THIS_DIR}/test_*.rb"].each do |tc|
  reqname = tc[0, tc.size-3]
  casename = reqname[THIS_DIR.size + 1, reqname.size - 1]
  require reqname unless EXCLUDE.include? casename
end
