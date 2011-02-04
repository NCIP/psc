describe "/studies/{study-identifier}/sites/{site-identifier}/subject-assignments" do

  before do
    #create a study with an amendment
    @nu480 = PscTest::Fixtures.createBasicTemplate("NU480")
    (@treatmentA, @treatmentB) = [*@nu480.planned_calendar.epochs.first.study_segments]
    @treatmentA.grid_id = "segment1" # replace auto-generated study-segment id
    @treatmentB.grid_id = "segment2"
    application_context['studyService'].save(@nu480)

    # Make template available to Pittsburgh
    @nu480_at_pitt = PscTest::Fixtures.createStudySite(@nu480, pittsburgh)
    @nu480_at_pitt.approveAmendment(@nu480.amendment, PscTest.createDate(2008, 12, 31))
    application_context['studySiteDao'].save(@nu480_at_pitt)
  end

  describe "GET" do
    before do
      # create subject
      @alan = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
      @bob = PscTest::Fixtures.createSubject("ID002", "Bob", "Boyarski", PscTest.createDate(1985, 5, 1))

      # register alan
      application_context['subjectService'].assignSubject(
        @nu480_at_pitt,
        Psc::Service::Presenter::Registration::Builder.new.
          subject(@alan).first_study_segment(@treatmentA).date(PscTest.createDate(2008, 12, 26)).
          study_subject_id("NU480-001").manager(erin).to_registration)

      # register bob
      application_context['subjectService'].assignSubject(
        @nu480_at_pitt,
        Psc::Service::Presenter::Registration::Builder.new.
          subject(@bob).first_study_segment(@treatmentB).date(PscTest.createDate(2008, 1, 13)).
          study_subject_id("NU480-002").manager(erin).to_registration)
    end

    it "forbids access to a subject assignment to an unauthorized user" do
      get "/studies/NU480/sites/PA015/subject-assignments", :as => :carla
      response.status_code.should == 403
    end

    it "allows access to an existing subject assignment to an authorized user" do
      get "/studies/NU480/sites/PA015/subject-assignments", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("subject", "first-name").should include("Bob")
      response.xml_attributes("subject", "first-name").should include("Alan")
      response.xml_attributes("subject", "person-id").should include("ID001")
      response.xml_attributes("subject", "person-id").should include("ID002")
      response.xml_elements('//subject').should have(2).elements
    end
  end

  describe "POST" do
    def subject_registration_xml(overrides={})
      psc_xml("registration",
        {
          'first-study-segment-id' => "segment1", 'date' => "2008-12-27",
          'subject-coordinator-name' => "erin", 'desired-assignment-id' => 'POP-4'
        }.merge(overrides[:registration] || {})
      ) { |registration|
        registration.subject(
          {
            'first-name' => "Andre", 'last-name' => "Suzuki",
            'birth-date' => "1982-03-12", 'person-id' => "ID006", 'gender'=> "Male"
          }.merge(overrides[:subject] || {})
        ) { |subject|
          yield subject if block_given?
        }
      }
    end

    it "does not allow the subject coordinator to assign patients when the template has not been made available" do
      post "/studies/NU480/sites/IL036/subject-assignments",
        subject_registration_xml, :as => :darlene

      response.status_code.should == 403
    end

    it "allows creation of a new subject-assignment for an authorized user" do
      post "/studies/NU480/sites/PA015/subject-assignments", subject_registration_xml, :as => :erin
      response.status_code.should == 201
      response.status_message.should == "Created"
      response.meta['location'].should =~ %r(studies/NU480/schedules/POP-4)
    end

    it "uses the specified assignment ID" do
      post "/studies/NU480/sites/PA015/subject-assignments", subject_registration_xml, :as => :erin

      application_context["studySubjectAssignmentDao"].getByGridId("POP-4").should_not be_nil
    end

    it "gives 422 if studySegment with gridId from xml not found in system" do
      xml = subject_registration_xml(
        :registration => { 'first-study-segment-id' => "unknownSegment" }
      )

      post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :erin
      response.status_code.should == 422
      response.entity =~ %r(Study Segment with grid id unknownSegment not found.)
    end

    it "uses the users's subject-managerness to determine whether a new subject may be created" do
      xml = subject_registration_xml(
        :registration => { 'subject-coordinator-name' => 'erin' }
      )

      post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :iris
      response.status_code.should == 403
      response.entity =~ %r(iris may not create new subjects.)
    end

    describe "with subject properties" do
      before do
        xml = subject_registration_xml { |subject|
          subject.property :name => "Car make", :value => "Nissan"
          subject.property :name => "Phone number", :value => "312-503-1212"
        }

        post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :erin
        response.status_code.should == 201
      end

      it "stores the properties" do
        subject = application_context["subjectDao"].get_by_person_id("ID006")
        subject.properties.collect { |p| [p.name, p.value] }.should == [
            ["Car make", "Nissan"], ["Phone number", "312-503-1212"]
        ]
      end

      it "merges the properties for a subsequent registration of the same person" do
        xml = subject_registration_xml(:registration => {
            'date' => '2010-01-04', 'desired-assignment-id' => 'POP-234'
        }) { |subject|
          subject.property :name => "E-mail address", :value => "suzuki@gmail.com"
          subject.property :name => "Car make", :value => "Toyota"
        }

        post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :erin
        response.status_code.should == 201

        subject = application_context["subjectDao"].get_by_person_id("ID006")
        subject.properties.collect { |p| [p.name, p.value] }.should == [
          ["Car make", "Toyota"],
          ["Phone number", "312-503-1212"],
          ["E-mail address", "suzuki@gmail.com"],
        ]
      end
    end

    it "fails creation of a new subject with wrong gender" do
      xml = subject_registration_xml(:subject => { "gender" => "female too" })

      post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
      response.entity.
        should include("The specified gender 'female too' is invalid: Please check the spelling")
    end

    [%w(Male Male),
     %w(MALE Male),
     %w(M Male),
     %w(mAle Male),
     %w(Female Female),
     %w(F Female),
     %w(FEMALE Female),
     %w(unknown Unknown),
     %w(Unknown Unknown), ['NOT REPORTED', 'Not Reported']
     ].each do |valid_gender, canonical_gender|
      it "allows the creation of a new subject with gender '#{valid_gender}' " do
        # create XML using valid_gender, do POST, check that it was successful
        xml = subject_registration_xml(:subject => { 'gender' => valid_gender })

        post "/studies/NU480/sites/PA015/subject-assignments", xml, :as => :erin
        response.status_code.should == 201
        application_context['subjectDao'].
          findSubjectByPersonId('ID006').gender.displayName.should == canonical_gender
      end
    end
  end

end
