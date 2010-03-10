describe "/reports/scheduled-activities.json" do

  before do
    #create site
    @site = PscTest::Fixtures.createSite("TestSite", "site")
    application_context['siteDao'].save( @site)

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
    @studySite = PscTest::Fixtures.createStudySite(@study, @site)
    application_context['studySiteDao'].save(@studySite)

    #approve an existing amendment
    @approve_date = PscTest.createDate(2008, 12, 20)
    @studySite.approveAmendment(@amendment, @approve_date)
    application_context['studySiteDao'].save(@studySite)

    #create subject
    @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))

    #Assign study to the subject coordinator
    application_context['templateService'].assignTemplateToSubjectCoordinator(@study, @site, erin)

    #create a study subject assignment
    @studySegment = @study.plannedCalendar.epochs.first.studySegments.first
    @studySubjectAssignment = application_context['subjectService'].assignSubject(@subject, @studySite, @studySegment, PscTest.createDate(2008, 12, 26) , "SS001", erin)
    application_context['studySubjectAssignmentDao'].save(@studySubjectAssignment)

    #get Scheduled activity
    @scheduled_activity1 = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities.first
    @scheduled_activity2 = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities
  end


  describe "GET" do
    it "returns all sources without parameters" do
      get '/reports/scheduled-activities.json', :as => :alice
      response.status_code.should == 200
    end
  end

  describe "json with param responsible-user" do
    before do
      get "/reports/scheduled-activities.json?responsible-user=1", :as => :alice
    end
    it "has a right value for filter responsible-user" do
      response.json["filters"]["responsible_user"].should == "juno"
    end
  end

  describe "json with param end-date" do
    before do
      get "/reports/scheduled-activities.json?end-date=2010-03-05", :as => :alice
    end
    it "has a right value for filter end_date" do
      response.json["filters"]["end_date"].should == "2010-03-05"
    end
  end

  describe "json with param start-date" do
    before do
      get "/reports/scheduled-activities.json?start-date=2010-03-02", :as => :alice
    end
    it "has a right value for filter start_date" do
      response.json["filters"]["start_date"].should == "2010-03-02"
    end
  end

  describe "json with param label" do
    before do
      get "/reports/scheduled-activities.json?label=a", :as => :alice
    end
    it "has a right value for the filter label" do
      response.json["filters"]["label"].should == "a"
    end
  end

  describe "json with param study" do
    before do
      get "/reports/scheduled-activities.json?study=a", :as => :alice
    end
    it "has a right value for filter study" do
      response.json["filters"]["study"].should == "a"
    end
  end

  describe "json with param site" do
    before do
      get "/reports/scheduled-activities.json?site=1", :as => :alice
    end
    it "has a right value for filter site" do
      response.json["filters"]["site"].should == "1"
    end
  end

  describe "json with param state" do
    before do
      get "/reports/scheduled-activities.json?state=2", :as => :alice
    end
    it "has a right value for filter state" do
      response.json["filters"]["state"].should == "Occurred"
    end
  end

  describe "json with param responsible-user" do
    before do
      get "/reports/scheduled-activities.json?start-date=2008-12-28&end-date=2009-03-01", :as => :alice
    end
    it "contains the right number of filters" do
      response.json["filters"].size.should == 2
    end
    it "has a right values for filter start_date and end_date" do
      response.json["filters"]["start_date"].should == "2008-12-28"
      response.json["filters"]["end_date"].should == "2009-03-01"
    end
    it "has the right number of rows" do
      response.json["rows"].size.should == 4
    end
  end
end