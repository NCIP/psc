describe "/subject_assignments" do
  
  #ISSUE:
  #1) post /studies/{study-identifier}/sites/{site-identifier}/subject-assignments returns error:
  #   Authenticated account is not authorized for this resource and method
  #   Need to call application_context['templateService'].assignTemplateToSubjectCoordinator in order for POST to succeed
  #   but encounter Hibernate lazy initialization issue
  
  before do
    #create a study with an amendment
    @nu480 = PscTest::Fixtures.createBasicTemplate("NU480")
    (@treatmentA, @treatmentB) = [*@nu480.planned_calendar.epochs.first.study_segments]
    @treatmentA.grid_id = "segment1" # replace auto-generated study-segment id
    @treatmentB.grid_id = "segment2"
    application_context['studyService'].save(@nu480)

    # Make template available to Pittsburgh
    @nu480_at_pitt = PscTest::Fixtures.createStudySite(@nu480, pittsburgh)
    @nu480_at_pitt.approveAmendment(@nu480.amendment, PscTest.createDate(2008, 12, 31))
    application_context['studySiteDao'].save(@nu480_at_pitt)
  end
  
  describe "GET" do
    before do
      # create subject              
      @alan = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
      @bob = PscTest::Fixtures.createSubject("ID002", "Bob", "Boyarski", PscTest.createDate(1985, 5, 1))         

      # register alan
      application_context['subjectService'].assignSubject(
        @alan, @nu480_at_pitt, @treatmentA, PscTest.createDate(2008, 12, 26), "ID001", erin)
       
      # register bob
      application_context['subjectService'].assignSubject(
        @bob, @nu480_at_pitt, @treatmentB, PscTest.createDate(2008, 1, 13), "ID002", erin)
    end

    it "forbids access to a subject assignment to an unauthorized user" do
      get "/studies/NU480/sites/PA015/subject-assignments", :as => :carla
      response.status_code.should == 403
    end
    
    it "allows access to an existing subject assignment to an authorized user" do
      get "/studies/NU480/sites/PA015/subject-assignments", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("subject", "first-name").should include("Bob")
      response.xml_attributes("subject", "first-name").should include("Alan")
      response.xml_attributes("subject", "person-id").should include("ID001")
      response.xml_attributes("subject", "person-id").should include("ID002")
      response.xml_elements('//subject').should have(2).elements          
    end
  end
  
  describe "POST" do
    before do
      @subject_registration_xml = psc_xml(
        "registration", 'first-study-segment-id' => "segment1", 'date' => "2008-12-27",
        'subject-coordinator-name' => "juno"
      ) { |subject|
        subject.tag!('subject',
            'first-name' => "Andre", 'last-name' => "Suzuki",
            'birth-date' => "1982-03-12", 'person-id' => "ID006", 'gender'=> "Male")
      }
    end
    
    it "does not allow the study coordinator to assign patients when the template has not been made available" do
      pending
      post "/studies/NU480/sites/PA015/subject-assignments", @subject_registration_xml, :as => :erin
      puts response.entity
      response.status_code.should == 403
    end
    
    it "allows creation of a new subject-assignment for an authorized user" do
      application_context['templateService'].assignTemplateToSubjectCoordinator(@nu480, pittsburgh, erin)
      post "/studies/NU480/sites/PA015/subject-assignments", @subject_registration_xml, :as => :erin
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.meta['location'].should =~ %r(studies/NU480/schedules/[^/]+)
    end
  end
          
end