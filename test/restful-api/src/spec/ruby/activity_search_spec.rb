START = [
  "Bone Marrow Aspirate",
  "Bone Marrow Biopsy (bilateral)",
  "Bone Marrow Biopsy (unilateral)",
  "Bone Scan", "Breast exam"
]
  
describe "/activities" do
  describe "GET" do
    it "returns all sources without parameters" do
      get '/activities', :as => :alice
      response.status_code.should == 200
      response.xml_elements("//source").should have(1).elements
      response.xml_attributes("source", "name").should include("Northwestern University")
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

    it "limits the number of activities with limit=" do
      get '/activities?limit=5', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 5
    end
    
    it "offsets the activity list with offset=" do
      get '/activities?offset=2', :as => :alice
      response.status_code.should == 200
      activity_names.should_not include( START[0] )
      activity_names.should_not include( START[1] )
    end
  end
end

describe "/activities.json" do
  def activity_names
    response.json['activities'].collect{|a| a['activity_name']}
  end

  def response_activity_count
    activity_names.count
  end

  describe "GET" do
    it "returns all activities" do
      get '/activities.json', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 1042
    end

    it "limits the number of activities with limit=" do
      get '/activities.json?limit=5', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 5
      activity_names.sort.should == START
    end

    it "offsets the activity list with offset=" do
      get '/activities.json?offset=2', :as => :alice
      response.status_code.should == 200
      activity_names.should_not include( START[0] )
      activity_names.should_not include( START[1] )
    end

    it "limits and offsets the activity list with limit= & offset=" do
      get '/activities.json?limit=50&offset=2', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 50
      activity_names.should_not include( START[0] )
      activity_names.should_not include( START[1] )
    end

    it "searches activity codes from q=" do
      get '/activities.json?q=788', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 1
      activity_names.first.should == "T3"
    end
  end
end
