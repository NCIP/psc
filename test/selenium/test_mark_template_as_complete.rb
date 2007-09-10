require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class MarkTemplateAsCompleteTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_confirm_marking
    init
    @name = get_study_name
    mark_as_complete
    confirm_completion
  end
  def test_cancel_marking
    init
    @name = get_study_name
    mark_as_complete
    cancel_completion
  end
  def mark_as_complete
    assert_page_contains('Mark this template complete')
    click_link_with_text('Mark this template complete')
    wait_for_page_to_load "30000"
    assert_page_contains('Mark template for ')
    assert_page_contains('You are about to mark the ')
  end
  def confirm_completion
    click_xpath("xpath=//input[@id ='complete-yes']")
    click_button_with_text('Submit', 'submit')
    wait_for_page_to_load "30000"
    assert_element_exists("xpath=//div[@id='main']//ul[@class='menu'][1]//li[last()]/a[child::text() ='#{@name}']")
    #assert that template is created by using the 'last' positioning feature found in xpath under Templates in design
  end
  def cancel_completion
    click_xpath("xpath=//input[@id ='complete-no']")
    click_button_with_text('Submit', 'submit')
    wait_for_page_to_load "30000"
    assert_element_exists("xpath=//div[@id='main']//ul[@class='menu'][1]//li[last()]/a[child::text() ='#{@name}']")
    #assert that template is created by using the 'last' positioning feature found in xpath under Templates in design
  end
  
end