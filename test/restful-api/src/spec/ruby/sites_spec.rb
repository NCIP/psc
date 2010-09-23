describe "/sites" do
  before do
    pending '#1209'
  end

  it "forbids any site access for unauthenticated users" do
    get "/sites", :as => nil
    response.status_code.should == 401
  end

  it "shows all sites to a user with an all sites role" do
    get "/sites", :as => :barbara
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_attributes("site", "assigned-identifier").should include("PA015")
    response.xml_elements('//site').should have(3).elements
  end

  it "shows all individually authorized sites to a user with several" do
    get "/sites", :as => :gertrude
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.xml_attributes("site", "assigned-identifier").should include("MN026")
    response.xml_attributes("site", "assigned-identifier").should include("IL036")
    response.xml_elements('//site').should have(2).elements
  end

  it "forbids access to users with only global roles" do
    get "/sites", :as => :zelda # is a sysadmin only
    response.status_code.should == 403
  end
end
