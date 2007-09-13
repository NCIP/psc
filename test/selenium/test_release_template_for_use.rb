require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class AddAmendment < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

    def test_release_template_for_use
      init
      @name = get_study_name
      cancel_releasing_template_for_use
      confirm_releasing_template_for_use
    end

    def cancel_releasing_template_for_use
      release_this_template_for_use
      cancel_release_template_for_use
    end

    def confirm_releasing_template_for_use
      release_this_template_for_use
      confirm_release_template_for_use
    end

    def release_this_template_for_use
      assert_page_contains('Release this template for use')
      click_link_with_text('Release this template for use')
      wait_for_page_to_load "30000"
      assert_page_contains('Release ')
    end

    def confirm_release_template_for_use
      click_button_by_input_type('submit')
      wait_for_page_to_load "30000"
      assert_page_contains("Create a new template")
      #assert_page_contains("Templates in design")
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

end