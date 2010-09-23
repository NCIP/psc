describe "/subjects/{subject-identifier}/schedules/activities" do
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
      @subject, @studySite, @studySegment, PscTest.createDate(2008, 12, 26) ,
      "SS001", Java::JavaUtil::HashSet.new, erin)
    application_context['studySubjectAssignmentDao'].save( @studySubjectAssignment)
    @scheduled_activities = @studySubjectAssignment.scheduledCalendar.scheduledStudySegments.first.activities
  end

  describe "POST" do
    it "updates the scheduled activity states for batch activities" do
      @JSONentity = "{#{@scheduled_activities[0].gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-28 },
                      #{@scheduled_activities[1].gridId} : { state : canceled, reason : Just canceled , date : 2009-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
        :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activities[0].gridId]["Status"].should == 201
      response.json[@scheduled_activities[1].gridId]["Status"].should == 201
      response.json[@scheduled_activities[0].gridId]["Location"].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[0].gridId}$}
      response.json[@scheduled_activities[1].gridId]["Location"].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[1].gridId}$}
    end

    it "updates scheduled activity state for one activity and send 400 - Bad Request for another activity with incorrect state in request" do
      @JSONentity = "{#{@scheduled_activities[0].gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-28 },
                      #{@scheduled_activities[1].gridId} : { state : canceledd, reason : Just canceled , date : 2009-12-29 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
            :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activities[0].gridId]["Status"].should == 201
      response.json[@scheduled_activities[1].gridId]["Status"].should == 400
      response.json[@scheduled_activities[0].gridId]["Location"].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[0].gridId}$}
    end

    it "updates scheduled activity state for one activity and send 400 - Bad Request for another activity with incorrect date in request" do
      @JSONentity = "{#{@scheduled_activities[0].gridId} : { state : scheduled, reason : Delay by two days , date : 2009-12-28 },
                      #{@scheduled_activities[1].gridId} : { state : canceled, reason : Just canceled , date : 2009 }}"
      post "/subjects/ID001/schedules/activities", @JSONentity,
            :as => :erin , 'Content-Type' => 'application/json'
      response.status_code.should == 207
      response.json[@scheduled_activities[0].gridId]["Status"].should == 201
      response.json[@scheduled_activities[1].gridId]["Status"].should == 400
      response.json[@scheduled_activities[0].gridId]["Location"].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activities[0].gridId}$}
    end

  end

end
