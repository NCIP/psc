require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class ArmTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_count
    init
    move_arm_down(2,2)
    move_arm_down(2,3)
    add_arm(1)
    add_arm(1)
    add_arm(1)
    add_arm(3)
    add_arm(2)
    move_arm_up(2,4)
    name_arm(1,4, 'Alexis')
    move_arm_up(1,2)
   move_arm_up(1,1)
   delete_arm(3,2)
   delete_arm(2,2)
  end
   def name_arm(epoch_number, arm_number, name)
     assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='Set name']")
     click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='Set name']")
     type_in_lone_text_field(name)
     click_button_with_text("OK", 'submit')
     wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class ='arm selected'][#{arm_number}]//a[child::text()='#{name}']")
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class= 'arm selected'][#{arm_number}]//a[child::text()='#{name}']")
   end
   def add_arm(epoch_number)
     assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class ='epoch last'][#{epoch_number}]/h4//div//a[child::text() = 'Add arm']")
     count = get_number_of_arms(epoch_number)
     click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]/h4//div//a[child::text() = 'Add arm']")
     wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class ='arm selected'][#{count +1}]//a[child::text()='[Unnamed arm]']")
     assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @class ='arm selected'][#{count +1}]//a[child::text()='[Unnamed arm]']")
      
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
   def move_arm_down(epoch_number, arm_number)
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▼']")
      visible = is_visible("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▼']")
      name = get_arm_name(epoch_number, arm_number)
      click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▼']")
      if (!visible)
        wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]//a[child::text() = '#{name}']")
        assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]//a[child::text() = '#{name}']")
      else
        wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'  or @class ='arm last'][#{arm_number + 1}]//a[child::text() = '#{name}']")
        assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number +1}]//a[child::text() = '#{name}']")
      end
    end
    def move_arm_up(epoch_number, arm_number)
       assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▲']")
       visible = is_visible("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▲']")
       name = get_arm_name(epoch_number, arm_number)
       click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='▲']")
       if (!visible)
         wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]//a[child::text() = '#{name}']")
         assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number}]//a[child::text() = '#{name}']")
       else
         wait_for_condition_element("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'  or @class ='arm last'][#{arm_number - 1}]//a[child::text() = '#{name}']")
         assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected' or @class ='arm last'][#{arm_number - 1}]//a[child::text() = '#{name}']")
       end
     end
           

    def delete_arm(epoch_number, arm_number)
      assert_element_exists("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='Delete']")
      arm_name = get_arm_name(epoch_number, arm_number)
      count = get_number_of_arms(epoch_number)
      matches = get_arm_matches(epoch_number, arm_name)
      click_xpath("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{arm_number}]/div[@class='arm-controls controls']//a[child::text() ='Delete']")
      wait_for_condition_confirmation()
      get_confirmation()
      assert (matches -1) == get_arm_matches(epoch_number, arm_name)
      assert (count - 1) == get_number_of_arms(epoch_number)
    end
    def get_arm_name(epoch_number, arm_number)
      name = get_text("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'  or @class ='arm last'][#{arm_number}]//a")
      return name
    end
    # Gets the number of arms that have the same name within a given epoch
    def get_arm_matches(epoch_number, name)
      position =1
      matches = 0
      count = get_number_of_arms(epoch_number)
      while (position <= count)
        
        if @browser.is_element_present("xpath=//div[@id='epochs-container']//div[@class='epoch' or @class = 'epoch last'][#{epoch_number}]//ul[@class = 'arms']//li[@class='arm' or @ class = 'arm selected'][#{position}]//a[child::text() = '#{name}']")
          matches += 1
        end
          position +=1
      end
      return matches
    end
end
  