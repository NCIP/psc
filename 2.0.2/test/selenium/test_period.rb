require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class PeriodTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_create_period
    init()
    add_period(2, 2)
    initialize_period('Period1', 2, 5, 'day', 4)
    add_period(3, 1)
    initialize_period('Period1', -3, 15, 'day', 3)
    add_period(2, 2)
    initialize_period('Period2', -2, 2, 'week', 3)
  end
  # sh work/psc/trunk/test/bin/selenium-interactive.sh 
   
    #for now, name is the name of the arm; number is to identify which name is the one being specified (in case there
    # are repeated names).  If it is the time the name appears, enter 1. If 2nd, 2 and so forth.
   def add_period(epoch_number, arm_number)
     assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class= 'arm selected'][#{arm_number}]//a[@class='arm']")
     arm_name = get_arm_name(epoch_number, arm_number)
     click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class= 'arm selected'][#{arm_number}]//a[@class='arm']")
     num = get_number_of_arms(epoch_number)
     if (num <= 1)
       wait_for_condition_element("xpath=//div[@id = 'selected-arm']//*[child::text() = '#{arm_name}']")
       assert_element_exists("xpath=//div[@id = 'selected-arm']//*[child::text() = '#{arm_name}']")
     else
       epoch_name = get_epoch_name(epoch_number)
       wait_for_condition_element("xpath=//div[@id = 'selected-arm']//*[child::text() = '#{epoch_name}: #{arm_name}']")
       assert_element_exists("xpath=//div[@id = 'selected-arm']//*[child::text() = '#{epoch_name}: #{arm_name}']")
     end
     click_link_with_text("Add period")
     wait_for_page_to_load "30000"
     assert_page_contains("Add period")
     assert_page_contains("Start day")
   end
   def initialize_period(name, start_day, duration_quantity, duration_unit, repetitions)
     
     type("xpath=//input[@type='text' and @id='period.name']", "#{name}")
     type("xpath=//input[@type='text' and @id='period.startDay']", "#{start_day}")
     type("xpath=//input[@type='text' and @id='period.duration.quantity']", "#{duration_quantity}")
     type("xpath=//input[@type='text' and @id='period.repetitions']", "#{repetitions}")
     select_from_combobox("xpath=//select[@id='period.duration.unit']", "#{duration_unit}")
     if (repetitions > 1)
       wait_for_condition_text('It will have the same events on')
       assert_page_contains('It will have the same events on')
     end
    # unit = 'duration_unit'
  #   if (str == name) name.to_i
  #   if (unit.eql?('day'))
     assert_page_contains("The configured period will last for" )
     click_button_with_text('Submit')
     wait_for_page_to_load "30000"
   end
   def create_new_activity
     assert_page_contains('Create new activity')
     click_link_with_text('Create new activity')
     wait_for_page_to_load "30000"
     assert_page_contains "New Activity"
     assert_page_contains "Activity name"
     assert_page_contains "Activity description"
     assert_page_contains "Activity type"
   end
   def initialize_activity(name, description, type)
     type("xpath=//input[@id='activityName' and @type='text']", "#{name}")
     type("xpath=//input[@id='activityDescription' and @type='text']", "#{description}")
     select_from_combobox("xpath=//select[@id='activityType']", "#{type}")
     click_button_with_text('Create')
     wait_for_page_to_load "30000"
     assert_page_contains('The numbers in the grid below show how many times each activity should be performed')
   end
   #rows and columns start with 0 as the first row/column
   def set_activity_frequency(row, column, number)
     type("xpath=//tbody[@id = 'input-body']//tr[@class='input-row']//td//input[@id = 'grid[#{row}].counts[#{column}]']", "#{number}")
   end
   #rows and columns start with 0 as the first row/column
   def set_activity_details(row, details)
     type("xpath=//tbody[@id = 'input-body']//tr[@class='input-row']//td//input[@id = 'grid[#{row}].details']", "#{details}")
   end
   def save_changes
     click_button_with_text('Save Changes')
     wait_for_page_to_load "30000"
   end
   def edit_period(number)
     click_nth_link_with_text('Edit', number)
     wait_for_page_to_load "30000"
     assert_page_contains("Add period")
     assert_page_contains("Start day")
   end
   def add_activity(type, name)
     select_from_combobox("xpath=//select[@id='select-activity-type']", "#{type}")
     select_from_combobox("xpath=//select[@id='add-activity']", "#{name}")
     click_xpath("xpath=//input[@id='add-activity-button']")
   #  wait_for_condition_text('#{name}')
   end
   def edit_period_activities(number)
     num =  number + 1
     click_xpath("xpath=//table[@class = 'periods']/tbody//tr[#{num}]/td[@class = 'repetition']/a")
     wait_for_page_to_load "30000"
     assert_page_contains('The numbers in the grid below show how many times each activity should be performed on each day of the period')
   end
  
end