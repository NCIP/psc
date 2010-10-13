describe "/study-site-link" do

  before do
    @studies = [
      # Released, but not approved studies
      @nu480 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String)),
      @ecog170 = PscTest::Fixtures.createSingleEpochStudy("ECOG170", "LTFU", [].to_java(:String))
    ]
    @studies.each do |s|
      application_context['studyService'].save(s)

    @study_site_link_xml = psc_xml("study-site-link", 'study-name' => "NU480", 'site-name' => "IL036")
    @valid_study_invalid_site_link_xml = psc_xml("study-site-link", 'study-name' => "NU480", 'site-name' => "AAAA12")
    @invalid_study_valid_site_link_xml = psc_xml("study-site-link", 'study-name' => "NU000", 'site-name' => "IL036")

    end
  end

  describe "PUT" do

    it "forbids creating a study-site-link for an unauthorized user" do
      put "/studies/NU480/sites/IL036", @study_site_link_xml, :as => nil
      response.status_code.should == 401
    end

    it "allows creating a study-site-link for an authorized user" do
      put "/studies/NU480/sites/IL036", @study_site_link_xml, :as => :juno
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      response.xml_attributes("study-site-link", "study-name").should include("NU480")
      response.xml_attributes("study-site-link", "site-name").should include("Northwestern University Robert H. Lurie Comprehensive Cancer Center")
      response.xml_elements('//study-site-link').should have(1).elements
    end

    it "forbids creating a study-site-link using a non-existing site" do
      put "/studies/NU480/sites/AAAA12", @valid_study_invalid_site_link_xml, :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "forbids creating a study-site-link using a non-existing study" do
      put "/studies/NU000/sites/IL036", @invalid_study_valid_site_link_xml, :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gives 400 if study from xml is not exists in system" do
      put "/studies/NU480/sites/IL036", @invalid_study_valid_site_link_xml, :as => :juno
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
      response.entity =~ %r(Study 'NU000' not found. Please define a study that exists.)
    end

    it "gives 400 if site from xml is not exists in system" do
      put "/studies/NU480/sites/IL036", @valid_study_invalid_site_link_xml, :as => :juno
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
      response.entity =~ %r(Site 'AAAA12' not found. Please define a site that exists.)
    end

  end

  describe "GET" do

    it "forbids access to study-site-link to an unauthorized user" do
      get "/studies/NU480/sites/IL036", :as => nil
      response.status_code.should == 401
    end

    it "forbid access to non existing study-site-link to an authorized user" do
      get "/studies/NU480/sites/IL036", :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
      response.content_type.should == 'text/plain'
    end

    it "allows access to an existing study-site-link to an authorized user" do
      put "/studies/NU480/sites/IL036", @study_site_link_xml, :as => :juno
      get "/studies/NU480/sites/IL036", :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
    end

  end

end
