describe "/sites" do

  it "forbids any site access for unauthenticated users" do
    get "/sites", :as => nil
    response.status_code.should == 401
  end

  it "shows all sites to superuser" do
    get "/sites", :as => :juno
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end

  it "shows all sites to study coordinator" do
    get "/sites", :as => :alice
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end

  it "shows all sites to study admin" do
    get "/sites", :as => :barbara
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end


  it "forbids access to the list of sites to the site coordinator/subject coordinator" do
    get "/sites", :as => :carla #doesn't have access to the all the sites of system
    response.status_code.should == 403
  end

end
