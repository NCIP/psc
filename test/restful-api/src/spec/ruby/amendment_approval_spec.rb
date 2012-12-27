#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "/studies/{study-identifier}/sites/{site-identifier}/approvals" do
  describe "POST" do
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      @amend_date1 = PscTest.createDate(2008, 12, 10)
      @amend_date2 = PscTest.createDate(2006, 1, 23)
      @amend_date3 = PscTest.createDate(2007, 4, 19)
      @amendment = PscTest::Fixtures.createAmendments([@amend_date1, @amend_date2, @amend_date3].to_java(Java::JavaUtil::Date))
      @study1.amendment = @amendment
      application_context['studyService'].save(@study1)
      @studySite1 = PscTest::Fixtures.createStudySite(@study1, northwestern)
      application_context['studySiteDao'].save(@studySite1)
      @approve_xml = psc_xml("amendment-approval", 'amendment' => "2007-04-19", 'date' => "2008-12-25")
    end

    it "forbids approving an amendment for unauthorized user" do
      post "/studies/NU480/sites/IL036/approvals", @approve_xml, :as => nil
      response.status_code.should == 401
    end

    it "forbids approving amendment for study subject calendar manager" do
      post "/studies/NU480/sites/IL036/approvals", @approve_xml, :as => erin
      response.status_code.should == 403
    end

    describe "when authorized" do
      before do
        post "/studies/NU480/sites/IL036/approvals", @approve_xml, :as => :carla
      end

      it "approves an amendment for an authorized user" do
        response.status_code.should == 201
        response.status_message.should == "Created"
      end

      it "includes the proper Location for the created resource" do
        response.meta['location'].should_not be_nil
        response.meta['location'].should =~ %r{^http:}
        response.meta['location'].should =~ %r{api/v1/studies/NU480/sites/IL036/approvals/2007-04-19$}
      end

      it "provides a reachable Location for the created resource" do
        pending "#655"
        get response.meta['location'], :as => :carla
        response.status_code.should == 200
        response.content_type.should == 'text/xml'
        response.xml_attributes("amendment-approval", "date").should include("2008-12-25")
        response.xml_attributes("amendment-approval", "amendment").should include("2007-04-19")
        response.xml_elements('//amendment-approval').should have(1).elements
      end
    end

    it "gives 400 if amendment not found for study" do
      @study2 = PscTest::Fixtures.createSingleEpochStudy("NU481", "Treatment", ["segment_A", "segment_B"].to_java(:String))
      @amend_date2 = PscTest.createDate(2008, 12, 10)
      @amendment2 = PscTest::Fixtures.createAmendment("am2", @amend_date2)
      @study2.amendment = @amendment2
      application_context['studyService'].save(@study2)
      @approveNew_xml = psc_xml("amendment-approval", 'amendment' => "2008-12-10~am2", 'date' => "2008-12-11")
      post "/studies/NU480/sites/IL036/approvals", @approveNew_xml, :as => :carla
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
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
      @studySite1 = PscTest::Fixtures.createStudySite(@study1, northwestern)
      @studySite1.approveAmendment(@amendment, @approve_date)
      application_context['studySiteDao'].save(@studySite1)
    end

    it "forbids access to amendment approvals to an unauthorized user" do
      get "/studies/NU480/sites/IL036/approvals", :as => nil
      response.status_code.should == 401
    end

    it "allows access to amendment approvals to an authorized user" do
      get "/studies/NU480/sites/IL036/approvals", :as => :carla
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("amendment-approval", "date").should include("2008-12-25")
      response.xml_attributes("amendment-approval", "amendment").should include("2007-04-19")
      response.xml_elements('//amendment-approval').should have(1).elements
    end
  end
end
