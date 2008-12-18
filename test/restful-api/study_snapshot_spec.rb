describe "/study_snapshot" do
  before do
    @studies = [
      # Released, but not approved studies
      @nu152 = PscTest::Fixtures.createSingleEpochStudy("NU152", "Treatment1", ["A", "B"].to_java(:String)),
      @nu562 = PscTest::Fixtures.createSingleEpochStudy("NU562", "Treatment2", ["C", "D"].to_java(:String))     
    ]
    @studies.each do|s|
      application_context['studyService'].createInDesignStudyFromExamplePlanTree(s)
      application_context['studyService'].save(s)
    end
  end
  
  def xml_attribute(element, attribute_name)
    response.xml_elements('//' + element).collect { |s| s.attributes[attribute_name] }
  end
  
  it "forbids study templates access for unauthenticated users" do
    get "/studies/NU152/template/development", :as => nil
    response.status_code.should == 401
  end

  it "shows a study template to a study admin" do
    get "/studies/NU152/template/development", :as => :barbara
    # puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.content_type.should == 'text/xml'
    xml_attribute("epoch", "name").should include("Treatment1")
    xml_attribute("study-segment", "name").should include("A")
    xml_attribute("study-segment", "name").should include("B")
  end
    
  it "shows a study template to a study coordinator" do
    get "/studies/NU562/template/development", :as => :alice
    #puts response.entity
    response.status_code.should == 200
    response.status_message.should == "OK"
    response.content_type.should == 'text/xml'
    xml_attribute("epoch", "name").should include("Treatment2")
    xml_attribute("study-segment", "name").should include("C")
    xml_attribute("study-segment", "name").should include("D")
  end 
  
end