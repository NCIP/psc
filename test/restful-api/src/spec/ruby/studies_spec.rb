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
    response.xml_attributes('study', "assigned-identifier")
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
    get "/studies", :as => :carla
    response.should be_success
    response.xml_elements('//study').should have(0).elements
  end

  describe "shows only assigned studies" do 
    before do
      @nu480_at_northwestern = PscTest::Fixtures.createStudySite(@nu480, northwestern)
      application_context['studySiteDao'].save(@nu480_at_northwestern)
      application_context['templateService'].assignTemplateToSubjectCoordinator(@nu480, northwestern, erin)
    end
    it " to a subject coordinator" do
      get "/studies", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_elements('//study').should have(1).elements
      study_names.should include("NU 480")
    end
  end

  it "shows nothing to to a sys admin" do
    get "/studies", :as => :zelda
    response.status_code.should == 403
  end

  describe "with assigned sites" do
    before do
      application_context['templateService'].assignTemplateToSites(@nu480, [northwestern])
      application_context['templateService'].assignTemplateToSites(@ecog170, [northwestern, mayo])
    end

    it "shows appropriate released studies released to an NU site coord" do
      get "/studies", :as => :carla
      response.should be_success
      response.xml_elements('//study').should have(2).elements
      study_names.should include("NU 480")
      study_names.should include("ECOG 170")
    end

    it "shows appropriate released studies released to a mayo site coord" do
      get "/studies", :as => :frieda
      response.should be_success
      response.xml_elements('//study').should have(1).elements
      study_names.should include("ECOG 170")
    end
  end

  describe "POST" do
    before do
      @nu328_xml = psc_xml("study-snapshot", 'assigned-identifier' => "NU328") { |ss|
        ss.tag!('planned-calendar') { |pc|
          pc.epoch('name' => 'Treatment')
          pc.epoch('name' => 'LTFU')
        }
        ss.population('abbreviation' => 'W', 'name' => 'Women of childbearing potential')
      }
    end

    it "does not accept a study snapshot from a study admin" do
      post '/studies', @nu328_xml, :as => :barbara
      response.status_code.should == 403
    end

    it "does not accept a study snapshot from a site coordinator" do
      post '/studies', @nu328_xml, :as => :carla
      response.status_code.should == 403
    end

    it "does not accept a study snapshot from a subject coordinator" do
      post '/studies', @nu328_xml, :as => :erin
      response.status_code.should == 403
    end

    it "does not accept a study snapshot from a sysadmin" do
      post '/studies', @nu328_xml, :as => :zelda
      response.status_code.should == 403
    end

    it "accepts a study snapshot from a study coordinator" do
      post '/studies', @nu328_xml, :as => :alice
      #response.should be_success
      get '/studies', :as => :alice
      response.xml_elements('//study').should have(4).elements
      study_names.should include("NU328")
    end

    it "accepts a study snapshot and provides a link to the permanent URI for it" do
      # pending
      post '/studies', @nu328_xml, :as => :alice
      response.status_code.should == 201
      response.meta['location'].should =~ %r{/api/v1/studies/NU328/template$}
      response.meta['location'].should =~ %r{^http}
    end

    it "does not accept a study snapshot with the same name as an existing study" do
      post '/studies', psc_xml("study-snapshot", 'assigned-identifier' => "NU 120"), :as => :alice
      response.should be_client_error
      response.status_code.should == 400
    end
  end
end