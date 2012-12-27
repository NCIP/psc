#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "/subjects/{subject-identifier}/assignments/{assignment-identifier}/notifications" do
  before do
    # The released version of NU480
    @nu480 = create_study 'NU480' do |s|
      s.planned_calendar do |cal|
        cal.epoch "Treatment" do |e|
          e.study_segment "A" do |a|
            a.period "P1", :start_day => 2, :duration => [7, :day], :repetitions => 4 do |p|
              p.activity "Rituximab", 1
              p.activity "Alcohol", 4
            end
          end
        end
      end
    end
    #create subject
    @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
    #create studysite
    @studySite = PscTest::Fixtures.createStudySite(@nu480, northwestern)
    @studySite.approveAmendment(@nu480.amendment, PscTest.createDate(2008, 12, 25))
    application_context['studySiteDao'].save(@studySite)

    @studySegment = @nu480.planned_calendar.epochs.first.study_segments.first
    @studySubjectAssignment = application_context['subjectService'].assignSubject(
      @studySite,
      Psc::Service::Presenter::Registration::Builder.new.
        subject(@subject).first_study_segment(@studySegment).
        date(PscTest.createDate(2008, 12, 26)).manager(erin).
        to_registration)
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment)
    application_context['studyService'].scheduleReconsent(@nu480, PscTest.createDate(2008, 12, 27), "Reconsent Activity1")
    application_context['studyService'].scheduleReconsent(@nu480, PscTest.createDate(2008, 12, 28), "Reconsent Activity2")
    application_context['studyService'].scheduleReconsent(@nu480, PscTest.createDate(2008, 12, 29), "Reconsent Activity3")
  end

  describe "GET" do
    describe "xml" do
      before do
        get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications", :as => :erin
      end

      it "is successful" do
        response.status_code.should == 200
      end

      it "is XML" do
        response.content_type.should == 'text/xml'
      end

      it "has the right number of notifications" do
        response.xml_elements('//notification').should have(3).elements
      end
    end

    describe "json" do
      before do
        get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications.json", :as => :erin
      end

      it "is successful" do
        response.status_code.should == 200
      end

      it "is JSON" do
        response.content_type.should == 'application/json'
      end

      it "contains the right number of notifications" do
        response.json["notifications"].size.should == 3
      end
    end
  end

  describe "/{notification-identifier}" do
    before do
       @notification1 = @studySubjectAssignment.notifications[0]
    end

    describe "GET" do
      describe "xml" do
        before do
          get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications/#{@notification1.gridId}", :as => :erin
        end

        it "is successful" do
          response.status_code.should == 200
        end

        it "is XML" do
          response.content_type.should == 'text/xml'
        end

        it "has the right content" do
          response.xml_attributes("notification", "title").should include("Reconsent scheduled for 12/29/2008")
        end
      end

      describe "json" do
        before do
          get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications/#{@notification1.gridId}.json", :as => :erin
        end

        it "is successful" do
          response.should be_success
        end

        it "is JSON" do
          response.content_type.should == 'application/json'
        end

        it "contains the right number of notifications" do
          response.json["title"].should == "Reconsent scheduled for 12/29/2008"
        end
      end
    end

    describe "PUT" do
      it "updates the notification dismissed flag to true" do
          get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications/#{@notification1.gridId}.json", :as => :erin
          response.json["dismissed"].should == false
          @JSONentity = "{ dismissed: true }"
          put "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications/#{@notification1.gridId}", @JSONentity, :as => :erin, 'Content-Type' => 'application/json'
          response.should be_success
          get "/subjects/ID001/assignments/#{@studySubjectAssignment.gridId}/notifications/#{@notification1.gridId}.json", :as => :erin
          response.json["dismissed"].should == true
      end
    end
  end
end
