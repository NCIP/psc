require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_create_no_arm_study
    testdata 'empty'

    open "/pages/newStudy"
    type "study-name", "Confidential study"
    type "epoch-name-0", "Treatment"
    click_button_with_text "Create"
    wait_for_page_to_load

    assert_page_contains "Template for Confidential study"
    assert_page_contains "Treatment"
    assert_page_contains "Add a period to epoch Treatment"
  end
  
  def test_create_multiple_arm_study
    testdata 'empty'

    open "/pages/newStudy"
    wait_for_page_to_load
    type "study-name", "Confidential study"
    type "epoch-name-0", "Treatment"
    click("arms[0]", "true", :label => "Multiple arms for epoch 1?")
    document_comment "Input fields for the names of arms 1 and 2 of epoch 1 should appear, as well as buttons for adding and removing arms"
    type "arm-name-0-0", "A"
    type "arm-name-0-1", "B"
    click_button_with_text "Add arm", "button"
    type "arm-name-0-2", "C"
    click_button_with_text "Add arm", "button"
    type "arm-name-0-3", "D"
    click_button_with_text "Remove last arm", "button"
    @browser.wait_for_condition("selenium.browserbot.getCurrentWindow().eval(\"$('arm-name-0-3') == null\")", 10000)
    
    click_button_with_text "Create"
    wait_for_page_to_load
    assert_page_contains "Template for Confidential study"
    assert_page_contains "Add a period to arm A"
    assert_page_contains "Add a period to arm B"
    assert_page_contains "Add a period to arm C"
    assert_page_does_not_contain "Add a period to arm D"
  end
end

__END__
Should create a document like:

Create Study
============

Create Single Arm Study
-----------------------

 1. Start at /pages/newStudy
 2. Under "Study name" type "Vioxx Study"

Create Multiple Arm Study
-------------------------

  1. Start at /pages/newStudy
  2. Under "Study name" type "Vioxx Study"
  3. Under "Multiple arms?" select "Yes"
     Input fields for the names of arms 1 and 2 should appear, as well as buttons for adding and removing arms
