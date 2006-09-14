require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyTest < Test::Unit::TestCase
	include StudyCalendar::SeleniumCommands
  
  
	def test_create_single_arm_study
		testdata 'empty'

		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Single arm study"
		type "epoch-name-0", "Treatment"
		click_button_with_text "Create"
		wait_for_page_to_load

		assert_page_contains "Template for Single arm study"
		assert_page_contains "Treatment"
		assert_page_contains "Add a period to epoch Treatment"
	end
  
  
  
	def test_create_two_arm_study

		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two arm study"
		type "epoch-name-0", "Treatment"	
		click("arms[0]", "true", :id  => "multiple-arms-0-yes")
		document_comment "Input fields for the names of arms 1 and 2 of epoch 1 should appear, as well as buttons for adding and removing arms"
		type "arm-name-0-0", "A"
		type "arm-name-0-1", "B"
    
		click_button_with_text "Create"
		wait_for_page_to_load
		assert_page_contains "Template for Two arm study"
		assert_page_contains "Add a period to arm A"
		assert_page_contains "Add a period to arm B"
	end
	
	
	
	def test_create_three_arm_study

		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Three arm study"
		type "epoch-name-0", "Treatment"	
		click("arms[0]", "true", :id  => "multiple-arms-0-yes")
		document_comment "Input fields for the names of arms 1 and 2 of epoch 1 should appear, as well as buttons for adding and removing arms"
		type "arm-name-0-0", "A"
		type "arm-name-0-1", "B"
		click_button_with_text "Add arm", "button"
		type "arm-name-0-2", "C"
		@browser.wait_for_condition("selenium.browserbot.getCurrentWindow().eval(\"$('arm-name-0-3') == null\")", 10000)
    
		click_button_with_text "Create"
		wait_for_page_to_load
		assert_page_contains "Template for Three arm study"
		assert_page_contains "Add a period to arm A"
		assert_page_contains "Add a period to arm B"
		assert_page_contains "Add a period to arm C"
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
