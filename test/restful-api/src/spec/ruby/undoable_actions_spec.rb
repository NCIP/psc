#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "/subjects/{subject-identifier}/schedules/undoable-actions" do
    def create_user_action(description, action_type, user, context)
      action = user_action(action_type, description, context)
      post "/user-actions", action, :as => user, 'Content-Type' => 'application/json'
      response.meta['location']
    end

    def user_action(action_type, description, context)
      attrs = {}
      attrs['context'] = context
      attrs['action_type'] = action_type
      attrs['description'] = description

      <<-JSON
        {
          #{attrs.collect{|a,b| "#{a}: " + (b ? "\"#{b}\"" : "null")}.join(', ')}
        }
      JSON
    end

    def make_request(user)
      get "/subjects/ID001/schedules/undoable-actions.json", :as => user
    end

    before do
      # The released version of NU480
      @nu480 = create_study 'NU480' do |s|
        s.planned_calendar do |cal|
          cal.epoch "Treatment" do |e|
            e.study_segment "A" do |a|
              a.period "P1", :start_day => 2, :duration => [4, :day], :repetitions => 2 do |p|
                p.activity "Rituximab", 1
                p.activity "Alcohol", 3
              end
            end
          end
        end
      end

      @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
      @studySite = PscTest::Fixtures.createStudySite(@nu480, northwestern)
      @studySite.approveAmendment(@nu480.amendment, PscTest.createDate(2008, 12, 25))
      application_context['studySiteDao'].save(@studySite)

      @studySegment = @nu480.planned_calendar.epochs.first.study_segments.first
      @studySubjectAssignment = application_context['subjectService'].assignSubject(
          @studySite,
          Psc::Service::Presenter::Registration::Builder.new.
            subject(@subject).first_study_segment(@studySegment).
            date(PscTest.createDate(2008, 12, 26)).
            study_subject_id("SS001").manager(erin).to_registration)
      application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment)
      @scheduled_activities = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities
      @context_uri = full_uri("/subjects/#{@subject.gridId}/schedules");
     end

  describe "GET" do
    before do
      @ua_header1 = create_user_action("Delay by 2 days", "delay", erin, @context_uri)
      @ua_header2 = create_user_action("Canceled on 2009-12-29", "canceled", erin, @context_uri)

      @JSONentity1 = "{#{@scheduled_activities[0].gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-28 },
                      #{@scheduled_activities[1].gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity1, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header1

      @JSONentity2 = "{ #{@scheduled_activities[1].gridId} : { state : canceled, reason : Just canceled , date : 2009-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity2, :as => :erin ,
           'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header2
    end

    describe "json" do
      before do
        get "/subjects/ID001/schedules/undoable-actions.json", :as => :erin
      end

      it "is successful with 200 success code return" do
        response.status_code.should == 200
      end

      it "is json" do
        response.content_type.should == 'application/json'
      end
      
      it "contains the right number of undoable actions" do
        response.json["undoable_actions"].size.should == 2
      end
      
      describe "the structure" do
        it "has the context uri" do
          response.json['context'].should == @context_uri
        end
        
        describe "[undoable_actions]" do
          before do
            @undoable_action = response.json['undoable_actions'][0]
          end

          it "has the description" do
            @undoable_action['description'].should == "Canceled on 2009-12-29"
          end

          it "has the context" do
            @undoable_action['context'].should == @context_uri
          end

          it "has the URI" do
            @undoable_action['URI'].should == @ua_header2
          end

          it "has the time" do
            @undoable_action['time'].should_not be_nil
          end

          it "has the action type" do
            @undoable_action['action_type'].should == "canceled"
          end
        end
      end
    end

    it "contains all valid user actions as undoable actions" do
      ua_header = create_user_action("Rescheduled for 2009-12-31", "scheduled", erin, @context_uri)
      @JSONentity = "{#{@scheduled_activities[2].gridId} : { state : scheduled, reason : Rescheduled on another day , date : 2009-12-31 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
                :as => :erin , 'Content-Type' => 'application/json', 'X-PSC-User-Action' => ua_header
      make_request(erin)
      response.json["undoable_actions"].size.should == 3
    end

    it "doesn't contain as undoable action if user action's user is not current user" do
      ua_header = create_user_action("Rescheduled for 2009-12-31", "scheduled", darlene, @context_uri)
      @JSONentity = "{#{@scheduled_activities[2].gridId} : { state : scheduled, reason : Rescheduled on another day , date : 2009-12-31 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
                :as => :erin , 'Content-Type' => 'application/json', 'X-PSC-User-Action' => ua_header
      make_request(erin)
      response.json["undoable_actions"].size.should == 2
    end
    
    it "doesn't contain as undoable action if there is no associated audit event for user action" do
      ua_header = create_user_action("Rescheduled for 2009-12-31", "scheduled", erin, @context_uri)
      make_request(erin)
      response.json["undoable_actions"].size.should == 2
    end
    
    it "doesn't contain as undoable action if user action is already undone" do
      ua_header = create_user_action("Rescheduled for 2009-12-31", "scheduled", erin, @context_uri)
      ua = application_context['userActionDao'].getByGridId(ua_header.split('/').last)
      ua.undone = true
      application_context['userActionDao'].save(ua)
      @JSONentity = "{#{@scheduled_activities[2].gridId} : { state : scheduled, reason : Rescheduled on another day , date : 2009-12-31 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
                :as => :erin , 'Content-Type' => 'application/json', 'X-PSC-User-Action' => ua_header
      make_request(erin)
      response.json["undoable_actions"].size.should == 2
    end

    it "doesn't contain as undoable action if there is already recent update on same object without any associated user action" do
      @JSONentity = "{ #{@scheduled_activities[0].gridId} : { state : scheduled, reason : Rescheduled event , date : 2009-12-30 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity, :as => :erin ,'Content-Type' => 'application/json'
      make_request(erin)
      response.json["undoable_actions"].size.should == 1
    end

    it "doesn't contain as undoable action if there is already recent update on same object with user action of different context" do
      another_context = full_uri("/subjects/another/schedules");
      ua_header = create_user_action("Delay for 2 days", "delay", erin, another_context)
      @JSONentity = "{ #{@scheduled_activities[0].gridId} : { state : scheduled, reason : Rescheduled event , date : 2009-12-30 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => ua_header
      make_request(erin)
      response.json["undoable_actions"].size.should == 1
    end

    it "doesn't contain as undoable action if there is already recent update on same object with user action of different user" do
      ua_header = create_user_action("Rescheduled for 2009-12-31", "scheduled", juno, @context_uri)
      @JSONentity = "{ #{@scheduled_activities[0].gridId} : { state : scheduled, reason : Rescheduled event , date : 2009-12-30 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity, :as => :juno ,
             'Content-Type' => 'application/json', 'X-PSC-User-Action' => ua_header
      make_request(erin)
      response.json["undoable_actions"].size.should == 1
    end
    
    it "gives 404 when there is no matching subject" do
      get "/subjects/ID002/schedules/undoable-actions.json", :as => :erin
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gives 403 status if user doesn't have access to the subject's undoable actions" do
      make_request(darlene)
      response.status_code.should == 403
      response.status_message.should == "Forbidden"
    end

    it "gives 404 when there is no undoable action is available for subject" do
      PscTest::Fixtures.createSubject("ID003", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
      get "/subjects/ID003/schedules/undoable-actions.json", :as => :erin
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end
  end
end