require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class ReleaseAmendedTemplateForUseTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

    def test_release_template_for_use
      init
      @name = get_study_name
      release_template_for_use
      click_add_amendment
      click_template_in_design
    end

    def cancel_releasing_template_for_use
      release_this_template_for_use
      cancel_release_template_for_use
    end

    def release_template_for_use
      assert_page_contains('Release this template for use')
      click_link_with_text('Release this template for use')
      wait_for_page_to_load "30000"
      assert_page_contains('Release ')
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      assert_page_contains("Available templates")
    end

    def cancel_release_template_for_use
      assert_element_exists("xpath=//div[@id='main']//div[@class='content']//p[last()]/a")
      assert_page_contains('return to the template')
      click_xpath("xpath=//div[@id='main']//div[@class='content']//p[last()]/a")
      wait_for_page_to_load "30000"
      assert_page_contains(@name)
      assert_page_contains("Epochs and arms")
      assert_page_contains('Release this template for use')
    end

    def click_add_amendment
      assert_element_exists("xpath=//ul[@class= 'menu']//li[last()]//ul[@class = 'controls']//li[last()]/a[child::text() = 'Add amendment']")
      click_xpath("xpath=//ul[@class= 'menu']//li[last()]//ul[@class = 'controls']//li[last()]/a[child::text() = 'Add amendment']")
      wait_for_page_to_load "30000"
      assert_page_contains("Amendment Name")
      assert_page_contains("Amendment Date")
      type("xpath=//input[@id='date']", "03/2008")
      type("xpath=//input[@id='name']", "abc")
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      assert_page_contains("Available templates")
      assert_page_contains("[Unnamed study] (abc)")
    end


    def click_template_in_design
       click_xpath("xpath=//ul[@class= 'menu']//li[last()]/a")
       wait_for_page_to_load "30000"
       release_template_for_use
       wait_for_condition_element_false("xpath=//ul[@class= 'menu']//li[last()]/a[child::text() = '[Unnamed study] (abc)'] ")
    end
end