require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyTest < Test::Unit::TestCase
	include StudyCalendar::SeleniumCommands
  
	def test_create_single_arm_study
		testdata 'empty' 
	
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Single arm study"
		type "epoch-name-0", "Treatment"
		click_button_with_text "Create"
		wait_for_page_to_load

		assert_page_contains "Template for Single arm study"
		assert_page_contains "Treatment"
		assert_page_contains "Add a period to epoch Treatment"
		
		open "/pages/studyList/"
		assert_page_contains "Single arm study"
	end
  
  	#This method also tests "Remove Last Arm" button
	def test_create_two_arm_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two arm study"
		type "epoch-name-0", "pre-tx"	
		click("arms[0]", "true", :id  => "multiple-arms-0-yes")
		document_comment "Input fields for the names of arms 1 and 2 of epoch 1 should appear, as well as buttons for adding and removing arms"
		type "arm-name-0-0", "A"
		type "arm-name-0-1", "B"
		click_button_with_text "Add arm", "button"
		type "arm-name-0-2", "C"
		click_button_with_text "Remove last arm", "button"
		@browser.wait_for_condition("selenium.browserbot.getCurrentWindow().eval(\"$('arm-name-0-2') == null\")", 10000)
		
		click_button_with_text "Create"
		wait_for_page_to_load
		assert_page_contains "Template for Two arm study"
		assert_page_contains "Add a period to arm A"
		assert_page_contains "Add a period to arm B"
		assert_page_does_not_contain "Add a period to arm C"
		
		open "/pages/studyList/"
		assert_page_contains "Two arm study"
	end
	
	def test_create_three_arm_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Three arm study"
		
		epochArmChoice("initialTreatment", 0)
		twoArmUnit(0,0)
		addOneArm(0,2,2)
		
		click_button_with_text "Create"
		wait_for_page_to_load
		
		armAssertUnit("Three arm study", 3)
		
		open "/pages/studyList/"
		assert_page_contains "Three arm study"
	end
	
	def test_create_twenty_arm_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Twenty arm study"
		
		epochArmChoice("enhanced treatment",0)
		twoArmUnit(0,0)
		i = 0
		while i<18
			addOneArm(0,i+2,i+2)
			i = i+1
		end
		
		click_button_with_text "Create"
		wait_for_page_to_load
		
		armAssertUnit("Twenty arm study", 20)
		
		open "/pages/studyList/"
		assert_page_contains "Twenty arm study"
		
		open "/pages/studyList/"
		assert_page_contains "Twenty arm study"
	end
	
	
	#This method also tests "Remove Last Epoch" button
	def test_create_two_singleArmEpoch_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two epoch study, each epoch has single arm"
		type "epoch-name-0", "Initial"	
		click("arms[0]", "false", :id  => "multiple-arms-0-no")
		click_button_with_text "Add epoch", "button" 
		document_comment "Input fields for the names of epoch 2 should appear, as well as radio buttons for choosing multiple arms"
		
		type "epoch-name-1", "Enhance"	
		click("arms[1]", "false", :id  => "multiple-arms-1-no")
		click_button_with_text "Add epoch", "button"  
		document_comment "Input fields for the names of epoch 3 should appear, as well as radio buttons for choosing multiple arms"
    	
    	type "epoch-name-2", "Longerperiod"	
    	click("arms[2]", "false", :id  => "multiple-arms-2-no")
    	click_button_with_text "Remove last epoch", "button"  
		@browser.wait_for_condition("selenium.browserbot.getCurrentWindow().eval(\"$('epoch-name-2') == null\")", 10000)
    	
		click_button_with_text "Create"
		wait_for_page_to_load
		assert_page_contains "Template for Two epoch study, each epoch has single arm"
		assert_page_contains "Add a period to epoch Initial"
		assert_page_contains "Add a period to epoch Enhance"
		assert_page_does_not_contain "Add a period to epoch Longerperiod"
		
		open "/pages/studyList/"
		assert_page_contains "Two epoch study, each epoch has single arm"
	end
	
	
	def test_create_singleArmEpoch_and_twoArmEpoch_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two epoch study, epoch-1 has single arm, epoch-2 has two arms"
		type "epoch-name-0", "Treatment A"
		click("arms[0]", "false", :id  => "multiple-arms-0-no")
		click_button_with_text "Add epoch", "button" 
		document_comment "Input fields for the names of epoch 2 should appear, as well as radio buttons for choosing multiple arms"
		
		epochArmChoice("Treatment B", 1)
		twoArmUnit(1,0)
		
		click_button_with_text "Create"
		wait_for_page_to_load
		
		armAssertUnit("Two epoch study, epoch-1 has single arm, epoch-2 has two arms", 2)
		assert_page_contains "Add a period to epoch Treatment A"
		
		open "/pages/studyList/"
		assert_page_contains "Two epoch study, epoch-1 has single arm, epoch-2 has two arms"
	end
	
	
	def test_create_twoArmEpoch_and_singleArmEpoch_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two epoch study, epoch-1 has two arms, epoch-2 has single arm"
		epochArmChoice("First Treatment", 0)
		twoArmUnit(0,0)
		
		click_button_with_text "Add epoch", "button" 
		document_comment "Input fields for the names of epoch 2 should appear, as well as radio buttons for choosing multiple arms"
		type "epoch-name-1", "Second Treatment"
		click("arms[1]", "false", :id  => "multiple-arms-0-no")
		
		click_button_with_text "Create"
		wait_for_page_to_load
		
		armAssertUnit("Two epoch study, epoch-1 has two arms, epoch-2 has single arm", 2)
		assert_page_contains "Add a period to epoch Second Treatment"
		
		open "/pages/studyList/"
		assert_page_contains "Two epoch study, epoch-1 has two arms, epoch-2 has single arm"
	end
	
	
	def test_create_threeArmEpoch_and_fourArmEpoch_study
		testdata 'empty'
		open "/pages/newStudy"
		wait_for_page_to_load
		type "study-name", "Two epoch study, first epoch has three arms, second epoch has four arms"
		epochArmChoice("ECT", 0)
		twoArmUnit(0,0)
		addOneArm(0,2,2)
		
		click_button_with_text "Add epoch", "button" 
		document_comment "Input fields for the names of epoch 2 should appear, as well as radio buttons for choosing multiple arms"
		
		epochArmChoice("AC Drug", 1)
		twoArmUnit(1,3)
		addOneArm(1,2,5)
		addOneArm(1,3,6)
		
		click_button_with_text "Create"
		wait_for_page_to_load
		
		armAssertUnit("Two epoch study, first epoch has three arms, second epoch has four arms", 7)
		
		open "/pages/studyList/"
		assert_page_contains "Two epoch study, first epoch has three arms, second epoch has four arms"
	end
	
	
	
	def epochArmChoice(epochName, epochId)
		type "epoch-name-"+"#{epochId}", "#{epochName}"	
		click("arms[#{epochId}]", "true", :id  => "multiple-arms-0-yes")
		document_comment "Input fields for the names of arms 1 and 2 of epoch #{epochId} should appear, as well as buttons for adding and removing arms"
	end
	
	def twoArmUnit(epochId, armNameIndex)
		type "arm-name-" + "#{epochId}" + "-0", "#{armName[armNameIndex]}"
		type "arm-name-" + "#{epochId}" + "-1", "#{armName[armNameIndex+1]}"
	end
	
	def addOneArm(epochId, armId, armNameIndex)
		#click_button_with_text "Add arm", "button"
		click_button_with_text_useId("add-arm-button-"+"#{epochId}", "button" )
		type "arm-name-" + "#{epochId}-" + "#{armId}", "#{armName[armNameIndex]}"
	end
	
	def armAssertUnit(templateName, armTotal)
		assert_page_contains "Template for #{templateName}"
		i = 0
		while i < armTotal 
			assert_page_contains("Add a period to arm " + "#{armName[i]}")
			i = i + 1
		end
	end
	
	def click_button_with_text_useId(buttonId, input_type='submit')
		@browser.click("xpath=//input[@type='#{input_type}' and @id='#{buttonId}']")
		documenter.document_step "Click \"#{buttonId}\" button"
	end
	
	def armName
		armName = ['high risk', 'low risk', 'normal', 'males', 'females', 'males and females', 'high weight', 'low weight', 'random', 'white', 'african-american', 
	'asian', 'young', 'old', 'smoker', 'alcoholic','age 20 to 30', 'age 30 to 40', 'age 40 to 50', 'age 50 to 60', 'age 60 to 70']
	end
	
	def epochName
		epochName = ['pre-tx','initial','enhance']
	end
end

__END__
Should create a document like:

Create Study
============

Create Single Arm Study
-----------------------

 1. Start at /pages/newStudy
 2. Under "Study name" type "Vioxx Study"

Create Multiple Arm Study
-------------------------

  1. Start at /pages/newStudy
  2. Under "Study name" type "Vioxx Study"
  3. Under "Multiple arms?" select "Yes"
     Input fields for the names of arms 1 and 2 should appear, as well as buttons for adding and removing arms
