require File.dirname(__FILE__) + '/../test_helper'

class CreateStudyTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  
  def test_create_single_arm_study
    open "/pages/newStudy"
    type "study-name", "Vioxx Study"
    document_comment "Cannot proceed because form submission is not implemented yet."
  end
  
  def test_create_multiple_arm_study
    open "/pages/newStudy"
    type "study-name", "Vioxx Study"
    check("arms", "yes", :label => "Multiple arms?")
    document_comment "Input fields for the names of arms 1 and 2 should appear, as well as buttons for adding and removing arms"
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
