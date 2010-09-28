describe "/activities" do
  describe "GET" do
    it "forbids access to sources for an unauthorized user" do
      get '/activities', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end

    it "allows access to sources for an authorized user" do
      get '/activities', :as => :juno

      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("source", "name").should include("Northwestern University")
    end

    it "allows access to a specific group of activity(s) by specifying a query parameter" do
      get '/activities/?q=x-ray', :as => :juno

      response.status_code.should == 200
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("X-Ray: Skeletal")
      response.xml_elements('//activity').should have(3).elements
    end
end
