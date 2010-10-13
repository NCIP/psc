describe "/activities/{source-name}" do

  describe "GET" do
    it "forbids access to sources for an unauthorized user" do
      get '/activities/Northwestern%20University', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end

    it "allows access to activities under a specific source for an authorized user" do
      get '/activities/Northwestern%20University', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("Atra")
      response.xml_attributes("activity", "name").should include("Copper")
      response.xml_attributes("activity", "name").should include("PET Scan")
      expected_count = application_context["sourceDao"].
        getByName("Northwestern University").activities.size
      response.xml_elements('//activity').should have(expected_count).elements
    end

    it "allows access to a specific group of activity(s) under a particular source by specifying a query parameter" do
      get '/activities/Northwestern%20University?q=CT%3A', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("CT: Chest")
      response.xml_elements('//activity').should have(5).elements
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

    describe "/manual-target" do
      it "allows to make source as manual activity target source for manual_target is true" do
        entity = "{ manual_target: true }"
        put "/activities/Northwestern%20University/manual-target", entity,
          :as => :juno, 'Content-Type' => 'application/json'
        response.status_code.should == 200
        response.status_message.should == "OK"
      end

      it "throws 400 request error for manual_target false" do
        entity = "{ manual_target: false }"
        put "/activities/Northwestern%20University/manual-target", entity,
          :as => :juno, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.status_message.should == "Bad Request"
      end

      it "prevents creation of manual activity target source for unauthorized user" do
        entity = "{ manual_target: true }"
        put "/activities/Cancer/manual-target", entity,
          :as => :nil, 'Content-Type' => 'application/json'
        response.status_code.should == 401
      end

      it "throws 404 request error for non exist source" do
        entity = "{ manual_target: true }"
        put "/activities/NonExistSource/manual-target", entity,
          :as => :juno, 'Content-Type' => 'application/json'
        response.status_code.should == 404
        response.status_message.should == "Not Found"
        response.entity.should =~ %r(No source found with the name NonExistSource)
      end
    end
  end

end
