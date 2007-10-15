require File.dirname(__FILE__) + '/test_helper'
require "test/unit"

##
# TODO: This test expects a bunch of templates to be present, but does not
# insert them using testdata

class EditStudyTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_edit_study
    login
    request "/pages/cal/studyList"
    access_template(1)
    # request "/pages/cal/studyList"
    # access_template(3)
    # request "/pages/cal/studyList"
    # access_template(7)
  end
  def access_template(number)
    assert_element_exists("xpath=//ul[@class= 'menu'][1]//li[#{number}]/a")
    click_xpath("xpath=//ul[@class= 'menu'][1]//li[#{number}]/a")
    wait_for_page_to_load "30000"
    #assert_page_contains("Patient Study Calendar")
    assert_page_contains("[Unnamed study]")
    assert_element_exists("study-name", false)
    assert_page_contains("Release this template for use")
    assert_element_exists("epochs")
    assert_element_exists("selected-arm")
    assert_element_exists("study-name")
  end
end