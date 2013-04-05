#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe "/provided-studies" do
  before do
    get "/osgi/bundles", :as => :zelda
    @bundles = response.json
    installed = @bundles.find { |b| b['symbolic_name'] =~ /edu.northwestern.bioinformatics.psc-providers-mock/ }
    put "/osgi/bundles/#{installed['id']}/state", "{ state: STARTING }",
      :as => :zelda, 'Content-Type' => 'application/json'
  end
  describe "GET" do
    it "forbids access to provided study list for unauthenticated users" do
      get '/provided-studies?q=ad',  :as => nil
      response.status_code.should == 401
    end
    it "forbids access to provided study list for unauthorized users" do
      get '/provided-studies?q=ad',  :as => :carla
      response.status_code.should == 403
    end
    describe "xml" do
      before do
        get '/provided-studies?q=ad', :as => :alice
      end
      it "is successful" do
        response.should be_success
      end
      it "is XML" do
        response.content_type.should == 'text/xml'
      end
      it "has the right number of studies" do
        response.xml_elements('//study').should have(9).studies
      end
      describe "study structure" do
        it "has assigned identifier" do
          response.xml_attributes("study", "assigned-identifier").should include("NCT00316888")
        end
        it "has provider" do
          response.xml_attributes("study", "provider").should include("mock - NOT FOR PRODUCTION")
        end
        it "has long title" do
          response.xml_elements('//long-title').should have(9).longtitles
        end
        describe "secondary identifiers structure" do
          it "has type" do
            response.xml_attributes("secondary-identifier", "type").should include("org_study")
          end
          it "has value" do
            response.xml_attributes("secondary-identifier", "value").should include("CDR0000470269")
          end
        end
      end
    end
    describe "json" do
      before do
        get '/provided-studies.json?q=ad', :as => :alice
      end
      it "is successful" do
        response.should be_success
      end
      it "is JSON" do
        response.content_type.should == 'application/json'
      end
      it "contains the right number of studies" do
        response.json["studies"].size.should == 9
      end
      describe "study structure" do
        before do
          @studies = response.json["studies"]
          @study = @studies[0]
        end
        it "has assigned identifier" do
          @study["assigned_identifier"].should == "NCT00316888"
        end
        it "has provider" do
          @study["provider"].should == "mock - NOT FOR PRODUCTION"
        end
        it "has long-title" do
          @study["long_title"].should == "Phase II Trial of Cetuximab Plus Cisplatin, 5- Fluorouracil and Radiation in Immunocompetent Patients With Anal Carcinoma"
        end
        it "has secondary-identifiers" do
          @study["secondary_identifiers"].size.should == 3
        end
        describe "secondary identifiers structure" do
          before do
            @secondary_identifier = @study["secondary_identifiers"][1]
          end
          it "has type" do
            @secondary_identifier["type"].should == "org_study"
          end
          it "has value" do
            @secondary_identifier["value"].should == "CDR0000470269"
          end
        end
      end
    end
  end
end
