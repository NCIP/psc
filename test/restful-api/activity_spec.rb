describe "/activity" do
  
  #ISSUE:
  #1. put /activities/{activity-source-name}/{activity-code} succeeds even when source does not exist yet
  #2. delete /activities/{activity-source-name}/{activity-code} fails with error: Authenticated account is not authorized for this resource & method
    
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end
 
 describe "PUT" do
    before do
      @source_xml = psc_xml("source", 'name' => "Diabetes")
      @activity_xml = psc_xml("activity", 'id' => "id1", 'type' => "DiabetesTreatment", 'type-id' => 23, 
      'name' => "DiabetesTreatment1", 'code' => "Code1", 'source' => "Diabetes") 
    end
 
    it "forbids activity creation for an unauthorized user" do
        put '/activities/Diabetes/', @source_xml, :as => :juno #creates source before creating activity
        put '/activities/Diabetes/Code1', @activity_xml, :as => nil #unauthorized user
        response.status_code.should == 401
        response.status_message.should == "Unauthorized"
        response.content_type.should == 'text/html'
    end
 
    it "forbids creation of a specific activity to an authorized user when source has not yet existed" do
        pending
        puts @activity_xml
        put '/activities/Diabetes/Code1', @activity_xml, :as => :juno
        response.status_code.should == 404
        response.status_message.should == "Not Found"
    end
 
 
    it "creates a specific activity for an authorized user when the source exists" do
        put '/activities/Diabetes/', @source_xml, :as => :juno #creates source before creating activity
        put '/activities/Diabetes/Code1', @activity_xml, :as => :juno #creates activity
        response.status_code.should == 201
        response.status_message.should == "Created"
        response.content_type.should == 'text/xml'
        xml_attribute("activity", "name").should include("DiabetesTreatment1")
        xml_attribute("activity", "type").should include("DiabetesTreatment")
        xml_attribute("activity", "source").should include("Diabetes")
        response.xml_elements('//activity').should have(1).elements
    end
    
  end
  
  describe "GET" do
      before do
        @source1 = PscTest::Fixtures.createSource("Malaria")
        application_context['sourceDao'].save(@source1)
        @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
        application_context['activityTypeDao'].save(@activityType1)
        @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
        application_context['activityDao'].save(@activity1)       
      end

      it "forbids access to a specific activity for an unauthorized user" do
        get '/activities/Malaria/diag1', :as => nil
        response.status_code.should == 401
        response.status_message.should == "Unauthorized"
        response.content_type.should == 'text/html'
      end

      it "allows access to a specific activity for an authorized user" do
        get '/activities/Malaria/diag1', :as => :juno
        # puts response.entity
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.content_type.should == 'text/xml'
        xml_attribute("activity", "name").should include("Initial Diagnosis")
        xml_attribute("activity", "type").should include("Malaria Treatment")
        xml_attribute("activity", "source").should include("Malaria")
        response.xml_elements('//activity').should have(1).elements
      end
    end
    
    describe "DELETE" do
      before do
        @source1 = PscTest::Fixtures.createSource("Malaria")
        application_context['sourceDao'].save(@source1)
        @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
        application_context['activityTypeDao'].save(@activityType1)
        @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
        application_context['activityDao'].save(@activity1)      
      end
      
      it "deletes a specific activity" do
        pending
        delete '/activities/Malaria/diag1', :as => :juno
        puts response.entity
      end
    end
    
    
    
  
end