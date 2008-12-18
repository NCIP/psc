describe "/schedule" do
  
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
    
    #approve an existing amendment
    @approve_date = PscTest.createDate(2008, 12, 31)
    @studySite1.approveAmendment(@amendment, @approve_date)
    application_context['studySiteDao'].save(@studySite1)    
    
    #create subject and subject coordinator user                
    @birthDate = PscTest.createDate(1983, 3, 23)           
    @subject1 = PscTest::Fixtures.createSampleMaleSubject("ID001", "Alan", "Boyarski", @birthDate)         
    @studySegment1 = PscTest::Fixtures.getStudySegmentFromStudy(@study1, 0, 0)      
    @date = PscTest.createDate(2008, 12, 26)   
    @user = PscTest::Fixtures.createSubjectCoordinatorUser("mary", 1, 2000)
    application_context['userDao'].save(@user)

    #create a study subject assignment
    @studySubjectAssignment1 = application_context['subjectService'].assignSubject(@subject1, @studySite1, @studySegment1, @date, "ID001", @user)
    @studySubjectAssignment1.grid_id = "assignment1" #replace auto-generated assignment-id
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment1)
    
  end
  
  describe "GET" do
     
     before do
       
       #create another subject under the same study
       @birthDate2 = PscTest.createDate(1985, 5, 1)           
       @subject2 = PscTest::Fixtures.createSampleFemaleSubject("ID002", "Amanda", "Boyarski", @birthDate2)         
       @studySubjectAssignment2 = application_context['subjectService'].assignSubject(@subject2, @studySite1, @studySegment1, @date, "ID002", @user)
       @studySubjectAssignment2.grid_id = "assignment2" #replace auto-generated assignment-id
       application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment2)
            
     end
     
     
    it "forbids access to a scheduled study segment of a given assignment to an unauthorized user" do
      get "/studies/NU480/schedules/assignment1", :as => nil
      response.status_code.should == 401
    end
    
    it "allows access to scheduled study segment of a given assignment to an authorized user" do
      get "/studies/NU480/schedules/assignment1", :as => :juno
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      xml_attribute("scheduled-calendar", "assignment-id").should include("assignment1")
      xml_attribute("scheduled-study-segment", "study-segment-id").should include("segment1")
      response.xml_elements('//scheduled-study-segment').should have(1).elements          
    end

  end
  
  
  describe "POST" do
    
    before do
    
      #xml request to add the study-segment next schedule
      @next_assignment1_xml = psc_xml("next-scheduled-study-segment", 'start-day' => 2, 'start-date' => "2008-12-27", 
      'study-segment-id' => "segment2", 'mode' => "immediate")     
    end
        
    it "allows scheduling a new study segment for an authorized user" do
      post "/studies/NU480/schedules/assignment1", @next_assignment1_xml, :as => :juno
      # puts response.entity
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      xml_attribute("scheduled-study-segment", "study-segment-id").should include("segment2")      
      
      #there should be 2 scheduled-study-segments
      get "/studies/NU480/schedules/assignment1", :as => :juno
      response.xml_elements('//scheduled-study-segment').should have(2).elements    
      xml_attribute("scheduled-study-segment", "study-segment-id").should include("segment2")
      xml_attribute("scheduled-study-segment", "study-segment-id").should include("segment1")      
    end
        
  end
          
end