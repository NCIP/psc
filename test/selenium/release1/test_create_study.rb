require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_create_simple_study
    open "/"
	document_comment "I have not filled anything else in yet, because we have nothing to test"
#    type "username", "moses"
#    type "password", "mypassword"
#    click_button_with_text "Continue"
#    wait_for_page_to_load
    dump_body_text
  end
end

__END__
Should create a document like

Create Study
============

Create Simple Study
-------------------

 1. Start at /selenium.html
 2. Type "moses" into "username" field
 3. Type "mypassword" into "password" field
 4. Click button with text "Continue"
 5. Wait for page to load
 6. Check that page contains the text "lahwoaeifhawoiefhowa"
    Note: oiwehofiahwef;oiawhe; foiawhef ;aowiehf ;awoihfe 
