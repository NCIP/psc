#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "GET" do
  describe "/users/{username}/roles" do
    it "includes all roles of the user" do
      get "/users/alice/roles", :as => :alice
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_elements('//role').size == 2
      response.xml_attributes("role", "name").should include("Study Calendar Template Builder")
      response.xml_attributes("role", "name").should include("Study Creator")
    end

    it "allows user administrators to read any user's roles" do
      get "/users/hannah/roles", :as => :yvette
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.xml_elements('//role').size == 3
      response.xml_attributes("role", "name").should include("Study Subject Calendar Manager")
      response.xml_attributes("role", "name").should include("Study Calendar Template Builder")
      response.xml_attributes("role", "name").should include("Study Creator")
    end

    it "prevents non-admin users from accessing each others' roles" do
      get "/users/hannah/roles", :as => :alice
      response.status_code.should == 403
    end

    describe "/{role-name}" do
      it "returns the associated sites of a given role" do
        get "/users/frieda/roles/Study%20Team%20Administrator", :as => :frieda
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.xml_attributes('role-sites', "all").should == ["false"]
        response.xml_elements('//site').size.should == 1
        response.xml_attributes("site", "assigned-identifier").should include("MN026")
      end

      it "404s for roles the user doesn't have" do
        get "/users/frieda/roles/Data%20Reader", :as => :frieda
        response.status_code.should == 404
        response.status_message.should == "Not Found"
      end

      it "404s for non-existent roles for admins" do
        get "/users/frieda/roles/Data%20Reader", :as => :yvette
        response.status_code.should == 404
      end

      it "forbids access to non-existent roles for other users" do
        get "/users/frieda/roles/Data%20Reader", :as => :hannah
        response.status_code.should == 403
      end

      it "returns the associated studies for a given role" do
        get "/users/hannah/roles/Study%20Calendar%20Template%20Builder", :as => :hannah
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.xml_elements('//role-studies').size.should == 1
        response.xml_attributes('role-studies', "all").should == ["true"]
      end
    end
  end
end
