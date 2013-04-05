#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe "/subjects/{subject-identifier}" do
  before do
    @solo = PscTest::Fixtures.createSubject("X79", "Han", "Solo",
      PscTest.createDate(1872, 3, 6), Psc::Domain::Gender::MALE)
    @leia = PscTest::Fixtures.createSubject("C34", "Leia", "Organa",
      PscTest.createDate(1878, 1, 20), Psc::Domain::Gender::FEMALE)
    @leia.grid_id = "4567-LL47"
    @leia.properties << Psc::Domain::SubjectProperty.new("Hair color", "brown")
    @leia.properties << Psc::Domain::SubjectProperty.new("Only hope", "Obi-wan")

    [@solo, @leia].each do |s|
      application_context["subjectDao"].save(s)
    end

    study = PscTest::Fixtures.createBasicTemplate("NU 480")
    application_context["studyService"].save(study)

    pitt480 = PscTest.make_template_available(study, pittsburgh)

    ssa = application_context['subjectService'].assignSubject(
      pitt480,
      Psc::Service::Presenter::Registration::Builder.new.
        subject(@leia).
        first_study_segment(study.planned_calendar.epochs.first.study_segments.first).
        date(PscTest.createDate(1900, 12, 1)).
        to_registration)
  end

  describe "GET" do
    it "can look up subjects by grid ID" do
      get "/subjects/4567-LL47.json", :as => erin

      response.json['first_name'].should == 'Leia'
    end

    it "can look up subjects by person ID" do
      get "/subjects/C34.json", :as => erin

      response.json['first_name'].should == 'Leia'
    end

    describe "authorization" do
      it "forbids access to unassociated subjects" do
        get "/subjects/X79.json", :as => erin

        response.status_code.should == 403
      end

      it "forbids access to subjects not associated with the user's site" do
        get "/subjects/C34.json", :as => darlene

        response.status_code.should == 403
      end

      it "allows access to subjects associated with the user's site" do
        get "/subjects/C34.json", :as => erin

        response.status_code.should == 200
      end
    end

    describe "JSON representation" do
      before do
        get "/subjects/C34.json", :as => erin

        response.status_code.should == 200
      end

      it "has the first name" do
        response.json['first_name'].should == 'Leia'
      end

      it "has the last name" do
        response.json['last_name'].should == 'Organa'
      end

      it "has the full name" do
        response.json['full_name'].should == 'Leia Organa'
      end

      it "has the last, first name" do
        response.json['last_first'].should == 'Organa, Leia'
      end

      it "has the gender" do
        response.json['gender'].should == 'Female'
      end

      it "has the birth date" do
        response.json['birth_date'].should == '1878-01-20'
      end

      it "has the person ID" do
        response.json['person_id'].should == 'C34'
      end

      it "has properties" do
        response.json['properties'].should == [
          { 'name' => 'Hair color', 'value' => 'brown' },
          { 'name' => 'Only hope', 'value' => 'Obi-wan' }
        ]
      end

      it "includes a link to the subject's schedule" do
        response.json['href'].should == {
          'schedules' => "#{psc_url}/api/v1/subjects/C34/schedules"
        }
      end
    end
  end

  describe "PUT" do
    describe "of a new subject" do
      before do
        @subject_json = {
          :first_name => 'Luke', :last_name => 'Starkiller',
          :person_id => 'TK-421'
        }.to_json
      end

      it "is blocked to authorized users" do
        put "/subjects/TK-421", @subject_json,
          'Content-Type' => 'application/json', :as => :erin

        response.status_code.should == 400
        response.entity.should =~ /This resource can not create new subjects../
        response.entity.should =~ /New subjects may only be created during registration./
      end

      it "does not leak the newness of the subject to unauthorized users" do
        put "/subjects/TK-421", @subject_json,
          'Content-Type' => 'application/json', :as => :hannah

        response.status_code.should == 403
      end
    end

    describe "of an existing subject" do
      def subject_json(overrides={})
        {
          :first_name => 'Leia', :last_name => 'Organa',
          :person_id => 'C34', :gender => 'Female',
          :birth_date => '1878-01-20',
          :properties => [
            { 'name' => 'Hair color', 'value' => 'brown' },
            { 'name' => 'Only hope', 'value' => 'Obi-wan' }
          ]
        }.merge(overrides).to_json
      end

      it "is not allowed unless the user is a subject manager" do
        put "/subjects/C34", subject_json,
          'Content-Type' => 'application/json', :as => :hannah

        response.status_code.should == 403
      end

      it "is not allowed unless the SM has a site relationship with the subject" do
        put "/subjects/C34", subject_json,
          'Content-Type' => 'application/json', :as => :darlene

        response.status_code.should == 403
      end

      [
        [:first_name, 'Leah'],
        [:last_name,  'Solo'],
        [:gender,     'Not reported', Psc::Domain::Gender::NOT_REPORTED],
        [:person_id,  'C43'],
      ].each do |attribute, submitted_value, expected_value|
        it "can update #{attribute}" do
          put "/subjects/C34", subject_json(attribute => submitted_value),
            'Content-Type' => 'application/json', :as => :erin

          response.status_code.should == 200
          reloaded = application_context['subjectDao'].getById(@leia.id)
          reloaded.send(attribute).should == (expected_value || submitted_value)
        end
      end

      it "can update the birth date" do
        put "/subjects/C34", subject_json(:birth_date => "1878-01-20"),
          'Content-Type' => 'application/json', :as => :erin

        response.status_code.should == 200
        reloaded = application_context['subjectDao'].getById(@leia.id)

        actual = Time.at(reloaded.date_of_birth.time/1000)
        actual.year.should == 1878
        actual.month.should == 1
        actual.day.should == 20
      end

      it "can update properties" do
        entity = subject_json(:properties => [
          { 'name' => 'Only hope', 'value' => 'Obi-wan' },
          { 'name' => 'Aliases', 'value' => 'Boushh' },
          { 'name' => 'Hair color', 'value' => 'braun' }
        ])
        put "/subjects/C34", entity,
          'Content-Type' => 'application/json', :as => :erin

        response.status_code.should == 200
        reloaded = application_context['subjectDao'].getById(@leia.id)

        reloaded.properties.collect { |prop|
          [prop.name, prop.value]
        }.should == [
          ['Only hope', 'Obi-wan'],
          ['Aliases', 'Boushh'],
          ['Hair color', 'braun']
        ]
      end

      it "can remove properties" do
        entity = subject_json(:properties => [
          { 'name' => 'Only hope', 'value' => 'Obi-wan' }
        ])
        put "/subjects/C34", entity,
          'Content-Type' => 'application/json', :as => :erin

        response.status_code.should == 200
        reloaded = application_context['subjectDao'].getById(@leia.id)

        reloaded.properties.collect { |prop|
          [prop.name, prop.value]
        }.should == [
          ['Only hope', 'Obi-wan']
        ]
      end
    end
  end
end
