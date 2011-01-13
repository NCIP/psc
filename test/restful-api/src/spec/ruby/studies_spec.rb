describe "/studies" do
  describe "xml" do
    before do
      @studies = [
        # Released, but not approved studies
        @nu480 = PscTest::Fixtures.createSingleEpochStudy("NU 480", "Treatment", ["A", "B"].to_java(:String)),
        @ecog170 = PscTest::Fixtures.createSingleEpochStudy("ECOG 170", "LTFU", [].to_java(:String)),
        # In development studies
        @nu120 = PscTest::Fixtures.createInDevelopmentBasicTemplate("NU 120")
      ]
      @studies.each do |s|
        s.add_managing_site(mayo)
        application_context['studyService'].save(s)
      end
    end

    def study_names
      response.xml_attributes('study', "assigned-identifier")
    end

    it "forbids access for unauthenticated users" do
      get "/studies", :as => nil
      response.status_code.should == 401
    end

    it "shows all studies to an all-studies SCTB" do
      get "/studies", :as => :alice
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_elements('//study').should have(3).elements
      study_names.should include("NU 480")
      study_names.should include("ECOG 170")
      study_names.should include("NU 120")
    end

    it "shows all studies to an all-studies SQM" do
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

    it "shows nothing to to a sys admin" do
      get "/studies", :as => :zelda
      response.status_code.should == 403
    end

    describe "with assigned sites" do
      before do
        application_context['studySiteService'].assignStudyToSites(@nu480, [northwestern])
        application_context['studySiteService'].assignStudyToSites(@ecog170, [northwestern, mayo])
      end

      it "shows appropriate released studies released to an NU SQM" do
        get "/studies", :as => :carla
        response.should be_success
        response.xml_elements('//study').should have(2).elements
        study_names.should include("NU 480")
        study_names.should include("ECOG 170")
      end

      it "shows appropriate released studies released to a mayo STA" do
        get "/studies", :as => :frieda
        response.should be_success
        response.xml_elements('//study').should have(1).elements
        study_names.should include("ECOG 170")
      end
    end

    describe "POST" do
      before do
        @studyDao = application_context['studyDao']
        @nu328_xml = psc_xml("study-snapshot", 'assigned-identifier' => "NU328") { |ss|
          ss.tag!('planned-calendar') { |pc|
            pc.epoch('name' => 'Treatment')
            pc.epoch('name' => 'LTFU')
          }
          ss.population('abbreviation' => 'W', 'name' => 'Women of childbearing potential')
        }
      end

      it "does not accept a study snapshot from an SQM" do
        post '/studies', @nu328_xml, :as => :barbara
        response.status_code.should == 403
      end

      it "does not accept a study snapshot from an SSCM" do
        post '/studies', @nu328_xml, :as => :erin
        response.status_code.should == 403
      end

      it "does not accept a study snapshot from a sysadmin" do
        post '/studies', @nu328_xml, :as => :zelda
        response.status_code.should == 403
      end

      it "accepts a study snapshot from a study creator" do
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
        response.status_code.should == 400
      end

      it "accepts a template using the old inline activity definitions (child of planed-activity)" do
        old = PscTest.template('study-snapshot-using-inline-activity-definitions')
        post '/studies', old, :as => :juno
        response.status_code.should == 201 #created
        created = @studyDao.getByAssignedIdentifier('1140')

        planned_activities = created.developmentAmendment.deltas[0].changes[0].child.getStudySegments[0].periods.first.plannedActivities
        planned_activities.should have(2).records

        a0 = planned_activities[0].activity
        a0.code.should == "My New Activity Code"
        a0.name.should == "My New Bone Marrow Aspirate"
        a0.type.name.should == "Disease Measure"
        a0.source.name.should == "My New Source"

        a1 = planned_activities[1].activity
        a1.code.should == "969"
        a1.name.should == "CT: Abdomen"
        a1.type.name.should == "Disease Measure"
        a1.source.name.should == "Northwestern University"
      end

      it "accepts a template using the new separated activity definitions" do
        new = PscTest.template('study-snapshot-using-separated-activity-definitions')
        post '/studies', new, :as => :juno
        response.status_code.should == 201 #created
        created = @studyDao.getByAssignedIdentifier('1140')

        planned_activities = created.developmentAmendment.deltas[0].changes[0].child.getStudySegments[0].periods.first.plannedActivities
        planned_activities.should have(2).records

        a0 = planned_activities[0].activity
        a0.code.should == "My New Activity Code"
        a0.name.should == "My New Bone Marrow Aspirate"
        a0.type.name.should == "Disease Measure"
        a0.source.name.should == "My New Source"

        a1 = planned_activities[1].activity
        a1.code.should == "969"
        a1.name.should == "CT: Abdomen"
        a1.type.name.should == "Disease Measure"
        a1.source.name.should == "Northwestern University"
      end
    end
  end

  describe "json" do
    before do
      @nu480 = psc_xml("study", 'assigned-identifier' => "NU480", 'provider' => "Provider For Study 480")  { |s|
        s.tag!('long-title', "long title for Study 480")
        s.tag!('secondary-identifier', 'type' =>'secondary', 'value' => "CDR0000066727")
        s.tag!('secondary-identifier', 'type' =>'secondary1', 'value' => "ECOG-1697")
      }
      @nu481 = psc_xml("study", 'assigned-identifier' => "NU481", 'provider' => "Provider For Study 481")
      post '/studies', @nu480, :as => :juno
      post '/studies', @nu481, :as => :juno
      response.status_code.should == 201

      get '/studies.json?q=NU', :as => :alice
    end

    it "is successful" do
      response.should be_success
    end

    it "contains the right number of studies" do
      response.json["studies"].size.should == 2
    end

    it "is JSON" do
      response.content_type.should == 'application/json'
    end

    it "contains the right number of studies" do
      response.json["studies"].size.should == 2
    end

    describe "study structure" do
      before do
        @studies = response.json["studies"].sort_by { |s| s["assigned_identifier"] }
        @study = @studies[0]
      end

      it "has assigned identifier" do
        @study["assigned_identifier"].should == "NU480"
      end

      it "has provider" do
        @study["provider"].should == "Provider For Study 480"
      end

      it "has long-title" do
        @study["long_title"].should == "long title for Study 480"
      end

      it "has secondary-identifiers" do
        @study["secondary_identifiers"].size.should == 2
      end

      describe "secondary identifiers structure" do
        before do
          @secondary_identifier = @study["secondary_identifiers"][1]
        end

        it "has type" do
          @secondary_identifier["type"].should == "secondary1"
        end

        it "has value" do
          @secondary_identifier["value"].should == "ECOG-1697"
        end
      end
    end
  end
end
