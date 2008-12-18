describe "/amendment" do 
  
  # ISSUE
  # 1. put /studies/{study-identifier}/template/amendments/{amendment-identifier} is not authorized for authenticated account
  # 2. delete /studies/{study-identifier}/template/amendments/{amendment-identifier} is not authorized for authenticated account
  
  
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end  
  
  describe "PUT" do
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      application_context['studyService'].save( @study1)
      
      @amend1_xml = psc_xml('amendment', 'name' => 'am1', 'date' => "2008-11-13", 'mandatory' => "true"){ |da|
          da.tag!('planned-calendar-delta', 'id' => 'd1', 'node-id' => 'pc1') { |pcd| 
            pcd.tag!('add', 'id' => 'add1') { |add|
              add.tag!('epoch','id' => 'epoch1', 'name' => 'Malaria Treatment') {|seg|
                seg.tag!('study-segment', 'id' => "segment1", 'name' => 'initial test')
              }
            }
          }
        }
    end
    
    it "creates a new amendment for an authorized user" do
      pending
      put '/studies/NU480/template/amendments/2008-11-13', @amend1_xml, :as => :juno
      puts response.entity
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.content_type.should == 'text/xml'
      xml_attribute("amendment", "name").should include("am1")
      xml_attribute("amendment", "date").should include("2008-11-13")
      xml_attribute("amendment", "mandatory").should include("true")
      response.xml_elements('//amendment').should have(1).elements
    end
  end
  
  describe "GET" do
      before do
        @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
        @amend_date1 = PscTest::Fixtures.createDateObject(2008, 12, 10)      
        @amendment = PscTest::Fixtures.createAmendment("am1", @amend_date1, true)
        @amended_study = PscTest::Fixtures.setAmendmentForStudy(@study1, @amendment)
        application_context['studyService'].save( @amended_study) 
      end

      it "allows access to a specific amendment for an authorized user by specifying date" do
        get '/studies/NU480/template/amendments/2008-12-10', :as => :juno
        # puts response.entity
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.content_type.should == 'text/xml'
        xml_attribute("amendment", "name").should include("am1")
        xml_attribute("amendment", "date").should include("2008-12-10")
        xml_attribute("amendment", "mandatory").should include("true")
        response.xml_elements('//amendment').should have(1).elements
      end
    
  end
  
  describe "DELETE" do
    
    before do
      @study1 = PscTest::Fixtures.createSingleEpochStudy("NU480", "Treatment", ["A", "B"].to_java(:String))
      @amend_date1 = PscTest::Fixtures.createDateObject(2008, 12, 10)      
      @amendment = PscTest::Fixtures.createAmendment("am1", @amend_date1, true)
      @amended_study = PscTest::Fixtures.setAmendmentForStudy(@study1, @amendment)
      application_context['studyService'].save( @amended_study) 
    end

    it "allows deletion of a specific amendment for an authorized user by specifying date" do
      pending
      delete '/studies/NU480/template/amendments/2008-12-10', :as => :juno
      puts response.entity
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
    end
    
  end
  
end