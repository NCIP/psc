require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class LogoutTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_logout
    init
    assert_page_contains('Logout')
    click_link_with_text('Logout')
    wait_for_page_to_load "30000"
    assert_page_contains('Please log in')
  end
end