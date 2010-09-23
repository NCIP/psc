describe "/schedule" do

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
    @subject1 = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", @birthDate)
    @studySegment1 = @study1.plannedCalendar.epochs.first.studySegments.first
    @date = PscTest.createDate(2008, 12, 26)

    #create a study subject assignment
    @studySubjectAssignment1 = application_context['subjectService'].assignSubject(@subject1, @studySite1, @studySegment1, @date, "ID001", erin)
    @studySubjectAssignment1.grid_id = "assignment1" #replace auto-generated assignment-id
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment1)

  end

  describe "GET" do

     before do

       #create another subject under the same study
       @birthDate2 = PscTest.createDate(1985, 5, 1)
       @subject2 = PscTest::Fixtures.createSubject("ID002", "Bob", "Boyarski", @birthDate2)
       @studySubjectAssignment2 = application_context['subjectService'].assignSubject(@subject2, @studySite1, @studySegment1, @date, "ID002", erin)
       @studySubjectAssignment2.grid_id = "assignment2" #replace auto-generated assignment-id
       application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment2)

     end


    it "forbids access to a scheduled study segment of a given assignment to an unauthorized user" do
      get "/studies/NU480/schedules/assignment1", :as => nil
      response.status_code.should == 401
    end

    it "allows access to scheduled study segment of a given assignment to an authorized user" do
      get "/studies/NU480/schedules/assignment1", :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("scheduled-calendar", "assignment-id").should include("assignment1")
      response.xml_attributes("scheduled-study-segment", "study-segment-id").should include("segment1")
      response.xml_elements('//scheduled-study-segment').should have(1).elements
    end

  end


  describe "POST" do

    before do

      #xml request to add the study-segment next schedule
      @next_assignment1_xml = psc_xml("next-scheduled-study-segment", 'start-day' => 2, 'start-date' => "2008-12-27",
      'study-segment-id' => "segment2", 'mode' => "immediate")
      @hibernate.interrupt_session
    end

    it "allows scheduling a new study segment for an authorized user" do
      post "/studies/NU480/schedules/assignment1", @next_assignment1_xml, :as => :juno
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      response.xml_attributes("scheduled-study-segment", "study-segment-id").should include("segment2")

      #there should be 2 scheduled-study-segments
      get "/studies/NU480/schedules/assignment1", :as => :juno
      response.xml_elements('//scheduled-study-segment').should have(2).elements
      response.xml_attributes("scheduled-study-segment", "study-segment-id").should include("segment2")
      response.xml_attributes("scheduled-study-segment", "study-segment-id").should include("segment1")
    end

    it "doesn't allow to schedule unmatched studysegment for study" do
      @study2 = PscTest::Fixtures.createSingleEpochStudy("NU481", "Treatment", ["segment_A", "segment_B"].to_java(:String))
      @amend_date2 = PscTest.createDate(2008, 12, 10)
      @amendment2 = PscTest::Fixtures.createAmendment("am2", @amend_date2)
      @study2.planned_calendar.epochs.first.study_segments[0].grid_id = "s1"
      @study2.amendment = @amendment2
      application_context['studyService'].save(@study2)

      @next_assignment2_xml = psc_xml("next-scheduled-study-segment", 'start-day' => 2, 'start-date' => "2008-12-27",
      'study-segment-id' => "s1", 'mode' => "immediate")
      post "/studies/NU480/schedules/assignment1", @next_assignment2_xml, :as => :juno
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end

    it "gives 400 if studySegment with gridId doesn't exists in system" do
      @next_assignment2_xml = psc_xml("next-scheduled-study-segment", 'start-day' => 2, 'start-date' => "2008-12-27",
      'study-segment-id' => "unknownSegment", 'mode' => "immediate")
      post "/studies/NU480/schedules/assignment1", @next_assignment2_xml, :as => :juno
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
      response.entity =~ %r(Segment with grid Identifier unknownSegment not found.)
    end
  end
end
