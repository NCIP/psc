require File.dirname(__FILE__) + '/../test_helper'

class StudyListTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

  def test_list_contains_existing_studies
    testdata 'empty', 'some-activities', 'one-study'

    open '/pages/studyList'
    wait_for_page_to_load
    assert_page_contains "Confidential study A"
  end
end
