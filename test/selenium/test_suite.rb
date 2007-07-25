require File.dirname(__FILE__) + '/test_helper'
require "test/unit"

#tests to run:
require "test_create_study"
require "test_epoch"
require "test_arm"
require "test_period"
require "test_manage_sites"
require "test_assign_sites"
require "test_mark_template_as_complete"
require "test_edit_study"


class SuiteTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_true
    assert true
  end
end