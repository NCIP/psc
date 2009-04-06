describe "/source" do
    
  describe "GET" do
    
    before do
      @source = PscTest::Fixtures.createSource("Cancer")
      application_context['sourceDao'].save(@source)
      @activityType = PscTest::Fixtures.createActivityType("CancerTreatment")
      application_context['activityTypeDao'].save(@activityType)     
      @activity = PscTest::Fixtures.createActivity("CancerTreatment1", "Code1", @source, @activityType)
      application_context['activityDao'].save(@activity)
      
      # create another activity under the same source
      application_context['sourceDao'].save(@source)
      @activityType1 = PscTest::Fixtures.createActivityType("EmergencyTreatment")
      application_context['activityTypeDao'].save(@activityType1)     
      @activity1 = PscTest::Fixtures.createActivity("CancerEmergencyTreatment1", "Code2", @source, @activityType1)
      application_context['activityDao'].save(@activity1)
    end
    
    it "forbids access to sources for an unauthorized user" do
      get '/activities/Cancer', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end    
    
    it "allows access to activities under a specific source for an authorized user" do
      get '/activities/Cancer', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("CancerTreatment1")
      response.xml_attributes("activity", "name").should include("CancerEmergencyTreatment1")
      response.xml_elements('//activity').should have(2).elements
    end
    
    it "allows access to a specific group of activity(s) under a particular source by specifying a query parameter" do
      get '/activities/Cancer?q=emergency', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("CancerEmergencyTreatment1")
      response.xml_elements('//activity').should have(1).elements
    end
    
  end
  
  describe "PUT" do
    before do
      @source_xml = psc_xml("source", 'name' => "Dental")
      @source1_xml = psc_xml("source", 'name' => "Dental") { |s|
        s.tag!('activity', 'name' => "Dental Surgery", 'code' => "DS", 'type' => "High-risk")}
    end
    
    it "prevents creation of a non-existing source for unauthorized user" do
      put '/activities/Dental', @source_xml, :as => nil
      response.status_code.should == 401
    end
    
    it "allows creation of a non-existing source with zero activity" do
      put '/activities/Dental', @source_xml, :as => :juno 
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      response.xml_attributes("source", "name").should include("Dental")
      response.xml_elements('//activity').should have(0).elements
    end      
    
  end
    
end