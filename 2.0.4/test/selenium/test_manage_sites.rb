require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class ManageSitesTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_manage_sites
    login()
    request "/pages/admin"    
    assert_page_contains('Manage sites')
    click_link_with_text('Manage sites')
    wait_for_page_to_load "30000"
    create_new_site('jack1')
    create_new_site('jack2')
    create_new_site('jack3')
    create_new_site('jack4')
    
  end
  def create_new_site(name)
    assert_page_contains('Create New Site')
    click_link_with_text('Create New Site')
    wait_for_page_to_load "30000"
    assert_page_contains('New Site')
    assert_page_contains('Site Name')
    type_in_lone_text_field("#{name}")
    click_button_with_text('Create')
    wait_for_page_to_load  "30000"
    check = get_text("xpath=//table/tbody//tr[last()]/td")
  #  assert check.eql?("#{name} Assign Site Coordinators to Site")
  end
    
end
