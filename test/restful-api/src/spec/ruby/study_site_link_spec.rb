describe "/studies/{study-identifier}/sites/{site-identifier}" do

  before do
    @studies = [
      # Released, but not approved studies
      @nu480 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String)),
      @ecog170 = PscTest::Fixtures.createSingleEpochStudy("ECOG170", "LTFU", [].to_java(:String))
    ]
    @studies.each do |s|
      application_context['studyService'].save(s)
    end

    @study_site_link_xml = psc_xml("study-site-link", 'study-identifier' => "NU480", 'site-identifier' => "IL036")
    @valid_study_invalid_site_link_xml = psc_xml("study-site-link", 'study-identifier' => "NU480", 'site-identifier' => "AAAA12")
    @invalid_study_valid_site_link_xml = psc_xml("study-site-link", 'study-identifier' => "NU000", 'site-identifier' => "IL036")
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
      response.xml_attributes("study-site-link", "study-identifier").should include("NU480")
      response.xml_attributes("study-site-link", "site-identifier").should include("Northwestern University Robert H. Lurie Comprehensive Cancer Center")
      response.xml_elements('//study-site-link').should have(1).elements
    end

    it "allows creating a study-site-link without specifying the linked refs in the body" do
      put "/studies/NU480/sites/IL036", psc_xml('study-site-link'), :as => :juno
      response.status_code.should == 201
    end

    it "prevents creating a study-site-link where the body study doesn't match the URL" do
      put "/studies/NU480/sites/IL036",
        psc_xml('study-site-link', 'study-identifier' => 'ECOG170'), :as => :juno
      response.status_code.should == 422
      response.entity.should =~
        %r{Entity- and URI-designated studies do not match. Either make them match or omit the one in the entity.}
    end

    it "prevents creating a study-site-link where the body site doesn't match the URL" do
      put "/studies/NU480/sites/IL036",
        psc_xml('study-site-link', 'site-identifier' => 'PA015'), :as => :juno
      response.status_code.should == 422
      response.entity.should =~
        %r{Entity- and URI-designated sites do not match. Either make them match or omit the one in the entity.}
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
