require File.dirname(__FILE__) + '/../test_helper'

class AddingPeriodsTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands

  def test_adding_one_day_period_to_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "1"
    type "duration.quantity", "1"
	select_from_combobox("duration.unit", "label=day")
    type "repetitions", "1"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    assert_element_exists "epoch-0-arm-0-day-1-period-0"
    assert_element_does_not_exist "epoch-0-arm-0-day-2*"
  end

  def test_adding_one_week_period_to_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "1"
    type "duration.quantity", "1"
	select_from_combobox("duration.unit", "label=week")
    type "repetitions", "1"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    ('1'..'7').to_a.each { |value| assert_element_exists "epoch-0-arm-0-day-" + value + "-period-0"}
    assert_element_does_not_exist "epoch-0-arm-0-day-8*"
  end
    
  def test_adding_mulitday_period_to_middle_of_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "3"
    type "duration.quantity", "2"
	select_from_combobox("duration.unit", "label=day")
    type "repetitions", "1"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    assert_element_exists "epoch-0-arm-0-day-3-period-0"
    assert_element_exists "epoch-0-arm-0-day-4-period-0"
    assert_element_does_not_exist "epoch-0-arm-0-day-2-period-0"
    assert_element_does_not_exist "epoch-0-arm-0-day-5-period-0"
  end

  def test_adding_mulitweek_period_to_middle_of_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "4"
    type "duration.quantity", "2"
	select_from_combobox("duration.unit", "label=week")
    type "repetitions", "1"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    ('4'..'17').to_a.each { |value| assert_element_exists "epoch-0-arm-0-day-" + value + "-period-0"}
    assert_element_does_not_exist "epoch-0-arm-0-day-3-period-0"
    assert_element_does_not_exist "epoch-0-arm-0-day-18-period-0"
  end

  def test_adding_verylong_period_to_middle_of_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "32"
    type "duration.quantity", "106"
	select_from_combobox("duration.unit", "label=week")
    type "repetitions", "1"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    ('32'..'773').to_a.each { |value| assert_element_exists("epoch-0-arm-0-day-" + value + "-period-0", true)}
    document_comment "Check that Epoch 0 arm 0 day 32-773 period 0 are elements on the page"
    assert_element_does_not_exist "epoch-0-arm-0-day-31-period-0"
    assert_element_does_not_exist "epoch-0-arm-0-day-774-period-0"
  end

  def test_adding_repeating_daylong_periods_to_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "1"
    type "duration.quantity", "1"
	select_from_combobox("duration.unit", "label=day")
    type "repetitions", "7"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    ('1'..'7').to_a.each { |value| assert_element_exists "epoch-0-arm-0-day-" + value + "-period-0"}
    assert_element_does_not_exist "epoch-0-arm-0-day-8-period-0"
  end  

  def test_adding_repeating_weeklong_periods_to_single_arm_epoch
    testdata 'empty', 'some-activities', 'periodless-study'

    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    assert_page_contains "Add a period to epoch ECT Level 1"
    click_link_with_text "Add a period to epoch ECT Level 1"
    wait_for_page_to_load
    type "name", "Stage 1"
    type "startDay", "8"
    type "duration.quantity", "2"
	select_from_combobox("duration.unit", "label=day")
    type "repetitions", "4"
	click_button_with_text "Add"
    wait_for_page_to_load
    open '/pages/calendarTemplate?id=-1'
    wait_for_page_to_load
    click("selectedCalendarView", "list")
    ('8'..'64').to_a.each { |value| assert_element_exists("epoch-0-arm-0-day-" + value + "-period-0", true)}
    document_comment "Check that Epoch 0 arm 0 day 8-64 period 0 are elements on the page"
    assert_element_does_not_exist "epoch-0-arm-0-day-8-period-0"
  end  
      
end
