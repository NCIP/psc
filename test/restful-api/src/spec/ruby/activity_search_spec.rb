#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

START = [
  "Bone Marrow Aspirate",
  "Bone Marrow Biopsy (bilateral)",
  "Bone Marrow Biopsy (unilateral)",
  "Bone Scan", "Breast exam"
]

module ActivitySearch
  module HelperMethods
    module Xml
      def response_activity_count
        activity_names.size
      end

      def activity_names
        response.xml_attributes("activity", "name").reject{|a| a =~ /Reconsent/i } # Somehow a reconsent activity is getting in the activity list
      end

      def activity_types
        response.xml_attributes("activity", "type")
      end
    end
    module Json
      def activity_names
        response.json['activities'].collect{|a| a['activity_name']}.reject{|a| a =~ /Reconsent/i } # Somehow a reconsent activity is getting in the activity list
      end

      def activity_types
        response.json['activities'].collect{|a| a['activity_type']}
      end

      def response_activity_count
        activity_names.count
      end
    end
  end


  module Examples
    class << self
      def included(mod)
        mod.class_eval do
          def url
            self.class.url
          end
          raise "A class method named url must be defined in order to include these examples" unless url


          if url =~ /.*\.json$/
            include ActivitySearch::HelperMethods::Json
          else
            include ActivitySearch::HelperMethods::Xml
          end

          def activities_with(s)
            "#{url}?#{s}"
          end

          it "limits to single activity type activity type=" do
            get activities_with('type=Intervention'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 246
            activity_types.compact.uniq.should have(1).kind
          end

          it "does not match partial activity types with type=" do
            get activities_with('type=Oth'), :as => :alice
            response.status_code.should == 400
            response.entity.should include("Unknown activity type: Oth")
          end

          it "searches activity names from q=" do
            get activities_with('q=CT%3A'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 5
            activity_names.should include("CT: Abdomen")
            activity_names.should include("CT: Chest")
            activity_names.should include("CT: head")
            activity_names.should include("CT: Other")
            activity_names.should include("CT: Pelvis")
          end

          it "searches activity codes from q=" do
            get activities_with('q=788'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 1
            activity_names.first.should == "T3"
          end

          it "obeys type= and q= simultaneously" do
            get activities_with('q=bone&type=Lab+Test'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 3
            activity_names.should include("Bone Marrow Biopsy")
            activity_names.should include("Bone Marrow Cultures")
            activity_names.should include("serum bone alkaline phosphatase")
            activity_types.compact.uniq.should have(1).kind
          end

          it "searches single source activities with activity type=" do
            get activities_with('type=Intervention'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 246
            activity_types.compact.uniq.should have(1).kind
          end

          it "limits the number of activities with limit=" do
            get activities_with('limit=5'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 5
          end

          it "offsets the activity list with offset=" do
            get activities_with('offset=2'), :as => :alice
            response.status_code.should == 200
            activity_names.should_not include( START[0] )
            activity_names.should_not include( START[1] )
          end

          it "limits and offsets the activity list with limit= & offset=" do
            get activities_with('limit=50&offset=2'), :as => :alice
            response.status_code.should == 200
            response_activity_count.should == 50
            activity_names.should_not include( START[0] )
            activity_names.should_not include( START[1] )
          end

          it "sorts the activities by activity type with sort=" do
            get activities_with('sort=activity_type&order=asc'), :as => :alice
            response.status_code.should == 200
            activity_types[0].should == "Disease Measure"
          end

          it "sorts the activities by activity name with sort=" do
            get activities_with('sort=activity_name'), :as => :alice
            response.status_code.should == 200
            activity_names[0].should == "11-Deoxycortisol (Compound S)"
          end

          it "controls the sort order with order=" do
            get activities_with('sort=activity_name&order=desc'), :as => :alice
            response.status_code.should == 200
            activity_names[0].should == "Zoladex"
          end
        end
      end
    end
  end
end

describe "/activities" do
  describe "GET" do
    def self.url
      '/activities'
    end
    include ActivitySearch::Examples

    it "returns all sources without parameters" do
      get '/activities', :as => :alice
      response.status_code.should == 200
      response.xml_elements("//source").should have(1).elements
      response.xml_attributes("source", "name").should include("Northwestern University")
    end
  end
end

describe "/activities.json" do
  describe "GET" do
    def self.url
      '/activities.json'
    end
    include ActivitySearch::Examples

    it "returns all activities" do
      get '/activities.json', :as => :alice
      response.status_code.should == 200
      response_activity_count.should == 1041
    end
  end
end

describe "/activities/{activity-source-name}" do
  describe "GET" do
    def self.url
      '/activities/Northwestern%20University'
    end
    include ActivitySearch::Examples
  end
end

describe "/activities/{activity-source-name}.json" do
  describe "GET" do
    def self.url
      '/activities/Northwestern%20University.json'
    end
    include ActivitySearch::Examples
  end
end