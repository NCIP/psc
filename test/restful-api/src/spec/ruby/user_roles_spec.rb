describe "GET" do
  it "all roles of the user" do
    get "/users/alice/roles", :as => :alice
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_elements('//role').size == 1
    response.xml_attributes("role", "name").should include("Study coordinator")
  end

  it "allows system administrators to read any user's roles" do
    get "/users/hannah/roles", :as => :zelda
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_elements('//role').size == 2
    response.xml_attributes("role", "name").should include("Subject coordinator")
    response.xml_attributes("role", "name").should include("Study coordinator")
  end

  it "forbids any user's roles access for unauthorised users" do
    get "/users/hannah/roles", :as => :alice
    response.status_code.should == 403
  end

  it "returns associated Sites of the given Role" do
    get "/users/frieda/roles/Site+coordinator", :as => :frieda
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_elements('//site').size == 1
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
  end

  it "does not show invalid role " do
    get "/users/frieda/roles/Subject+coordinator", :as => :frieda
    response.status_code.should == 404
    response.status_message.should == "Not Found"
  end

  it "404s for non-existent roles for sys admins" do
    get "/users/frieda/roles/Subject+coordinator", :as => :zelda
    response.status_code.should == 404
  end

  it "forbids access to non-existent roles for other users" do
    get "/users/frieda/roles/Subject+coordinator", :as => :hannah
    response.status_code.should == 403
  end

  describe "returns StudySites for Subject coordinator role" do
    before do
      @nu480 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      application_context['studyService'].save(@nu480)
      @nu480_at_mayo = PscTest::Fixtures.createStudySite(@nu480, mayo)
      application_context['studySiteDao'].save(@nu480_at_mayo)
      application_context['templateService'].assignTemplateToSubjectCoordinator(@nu480, mayo, hannah)
    end

    it "of the given User:" do
      get "/users/hannah/roles/Subject+coordinator", :as => :hannah
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_elements('//site').size == 1
      response.xml_attributes("site", "assigned-identifier").should include("MN026")
      response.xml_elements('//study').size == 2
      response.xml_attributes("study", "assigned-identifier").should include("NU480")
    end
  end
end
