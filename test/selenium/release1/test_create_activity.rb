require File.dirname(__FILE__) + '/../test_helper'

class CreateActivityTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_create_activity
      open "/pages/newActivity"
      assert_page_contains "New Activity"
	  type "activityName", "Administer b249"
	  type "activityDescription", "Administer applied dosage of agent b249"
	  select_from_combobox("activityTypeId", "label=supportive care")
      click_button_with_text "Create"
      wait_for_page_to_load
      assert_page_contains "Activity: Administer b249"
      assert_page_contains "Activity Description: Administer applied dosage of agent b249"
      assert_page_contains "Activity Type: supportive care"
  end
  
end

__END__
