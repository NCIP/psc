describe "/study" do
  before do
    @studies = [
      # Released, but not approved studies
      @nu481 = PscTest::Fixtures.createSingleEpochStudy("NU481", "Treatment1", ["A", "B"].to_java(:String)),
      @nu561 = PscTest::Fixtures.createSingleEpochStudy("NU561", "Treatment1", ["C", "D"].to_java(:String))  
    ]
    @studies.each do|s|
      application_context['studyService'].createInDesignStudyFromExamplePlanTree(s)
      application_context['studyService'].save(s)
    end    
  end

  #get methods test
  describe "GET" do

    it "forbids study templates access for unauthenticated users" do
      get "/studies/NU481/template", :as => nil
      response.status_code.should == 401
    end

    it "shows a specific template to a study admin given the study-identifier" do
      get "/studies/NU481/template", :as => :barbara
      #puts response.entity
      #puts @nu481
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
    end
  
  end
  
  
  describe "PUT" do
    before do
      @nu482_xml = psc_xml("study", 'assigned-identifier' => "NU482") { |ss|
        ss.tag!('planned-calendar', 'id' =>'pc1') 
        ss.tag!('development-amendment', 'name' => 'am1', 'date' => "2008-11-13", 'mandatory' => "true"){ |da|
          da.tag!('planned-calendar-delta', 'id' => 'd1', 'node-id' => 'pc1') { |pcd| 
            pcd.tag!('add', 'id' => 'add1') { |add|
              add.tag!('epoch','id' => 'epoch1', 'name' => 'Treatment2') {|seg|
                seg.tag!('study-segment', 'id' => "segment1", 'name' => 'initial study')
              }
            }
          }
        }
      }
    end
    
    it "does not accept a study from a study admin" do
      put '/studies/NU482/template', @nu482_xml, :as => :barbara
      response.status_code.should == 403
    end
    
    it "it accepts a study from a study coordinator" do
      # puts @nu482_xml
      put '/studies/NU482/template', @nu482_xml, :as => :juno
      response.status_code.should == 201 #created
      get '/studies/', :as => :alice
      # puts response.entity
      response.status_code.should == 200 #OK
      response.xml_attributes("study", "assigned-identifier").should include("NU482")
    end
    
  end
  
end