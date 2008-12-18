
# Resource:
# /studies/{study-identifier}/template/{amendment-identifier}/epochs/{epoch-name}/study-segments/
# {segment-name}/periods/{period-identifier}/planned-activities/{planned-activity-identifier}

describe "/planned-activity" do
  
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end
  
  before do 
    #create study
    @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
    @study1.planned_calendar.epochs.first.study_segments[0].grid_id = "segment1" #replace auto-generated study-segment id
    @study1.planned_calendar.epochs.first.study_segments[1].grid_id = "segment2"
      
    #create amendment and link to study
    @amend_date1 = PscTest.createDate(2008, 12, 10)      
    @amendment = PscTest::Fixtures.createAmendment("am1", @amend_date1, true)
    @study1.amendment = @amendment
    application_context['studyService'].save(@study1)
    
    #create period and link to study
    @period1 = PscTest::Fixtures.createPeriod("Period1", 2, 4, 5)
    @period1 = PscTest::Fixtures.setGridId("10001", @period1) #replace auto-generated period id
    application_context['periodDao'].save(@period1) 
    @study = PscTest::Fixtures.addPeriodToStudySegmentOfStudy(@study1, 0, 0, @period1)     
    application_context['studyService'].save(@study)
    
    #create planned activity and link to study
    @source1 = PscTest::Fixtures.createSource("Malaria")
    application_context['sourceDao'].save(@source1)
    @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
    application_context['activityTypeDao'].save(@activityType1)
    @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
    application_context['activityDao'].save(@activity1)
    @planned_activity1 = PscTest::Fixtures.createPlannedActivity(@activity1, 4)
    @planned_activity1 = PscTest::Fixtures.setGridId("301", @planned_activity1) #replace auto-generated planned-activity id
    application_context['plannedActivityDao'].save(@planned_activity1) 
    @study = PscTest::Fixtures.addPlannedActivityToStudySegmentOfStudy(@study1, 0, 0, "10001", @planned_activity1)
    application_context['studyService'].save(@study)
    
  end
  
  describe "GET" do 
    
    it "shows a study snapshot" do
      pending
      # code below display a study-snapshot
      get "/studies/NU480/template/2008-12-10~am1", :as => :juno
      puts response.entity
    end
    
    it "allows access to planned activity" do
      pending
      get "/studies/NU480/template/2008-12-10~am1/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", :as => :juno
      puts response.entity
    end
  
  end
  
  describe "DELETE" do
    
    it "shows a study snapshot" do
      #VERIFY BEFORE DELETE: 
      get "/studies/NU480/template/development", :as => :juno
      # puts response.entity #code displaying a study-snapshot before delete
      xml_attribute("planned-activity", "id").should include("301")
      response.xml_elements('//planned-activity').should have(1).elements      
      
      #DELETE               
      delete "/studies/NU480/template/development/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", :as => :juno
      response.status_code.should == 204
      
      # code below should be used instead for verification but the code causes Hibernate lazy initialization problem
      # application_context['plannedActivityDao'].getByGridId("301").should == nil
      
      #VERIFY AFTER DELETE: 
      get "/studies/NU480/template/development", :as => :juno
      # puts response.entity
      response.xml_elements('//planned-activity').should have(0).elements          
    end
        
  end
  
  describe "PUT" do
    
      before do
        #create an activity to be linked to planned activity
        @activity2 = PscTest::Fixtures.createActivity("Immunization", "treat2", @source1, @activityType1, "Stage 2 treatment for malaria")
        application_context['activityDao'].save(@activity2)
        @data = 'day=2&activity=Immunization&activity-code=treat2&activity-source=Malaria&condition=OK&details=No%20detail&label='
      end
    
      it "modifies an existing planned activity" do       
        #VERIFY BEFORE PUT: 
        get "/studies/NU480/template/development", :as => :juno
        # puts response.entity #code displaying a study-snapshot before put
        response.xml_elements('//activity').should have(1).elements
        xml_attribute("activity", "name").should include("Initial Diagnosis")
               
        #PUT
        put "/studies/NU480/template/development/epochs/Treatment/study-segments/A/periods/10001/planned-activities/301", @data, 
        :as => :juno, 'Content-Type' => "application/x-www-form-urlencoded"
        response.status_code.should == 200
        
        #VERIFY AFTER PUT: 
        get "/studies/NU480/template/development", :as => :juno
        # puts response.entity #code displaying a study-snapshot after put
        response.xml_elements('//activity').should have(1).elements  
        xml_attribute("activity", "name").should include("Immunization")
        
      end
    
  end
  
  
end