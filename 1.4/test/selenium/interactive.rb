######
# This is for executing selenium tests interactively

require File.dirname(__FILE__) + '/test_helper'

include StudyCalendar::SeleniumCommands

puts "Enter `setup` to open a browser window and `teardown` to close it."