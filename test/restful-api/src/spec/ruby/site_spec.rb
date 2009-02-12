describe "/site" do

  #ISSUE
  #1. put /sites/{site-identifier} generates the following error:
  # could not execute query; nested exception is org.hibernate.exception.SQLGrammarException: could not execute query
  #2. delete /sites/{site-identifier} generates the folloing error:
  # Authenticated account is not authorized for this resource and method 

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
      pending
      get "/sites/MN026", :as => :carla #carla is only authorized for IL036
      puts response.entity
      response.status_code.should == 401
    end   
  end
  
  describe "PUT" do
    
    before do
      @newsite_xml = psc_xml("site", 'assigned-identifier' => "DB026", 'site-name' => "DB026")
    end
    
    it "forbid site creation for unauthorized user" do
      # puts @newsite_xml
      put '/sites/DB026', @newsite_xml, :as => nil
      response.status_code.should == 401
    end
    
    it "updates an existing site for an authorized user" do
      pending
      put '/sites/MN026', @newsite_xml, :as => :juno
      puts response.entity
    end
            
    it "creates a new site for authorized user" do
      pending
      put '/sites/DB026', @newsite_xml, :as => :juno
      puts response.entity
      #get '/sites/', :as => :juno
      #puts response.entity
      response.status_code.should == 201
    end
              
  end
  
  describe "DELETE" do
    
    before do
      @site2 = PscTest::Fixtures.createSite("My Site", "site2")
      application_context['siteDao'].save(@site2)
    end
    
    it "delete a site for an authorized user" do
      pending
      delete '/sites/site2', :as => :juno
      # puts response.entity
    end  

  end  
  
end