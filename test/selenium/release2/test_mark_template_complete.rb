require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyAddedTest < Test::Unit::TestCase
	include StudyCalendar::SeleniumCommands
  
	def test_mark_template_complete_no
		testdata 'empty', 'some-activities', 'one-study'
		
		open "/pages/calendarTemplate?id=-1"
		wait_for_page_to_load
		
		click_link_with_text("Mark this template complete")
		assert_page_contains("Mark template for Confidential study A")
		click("completed", "false", :id  => "complete-no")
		click_button_with_text("Submit")
		wait_for_page_to_load
		
		open "/pages/calendarTemplate?id=-1"
		wait_for_page_to_load
		
		assert_page_contains("Template for Confidential study A")
		assert_page_contains("Mark this template complete")
		
		click("selectedCalendarView", "list")
		assert_page_contains("Template for Confidential study A")
		assert_page_contains("Mark this template complete")
		
		
		click("selectedCalendarView", "grid")
		assert_page_contains("Template for Confidential study A")
		assert_page_contains("Mark this template complete")
	end
	
	
	def test_mark_template_complete_yes
		testdata 'empty', 'some-activities', 'one-study'
	
		open "/pages/calendarTemplate?id=-1"
		wait_for_page_to_load
		
		click_link_with_text("Mark this template complete")
		assert_page_contains("Mark template for Confidential study A")
		click("completed", "true", :id  => "complete-yes")
		click_button_with_text("Submit")
		wait_for_page_to_load
		
		open "/pages/calendarTemplate?id=-1"
		wait_for_page_to_load
		
		assert_page_contains("Template for Confidential study A")
		assert_page_does_not_contain("Mark this template complete")
		
		click("selectedCalendarView", "list")
		assert_page_contains("Template for Confidential study A")
		assert_page_does_not_contain("Mark this template complete")
		
		
		click("selectedCalendarView", "grid")
		assert_page_contains("Template for Confidential study A")
		assert_page_does_not_contain("Mark this template complete")
	end
	
end