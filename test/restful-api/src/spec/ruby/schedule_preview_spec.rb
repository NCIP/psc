describe "/schedulePreview" do
  before do
      #create study with an amendment
      @study = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["segment_A", "segment_B"].to_java(:String)) 
      @amendment = PscTest::Fixtures.createAmendment("Amendment", PscTest.createDate(2008, 12, 10)) 
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
      
      @studySegment = @study.plannedCalendar.epochs.first.studySegments.first
   end
  describe "GET" do
    it "returns scheduled calednar for current amendment" do
      get "/studies/NU480/template/current/schedulePreview?segment%5B0%5D=#{@studySegment.gridId}&start_date%5B0%5D=2009-05-04", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_elements('//scheduled-activity').size.should == 4
      puts response.entity
    end
    it "returns scheduled calednar in json format" do
      get "/studies/NU480/template/current/schedulePreview.json?segment%5B0%5D=#{@studySegment.gridId}&start_date%5B0%5D=2009-05-04", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'application/json'
      puts response.entity
      response.json["2009-05-07"][0]["activity"].should include("Initial Diagnosis")
      response.json["2009-05-07"][0]["studySegment"].should include("Treatment: segment_A")
    end
    it "returns 400 for unparsable date" do
      get "/studies/NU480/template/current/schedulePreview.json?segment%5B0%5D=#{@studySegment.gridId}&start_date%5B0%5D=200905-04", :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end
    it "returns 400 for no pair for segment & date" do
      get "/studies/NU480/template/current/schedulePreview.json?segment%5B0%5D=#{@studySegment.gridId}", :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end
    it "returns 404 for no study exist" do
      get "/studies/NoStudy/template/current/schedulePreview.json?segment%5B0%5D=#{@studySegment.gridId}", :as => :erin
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
    
  end   
  
end