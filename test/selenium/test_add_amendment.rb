require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class ReleaseTemplateForUserTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

    def test_add_amendment
      init
      @name = get_study_name
      release_template_for_use
      click_add_amendment
      set_wrong_amendment("")
      set_wrong_amendment("022005")
      set_wrong_amendment("02/a005")
      set_wrong_amendment("02/200")
      set_correct_amendment("02/2005")
      click_add_amendment
      set_correct_amendment_with_name("abc", "03/2008")
    end

    def release_template_for_use
      call_release_this_template_for_use
      confirm_release_template_for_use
    end

    def call_release_this_template_for_use
      assert_page_contains('Release this template for use')
      click_link_with_text('Release this template for use')
      wait_for_page_to_load "30000"
      assert_page_contains('Release ')
    end

    def confirm_release_template_for_use
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      assert_page_contains("Available templates")
    end

    def click_add_amendment
      assert_element_exists("xpath=//ul[@class= 'menu']//li[last()]//ul[@class = 'controls']//li[last()]/a[child::text() = 'Add amendment']")
      click_xpath("xpath=//ul[@class= 'menu']//li[last()]//ul[@class = 'controls']//li[last()]/a[child::text() = 'Add amendment']")
      wait_for_page_to_load "30000"
      assert_page_contains("Amendment Name")
      assert_page_contains("Amendment Date")
    end

    def set_wrong_amendment(number)
      assert_element_exists("xpath=//input[@id='date']")
      type("xpath=//input[@id='date']", "#{number}")
      click_button_by_input_type('submit')

      wait_for_condition_text('Error ')
      assert_page_does_not_contain('Exception')
      assert_page_does_not_contain('null')
    end

    def set_correct_amendment(number)
      type("xpath=//input[@id='date']", "#{number}")
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      assert_page_contains("Available templates")
      assert_page_contains("[Unnamed study] ()")
    end

    def  set_correct_amendment_with_name(amendmentName, amendmentDate)
      type("xpath=//input[@id='date']", "#{amendmentDate}")
      type("xpath=//input[@id='name']", "#{amendmentName}")
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      assert_page_contains("Available templates")
      assert_page_contains("[Unnamed study] (abc)")
    end
end