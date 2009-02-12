describe "/sites" do

  #ISSUE
  # 1. get /sites will return all sites to any authenticate user

  it "forbids any site access for unauthenticated users" do
    get "/sites", :as => nil
    response.status_code.should == 401
  end

  it "shows all sites to superuser" do
    get "/sites", :as => :juno
    #puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end

  it "shows all sites to study coordinator" do
    get "/sites", :as => :alice
    #puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end
  
  it "shows all sites to study admin" do
    get "/sites", :as => :barbara
    #puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end


  it "shows 1 site to the site coordinator of the site" do
    pending
    get "/sites", :as => :carla
    puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_elements('//site').should have(1).elements
  end 
  
end