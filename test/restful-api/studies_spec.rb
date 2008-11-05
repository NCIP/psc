describe "/studies" do
  before do
    @studies = [
      # Released, but not approved studies
      @nu480 = PscTest::Fixtures.createSingleEpochStudy("NU 480", "Treatment", ["A", "B"].to_java(:String)),
      @ecog170 = PscTest::Fixtures.createSingleEpochStudy("ECOG 170", "LTFU", [].to_java(:String)),
      # In development studies
      @nu120 = PscTest::Fixtures.createInDevelopmentBasicTemplate("NU 120")
    ]
    @studies.each do |s|
      application_context['studyService'].save(s)
    end
  end

  it "forbids access for unauthenticated users" do
    get "/studies", :as => nil
    response.status_code.should == 401
  end

  def study_names
    response.xml_elements('//study').collect { |s| s.attributes["assigned-identifier"] }
  end

  it "shows all studies to a study coordinator" do
    get "/studies", :as => :alice
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_elements('//study').should have(3).elements
    study_names.should include("NU 480")
    study_names.should include("ECOG 170")
    study_names.should include("NU 120")
  end

  it "shows all studies to a study admin" do
    get "/studies", :as => :barbara
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_elements('//study').should have(3).elements
    study_names.should include("NU 480")
    study_names.should include("ECOG 170")
    study_names.should include("NU 120")
  end

  it "shows nothing to a site coordinator when nothing is released for her site" do
    pending
    get "/studies", :as => :carla
    response.should be_success
    response.xml_elements('//study').should have(0).elements
  end

  describe "with assigned sites" do
    before do
      application_context['templateService'].assignTemplateToSites(@nu480, [northwestern])
      application_context['templateService'].assignTemplateToSites(@ecog170, [northwestern, mayo])
    end

    it "shows appropriate released studies released to an NU site coord" do
      pending
      get "/studies", :as => :carla
      response.should be_success
      response.xml_elements('//study').should have(2).elements
      study_names.should include("NU 480")
      study_names.should include("ECOG 170")
    end

    it "shows appropriate released studies released to a mayo site coord" do
      pending
      get "/studies", :as => :frieda
      response.should be_success
      response.xml_elements('//study').should have(1).elements
      study_names.should include("ECOG 170")
    end
  end

  it "shows only assigned studies to a subject coordinator"
  it "shows nothing to to a sys admin"
end