#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe "/provided-sites" do
  before do
    get "/osgi/bundles", :as => :zelda
    @bundles = response.json
    installed = @bundles.find { |b| b['symbolic_name'] =~ /edu.northwestern.bioinformatics.psc-providers-mock/ }
    put "/osgi/bundles/#{installed['id']}/state", "{ state: STARTING }",
      :as => :zelda, 'Content-Type' => 'application/json'
  end
  describe "GET" do
    describe "xml" do
      before do
        get '/provided-sites?q=ab', :as => :zelda
      end
      it "is successful" do
        response.should be_success
      end
      it "is XML" do
        response.content_type.should == 'text/xml'
      end
      it "has the right number of sites" do
        response.xml_elements('//site').should have(4).studies
      end
      describe "site structure" do
        it "has assigned identifier" do
          response.xml_attributes("site", "assigned-identifier").should include("MN008")
        end
        it "has provider" do
          response.xml_attributes("site", "provider").should include("mock - NOT FOR PRODUCTION")
        end
        it "has site name" do
          response.xml_attributes("site", "site-name").should include("Abbott Northwestern Hospital")
        end
      end
    end
  end
end
