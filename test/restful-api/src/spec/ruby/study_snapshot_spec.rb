#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

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

  it "forbids study templates access for unauthenticated users" do
    get "/studies/NU152/template/development", :as => nil
    response.status_code.should == 401
  end

  it "shows a study template to a study admin" do
    get "/studies/NU152/template/development", :as => :barbara

    response.status_code.should == 200
    response.status_message.should == "OK"
    response.content_type.should == 'text/xml'
    response.xml_attributes("epoch", "name").should include("Treatment1")
    response.xml_attributes("study-segment", "name").should include("A")
    response.xml_attributes("study-segment", "name").should include("B")
  end

  it "shows a study template to a study coordinator" do
    get "/studies/NU562/template/development", :as => :alice

    response.status_code.should == 200
    response.status_message.should == "OK"
    response.content_type.should == 'text/xml'
    response.xml_attributes("epoch", "name").should include("Treatment2")
    response.xml_attributes("study-segment", "name").should include("C")
    response.xml_attributes("study-segment", "name").should include("D")
  end

end
