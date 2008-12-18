describe "/subject_assignments" do
  
  #ISSUE:
  #1) post /studies/{study-identifier}/sites/{site-identifier}/subject-assignments returns error:
  #   Authenticated account is not authorized for this resource and method
  #   Need to call application_context['templateService'].assignTemplateToSubjectCoordinator in order for POST to succeed
  #   but encounter Hibernate lazy initialization issue
  
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end

  before do
    #create site
    @site1 = PscTest::Fixtures.createSite("My Site", "site1")
    application_context['siteDao'].save( @site1)
    
    #create a study with an amendment
    @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["segment_A", "segment_B"].to_java(:String)) 
    @amend_date1 = PscTest.createDate(2008, 12, 10)          
    @amendment = PscTest::Fixtures.createAmendment("am1", @amend_date1)
    @study1.planned_calendar.epochs.first.study_segments[0].grid_id = "segment1" #replace auto-generated study-segment id
    @study1.planned_calendar.epochs.first.study_segments[1].grid_id = "segment2"
    @study1.amendment = @amendment
    application_context['studyService'].save(@study1)

    #create a studysite       
    @studySite1 = PscTest::Fixtures.createStudySite(@study1, @site1)
    application_context['studySiteDao'].save(@studySite1)
                  
  end
  
  describe "GET" do
     
     before do
       
       # create user
       @user = PscTest::Fixtures.createSubjectCoordinatorUser("mary", 1, 2000)
       application_context['userDao'].save(@user)

       #approve an existing amendment
       @approve_date = PscTest.createDate(2008, 12, 31)
       @studySite1.approveAmendment(@amendment, @approve_date)
       application_context['studySiteDao'].save(@studySite1)
              
       #create subject              
       @birthDate = PscTest.createDate(1983, 3, 23)           
       @subject1 = PscTest::Fixtures.createSampleMaleSubject("ID001", "Alan", "Boyarski", @birthDate)         
       @studySegment1 = PscTest::Fixtures.getStudySegmentFromStudy(@study1, 0, 0)      
       @date = PscTest.createDate(2008, 12, 26)   

       #create a study subject assignment
       @studySubjectAssignment1 = application_context['subjectService'].assignSubject(@subject1, @studySite1, @studySegment1, @date, "ID001", @user)
       application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment1)
       
       #create another subject under the same study
       @birthDate2 = PscTest.createDate(1985, 5, 1)           
       @subject2 = PscTest::Fixtures.createSampleFemaleSubject("ID002", "Amanda", "Boyarski", @birthDate2)         
       @studySubjectAssignment2 = application_context['subjectService'].assignSubject(@subject2, @studySite1, @studySegment1, @date, "ID002", @user)
       application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment2)       
     end
     
     
    it "forbids access to a subject assignment to an unauthorized user" do
      get "/studies/NU480/sites/site1/subject-assignments", :as => nil
      response.status_code.should == 401
    end
    
    it "allows access to an existing subject assignment to an authorized user" do
      get "/studies/NU480/sites/site1/subject-assignments", :as => :mary
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      xml_attribute("subject", "first-name").should include("Amanda")
      xml_attribute("subject", "first-name").should include("Alan")
      xml_attribute("subject", "person-id").should include("ID001")
      xml_attribute("subject", "person-id").should include("ID002")
      response.xml_elements('//subject').should have(2).elements          
    end

  end
  
  
  describe "POST" do
      
      before do
        
        # load existing user
        @user = application_context['userService'].getUserByName("juno")
        application_context['userDao'].save(@user)
        
        # @user = application_context['templateService'].assignTemplateToSubjectCoordinator(@amended_study, @site1, @user)
        # application_context['userDao'].save(@user)
        
        #approve an existing amendment
        @approve_date = PscTest.createDate(2008, 12, 31)
        @studySite1.approveAmendment(@amendment, @approve_date)
        application_context['studySiteDao'].save(@studySite1)
                
        @subject_registration1_xml = psc_xml("registration", 'first-study-segment-id' => "segment1", 'date' => "2008-12-27", 
        'subject-coordinator-name' => "juno"){|subject| subject.tag!('subject', 'first-name' => "Andre", 'last-name' => "Suzuki", 
          'birth-date' => "1982-03-12", 'person-id' => "ID006", 'gender'=> "Male")}   
      end
      
      it "allows creation of a new subject-assignment for an authorized user" do
        pending
        post "/studies/NU480/sites/site1/subject-assignments", @subject_registration1_xml, :as => :juno
        puts response.entity
        response.status_code.should == 201
        response.status_message.should == "Created"
        response.content_type.should == 'text/xml'
        
      end
      
      
       
  end
          
end