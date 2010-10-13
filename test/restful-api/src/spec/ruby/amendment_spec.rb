describe "/amendment" do

  describe "PUT" do
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      @amend_date1 = PscTest.createDate(2008, 11, 10)
      @amendment = PscTest::Fixtures.createAmendment("[Original]", @amend_date1, true)
      @study1.amendment = @amendment
      application_context['studyService'].save( @study1)
      @amend1_xml = psc_xml('amendment', 'name' => 'am1', 'date' => "2008-11-13", 'mandatory' => "true")
    end

    it "creates a new amendment for an authorized user" do
      put '/studies/NU480/template/amendments/2008-11-10~%5BOriginal%5D', @amend1_xml, :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("amendment", "name").should include("am1")
      response.xml_attributes("amendment", "date").should include("2008-11-13")
      response.xml_attributes("amendment", "mandatory").should include("true")
      response.xml_elements('//amendment').should have(1).elements
    end
  end

  describe "GET" do
      before do
        @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
        @amend_date1 = PscTest.createDate(2008, 12, 10)
        @amendment = PscTest::Fixtures.createAmendment("am1", @amend_date1, true)
        @study1.amendment = @amendment
        application_context['studyService'].save(@study1)
      end

      it "allows access to a specific amendment for an authorized user by specifying date" do
        get '/studies/NU480/template/amendments/2008-12-10', :as => :juno
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.content_type.should == 'text/xml'
        response.xml_attributes("amendment", "name").should include("am1")
        response.xml_attributes("amendment", "date").should include("2008-12-10")
        response.xml_attributes("amendment", "mandatory").should include("true")
        response.xml_elements('//amendment').should have(1).elements
      end

  end

  describe "DELETE" do

    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      application_context['studyService'].save(@study1)
      @amend_date = PscTest.createDate(2008, 12, 10)
      @amendment = PscTest::Fixtures.createInDevelopmentAmendment("Amendment", @amend_date, true)
      @study1.developmentAmendment = @amendment
      application_context['studyService'].save(@study1)
    end

    it "allows deletion of a specific amendment for an authorized user by specifying date" do
      delete '/studies/NU480/template/amendments/2008-12-10~Amendment', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
    end

  end

end
