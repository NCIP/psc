#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe "/docs" do
  it "returns HTML by default" do
    get "/docs"
    response.status_code.should == 200
    response.content_type.should == 'text/html'
  end

  describe "human-readable" do
    it "returns contains the full URIs for resources" do
      get "/docs"
      response.status_code.should == 200
      response.entity.should =~ %r{#{psc_url}/api/v1/studies}
    end
  end

  describe "/psc.wadl" do
    before do
      get "/docs/psc.wadl"
      response.status_code.should == 200
    end

    it "returns WADL" do
      response.content_type.should == 'application/vnd.sun.wadl+xml'
    end

    it "contains the actual URI for the application in the resources" do
      response.entity.should =~ %r{#{psc_url}/api/v1}
    end
  end

  describe "/psc.xsd" do
    before do
      get "/docs/psc.xsd"
      response.status_code.should == 200
    end

    it "returns XML Schema" do
      response.content_type.should == 'application/x-xsd+xml'
    end
  end
end
