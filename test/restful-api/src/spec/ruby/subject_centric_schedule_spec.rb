describe "/subject_centric_schedule" do
  before do
    begin
    #create site
    @site = PscTest::Fixtures.createSite("TestSite", "site")
    application_context['siteDao'].save( @site)

    #create two studies with an amendment
    @studies = [
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["segment_A", "segment_B"].to_java(:String)), 
      @study2 = PscTest::Fixtures.createSingleEpochStudy("ECOG170", "FollowUp", ["segment_A", "segment_B"].to_java(:String))
    ]    
    @amendment1 = PscTest::Fixtures.createAmendment("am1", PscTest.createDate(2008, 12, 10)) 
    @amendment2 = PscTest::Fixtures.createAmendment("am2", PscTest.createDate(2008, 12, 15))
    @study1.amendment = @amendment1 
    @study2.amendment = @amendment2
    @studies.each do |s|
      application_context['studyService'].save(s)
    end
    
    #create period and link to studies
    @periods = [
      @period1 = PscTest::Fixtures.createPeriod("Period1", 2, 4, 2),
      @period2 = PscTest::Fixtures.createPeriod("Period2", 2, 3, 1)
    ]
    @periods.each do |p|
      application_context['periodDao'].save(p) 
    end
    @study1.plannedCalendar.epochs.first.studySegments.first.addPeriod(@period1)
    @study2.plannedCalendar.epochs.first.studySegments.first.addPeriod(@period2)
  
    #create planned activity and link to studies
    @source = PscTest::Fixtures.createSource("Malaria")
    application_context['sourceDao'].save(@source)
    @activityType = PscTest::Fixtures.createActivityType("Malaria Treatment")
    application_context['activityTypeDao'].save(@activityType)
    @activity = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source, @activityType, "Stage 1 diagnosis for malaria")
    application_context['activityDao'].save(@activity)
    @planned_activities = [
       @planned_activity1 = PscTest::Fixtures.createPlannedActivity(@activity, 4),
       @planned_activity2 = PscTest::Fixtures.createPlannedActivity(@activity, 2)
    ]
    @planned_activities.each do |pa|
      application_context['plannedActivityDao'].save(pa)
    end
    @period1.addPlannedActivity(@planned_activity1)
    @period2.addPlannedActivity(@planned_activity2)
   
    @studies.each do |s|
       application_context['studyService'].save(s)
    end
    
    #create a studysites
    @studySites = [       
       @studySite1 = PscTest::Fixtures.createStudySite(@study1, @site),
       @studySite2 = PscTest::Fixtures.createStudySite(@study2, @site)
    ] 
    @studySites.each do |ss|
       application_context['studySiteDao'].save(ss)
    end
    
    #approve an existing amendment
    @approve_date = PscTest.createDate(2008, 12, 20)
    @studySite1.approveAmendment(@amendment1, @approve_date)
    @studySite2.approveAmendment(@amendment2, @approve_date)
    application_context['studySiteDao'].save(@studySite1)
    application_context['studySiteDao'].save(@studySite2)    
    
    #create subject                
    @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23)) 
    
    #Assign studies to the subject coordinator
    application_context['templateService'].assignTemplateToSubjectCoordinator(@study1, @site, erin)
    application_context['templateService'].assignTemplateToSubjectCoordinator(@study2, @site, erin)
            
    #create a study subject assignment
    @studySegment1 = @study1.plannedCalendar.epochs.first.studySegments.first
    @studySegment2 = @study2.plannedCalendar.epochs.first.studySegments.first
    @studySubjectAssignment1 = application_context['subjectService'].assignSubject(@subject, @studySite1, @studySegment1, PscTest.createDate(2008, 12, 26) , "SS001", erin)
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment1)
    @studySubjectAssignment2 = application_context['subjectService'].assignSubject(@subject, @studySite2, @studySegment2, PscTest.createDate(2008, 12, 28) , "SS002", erin)
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment2)
    
    @scheduled_activity1 = @studySubjectAssignment1.scheduledCalendar.scheduledStudySegments.first.activities.first
    @scheduled_activity2 = @studySubjectAssignment2.scheduledCalendar.scheduledStudySegments.first.activities.first
    rescue Exception => e
        application_context['databaseInitializer'].afterEach
        raise e
    end
  end
  describe "GET" do
        
    it "allows to get schedule for subject in xml representation to an authorized user" do
      get "/schedules/ID001", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("subject-assignment", "site-name").should include("TestSite")
      response.xml_attributes("subject-assignment", "study-name").should include("NU480")
      response.xml_attributes("subject-assignment", "study-name").should include("ECOG170")
      
    end
    
    it "allows to get schedule for subject in JSON representation to an authorized user" do
      get "/schedules/ID001.json", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'application/json'
      response.json["2008-12-29"][0]["activity"].should include("Initial Diagnosis")
      response.json["2008-12-29"][0]["study"].should include("NU480")
      response.json["2008-12-29"][1]["study"].should include("ECOG170")
      response.json["2008-12-29"][0]["studySegment"].should include("Treatment: segment_A")
      response.json["2008-12-29"][1]["studySegment"].should include("FollowUp: segment_A")
      
    end
    
    it "doesn't give schedule for invalid subject" do
      get "/schedules/ID002", :as => :erin  
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
    
  end
  
  describe "POST" do
    
    it "updates the scheduled activity states for batch activities" do
      @JSONentity = "{#{@scheduled_activity1.gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-30 },
                      #{@scheduled_activity2.gridId} : { state : canceled, reason : Just canceled , date : 2009-12-29 }}"
      post "/schedules/ID001/batchUpdate", @JSONentity, 
        :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activity1.gridId]["Status"] == 201
      response.json[@scheduled_activity2.gridId]["Status"] == 201
      response.json[@scheduled_activity1.gridId]["Location"] =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment1.gridId}/activities/#{@scheduled_activity1.gridId}$}
      response.json[@scheduled_activity2.gridId]["Location"] =~ %r{api/v1/studies/ECOG170/schedules/#{@studySubjectAssignment2.gridId}/activities/#{@scheduled_activity2.gridId}$}
    end
    
    it "updates scheduled activity state for one activity and send 400 - Bad Request for another activity with incorrect state in request" do
      @JSONentity = "{#{@scheduled_activity1.gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-30 },
                      #{@scheduled_activity2.gridId} : { state : canceledd, reason : Just canceled , date : 2009-12-29 }}"
      post "/schedules/ID001/batchUpdate", @JSONentity,
            :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activity1.gridId]["Status"] == 201
      response.json[@scheduled_activity2.gridId]["Status"] == 400
      response.json[@scheduled_activity1.gridId]["Location"] =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment1.gridId}/activities/#{@scheduled_activity1.gridId}$}
    end
              
    it "updates scheduled activity state for one activity and send 400 - Bad Request for another activity with incorrect date in request" do
      @JSONentity = "{#{@scheduled_activity1.gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-30 },
                      #{@scheduled_activity2.gridId} : { state : canceled, reason : Just canceled , date : 2009 }}"
      post "/schedules/ID001/batchUpdate", @JSONentity,
            :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activity1.gridId]["Status"] == 201
      response.json[@scheduled_activity2.gridId]["Status"] == 400
      response.json[@scheduled_activity1.gridId]["Location"] =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment1.gridId}/activities/#{@scheduled_activity1.gridId}$}
    end
  end
end
