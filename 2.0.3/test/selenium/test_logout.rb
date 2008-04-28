require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class LogoutTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

  def test_logout
    init
    click_link_with_text('Log out')
    wait_for_page_to_load "30000"
    assert_page_contains('Please log in')
  end

  def test_logout_right_away
    open "/public/login"
    wait_for_page_to_load "30000"
    click_link_with_text('Log out')
    wait_for_page_to_load "30000"
    assert_page_contains('Please log in')
  end
end