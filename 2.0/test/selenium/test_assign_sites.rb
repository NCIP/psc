require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class AssignSitesTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_assign_site_page
  
    login()
    click_on_assign_sites_home(1)
    assign_site('jack1')
    go_home
    click_on_assign_sites_home(1)
    assign_site('jack2')
    go_up_one_level()
    click_on_assign_sites_template
    remove_site('jack2')
    go_home
    click_on_assign_sites_home(2)
    assign_site('jack1')
    go_home
    click_on_assign_sites_home(2)
    assign_site('jack4')
    go_up_one_level()
    click_on_assign_sites_template
    remove_site('jack4')

    
  end
  def click_on_assign_sites_home(number)
    assert_element_exists("xpath=//ul[@class= 'menu'][2]//li[#{number}]//ul[@class = 'controls']//li[1]/a[child::text() = 'Assign sites']")
    click_xpath("xpath=//ul[@class= 'menu'][2]//li[#{number}]//ul[@class = 'controls']//li[1]/a[child::text() = 'Assign sites']")
    wait_for_page_to_load "30000"
    assert_page_contains("Assign Sites")
    assert_page_contains("Available Sites")
    assert_page_contains("Assigned Sites")
  end
  def click_on_assign_sites_template()
    assert_page_contains("Assign sites")  
    click_link_with_text('Assign sites')
    wait_for_page_to_load "30000"
    assert_page_contains("Assign Sites")
    assert_page_contains("Available Sites")
    assert_page_contains("Assigned Sites")
  end
  def assign_site(name)
    count1 = get_number_of_available_sites
    count2 = get_number_of_assigned_sites
    
    assert_element_exists("xpath=//form[@id='command'][1]//div[@class = 'row'][1]/div[@class='value']/select//option[child::text() = '#{name}']")
    select_from_combobox(id='availableSites', label ="#{name}")
    click_button_with_text('Assign')
    wait_for_page_to_load "30000"
    assert_page_contains('Template for')
    assert_page_contains('Assign Participant Coordinators')
    assert_page_contains('Assign Participant')
    click_link_with_text('Assign sites')
    wait_for_page_to_load "30000"
    #asserts site was properly added
    last_added = get_text("xpath=//form[@id='command'][2]//div[@class = 'row'][1]/div[@class='value']/select//option[last()]")
    assert last_added.eql?("#{name}")
    
    assert (count1 -1) == get_number_of_available_sites
    assert (count2 +1) == get_number_of_assigned_sites
  end
  def remove_site(name)
    count1 = get_number_of_available_sites
    count2 = get_number_of_assigned_sites
    
    assert_element_exists("xpath=//form[@id='command'][2]//div[@class = 'row'][1]/div[@class='value']/select//option[child::text() = '#{name}']")
    select_from_combobox(id='assignedSites', label ="#{name}")
    click_button_with_text('Remove')
    wait_for_page_to_load "30000"
    assert_page_contains('Template for')
    if (count2 -1 <= 0)
      assert_text_is_not_present('Assign Participant Coordinators')
      assert_text_is_not_present('Assign Participant')
    else
      assert_page_contains('Assign Participant Coordinators')
      assert_page_contains('Assign Participant')
    end
    click_link_with_text('Assign sites')
    wait_for_page_to_load "30000"
    assert_element_exists("xpath=//form[@id='command'][1]//div[@class = 'row'][1]/div[@class='value']/select//option[child::text() = '#{name}']")
    assert_element_does_not_exist("xpath=//form[@id='command'][2]//div[@class = 'row'][1]/div[@class='value']/select//option[child::text() = '#{name}']")
    
    assert (count1 +1) == get_number_of_available_sites
    assert (count2 -1) == get_number_of_assigned_sites
  end
  def get_number_of_assigned_sites
   continue = true
   site_number = 0
   while (continue)

     if (@browser.is_element_present("xpath=//form[@id='command'][2]//div[@class = 'row'][1]/div[@class='value']/select//option[#{site_number + 1}]"))
       site_number +=1
       continue = @browser.is_element_present("xpath=//form[@id='command'][2]//div[@class = 'row'][1]/div[@class='value']/select//option[#{site_number}]")
     else
       site_number = site_number
       continue =false
     end
   end
   return site_number
  end
  def get_number_of_available_sites
   continue = true
   site_number = 0
   while (continue)

     if (@browser.is_element_present("xpath=//form[@id='command'][1]//div[@class = 'row'][1]/div[@class='value']/select//option[#{site_number + 1}]"))
       site_number +=1
       continue = @browser.is_element_present("xpath=//form[@id='command'][1]//div[@class = 'row'][1]/div[@class='value']/select//option[#{site_number}]")
     else
       site_number = site_number
       continue =false
     end
   end
   return site_number
  end
end