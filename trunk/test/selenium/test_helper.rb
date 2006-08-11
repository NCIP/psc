require 'test/unit'
require File.dirname(__FILE__) + '/../lib/selenium'

module StudyCalendar
  module SeleniumCommands
    TEN_SECOND_TIMEOUT = 10000
    BROWSER_URL = "http://localhost:8080"
    
    def setup
      @browser = Selenium::SeleneseInterpreter.new("localhost", 12452, "*firefox", BROWSER_URL, TEN_SECOND_TIMEOUT)
      @browser.start
    end

    def teardown
      sleep(2)
      @browser.stop
    end

    protected
    def open(relative_url)
      @browser.open("/studycalendar#{relative_url}")
      documenter.document_step "Start at relative URL \"#{relative_url}\""
    end
    
    def document_comment(comment)
      documenter.document_comment(comment)
    end
    
    def documenter
      unless @documenter
        @documenter = Test::Unit::UI::DocumentGeneration::TestRunner.documenter
        @documenter = StdoutDocumenter.new unless @documenter
      end
      @documenter
    end

    class Field
      def initialize(locator, browser)
        @browser = browser
        @locator = locator
      end

      def type(value)
        @browser.type(@locator, value)
      end
    end

    def into(field)
      Field.new(field, @browser)
    end

    def dump_body_text
      puts @browser.get_body_text
    end

    def wait_for_page_to_load(timeout=TEN_SECOND_TIMEOUT)
      @browser.wait_for_page_to_load(timeout)
    end

    def type(locator, value, options = {})
      @browser.type(locator, value)
      documenter.document_step "Under \"#{locator_to_label(locator, options)}\" type \"#{value}\""
    end
    
    def check(name, value, options = {})
      @browser.check("name=#{name} value=#{value}")
      documenter.document_step "Under \"#{locator_to_label(name, options)}\" select \"#{value.capitalize}\""
    end
    
    def click(name, value, options = {})
      @browser.click("name=#{name} value=#{value}")
      documenter.document_step "Under \"#{locator_to_label(name, options)}\" click \"#{value.capitalize}\""
    end
    
    def click_button_with_text(text, input_type='submit')
      @browser.click("xpath=//input[@type='#{input_type}' and @value='#{text}']")
      documenter.document_step "Click \"#{text}\" button"
    end

    def click_link_with_text(text)
      @browser.click("xpath=//a[child::text()='#{text}']")
    end

    def select_from_combobox(locator, optionLocator)
      @browser.select(locator, optionLocator)
      documenter.document_step "Under \"#{locator_to_label(locator)}\" select option where \"#{optionLocator}\""
    end
    
    #### ASSERTIONS
    
    def assert_page_contains(text)
      assert @browser.is_text_present(text), "\"#{text}\" is not on the page"
      documenter.document_step "Check that \"#{text}\" appears somewhere in the page"
    end
    
    def assert_page_does_not_contain(text)
      assert !@browser.is_text_present(text), "\"#{text}\" is on the page"
      documenter.document_step "Check that \"#{text}\" does not appear somewhere in the page"
    end
    
    private
    def locator_to_label(locator, options = {})
      options[:label] || locator.split("-").join(" ").capitalize
    end
  end
  
  private
  class StdoutDocumenter
    def document_step(str)
      puts "   Step: #{str}"
    end
    
    def document_comment(str)
      puts "Comment: #{str}"
    end
  end
  
end

require File.dirname(__FILE__) + '/document_generation'

at_exit do
  unless $! || Test::Unit.run?
    runner = Test::Unit::AutoRunner.new(false) do |config|
      config.runner = proc { |r| Test::Unit::UI::DocumentGeneration::TestRunner }
    end
    exit runner.run
  end
end
