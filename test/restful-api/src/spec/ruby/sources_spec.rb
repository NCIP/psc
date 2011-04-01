describe "/activities" do
  describe "GET" do
    it "forbids access to sources for an unauthorized user" do
      get '/activities', :as => :zelda
      response.status_code.should == 403
    end

    it "allows access to sources for an authorized user" do
      get '/activities', :as => :juno

      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("source", "name").should include("Northwestern University")
    end

    it "allows access to a specific group of activity(s) by specifying a query parameter" do
      get '/activities?q=x-ray', :as => :juno

      response.status_code.should == 200
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("X-Ray: Skeletal")
      response.xml_elements('//activity').should have(3).elements
    end
  end
end

describe "json" do
    it "/activities.json" do
      get "/activities.json?limit=100&source=Northwestern%20University", :as => :juno, 'Content-Type' => 'application/json'
      response.status_code.should == 200
      response.status_message.should == "OK"
    end

    it "is JSON" do
      get "/activities.json?limit=100&source=Northwestern%20University", :as => :juno, 'Content-Type' => 'application/json'
      response.content_type.should == 'application/json'
    end

    it "has the right number of activities with pagination" do
      get "/activities.json?limit=100&source=Northwestern%20University", :as => :juno, 'Content-Type' => 'application/json'
      response.json["activities"].size.should == 100
    end

    it "[activities][each activity] contains activity_id, activity_code, activity_name" do
      get "/activities.json?limit=100&source=Northwestern%20University", :as => :juno, 'Content-Type' => 'application/json'
      response.json["activities"][0]["activity_id"].should_not be_nil
      response.json["activities"][0]["activity_name"].should_not be_nil
      response.json["activities"][0]["activity_code"].should_not be_nil
    end

    it "has the right number of activity_types" do
      get "/activities.json?limit=100&source=Northwestern%20University", :as => :juno, 'Content-Type' => 'application/json'
      response.json["activity_types"].size.should == 5
    end

    it "deletes one activity" do
      delete "/activities/Northwestern%20University/2", :as => :juno, 'Content-Type' => 'application/json'
      application_context['activityDao'].get_by_name("506U78").should == nil
    end

    it "adds one new activity" do
      get "/activities/Northwestern%20University/5000", :as => :juno, 'Content-Type' => 'application/json'
      response.status_code.should == 404
      @activity_xml = psc_xml("Activity", 'type' => "DiabetesTreatment", 'name' => "Testing new activity", 'code' => "5000", 'source' => "Northwestern University")
      put '/activities/Northwestern%20University/5000', @activity_xml, :as => :juno
      application_context['activityDao'].get_by_name("Testing new activity").should_not be_nil
    end
end
