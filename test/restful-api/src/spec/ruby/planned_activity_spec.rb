
# Resource:
# /studies/{study-identifier}/template/{amendment-identifier}/epochs/{epoch-name}/study-segments/
# {segment-name}/periods/{period-identifier}/planned-activities/{planned-activity-identifier}

describe "/studies/{study-identifier}/template/{amendment-identifier}/epochs/{epoch-name}/study-segments/{segment-name}/periods/{period-identifier}/planned-activities/{planned-activity-identifier}" do

  before do
    #create study
    @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
    @study1.planned_calendar.epochs.first.study_segments[0].grid_id = "segment1" #replace auto-generated study-segment id
    @study1.planned_calendar.epochs.first.study_segments[1].grid_id = "segment2"

    #create amendment,Add period and link to study
    @amend_date = PscTest.createDate(2008,11,10)
    @am = PscTest::Fixtures.createAmendment("am",@amend_date,true)
    @study1.amendment = @am
    @period = PscTest::Fixtures.createPeriod("PeriodInAm", 3, 5, 6)
    @period = PscTest::Fixtures.setGridId("10002", @period) #replace auto-generated period id
    application_context['periodDao'].save(@period)
    @study1.plannedCalendar.epochs.first.study_segments[1].addPeriod(@period)
    application_context['studyService'].save(@study1)

    #create development amendment and link to study
    @amend_date1 = PscTest.createDate(2008, 12, 10)
    @amendment = PscTest::Fixtures.createInDevelopmentAmendment("am1", @amend_date1, true)
    @study1.developmentAmendment = @amendment
    application_context['studyService'].save(@study1)

    #create period and link to study
    @period1 = PscTest::Fixtures.createPeriod("Period1", 2, 4, 5)
    @period1 = PscTest::Fixtures.setGridId("10001", @period1) #replace auto-generated period id
    application_context['periodDao'].save(@period1)
    @study1.plannedCalendar.epochs.first.studySegments.first.addPeriod(@period1)
    application_context['studyService'].save(@study1)

    #create planned activity and link to study
    @source1 = PscTest::Fixtures.createSource("Malaria")
    application_context['sourceDao'].save(@source1)
    @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
    application_context['activityTypeDao'].save(@activityType1)
    @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
    application_context['activityDao'].save(@activity1)
    @planned_activity1 = PscTest::Fixtures.createPlannedActivity(@activity1, 4, 6)
    @planned_activity1 = PscTest::Fixtures.setGridId("301", @planned_activity1) #replace auto-generated planned-activity id
    application_context['plannedActivityDao'].save(@planned_activity1)
    @period1.addPlannedActivity(@planned_activity1)
    application_context['studyService'].save(@study1)

  end

  describe "GET" do

    # TODO: this is out of place
    it "shows a study snapshot" do
      # code below display a study-snapshot
      get "/studies/NU480/template/2008-11-10~am", :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("period","name").should include("PeriodInAm")
    end

    it "allows access to planned activity" do
      get "/studies/NU480/template/2008-11-10~am/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'application/x-www-form-urlencoded'
    end

  end

  describe "DELETE" do

    it "shows a study snapshot" do
      #VERIFY BEFORE DELETE:
      get "/studies/NU480/template/development", :as => :juno
      response.xml_attributes("planned-activity", "id").should include("301")
      response.xml_elements('//planned-activity').should have(1).elements

      #DELETE
      delete "/studies/NU480/template/development/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", :as => :juno
      response.status_code.should == 204

      # code below should be used instead for verification but the code causes Hibernate lazy initialization problem
      # application_context['plannedActivityDao'].getByGridId("301").should == nil

      #VERIFY AFTER DELETE:
      get "/studies/NU480/template/development", :as => :juno
      response.xml_elements('//planned-activity').should have(0).elements
    end

  end

  describe "PUT" do

      before do
        #create an activity to be linked to planned activity
        @activity2 = PscTest::Fixtures.createActivity("Immunization", "treat2", @source1, @activityType1, "Stage 2 treatment for malaria")
        application_context['activityDao'].save(@activity2)
        @data = 'day=2&activity=Immunization&activity-code=treat2&activity-source=Malaria&weight=0&condition=OK&details=No%20detail&label='
      end

      it "modifies an existing planned activity" do
        #VERIFY BEFORE PUT:
        get "/studies/NU480/template/development", :as => :juno
        response.xml_elements('//activity').should have(1).elements
        response.xml_attributes("activity", "name").should include("Initial Diagnosis")

        #PUT
        put "/studies/NU480/template/development/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", @data,
        :as => :juno, 'Content-Type' => "application/x-www-form-urlencoded"
        response.status_code.should == 200

        #VERIFY AFTER PUT:
        get "/studies/NU480/template/development", :as => :juno
        response.xml_elements('//activity').should have(1).elements
        response.xml_attributes("activity", "name").should include("Immunization")

      end

  end


  describe "POST" do
      before do
        @data = 'day=4&activity=Initial+Diagnosis&activity-code=diag1&activity-source=Malaria&weight=9&condition=&details=%20detail&label='
        get "/studies/NU480/template/development", :as => :juno

        response.xml_attributes("planned-activity", "id").should include("301")
        response.xml_attributes("planned-activity", "weight").should include("6")
        response.xml_elements('//planned-activity').should have(1).elements
      end

      it "modifies an existing planned activity" do
        #PUT
        put "/studies/NU480/template/development/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", @data,
        :as => :juno, 'Content-Type' => "application/x-www-form-urlencoded"

        response.status_code.should == 200

        #VERIFY AFTER PUT:
        get "/studies/NU480/template/development", :as => :juno
        response.xml_elements('//planned-activity').should have(1).elements
        response.xml_attributes("planned-activity", "weight").should include("9")

      end

  end


end
