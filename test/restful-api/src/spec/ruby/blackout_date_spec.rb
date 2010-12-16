describe "/sites/{site-identifier}/blackout-dates" do

  describe "GET" do
    before do
      @blackoutdate1 = PscTest::Fixtures.createBlackoutDate(2008, 10, 12, "My birthday", pittsburgh)
      @blackoutdate2 = PscTest::Fixtures.createBlackoutDate(2008, 12, 25, "Christmas party", pittsburgh)
      application_context['blackoutDateDao'].save(@blackoutdate1)
      application_context['blackoutDateDao'].save(@blackoutdate2)
    end

    it "forbid access to blackout-dates for unauthorized user" do
      get '/sites/PA015/blackout-dates', :as => :erin
      response.status_code.should == 403
    end

    it "allows access to blackout-dates for authorized user" do
      get '/sites/PA015/blackout-dates', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_elements('//blackout-date').should have(2).elements
      response.xml_attributes("blackout-date", "description").should include("My birthday")
      response.xml_attributes("blackout-date", "description").should include("Christmas party")
    end

    it "gives 400 for unknown site" do
      get '/sites/siteUnknown/blackout-dates', :as => :juno
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
      response.entity =~ %r(Unknown site siteUnknown)
    end
  end

  describe "POST" do
      before do
        @blackoutdate_xml = psc_xml("blackout-date", 'description' => "Labor Day", 'site-identifier' => "MN026", 'day' => 17, 'month' => 9, 'year' =>2008) #MN026
      end

      it "creates a specific blackout date for a site" do
          post '/sites/MN026/blackout-dates', @blackoutdate_xml, :as => :juno
          response.status_code.should == 201
          response.status_message.should == "Created"
          #check after post
          get '/sites/MN026/blackout-dates', :as =>:juno
          response.status_code.should == 200
          response.status_message.should == "OK"
          response.content_type.should == 'text/xml'
          response.xml_attributes("blackout-date", "description").should include("Labor Day")
          response.xml_attributes("blackout-date", "site-identifier").should include("MN026")
      end

      it "gives 400 for unknown site identifier in xml" do
        @blackoutdate_unknownsite_xml = psc_xml("blackout-date", 'description' => "Labor Day", 'site-identifier' => "siteIdUnknown",
          'day' => 17, 'month' => 9, 'year' =>2008)
        post '/sites/siteId/blackout-dates', @blackoutdate_unknownsite_xml, :as => :juno
        response.status_code.should == 400
        response.status_message.should == "Bad Request"
        response.entity.should =~ %r(Site 'siteIdUnknown' not found. Please define a site that exists.)
      end
  end
end

describe "DELETE" do
  describe "/sites/{site-identifier}/blackout-dates/{blackout-date-identifier}" do
    before do
      @blackoutdate = PscTest::Fixtures.createBlackoutDate(2008, 10, 12, "My birthday", mayo)
      @blackoutdate.grid_id = "1111"
      application_context['blackoutDateDao'].save(@blackoutdate)
    end

    it "allows deletion of a blackout-date for authorized user" do
      delete '/sites/MN026/blackout-dates/1111', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"
    end
  end
end
