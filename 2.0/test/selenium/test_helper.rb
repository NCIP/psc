require 'test/unit'
require File.dirname(__FILE__) + '/../lib/selenium'

module StudyCalendar
  module SeleniumCommands
    TEN_SECOND_TIMEOUT = 10000
    BROWSER_URL = "http://localhost:8080"
    
    def setup
      @browser = Selenium::SeleneseInterpreter.new("localhost", 12452, "*firefox", BROWSER_URL, TEN_SECOND_TIMEOUT)
      @browser.start
      
      #Instance Fields
      @study_name = "[Unnamed study]"
      @epoch_count = 3
      @epoch_number = 1
      @arm_number = 1
    end

    def teardown
      sleep(2)
      @browser.stop
    end

    protected
    def open(relative_url)
      request relative_url
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

    def testdata(*names)
      request "/test/load/#{names.join ','}"
      wait_for_page_to_load(TEN_SECOND_TIMEOUT * 8)
      assert !@browser.is_text_present("ERROR"), "Error loading test data"
      documenter.document_step "Load test data sets: #{names.join ', '}"
    end
    

    def dump_body_text
      puts @browser.get_body_text
    end
    def check(name, value, options = {})
      @browser.check("name=#{name} value=#{value}")
      documenter.document_step "Under \"#{locator_to_label(name, options)}\" select \"#{value.capitalize}\""
    end
    
    def click(name, value, options = {})
      @browser.click("name=#{name} value=#{value}")
      documenter.document_step "Under \"#{locator_to_label(name, options)}\" click \"#{value.capitalize}\""
    end
    def click_xpath(locator)
      @browser.click(locator)
      documenter.document_step "Click element with #{locator}"
    end
   # "xpath=//input[@type='submit' and @value='OK']"
    # sh work/psc/psc/test/bin/ selenium-interactive.sh
    def click_button_with_text(text, input_type='submit')
      @browser.click("xpath=//input[@type='#{input_type}' and @value='#{text}']")
      documenter.document_step "Click \"#{text}\" button"
    end
    def click_button_by_input_type(input_type='submit')
      @browser.click("xpath=//input[@type='#{input_type}']")
      documenter.document_step "Click \"#{input_type}\" button"
    end

#  @browser.click("xpath=//div[position()=12]//a[child::text()='Set name']")
    def click_link_with_text(text)
      @browser.click("xpath=//a[child::text()='#{text}']")
      documenter.document_step "Click link with text \"#{text}\" "
    end
    #To click on a nth link when the same link text repeats on the page --first link is 1, and not 0.
    def click_nth_link_with_text(text, number)
      @browser.click("xpath=//a[child::text()='#{text}'][#{number}]")
      num = number.to_s
      documenter.document_step "Click link No.#{num} with text \"#{text}\" "
    end
    def get_confirmation()
      return @browser.get_confirmation
    end
    def is_visible(locator)
      return @browser.is_visible(locator)
    end  
    # @browser.click("xpath=//div[@param="epoch" and child::text()='Set Name' and position()=2]")
# documenter.document_step "Click " + number.to_s th link with text \"#{text}\" "
    def select_from_combobox(locator, optionLocator)
      @browser.select(locator, optionLocator)
      documenter.document_step "Under \"#{locator_to_label(locator)}\" select option where \"#{optionLocator}\""
    end
    #-▼-changes ►◄▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲
     def get_text(locator)
       return @browser.get_text(locator)
     end
     def click_element(locator)
        @browser.click("id=#{locator}")
        documenter.document_step "Click \"#{locator}\" element"
     end
     def click_submit()
       click_button_with_text('Submit')
       wait_for_page_to_load('30000')
     end
     def type(locator, value, options = {})
       @browser.type(locator, value)
       documenter.document_step "Under \"#{locator_to_label(locator, options)}\" type \"#{value}\""
     end
     def type_in_lone_text_field(value)
       @browser.type("xpath=//input[@type='text']", "#{value}")
       documenter.document_step "Type \"#{value}\" in text field"
     end
     #waits until text appears on page
     def wait_for_condition_text(text)
       @browser.wait_for_condition("var value = selenium.isTextPresent(\"#{text}\"); value==true;", 15000)
     end
     #waits for element to exist
     def wait_for_condition_element(locator)
       @browser.wait_for_condition("var value = selenium.isElementPresent(\"#{locator}\"); value==true;", 15000)
     end
     #waits for element to go away
     def wait_for_condition_element_false(locator)
       @browser.wait_for_condition("var value = selenium.isElementPresent(\"#{locator}\"); value==false;", 15000)
     end
     #waits until a confirmation window pops up
     def wait_for_condition_confirmation
       @browser.wait_for_condition("var value = selenium.isConfirmationPresent();  value==true;", 15000)
     end
     def wait_for_page_to_load(timeout=TEN_SECOND_TIMEOUT)
       @browser.wait_for_page_to_load(timeout)
     end
     def replace_value_in_text_field(old_val, new_val, options = {})
       @browser.type("xpath=//input[@type='text' and @value=#{old_val}]", "#{new_val}")
       documenter.document_step "Delete \"#{old_val}\" in text field and type \"#{new_val}\""
     end

       
#@browser.wait_for_condition(<script LANGUAGE = "JavaScript"> function loop()@browser.is_text_present('TEST');} loop(); </script>, 15000)
    #### ASSERTIONS
    
    def assert_page_contains(text)
      assert @browser.is_text_present(text), "\"#{text}\" is not on the page"
      documenter.document_step "Check that \"#{text}\" appears somewhere in the page"
    end
    def assert_element_contains(text)
      
    end
    def assert_element_is_visible(locator)
      assert @browser.is_visible(locator)
      documenter.document_step "Check that element \"#{locator}\" is on the page"
    end
    def assert_element_is_hidden(locator)
      assert !@browser.is_visible(locator)
      documenter.document_step "Check that element \"#{locator}\" is on the page"
    end
    def assert_page_does_not_contain(text)
      assert !@browser.is_text_present(text), "\"#{text}\" is on the page"
      documenter.document_step "Check that \"#{text}\" does not appear somewhere in the page"
    end

    def assert_element_exists(locator, suppressDocumentation = false)
      assert @browser.is_element_present(locator), "Element \"#{locator}\" is not on the page"
      if !suppressDocumentation
      	documenter.document_step "Check that \"#{locator_to_label(locator)}\" is an element on the page"
      end
    end
    
    def assert_element_does_not_exist(locator)
      assert !@browser.is_element_present(locator), "Element \"#{locator}\" is not on the page"
      documenter.document_step "Check that \"#{locator_to_label(locator)}\" is not an element on the page"
	  end
	
	def assert_element_is_equal_to(locator, text)
	  
	end
#	      @browser.click("xpath=//a[child::text()='Set name' and position()=2]")
	def tester
    #insert commands here to call for quick testing 
  end
  def init
 #   testdata('empty')
    login
    create_new_study
  end
  def login
    open "/public/login"
    wait_for_page_to_load "30000"
    
  # -commented section only to be used if referenced to the home site#
  # click_link_with_text("Public Test Site - start page")
  # wait_for_page_to_load "30000"
  
    type("j_username", "superuser")
    type("j_password", "superuser")
    click_button_with_text("Log in", 'submit')
    wait_for_page_to_load "30000"  
  end
  def create_new_study
    request "/pages/cal/studyList"
  	#click_link_with_text('Public Test Site - start page')
   	wait_for_page_to_load "30000"
    click_link_with_text("Create a new template")
    wait_for_page_to_load "30000"
  end
  def name_study(name)
    click_link_with_text("Set name")
    type_in_lone_text_field(name)
    click_button_with_text("OK", 'submit')
    @name_study=name
  end
  def add_epoch
    click_link_with_text("Add epoch")
    @epoch_count= @epoch_count + 1
  end

   # ep is a temporary method for quick development
   def ep(xpath)
     @browser.is_element_present("#{xpath}")
   end
   def go_back_to_template
     click_link_with_text(@study_name)
     wait_for_page_to_load "30000"
   end
   def create_activity(name, description, type)
     click_link_with_text('Create new activity')
     wait_for_page_to_load "30000"
     type("xpath=//input[@id='activityName' and @type='text']", "#{name}")
     type("xpath=//input[@id='activityDescription' and @type='text']", "#{description}")
     select_from_combobox("xpath=//select[@id='activityType']", "#{type}")
     click_button_with_text('Create')
     wait_for_page_to_load "30000"
   end
   #rows and columns start with 0 as the first row/column
   def set_activity_frequency(row, column, number)
     type("xpath=//tbody[@id = 'input-body']//tr[@class='input-row']//td//input[@id = 'grid[#{row}].counts[#{column}]']", "#{number}")
   end
   #rows and columns start with 0 as the first row/column
   def set_activity_details(row, details)
     type("xpath=//tbody[@id = 'input-body']//tr[@class='input-row']//td//input[@id = 'grid[#{row}].details']", '#{details}')
   end
   def save_changes
     click_button_with_text('Save Changes')
     wait_for_page_to_load "30000"
   end
   def edit_period_activities(number)
     num =  number + 1
     click_xpath("xpath=//table[@class = 'periods']/tbody//tr[#{num}]/td[@class = 'repetition']/a")
     wait_for_page_to_load "30000"
   end
   def edit_period(number)
     click_nth_link_with_text('Edit', number)
     wait_for_page_to_load "30000"
   end
   def add_activity(type, name)
     select_from_combobox("xpath=//select[@id='select-activity-type']", "#{type}")
     select_from_combobox("xpath=//select[@id='add-activity']", "#{name}")
     click_xpath("xpath=//input[@id='add-activity-button']")
   end
   def go_back_to_template
     assert_page_contains(@study_name)
     click_link_with_text(@study_name)
     wait_for_page_to_load "30000"
     assert_page_contains("Template for")
     assert_page_contains("Epochs and arms")
   end

   #Must be in the template page or error will occur
   def get_study_name()
     return get_text("xpath=//div[@id='main']/h1/span[@id='study-name']")
   end


   def get_number_of_arms(epoch_number)
     continue = true
     arm_number = 1
     while (continue)
             arm_number += 1
       continue = @browser.is_element_present("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{arm_number}]")

       if !(@browser.is_element_present("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class ='arm selected'][#{arm_number}]"))
         arm_number -=1
       else
         arm_number = arm_number
       end

     end
     return arm_number
    end
    def get_epoch_name(epoch_number)
      return get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{epoch_number}]/h4/span[child::text()]")
    end
    def get_arm_name(epoch_number, arm_number)
      return get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class= 'arm selected'][#{arm_number}]//a[child::text()]")
    end
   #navigation methods that help go back to the template or the home page
   #warning: changes may not save
   def go_up_one_level()
     click_xpath("xpath=//div[@id='breadcrumbs']//a[last()-1]")
     wait_for_page_to_load "30000"
   end
   def go_home()
      click_xpath("xpath=//div[@id='breadcrumbs']//a[1]")
      wait_for_page_to_load "30000"
   end
   
# End of Helper Methods
# ------------------------------------------------------------------------------------------------------ 
   
   
  private
    def locator_to_label(locator, options = {})
      options[:label] || locator.split("-").join(" ").capitalize
    end

    def request(relative_url)
      @browser.open("/studycalendar#{relative_url}")
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

