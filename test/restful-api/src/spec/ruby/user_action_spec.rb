#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "/user-actions/{user-action-identifier}" do
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
  def retrieve(grid_id)
    application_context['userActionDao'].getByGridId(grid_id)
  end
  def subject_json(overrides={})
    {
      :first_name => 'Alan', :last_name => 'Boyarski',
      :person_id => 'ID001', :gender => 'Male',
      :birth_date => '1983-03-23',
      :properties => [
        { 'name' => 'Hair color', 'value' => 'brown' },
        { 'name' => 'Only hope', 'value' => 'Obi-wan' }
      ]
    }.merge(overrides).to_json
  end
  def apply_undo
    get "/subjects/ID001/schedules/undoable-actions.json", :as => :erin
    @ua_identifier = response.json['undoable_actions'][0]['URI'].split('/').last
    delete "/user-actions/#{@ua_identifier}", :as => :erin
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
    @ua_header = create_user_action("Canceled on 2008-12-29", "canceled", erin, @context_uri)
    @ua_identifier = @ua_header.split('/').last
  end
  
  describe "DELETE" do
    it "sets user action flag to undone and reverse the effect of audit event" do
      @JSONentity = "{#{@scheduled_activities[1].gridId} : { state : canceled, reason : Just canceled , date : 2008-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("canceled")
      response.xml_attributes("current-scheduled-activity-state", "reason").should include("Just canceled")
      response.xml_attributes("current-scheduled-activity-state", "date").should include("2008-12-29")

      delete "/user-actions/#{@ua_identifier}", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      actual = retrieve(@ua_identifier)
      actual.undone.should be(true)

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("scheduled")
      response.xml_attributes("current-scheduled-activity-state", "reason").should include("Initialized from template")
      response.xml_attributes("current-scheduled-activity-state", "date").should include("2008-12-28")
    end

    #1369
    it "sets user action flag to undone and reverse the effect of audit event for multiple update to same event without reason" do
      @ua_header1 = create_user_action("Occurred on 2008-12-28", "occurred", erin, @context_uri)
      @JSONentity1 = "{#{@scheduled_activities[1].gridId} : { state : occurred, reason : nil, date : 2008-12-28}}"
      post "/subjects/ID001/schedules/activities", @JSONentity1, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header1
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("occurred")

      @ua_header2 = create_user_action("Canceled on 2008-12-28", "canceled", erin, @context_uri)
      @JSONentity2 = "{#{@scheduled_activities[1].gridId} : { state : canceled, reason : nil, date : 2008-12-28 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity2, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header2

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("canceled")

      @ua_header3 = create_user_action("Missed on 2008-12-28", "missed", erin, @context_uri)
      @JSONentity3 = "{#{@scheduled_activities[1].gridId} : { state : missed, reason : nil, date : 2008-12-28 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity3, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header3

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("missed")

      @ua_identifier3 = @ua_header3.split('/').last

      delete "/user-actions/#{@ua_identifier3}", :as => :erin
      response.status_code.should == 200
      response.status_message.should == "OK"
      actual = retrieve(@ua_identifier3)
      actual.undone.should be(true)

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("canceled")
      response.xml_attributes("current-scheduled-activity-state", "date").should include("2008-12-28")
    end
    
    #1359
    it "undo can undo all level back for multiple update to same event" do
      @ua_header1 = create_user_action("Occurred on 2008-12-28", "occurred", erin, @context_uri)
      @JSONentity1 = "{#{@scheduled_activities[1].gridId} : { state : occurred, reason : nil, date : 2008-12-28}}"
      post "/subjects/ID001/schedules/activities", @JSONentity1, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header1
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("occurred")

      @ua_header2 = create_user_action("Canceled on 2008-12-28", "canceled", erin, @context_uri)
      @JSONentity2 = "{#{@scheduled_activities[1].gridId} : { state : canceled, reason : nil, date : 2008-12-28 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity2, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header2

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("canceled")

      @ua_header3 = create_user_action("Missed on 2008-12-28", "missed", erin, @context_uri)
      @JSONentity3 = "{#{@scheduled_activities[1].gridId} : { state : missed, reason : nil, date : 2008-12-28 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity3, :as => :erin ,
            'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header3

      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("missed")
      
      # Undo upto initial state
      apply_undo
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("canceled")
      response.xml_attributes("current-scheduled-activity-state", "date").should include("2008-12-28")
      apply_undo
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("occurred")
      apply_undo
      get "/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}", :as => :erin
      response.xml_attributes("current-scheduled-activity-state", "state").should include("scheduled")
      response.xml_attributes("current-scheduled-activity-state", "reason").should include("Initialized from template")
      response.xml_attributes("current-scheduled-activity-state", "date").should include("2008-12-28")
    end
    
    it "reverse the effect of audit event for component collection ( Subject properties)" do
      @subject.grid_id = "4567-LL47"
      @subject.properties << Psc::Domain::SubjectProperty.new("Hair color", "brown")
      @subject.properties << Psc::Domain::SubjectProperty.new("Only hope", "Obi-wan")
      application_context["subjectDao"].save(@subject)
      get "/subjects/ID001.json", :as => erin
      response.json['properties'].should == [
        { 'name' => 'Hair color', 'value' => 'brown' },
        { 'name' => 'Only hope', 'value' => 'Obi-wan' }
      ]
      @ua_header1 = create_user_action("Subject properties are updated", "property", erin, @context_uri)
      entity = subject_json(:properties => [
        { 'name' => 'Only hope', 'value' => 'Obi-wan' },
        { 'name' => 'Aliases', 'value' => 'Boushh' },
        { 'name' => 'Hair color', 'value' => 'braun' }
      ])
      put "/subjects/ID001", entity,
        'Content-Type' => 'application/json', :as => :erin , 'X-PSC-User-Action' => @ua_header1
      response.status_code.should == 200
      reloadedAfterUpdate = application_context['subjectDao'].getById(@subject.id)
      reloadedAfterUpdate.properties.collect { |prop|
             [prop.name, prop.value]
           }.should == [
             ['Only hope', 'Obi-wan'],
             ['Aliases', 'Boushh'],
             ['Hair color', 'braun']
           ]
      @ua_identifier1 = @ua_header1.split('/').last
      delete "/user-actions/#{@ua_identifier1}", :as => :erin
      actual = retrieve(@ua_identifier1)
      actual.undone.should be(true)
      get "/subjects/ID001.json", :as => erin
      response.json['properties'].should == [
        { 'name' => 'Hair color', 'value' => 'brown' },
        { 'name' => 'Only hope', 'value' => 'Obi-wan' }
      ]
    end
    
    it "gives 403 status if user doesn't have access to undo the user actions" do
      delete "/user-actions/#{@ua_identifier}", :as => :darlene
      response.status_code.should == 403
      response.status_message.should == "Forbidden"
    end

    it "gives 404 status for unknown user action" do
      delete "/user-actions/unknownId", :as => :erin
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gives 400 status if user action is already undone" do
      ua = retrieve(@ua_identifier)
      ua.undone = true
      application_context['userActionDao'].save(ua)
      delete "/user-actions/#{@ua_identifier}", :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end

    it "gives 400 status if no undoable actions for user action's context" do
      delete "/user-actions/#{@ua_identifier}", :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end

    it "gives 400 status if user action is out of order from undoable actions" do
      @JSONentity = "{#{@scheduled_activities[1].gridId} : { state : canceled, reason : Just canceled , date : 2008-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity, :as => :erin ,
           'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header

      @ua_header1 = create_user_action("Delay by 2 days", "delay", erin, @context_uri)
      @JSONentity1 = "{#{@scheduled_activities[0].gridId} : { state : scheduled, reason : Delay by two days , date : 2008-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity1, :as => :erin ,
             'Content-Type' => 'application/json', 'X-PSC-User-Action' => @ua_header1

      delete "/user-actions/#{@ua_identifier}", :as => :erin
      response.status_code.should == 400
      response.status_message.should == "Bad Request"
    end
  end
end