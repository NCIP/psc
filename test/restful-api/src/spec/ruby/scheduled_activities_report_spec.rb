#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe "/reports/scheduled-activities.json" do

  before do
    #create study with an amendment
    study = PscTest::Fixtures.createSingleEpochStudy(
      "NU480", "Treatment", ["segment_A", "segment_B"].to_java(:String))
    amendment = PscTest::Fixtures.createAmendment("am", PscTest.createDate(2008, 12, 10))
    study.amendment = amendment
    application_context['studyService'].save(study)

    #create period and link to study
    period = PscTest::Fixtures.createPeriod("Period", 2, 4, 2)
    application_context['periodDao'].save(period)
    study.plannedCalendar.epochs.first.studySegments.first.addPeriod(period)

    #create planned activity and link to study
    source = PscTest::Fixtures.createSource("Malaria")
    application_context['sourceDao'].save(source)
    activityType = PscTest::Fixtures.createActivityType("Malaria Treatment")
    application_context['activityTypeDao'].save(activityType)
    activity = PscTest::Fixtures.createActivity(
      "Initial Diagnosis", "diag1", source, activityType, "Stage 1 diagnosis for malaria")
    application_context['activityDao'].save(activity)
    planned_activity = PscTest::Fixtures.createPlannedActivity(activity, 4)
    planned_activity1 = PscTest::Fixtures.createPlannedActivity(activity, 4)
    planned_activity1.setDetails("DetailsOneTwoAnd Three");
    planned_activity1.setCondition("If CBC>100")

    application_context['plannedActivityDao'].save(planned_activity)
    application_context['plannedActivityDao'].save(planned_activity1)
    period.addPlannedActivity(planned_activity)
    period.addPlannedActivity(planned_activity1)
    application_context['studyService'].save(study)

    #create a studysite
    studySite = PscTest::Fixtures.createStudySite(study, northwestern)
    application_context['studySiteDao'].save(studySite)

    #approve an existing amendment
    approve_date = PscTest.createDate(2008, 12, 20)
    studySite.approveAmendment(amendment, approve_date)
    application_context['studySiteDao'].save(studySite)

    #create subject
    subject = PscTest::Fixtures.createSubject(
      "ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))

    #create a study subject assignment
    studySegment = study.plannedCalendar.epochs.first.studySegments.first
    @studySubjectAssignment = application_context['subjectService'].assignSubject(
      studySite,
      Psc::Service::Presenter::Registration::Builder.new.
        subject(subject).first_study_segment(studySegment).
        study_subject_id("SS001").
        date(PscTest.createDate(2008, 12, 26)).manager(erin).
        to_registration)
  end

  describe "?responsible-user" do
    before do
      get "/reports/scheduled-activities.json?responsible-user=juno", :as => :erin
    end

    it "has the right value for filter responsible-user" do
      response.json["filters"]["responsible_user"].should == "juno"
    end
  end

  describe "?end-date" do
    before do
      get "/reports/scheduled-activities.json?end-date=2010-03-05", :as => :erin
    end

    it "has the right value for filter end_date" do
      response.json["filters"]["end_date"].should == "2010-03-05"
    end
  end

  describe "?start-date" do
    before do
      get "/reports/scheduled-activities.json?start-date=2010-03-02", :as => :erin
    end

    it "has the right value for filter start_date" do
      response.json["filters"]["start_date"].should == "2010-03-02"
    end
  end

  describe "?end-ideal-date" do
    before do
      get "/reports/scheduled-activities.json?end-ideal-date=2010-03-05", :as => :erin
    end

    it "has the right value for filter end_ideal_date" do
      response.json["filters"]["end_ideal_date"].should == "2010-03-05"
    end
  end

  describe "?start-ideal-date" do
    before do
      get "/reports/scheduled-activities.json?start-ideal-date=2010-02-05", :as => :erin
    end

    it "has the right value for filter start_ideal_date" do
      response.json["filters"]["start_ideal_date"].should == "2010-02-05"
    end
  end

  describe "?label" do
    before do
      get "/reports/scheduled-activities.json?label=a", :as => :erin
    end

    it "has the right value for the filter label" do
      response.json["filters"]["label"].should == "a"
    end
  end

  describe "?study" do
    before do
      get "/reports/scheduled-activities.json?study=a", :as => :erin
    end

    it "has the right value for filter study" do
      response.json["filters"]["study"].should == "a"
    end
  end

  describe "?site" do
    before do
      get "/reports/scheduled-activities.json?site=Northwestern%20University", :as => :erin
    end

    it "has the right value for filter site" do
      response.json["filters"]["site"].should == "Northwestern University"
    end
  end

  describe "?state" do
    before do
      get "/reports/scheduled-activities.json?state=Occurred", :as => :erin
    end

    it "has the right value for filter state" do
      response.json["filters"]["states"].should == ["Occurred"]
    end
  end

  describe "?start-date=&end-date=" do
    before do
      get "/reports/scheduled-activities.json?start-date=2008-12-28&end-date=2009-03-01", :as => :erin
    end

    it "contains the right number of filters" do
      response.json["filters"].size.should == 2
    end

    it "has the right values for filter start_date and end_date" do
      response.json["filters"]["start_date"].should == "2008-12-28"
      response.json["filters"]["end_date"].should == "2009-03-01"
    end

    it "has the right number of rows" do
      response.json["rows"].size.should == 4
    end
  end

  describe "?start-ideal-date=&end-ideal-date=" do
    before do
      get "/reports/scheduled-activities.json?start-ideal-date=2008-12-29&end-ideal-date=2008-12-31", :as => :erin
    end

    it "contains the right number of filters" do
      response.json["filters"].size.should == 2
    end

    it "has the right values for filter start_date and end_date" do
      response.json["filters"]["start_ideal_date"].should == "2008-12-29"
      response.json["filters"]["end_ideal_date"].should == "2008-12-31"
    end

    it "has the right number of rows" do
      response.json["rows"].size.should == 2
    end
  end

  describe "?start-date=&end-date='with activity scheduled_date as end_date which has time" do
    before do
      scheduled_activity = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities.first
      scheduled_activity_state_json = "{#{scheduled_activity.gridId} : { state : scheduled, reason : Add Time to the activity , date : 2008-12-28, time : '14:20' }}"
      post "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{scheduled_activity.gridId}", scheduled_activity_state_json,
           :as => :erin, 'Content-Type' => 'application/json'
      get "/reports/scheduled-activities.json?start-date=2008-12-27&end-date=2008-12-28", :as => :erin
    end

    it "contains the right number of filters" do
      response.json["filters"].size.should == 2
    end

    it "has the right values for filter start_date and end_date" do
      response.json["filters"]["start_date"].should == "2008-12-27"
      response.json["filters"]["end_date"].should == "2008-12-28"
    end

    it "has the right number of rows" do
      response.json["rows"].size.should == 1
    end
  end

  describe "content" do
    before do
      get "/reports/scheduled-activities.json?start-date=2008-12-28&end-date=2009-03-01", :as => :erin
    end

    it "has the right values for details" do
      response.json["rows"][1]["details"].should == "DetailsOneTwoAnd Three"
    end

    it "has the right values for reason" do
      response.json["rows"][1]["last_change_reason"].should == "Initialized from template"
    end

    it "has the right values for condition" do
      response.json["rows"][1]["condition"].should == "If CBC>100"
    end

    it "has the right values for study subject ID" do
      response.json["rows"][1]["study_subject_id"].should == "SS001"
    end

    describe 'for scheduled segment' do
      before do
        @segment = @studySubjectAssignment.scheduled_calendar.scheduled_study_segments.first
        @segment_json = response.json["rows"][1]['scheduled_study_segment']
        @segment_json.should_not be_nil
      end

      it 'has the public ID' do
        @segment_json['grid_id'].should == @segment.grid_id
      end

      it 'has the start day' do
        @segment_json['start_day'].should == 2
      end

      it 'has the start date' do
        @segment_json['start_date'].should == '2008-12-26'
      end
    end
  end
end
