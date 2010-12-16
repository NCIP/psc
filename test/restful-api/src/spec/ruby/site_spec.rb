describe "/sites/{site-identifier}" do
  before do
    @massgeneral = PscTest::Fixtures.createSite("Massachussets General Hospital", "MA034")
    application_context['siteDao'].save(@massgeneral)
  end

  describe "GET" do
    it "forbids site access for unauthenticated users" do
      get "/sites/MA034", :as => nil
      response.status_code.should == 401
    end

    it "includes site attributes in XML response" do
      get "/sites/MA034", :as => :juno
      response.xml_attributes("site", "assigned-identifier").should include("MA034")
      response.xml_attributes("site", "site-name").should include("Massachussets General Hospital")
      response.xml_elements('//site').should have(1).elements
    end

    it "shows all sites to POIMs" do
      get "/sites/MA034", :as => :juno
      response.status_code.should == 200
    end

    it "does not show an invalid site" do
      get "/sites/MN009", :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "shows sites to affiliated users" do
      get "/sites/IL036", :as => :carla # carla is authorized for IL036
      response.status_code.should == 200
    end

    it "limits site visibility to associated users" do
      get "/sites/MN026", :as => :carla # carla is only authorized for IL036
      response.status_code.should == 403
    end
  end

  describe "PUT" do
    describe "new" do
      before do
        @tju_xml = psc_xml("site",
          'assigned-identifier' => "PA121", 'site-name' => "Thomas Jefferson University")
      end

      it "is forbidden for unauthorized users" do
        put '/sites/PA121', @tju_xml, :as => :gertrude
        response.status_code.should == 403
      end

      it "works for authorized user" do
        put '/sites/PA121', @tju_xml, :as => :juno
        response.status_code.should == 201
        response.xml_attributes("site", "assigned-identifier").should include("PA121")
        response.xml_attributes("site", "site-name").should include("Thomas Jefferson University")
        response.xml_elements('//site').should have(1).elements
      end
    end

    describe "update" do
      before do
        @update_mass = psc_xml("site",
          'assigned-identifier' => @massgeneral.assigned_identifier,
          'site-name' => "Massive Dynamic Cancer Center")
      end

      it "is forbidden for unauthorized users" do
        put '/sites/PA121', @update_mass, :as => :hannah
        response.status_code.should == 403
      end

      it "updates an existing site for an authorized sysadmin" do
        put '/sites/MA034', @update_mass, :as => :juno
        response.status_code.should == 200
        response.xml_attributes("site", "assigned-identifier").should include("MA034")
        response.xml_attributes("site", "site-name").should include("Massive Dynamic Cancer Center")
        response.xml_elements('//site').should have(1).elements
      end
    end
  end

  describe "DELETE" do
    it "deletes a site for an authorized user" do
      delete '/sites/MA034', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"

      get '/sites/MA034', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gets 404 if site with given assigned identifier doesn't exist" do
      delete '/sites/IL000', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
  end
end
