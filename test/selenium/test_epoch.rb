require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class EpochTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

    def test_epoch
      init
      @epoch_count = 3
     add_epoch
     add_epoch
      add_epoch
      add_epoch
      add_epoch
      name_epoch1('#1', 1)
      name_epoch1('#2', 2)
      name_epoch1('#3', 3)
      name_epoch1('#4', 4)
      name_epoch2('#1a', 1)
      name_epoch2('#2a', 2)
      name_epoch2('#3a', 3)
      name_epoch2('#4a', 4)
      move_epoch_left(2)
      move_epoch_right(2)
      delete_epoch(@epoch_count)
      delete_epoch(3)

      add_epoch()
      add_epoch
      delete_all_but_one_epoch

    end
    
    def add_epoch
      assert_page_contains("Add epoch")
      click_link_with_text("Add epoch")
      @epoch_count += 1
#      @browser.is_element_present("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{@epoch_count}]/h4/span[child::text() = '[Unnamed epoch]']")
      wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{@epoch_count}]/h4/span[child::text() = '[Unnamed epoch]']")
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{@epoch_count}]/h4/span[child::text() = '[Unnamed epoch]']")
    end
    def move_epoch_left(position)
      epoch_name = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span")
      click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch'][#{position}]/h4/div//a[child::text() = '\342\227\204']")
#      wait_for_condition_element("xpath=//div")
      wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position -1}]/h4/span[child::text() ='#{epoch_name}']")
      epoch_name2 = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position -1}]/h4/span")
      assert epoch_name.eql?(epoch_name2)
      assert_element_is_hidden("xpath=//div[@id='epochs-container']//div[@class='epoch'][1]/h4/div//a[child::text() = '\342\227\204']")
   #   assert_element_is_hidden("xpath=//div[@id='epochs-container']//div[@class='epoch'][#{@epoch_count}]/h4/div//a[child::text() ='►']")
    end
    def move_epoch_right(position)
      epoch_name = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span")
      click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch'][#{position}]/h4/div//a[child::text() ='►']")
      wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position +1}]/h4/span[child::text()='#{epoch_name}']")
      epoch_name2 = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position +1}]/h4/span")
      assert epoch_name.eql?(epoch_name2)
      assert_element_is_hidden("xpath=//div[@id='epochs-container']//div[@class='epoch'][1]/h4/div//a[child::text() = '\342\227\204']")
    #  assert_element_is_hidden("xpath=//div[@id='epochs-container']//div[@class='epoch'][#{@epoch_count}]/h4/div//a[child::text() ='►']")
    end
    #names the chosen epoch. Name is the new name and position is the position of 
    #the epoch going from left to right with respect to the other epochs.
    def name_epoch1(name, position)

      #### This will change the name via the 'Set name' button
      @browser.click("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/div//a[@title='Change the name of this epoch']")
      type_in_lone_text_field(name)
      click_button_with_text('OK', 'submit')
      # waits for the epoch title to become the new name
      wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span[child::text() = '#{name}']")
      # checks to see that the epoch being modified now holds the new name.
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span[child::text() = '#{name}']")
      # return get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{epoch_number}]/h4/span[child::text()]")
    end
    def name_epoch2(name, position)
      
      #### This will change the name by clicking on the span tag with the epoch's title
      @browser.click("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span")
      type_in_lone_text_field(name)
      click_button_with_text('OK', 'submit')
      # waits for the epoch title to become the new name
      wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span[child::text() = '#{name}']")
      # checks to see that the epoch being modified now holds the new name.
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span[child::text() = '#{name}']")
    end
    
    def delete_epoch(position)
       assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/div//a[@title='Delete this epoch']")
       if (position == @epoch_count)
         @browser.click("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/div//a[@title='Delete this epoch']")
         @browser.get_confirmation()
         @epoch_count -= 1
         wait_for_condition_element_false("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]")
         assert_element_does_not_exist("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/div//a[@title='Delete this epoch']")
       else
         next_epoch_name = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position +1}]/h4/span")
         @browser.click("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/div//a[@title='Delete this epoch']")
         @browser.get_confirmation()
         @epoch_count -= 1
         wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span[child::text() = '#{next_epoch_name}']")
         epoch_name = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][#{position}]/h4/span")
         assert next_epoch_name.eql?(epoch_name)
       end
    end
    def delete_all_but_one_epoch
      (@epoch_count -1).times do
        delete_epoch(1)
      end
      assert (@epoch_count == 1)
      assert_element_is_hidden("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][1]/h4/div//a[@title='Delete this epoch']")
      assert_element_does_not_exist("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class='epoch last'][2]/h4/div//a[@title='Delete this epoch']")
    end
end 