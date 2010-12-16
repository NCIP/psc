describe "/activity" do

 describe "PUT" do
    before do
      @source = PscTest::Fixtures.createSource("Diabetes")
      application_context['sourceDao'].save(@source)
      @activity_xml = psc_xml("activity", 'id' => "id1", 'type' => "DiabetesTreatment", 'type-id' => 23,
      'name' => "DiabetesTreatment1", 'code' => "Code1", 'source' => "Diabetes")
    end

    it "forbids activity creation for an unauthorized user" do
        put '/activities/Diabetes/Code1', @activity_xml, :as => :zelda
        response.status_code.should == 403
    end

    it "forbids creation of a specific activity to an authorized user when source has not yet existed" do
        put '/activities/NewSoure/Code1', @activity_xml, :as => :juno
        response.status_code.should == 400
        response.status_message.should == "Bad Request"
    end

    it "creates a specific activity for an authorized user when the source exists" do
        put '/activities/Diabetes/Code1', @activity_xml, :as => :juno #creates activity
        response.status_code.should == 201
        response.status_message.should == "Created"
        response.content_type.should == 'text/xml'
        response.xml_attributes("activity", "name").should include("DiabetesTreatment1")
        response.xml_attributes("activity", "type").should include("DiabetesTreatment")
        response.xml_attributes("activity", "source").should include("Diabetes")
        response.xml_elements('//activity').should have(1).elements
    end

    describe "updates properties of" do
      before do
         @activityType = PscTest::Fixtures.createActivityType("MalariaTreatment")
         application_context['activityTypeDao'].save(@activityType)
         @activity = PscTest::Fixtures.createActivity("InitialDiagnosis", "ActivityCode",@source, @activityType, "diagnosis for diabetes")
         application_context['activityDao'].save(@activity)
         @updateActivity_xml = psc_xml("activity", 'name' => "InitialDiagnosis", 'code' => "UpdatedActivityCode", 'source' => "Diabetes",
          'type' => @activityType, 'description' => "diagnosis for diabetes")
      end

      it "the existing activity" do
         put '/activities/Diabetes/ActivityCode', @updateActivity_xml, :as => :juno
         response.status_code.should == 200
         response.content_type.should == 'text/xml'
         response.xml_attributes("activity", "code").should include("UpdatedActivityCode")
      end
    end

  end

  describe "GET" do
      before do
        @source1 = PscTest::Fixtures.createSource("Malaria")
        application_context['sourceDao'].save(@source1)
        @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
        application_context['activityTypeDao'].save(@activityType1)
        @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
        application_context['activityDao'].save(@activity1)
      end

      it "forbids access to a specific activity for an unauthorized user" do
        get '/activities/Malaria/diag1', :as => :zelda
        response.status_code.should == 403
      end

      it "allows access to a specific activity for an authorized user" do
        get '/activities/Malaria/diag1', :as => :juno
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.content_type.should == 'text/xml'
        response.xml_attributes("activity", "name").should include("Initial Diagnosis")
        response.xml_attributes("activity", "type").should include("Malaria Treatment")
        response.xml_attributes("activity", "source").should include("Malaria")
        response.xml_elements('//activity').should have(1).elements
      end
    end

    describe "DELETE" do
      before do
        @source1 = PscTest::Fixtures.createSource("Malaria")
        application_context['sourceDao'].save(@source1)
        @activityType1 = PscTest::Fixtures.createActivityType("Malaria Treatment")
        application_context['activityTypeDao'].save(@activityType1)
        @activity1 = PscTest::Fixtures.createActivity("Initial Diagnosis", "diag1", @source1, @activityType1, "Stage 1 diagnosis for malaria")
        application_context['activityDao'].save(@activity1)
      end

      it "deletes a specific activity" do
        #Verify Before Delete
        get '/activities/Malaria/diag1', :as => :juno
        response.status_code.should == 200
        response.xml_attributes("activity", "source").should include("Malaria")
        response.xml_elements('//activity').should have(1).elements
        #Delete the Activity
        delete '/activities/Malaria/diag1', :as => :juno
        response.status_code.should == 200
        response.status_message.should == "OK"
        #Check after delete
        get '/activities/Malaria/diag1', :as => :juno
        response.status_code.should == 404
        response.status_message.should == "Not Found"
      end
    end




end
