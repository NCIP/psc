describe "/subjects/{subject-identifier}/schedules" do
  before do
    # The released version of NU480
    @nu480 = create_study 'NU480' do |s|
      s.planned_calendar do |cal|
        cal.epoch "Treatment" do |e|
          e.study_segment "A" do |a|
            a.period "P1", :start_day => 2, :duration => [4, :day], :repetitions => 2 do |p|
              p.activity "Rituximab", 4
              p.activity "Alcohol", 4
            end
          end
        end
      end
    end

    # The released version of ECOG170
    @ecog170 = create_study 'ECOG170' do |s|
      s.planned_calendar do |cal|
        cal.epoch "Followup" do |e|
          e.study_segment "B" do |a|
            a.period "P2", :start_day => 2, :duration => [3, :day], :repetitions => 1 do |p|
              p.activity "Lab Test", 1
              p.activity "Physical Test", 3
            end
          end
        end
      end
    end

    @studySites = [
       @studySite1 = PscTest::Fixtures.createStudySite(@nu480, northwestern),
       @studySite2 = PscTest::Fixtures.createStudySite(@ecog170, northwestern)
    ]

    @studySites.each do |ss|
       application_context['studySiteDao'].save(ss)
    end

    #approve an existing amendment
    @approve_date = PscTest.createDate(2008, 12, 20)
    @studySite1.approveAmendment(@nu480.amendment, @approve_date)
    @studySite2.approveAmendment( @ecog170.amendment, @approve_date)
    application_context['studySiteDao'].save(@studySite1)
    application_context['studySiteDao'].save(@studySite2)

    #create subject
    @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))

    #create a study subject assignment
    @studySegment1 = @nu480.plannedCalendar.epochs.first.studySegments.first
    @studySegment2 = @ecog170.plannedCalendar.epochs.first.studySegments.first
    @studySubjectAssignment1 = application_context['subjectService'].assignSubject(
      @studySite1,
      Psc::Service::Presenter::Registration::Builder.new.
        subject(@subject).first_study_segment(@studySegment1).
        date(PscTest.createDate(2008, 12, 26)).manager(erin).
        to_registration)
    @studySubjectAssignment2 = application_context['subjectService'].assignSubject(
      @studySite2,
      Psc::Service::Presenter::Registration::Builder.new.
        subject(@subject).first_study_segment(@studySegment2).
        date(PscTest.createDate(2008, 12, 28)).manager(erin).
        to_registration)
  end

  describe "GET" do

    describe "xml" do
      before do
        get "/subjects/ID001/schedules", :as => :erin
      end

      it "is successful" do
        response.status_code.should == 200
      end

      it "is XML" do
        response.content_type.should == 'text/xml'
      end

      it "has the right number of activities" do
        response.xml_elements('//scheduled-activity').should have(6).activities
      end
    end

    describe "json" do
      before do
        get "/subjects/ID001/schedules.json", :as => :erin
      end

      it "is sucessful" do
        response.status_code.should == 200
      end

      it "is json" do
        response.content_type.should == 'application/json'
      end

      it "contains the right number of activities" do
        response.json["days"].inject(0) { |sum, (_, day)| sum + day["activities"].size }.should == 6
      end

      describe "the structure" do
        describe "[days]" do
          before do
            @days = response.json["days"]
          end

          it "has the correct days" do
            @days.keys.sort.should == %w(2008-12-28 2008-12-29 2008-12-30 2009-01-02)
          end

          describe "[2008-12-29][activities][0]" do
            before do
              @activity = @days['2008-12-29']['activities'][0]
            end

            it "refers to the study" do
              @activity["study"].should == "NU480"
            end

            it "refers to the segment" do
              @activity["study_segment"].should == "Treatment"
            end

            it "refers to the plan day" do
              @activity["plan_day"].should == "5"
            end

            describe "[current_state]" do
              it "has the name" do
                @activity["current_state"]["name"].should == "scheduled"
              end

              it "has the date" do
                @activity["current_state"]["date"].should == "2008-12-29"
              end
            end

            describe "[activity]" do
              it "has the name" do
                @activity["activity"]["name"].should == "Rituximab"
              end

              it "has the type" do
                @activity["activity"]["type"].should == "Intervention"
              end
            end

            describe "[state_history]" do
              it "has one item" do
                @activity["state_history"].should have(1).item
              end

              it "has the current state" do
                @activity["state_history"][0]["name"].should == "scheduled"
                @activity["state_history"][0]["date"].should == "2008-12-29"
              end
            end
          end
        end
        describe "[study_segments]" do
          before do
            response.json["study_segments"].should have(2).segment
            @segment = response.json["study_segments"][0]
          end

          it "includes the name" do
            @segment['name'].should == "Treatment"
          end

          describe "[range]" do
            it "includes the start date" do
              @segment["range"]["start_date"].should == "2008-12-26"
            end
            it "includes the stop date" do
              @segment["range"]["stop_date"].should == "2009-01-02"
            end
          end

          describe "[planned]" do
            describe "[segment]" do
              it "has the name" do
                @segment["planned"]["segment"]["name"].should == "A"
              end

              it "has the ID" do
                @segment["planned"]["segment"]["id"].should == @studySegment1.grid_id
              end
            end

            describe "[epoch]" do
              it "has the name" do
                @segment["planned"]["epoch"]["name"].should == "Treatment"
              end

              it "has the ID" do
                @segment["planned"]["epoch"]["id"].should == @studySegment1.epoch.grid_id
              end
            end

            describe "[study]" do
              it "has the assigned ident" do
                @segment["planned"]["study"]["assigned_identifier"].should == "NU480"
              end
            end
          end
        end
      end
    end

    describe "ics" do
      before do
        get "/subjects/ID001/schedules.ics", :as => :erin
      end

      it "is successful" do
        response.status_code.should == 200
      end

      it "is ICS calendar" do
        response.content_type.should == 'text/calendar'
      end

      it "has the right number of events" do
        response.calendar.events.size.should == 6
      end

      it "has right calendar properties" do
        response.calendar.calscale.to_s.should =="GREGORIAN"
        response.calendar.prodid.to_s.should == "-//Events Calendar//iCal4j 1.0//EN"
        response.calendar.ip_method.to_s.should == "PUBLISH"
      end

      describe "events" do
        before do
          @event = response.calendar.events[0]
          @scheduled_activity = @studySubjectAssignment2.scheduledCalendar.scheduledStudySegments.first.activities.first
        end

        it 'has the UID property' do
          @event.uid.should == @scheduled_activity.grid_id
        end

        it 'has the TRANSP property' do
          @event.transp.should == "TRANSPARENT"
        end

        it 'has the Dtstart property' do
          @event.dtstart.to_s.should == "2008-12-28"
        end

        it 'has the Summary property' do
          @event.summary.should == "ECOG170/Lab Test"
        end

        describe "Description" do
          it "includes subject name" do
            @event.description.should =~ /Subject:Alan Boyarski/
          end

          it "includes study name" do
            @event.description.should =~ /Study:ECOG170/
          end

          it "includes segment name" do
            @event.description.should =~ /Study Segment:Followup/
          end

          it "includes activity name" do
            @event.description.should =~ /Activity:Lab Test/
          end

          it "includes study plan day" do
            @event.description.should =~ /Study plan day:Day 2/
          end
        end

        it 'has the URL property' do
          @event.url.to_s.should =~ %r{/pages/cal/scheduleActivity\?event=#{@scheduled_activity.grid_id}$}
        end
      end
    end

    it "doesn't give schedule for invalid subject" do
      get "/subjects/ID002/schedules", :as => :erin
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gives 403 status if user doesn't have access to the subject's schedule" do
      get "/subjects/ID001/schedules", :as => :darlene
      response.status_code.should == 403
      response.status_message.should == "Forbidden"
    end
  end
end
