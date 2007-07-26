require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class HolidaysTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_holidays
    login()
    click_link_with_text('Manage sites')
    wait_for_page_to_load "30000"
    assert true
    
    #manage_holidays(1)
  end
  def test_special_cases
      login()
      click_link_with_text('Manage sites')
      wait_for_page_to_load "30000"
    
    add_all_with_default_conditions(2)
  end
 # def check1
#  end
#  def check2
#  end
#  def check3
#  end
#number refers to the row in which the site is found
  def manage_holidays(number)
    assert_element_exists("xpath=//table/tbody//tr[#{number}]//td[2]/a")
    click_xpath("xpath=//table/tbody//tr[#{number}]//td[2]/a")
    wait_for_page_to_load "30000"
    assert_page_contains('Manage Holidays And Weekends')
    assert_page_contains('Please select the holiday from the list:')
    assert_page_contains('List of Selected Holidays:')

  # set_day_of_the_week('Monday')
  #  remove_day_of_the_week('Monday')
  #  set_day_of_the_week('Tuesday')
  #  set_day_of_the_week('Tuesday')
  #  set_day_of_the_week('Sunday')
  #  remove_day_of_the_week('Tuesday')

    set_recurring_holiday('12/4', 'Fourth of Decemeber Holiday')
    remove_recurring_holiday('12/4', 'Fourth of Decemeber Holiday')
    set_non_recurring_holiday('4/1/2007', 'April Fools this is not a holiday')
    remove_non_recurring_holiday('4/1/2007', 'April Fools this is not a holiday')
    set_relative_recurring_holiday('Third','Sunday','June', "Fathers day")
    remove_relative_recurring_holiday('Third','Sunday','June', "Fathers day")
  end
  def set_day_of_the_week(day)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Day Of The Week']")
    select_from_combobox('typeOfHolidays', 'Day Of The Week')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Day Of The Week']")
    wait_for_condition_element("xpath=//div[@id='dayOfTheWeek-div' and @style='opacity: 0.999999;']")
    assert_element_exists('dayOfTheWeek')
    assert_element_exists("xpath=//select[@id='dayOfTheWeek']//option[child::text() = '#{day}']")
    select_from_combobox('dayOfTheWeek', "#{day}")
    click_button_with_text('Add')
    wait_for_condition_element("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{day} (Office is Closed)']")
    assert_element_exists('selectedHoliday')
    # temporary solution to limit the speed of the test.
    
    sleep(4)
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{day} (Office is Closed)']")
  end
  def remove_day_of_the_week(day)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{day} (Office is Closed)']")
    select_from_combobox('selectedHoliday', "#{day} (Office is Closed)")
    click_button_with_text('Remove')
    wait_for_condition_element_false("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{day} (Office is Closed)']")
    assert_element_does_not_exist("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{day} (Office is Closed)']")
     sleep(5)
  end
  # For month, enter a value between 1-12, then a '/', and then a value for a day.
  #check if its possible to have it on the thirtieth of february. Accordingly, 31 day in june... 45 day in the thirteenth month.
  #third null of april?
  #non recurring with spaces as dates.
  def set_recurring_holiday(date, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Recurring Holiday']")
    select_from_combobox('typeOfHolidays', 'Recurring Holiday')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Recurring Holiday']")
    wait_for_condition_element("xpath=//div[@id='holidayRecurring-div' and @style='opacity: 0.999999;']")
    assert_element_exists("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDate']")
    assert_element_exists("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDescription']")
 #   type('holidayDate', "#{date}")
    type("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDate']", "#{date}")
#    type('holidayDescription', "#{description}")
    type("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDescription']", "#{description}")
    click_xpath("xpath=//form[@id = 'recurringHoliday']//input[@type='submit']")
    sleep(5)

    wait_for_condition_element("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
  end
  def remove_recurring_holiday(date, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    select_from_combobox('selectedHoliday', "#{date} (#{description})")
    click_button_with_text('Remove')
    wait_for_condition_element_false("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    assert_element_does_not_exist("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
     sleep(5)
  end
  # months and days with single digits should not have a 0 in front of it. Ex. 4/5/1990 instead of 04/05/1990
  def set_non_recurring_holiday(date, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Non Recurring Holiday']")
    select_from_combobox('typeOfHolidays', 'Non Recurring Holiday')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Non Recurring Holiday']")
    wait_for_condition_element("xpath=//div[@id='holidayNotRecurring-div' and @style='opacity: 0.999999;']")
    assert_element_exists("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDate']")
    assert_element_exists("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDescription']")
    type("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDate']", "#{date}")
    type("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDescription']", "#{description}")
    click_xpath("xpath=//div[@id= 'holidayNotRecurring-div']//input[@type='submit']")
    wait_for_condition_element("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    sleep(5)
  end
  def remove_non_recurring_holiday(date, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    sleep(5)
    select_from_combobox('selectedHoliday', "#{date} (#{description})")
    click_button_with_text('Remove')
    wait_for_condition_element_false("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
    assert_element_does_not_exist("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{date} (#{description})']")
     sleep(5)
  end
  def set_relative_recurring_holiday(week, day, month, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Relative Recurring Holiday']")
    select_from_combobox('typeOfHolidays', 'Relative Recurring Holiday')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Relative Recurring Holiday']")
    wait_for_condition_element("xpath=//div[@id='relativeRecurring-div' and @style='opacity: 0.999999;']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='week']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='dayOfTheWeek']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='month']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//input[@name = 'holidayDescription']")
    select_from_combobox("xpath=//div[@id='relativeRecurring-div']//table//select[@name='week']", "#{week}")
    select_from_combobox("xpath=//div[@id='relativeRecurring-div']//table//select[@name='dayOfTheWeek']", "#{day}")
    select_from_combobox("xpath=//div[@id='relativeRecurring-div']//table//select[@name='month']", "#{month}")
    type("xpath=//div[@id='relativeRecurring-div']//input[@name = 'holidayDescription']", "#{description}")
    click_xpath("xpath=//div[@id = 'relativeRecurring-div']//input[@type='submit']")
    wait_for_condition_element("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{week} #{day} of #{month} (#{description})']")
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{week} #{day} of #{month} (#{description})']")
    sleep(5)
  end
  def remove_relative_recurring_holiday(week, day, month, description)
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{week} #{day} of #{month} (#{description})']")
    select_from_combobox("selectedHoliday", "#{week} #{day} of #{month} (#{description})")
    click_button_with_text('Remove')
    wait_for_condition_element_false("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{week} #{day} of #{month} (#{description})']")
    assert_element_does_not_exist("xpath=//select[@id='selectedHoliday']//option[child::text() = '#{week} #{day} of #{month} (#{description})']")
     sleep(5)
  end
  def add_all_with_default_conditions(number)
    
    assert_element_exists("xpath=//table/tbody//tr[#{number}]//td[2]/a")
    click_xpath("xpath=//table/tbody//tr[#{number}]//td[2]/a")
    wait_for_page_to_load "30000"
    
   #adds a blank day of the week that is a holiday.
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Day Of The Week']")
    select_from_combobox('typeOfHolidays', 'Day Of The Week')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Day Of The Week']")
    wait_for_condition_element("xpath=//div[@id='dayOfTheWeek-div' and @style='opacity: 0.999999;']")
    assert_element_exists('dayOfTheWeek')
    assert_element_exists("xpath=//select[@id='dayOfTheWeek']//option[child::text()]")
    click_button_with_text('Add')
    
      sleep(5)
      
    assert_page_does_not_contain('Exception')
    assert_page_does_not_contain('null')
    
    #adds a blank recurring date as a holiday
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Recurring Holiday']")
    select_from_combobox('typeOfHolidays', 'Recurring Holiday')
       click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Recurring Holiday']")
      wait_for_condition_element("xpath=//div[@id='holidayRecurring-div' and @style='opacity: 0.999999;']")
      assert_element_exists("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDate']")
      assert_element_exists("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDescription']")
      click_xpath("xpath=//form[@id = 'recurringHoliday']//input[@type='submit']")
        wait_for_condition_text('Error enterring the date ')
      assert_page_does_not_contain('Exception')
      assert_page_does_not_contain('null')
      type("xpath=//form[@id = 'recurringHoliday']//input[@id= 'holidayDate']", "12/20")
      click_xpath("xpath=//form[@id = 'recurringHoliday']//input[@type='submit']")
      wait_for_condition_text('Missing Description field')
        assert_page_does_not_contain('Exception')
        assert_page_does_not_contain('null')
      
      
  
     #adds a blank non-recurring date as a holiday
     assert_element_exists('typeOfHolidays')
      assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Non Recurring Holiday']")
      select_from_combobox('typeOfHolidays', 'Non Recurring Holiday')
      click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Non Recurring Holiday']")
      wait_for_condition_element("xpath=//div[@id='holidayNotRecurring-div' and @style='opacity: 0.999999;']")
      assert_element_exists("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDate']")
      assert_element_exists("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDescription']")
      click_xpath("xpath=//div[@id= 'holidayNotRecurring-div']//input[@type='submit']")
        #temporary because I don't know what web page will say so wait_for_condition cannot be used
      wait_for_condition_text('Error enterring the date ')
      assert_page_does_not_contain('Exception')
      assert_page_does_not_contain('null')
      type("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDate']", "12/6/2005")
      click_xpath("xpath=//div[@id= 'holidayNotRecurring-div']//input[@type='submit']")
      wait_for_condition_text('Missing Description field')
      assert_page_does_not_contain('Exception')
      assert_page_does_not_contain('null')
      type("xpath=//div[@id='holidayNotRecurring-div']//input[@name = 'holidayDescription']", "caca")
       click_xpath("xpath=//div[@id= 'holidayNotRecurring-div']//input[@type='submit']")
      wait_for_condition_element("xpath=//select[@id='selectedHoliday']//option[child::text() = '12/6/2005 (caca)']")
            
    #adds a blank relative recurring date as a holiday
    assert_element_exists('typeOfHolidays')
    assert_element_exists("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Relative Recurring Holiday']")
    select_from_combobox('typeOfHolidays', 'Relative Recurring Holiday')
    click_xpath("xpath=//select[@id='typeOfHolidays']//option[child::text() = 'Relative Recurring Holiday']")
    wait_for_condition_element("xpath=//div[@id='relativeRecurring-div' and @style='opacity: 0.999999;']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='week']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='dayOfTheWeek']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//table//select[@name='month']")
    assert_element_exists("xpath=//div[@id='relativeRecurring-div']//input[@name = 'holidayDescription']")
    click_xpath("xpath=//div[@id = 'relativeRecurring-div']//input[@type='submit']")
      #temporary because I don't know what web page will say so wait_for_condition cannot be used
    wait_for_condition_text('Missing Description field')
      assert_page_does_not_contain('Exception')
      assert_page_does_not_contain('null')
  end
end
    