describe "/studies/{study-identifier}/schedules/{assignment-identifier}/activities/{scheduled-activity-identifier}" do
  before do
      #create study with an amendment
      @study = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["segment_A", "segment_B"].to_java(:String))
      @amendment = PscTest::Fixtures.createAmendment("am", PscTest.createDate(2008, 12, 10))
      @study.amendment = @amendment
      application_context['studyService'].save( @study)

      #create period and link to study
      @period = PscTest::Fixtures.createPeriod("Period", 2, 4, 2)
      application_context['periodDao'].save(@period)
      @study.plannedCalendar.epochs.first.studySegments.first.addPeriod(@period)

      #create planned activity and link to study
      @source = PscTest::Fixtures.createSource("Malaria")
      application_context['sourceDao'].save(@source)
      @activityType = PscTest::Fixtures.createActivityType("Malaria Treatment")
      application_context['activityTypeDao'].save(@activityType)
      @activity = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source, @activityType, "Stage 1 diagnosis for malaria")
      application_context['activityDao'].save(@activity)
      @planned_activity = PscTest::Fixtures.createPlannedActivity(@activity, 4)
      @planned_activity1 = PscTest::Fixtures.createPlannedActivity(@activity, 4)
      application_context['plannedActivityDao'].save(@planned_activity)
      application_context['plannedActivityDao'].save(@planned_activity1)
      @period.addPlannedActivity(@planned_activity)
      @period.addPlannedActivity(@planned_activity1)
      application_context['studyService'].save(@study)

      #create a studysite
      @studySite = PscTest::Fixtures.createStudySite(@study, northwestern)
      application_context['studySiteDao'].save(@studySite)

      #approve an existing amendment
      @approve_date = PscTest.createDate(2008, 12, 20)
      @studySite.approveAmendment(@amendment, @approve_date)
      application_context['studySiteDao'].save(@studySite)

      #create subject
      @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))

      #create a study subject assignment
      @studySegment = @study.plannedCalendar.epochs.first.studySegments.first
      @studySubjectAssignment = application_context['subjectService'].assignSubject(
        @studySite,
        Psc::Service::Presenter::Registration::Builder.new.
          subject(@subject).first_study_segment(@studySegment).
          date(PscTest.createDate(2008, 12, 26)).manager(erin).
          to_registration)

      #get Scheduled activity
      @scheduled_activity = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities.first
   end

  describe "GET" do
    it "returns all the scheduled activities for particular date" do
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/on/2008/12/29", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_elements('//scheduled-activity').should have(2).elements
      response.xml_attributes("scheduled-activity", "ideal-date").should include("2008-12-29")
    end
  
    it "returns the details for the scheduled activity" do
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
    end
  
    it "forbids access to a scheduled activity to an unauthorized user" do
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}", :as => nil
      response.status_code.should == 401
    end
  end

  describe "POST" do
    it "updates the scheduled activity state with XML request" do
      #xml request to update scheduled activity state
      @scheduled_activity_state_xml = psc_xml("scheduled-activity-state", 'state' => "canceled", 'date' => "2008-12-29", 'reason' => "Just canceled")
    
      post "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}", @scheduled_activity_state_xml, :as => :erin
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.meta['location'].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}$}
    end
    
    it "updates the scheduled activity state with JSON request" do
      #json request to update scheduled activity state
      @scheduled_activity_state_json = "{#{@scheduled_activity.gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-30 }}"
    
      post "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}", @scheduled_activity_state_json,
           :as => :erin, 'Content-Type' => 'application/json'
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.meta['location'].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}$}
    end
    
    it "updates the scheduled activity state with time with JSON request" do
      #json request to update scheduled activity state
      @scheduled_activity_state_json = "{#{@scheduled_activity.gridId} : { state : scheduled, reason : Delay by two days and change the time , date : 2009-12-30, time : '14:20' }}"
      post "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}", @scheduled_activity_state_json,
           :as => :erin, 'Content-Type' => 'application/json'
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.meta['location'].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}$}
      
      sa = application_context['scheduledActivityDao'].getByGridId(@scheduled_activity.gridId)
      sa.currentState.withTime.should == true
      sa.currentState.date.to_s.should == "2009-12-30 14:20:00.0"
    end
  end

end
