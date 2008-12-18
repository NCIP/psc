describe "/sources" do
    
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end  
  
  
  describe "GET" do
    
    before do
      @source = PscTest::Fixtures.createSource("Diabetes")
      application_context['sourceDao'].save(@source)
      @activityType = PscTest::Fixtures.createActivityType("DiabetesTreatment")
      application_context['activityTypeDao'].save(@activityType)     
      @activity = PscTest::Fixtures.createActivity("DiabetesTreatment1", "Code1", @source, @activityType)
      application_context['activityDao'].save(@activity)
    end
    
    it "forbids access to sources for an unauthorized user" do
      get '/activities/', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end    
    
    it "allows access to sources for an authorized user" do
      get '/activities/', :as => :juno
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      xml_attribute("source", "name").should include("Diabetes")
    end
    
    it "allows access to a specific group of activity(s) by specifying a query parameter" do
      get '/activities/?q=Diabetes', :as => :juno
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      xml_attribute("activity", "name").should include("DiabetesTreatment1")
      response.xml_elements('//activity').should have(1).elements
    end
    
  end
  
  
end