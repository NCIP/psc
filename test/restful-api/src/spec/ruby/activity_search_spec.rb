describe "/activities" do
  before do
    @manual_source = application_context['sourceDao'].get_by_name('PSC - Manual Activity Creation')
    @manual_source.should_not be_nil
  end

  describe "GET" do
    it "returns all sources without parameters" do
      get '/activities', :as => :alice
      response.status_code.should == 200
      response.xml_elements("//source").should have(2).elements
      response.xml_attributes("source", "name").should include("Northwestern University")
      response.xml_attributes("source", "name").should include("PSC - Manual Activity Creation")
    end

    def response_activity_count
      response.xml_elements('//activity').size
    end

    def activity_names
      @response_activity_names ||= response.xml_attributes("activity", "name")
    end

    it "limits to a single activity type with type=" do
      get '/activities?type=Other', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 55
      response.xml_attributes("activity", "type").uniq.should have(1).kind
    end

    it "does not match partial activity types with type=" do
      get '/activities?type=Oth', :as => :alice
      response.status_code.should == 400
      response.entity.should include("Unknown activity type: Oth")
    end

    it "searches activity names from q=" do
      get '/activities?q=CT%3A', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 5
      activity_names.should include("CT: Abdomen")
      activity_names.should include("CT: Chest")
      activity_names.should include("CT: head")
      activity_names.should include("CT: Other")
      activity_names.should include("CT: Pelvis")
    end

    it "searches activity codes from q=" do
      get '/activities?q=788', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 1
      activity_names.first.should == "T3"
    end

    it "obeys type= and q= simultaneously" do
      get '/activities?q=bone&type=Lab+Test', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 3
      activity_names.should include("Bone Marrow Biopsy")
      activity_names.should include("Bone Marrow Cultures")
      activity_names.should include("serum bone alkaline phosphatase")
    end

    it "searches single source activities with activity type=" do
      get'/activities/Northwestern%20University?type=Intervention', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 246
      response.xml_attributes("activity", "type").uniq.should have(1).kind
    end

    it "searches single source activities with q= and type=" do
      get'/activities/Northwestern%20University?q=HLA&type=Lab+Test',:as => :alice
      response.status_code.should == 200
      response_activity_count.should == 3
      response.xml_attributes("activity","type").uniq.should have(1).kind
    end
  end
end
