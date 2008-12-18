describe "/amendment_approval" do
  
  #ISSUE:
  #1) post /studies/{study-identifier}/sites/{site-identifier}/approvals returns error:
  #   Authenticated account is not authorized for this resource and method
  
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end
  
  describe "POST" do
    
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      @amend_date1 = PscTest.createDate(2008, 12, 10)
      @amend_date2 = PscTest.createDate(2006, 1, 23)
      @amend_date3 = PscTest.createDate(2007, 4, 19)            
      @amendment = PscTest::Fixtures.createAmendments([@amend_date1, @amend_date2, @amend_date3].to_java(Java::JavaUtil::Date))
      @study1.amendment = @amendment
      application_context['studyService'].save(@study1)
      @site1 = PscTest::Fixtures.createSite("My Site", "site1")
      application_context['siteDao'].save( @site1)
      @studySite1 = PscTest::Fixtures.createStudySite(@study1, @site1)
      application_context['studySiteDao'].save(@studySite1)
      @approve_xml = psc_xml("amendment-approval", 'amendment' => "2007-04-19", 'date' => "2008-12-25")   
    end
    
    it "forbids approving an amendment for unauthorized user" do
      post "/studies/NU480/sites/site1/approvals", @approve_xml, :as => nil
      response.status_code.should == 401
    end
    
    it "approves an amendment for an authorized user" do
      pending
      puts @approve_xml
      post "/studies/NU480/sites/site1/approvals", @approve_xml, :as => :juno
      puts response.entity
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      xml_attribute("amendment-approval", "date").should include("2008-12-25")
      xml_attribute("amendment-approval", "amendment").should include("2007-04-19")
      response.xml_elements('//amendment-approval').should have(1).elements      
    end
    
        
  end
  
  describe "GET" do
    
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      @amend_date1 = PscTest.createDate(2008, 12, 10)
      @amend_date2 = PscTest.createDate(2006, 1, 23)
      @amend_date3 = PscTest.createDate(2007, 4, 19)            
      @amendment = PscTest::Fixtures.createAmendments([@amend_date1, @amend_date2, @amend_date3].to_java(Java::JavaUtil::Date))
      @study1.amendment = @amendment
      application_context['studyService'].save(@study1)
      @approve_date = PscTest.createDate(2008, 12, 25)
      @site1 = PscTest::Fixtures.createSite("My Site", "site1")
      application_context['siteDao'].save( @site1)
      @studySite1 = PscTest::Fixtures.createStudySite(@study1, @site1)
      @studySiteApproved = PscTest::Fixtures.approveAmendment(@studySite1, @amendment, @approve_date)
      application_context['studySiteDao'].save(@studySiteApproved)
    end
     
    it "forbids access to amendment approvals to an unauthorized user" do
      get "/studies/NU480/sites/site1/approvals", :as => nil
      response.status_code.should == 401
    end
    
    it "allows access to amendment approvals to an authorized user" do
      get "/studies/NU480/sites/site1/approvals", :as => :juno
      # puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      xml_attribute("amendment-approval", "date").should include("2008-12-25")
      xml_attribute("amendment-approval", "amendment").should include("2007-04-19")
      response.xml_elements('//amendment-approval').should have(1).elements      
    end
        
  end
  
  
end