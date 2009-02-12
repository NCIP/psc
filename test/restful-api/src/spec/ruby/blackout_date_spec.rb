describe "/blackout-date" do

  #ISSUE
  #1. POST /sites/{site-identifier}/blackout-dates request returns "Authenticated account is not authorized for this resource and method"
  #   BUG: POST should not require site-id but site assigned-identifier instead
  #2. DELETE /sites/{site-identifier}/blackout-dates/{blackout-date identifier} request returns "Authenticated account is not authorized for this resource and method"
  #   Cannot setId for blackOutDate
  
  describe "GET" do
    before do
      @site1 = PscTest::Fixtures.createSite("My Site", "site1")
      application_context['siteDao'].save(@site1)
      @blackoutdate1 = PscTest::Fixtures.createBlackoutDate(2008, 10, 12, "My birthday", @site1)
      @blackoutdate2 = PscTest::Fixtures.createBlackoutDate(2008, 12, 25, "Christmas party", @site1)
      application_context['blackoutDateDao'].save(@blackoutdate1)
      application_context['blackoutDateDao'].save(@blackoutdate2)      
    end
    
    it "forbid access to blackout-dates for unauthorized user" do      
      get '/sites/site1/blackout-dates', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end
    
    it "allows access to blackout-dates for authorized user" do      
      get '/sites/site1/blackout-dates', :as => :juno
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_elements('//blackout-date').should have(2).elements
      response.xml_attributes("blackout-date", "description").should include("My birthday")
      response.xml_attributes("blackout-date", "description").should include("Christmas party")
    end   
  end
  
  describe "POST" do
      before do
        @blackoutdate_xml = psc_xml("blackout-date", 'description' => "Labor Day", 'assigned-identifier' => "site1", 'day' => 17, 'month' => 9, 'year' =>2008) #MN026
        @site1 = PscTest::Fixtures.createSite("My Site", "site1")
        application_context['siteDao'].save(@site1)
      end
  
      it "creates a specific blackout date for a site" do
          pending
          post '/sites/site1/blackout-dates', @blackoutdate_xml, :as => :juno
          puts response.entity
          response.status_code.should == 201
          response.status_message.should == "Created"
          response.content_type.should == 'text/xml'
          # response.xml_elements('//blockout-date').should have(1).elements
      end
  end
  
  describe "DELETE" do
    before do
      @site1 = PscTest::Fixtures.createSite("My Site", "site1")
      application_context['siteDao'].save(@site1)
      @blackoutdate1 = PscTest::Fixtures.createBlackoutDate(2008, 10, 12, "My birthday", @site1)
      application_context['blackoutDateDao'].save(@blackoutdate1)
      @blackoutdate2 = PscTest::Fixtures.createBlackoutDate(2008, 12, 25, "Christmas party", @site1)
      application_context['blackoutDateDao'].save(@blackoutdate2)
      ## need code to modify blackoutDate id       
    end
    
    it "allows deletion of a blackout-date for authorized user" do      
      pending
      delete '/sites/site1/blackout-dates/someIdentifier', :as => :juno
      puts response.entity
      
    end
     
  end
  
  
end