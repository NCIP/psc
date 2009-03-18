describe "/site" do

  #3. get /sites/{site-identifier} should not allowed to unauthorized user # Related to '#635'

  describe "GET" do
    before do
      @site1 = PscTest::Fixtures.createSite("My Site", "site1")
      application_context['siteDao'].save(@site1)
    end
      
    it "forbids site access for unauthenticated users" do
      get "/sites/site1", :as => nil
      response.status_code.should == 401
    end

    it "shows a specific site to the user" do
      get "/sites/site1", :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_attributes("site", "assigned-identifier").should include("site1")
      response.xml_elements('//site').should have(1).elements
    end
  
    it "does not show an invalid site" do
      get "/sites/MN009", :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
  
    it "does not show a site of a user to the other users" do
      pending '#635'
      get "/sites/MN026", :as => :carla #carla is only authorized for IL036
      puts response.entity
      response.status_code.should == 401
    end   
  end
  
  describe "PUT" do
    
    before do
      @newsite_xml = psc_xml("site", 'assigned-identifier' => "DB026", 'site-name' => "DB026")
      @updatesite_xml = psc_xml("site", 'assigned-identifier' => "MN026", 'site-name' => "NewSiteName")
    end
    
    it "forbid site creation for unauthorized user" do
      # puts @newsite_xml
      put '/sites/DB026', @newsite_xml, :as => nil
      response.status_code.should == 401
    end
    
    it "updates an existing site for an authorized user" do
      put '/sites/MN026', @updatesite_xml, :as => :juno
      response.status_code.should == 200
      response.xml_attributes("site", "assigned-identifier").should include("MN026")
      response.xml_attributes("site", "site-name").should include("NewSiteName")
      response.xml_elements('//site').should have(1).elements
      
    end
            
    it "creates a new site for authorized user" do
      put '/sites/DB026', @newsite_xml, :as => :juno
      response.status_code.should == 201
      response.xml_attributes("site", "assigned-identifier").should include("DB026")
      response.xml_attributes("site", "site-name").should include("DB026")
      response.xml_elements('//site').should have(1).elements
    end
              
  end
  
  describe "DELETE" do
    
    before do
      @site2 = PscTest::Fixtures.createSite("My Site", "site2")
      application_context['siteDao'].save(@site2)
    end
    
    it "delete a site for an authorized user" do
      #Verify before Delete
      get '/sites/site2', :as => :juno
      response.status_code.should == 200 
      response.xml_attributes("site", "site-name").should include("My Site")
      response.xml_attributes("site", "assigned-identifier").should include("site2")
      response.xml_elements('//site').should have(1).elements
      #Delete the Site
      delete '/sites/site2', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      #Check after delete
      get '/sites/site2', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end 
    
    it "gets 404 if site with given assigned identifier doesn't exist" do 
      delete '/sites/My%20Site', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
  end 
end