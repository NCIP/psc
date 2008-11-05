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

  it "shows all studies to a study admin" do
    get "/studies", :as => :barbara
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.content_type.should == 'text/xml'
    response.xml_elements('//study').should have(3).elements
    study_names = response.xml_elements('//study').collect { |s| s.attributes["assigned-identifier"] }
    study_names.should include("NU 480")
    study_names.should include("ECOG 170")
    study_names.should include("NU 120")
  end
end