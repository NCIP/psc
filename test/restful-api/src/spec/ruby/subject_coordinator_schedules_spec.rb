describe "/users/{username}/roles/subject-coordinator/schedules" do
  before do
    #create site
    @site = PscTest::Fixtures.createSite("TestSite", "site")
    application_context['siteDao'].save( @site)

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
              p.activity "Physical Test", 2
            end
          end
        end
      end
    end
    #create a studysites
    @studySites = [
       @studySite1 = PscTest::Fixtures.createStudySite(@nu480, @site),
       @studySite2 = PscTest::Fixtures.createStudySite(@ecog170, @site)
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
    @subject1 = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
    @subject2 = PscTest::Fixtures.createSubject("ID002", "Perry", "Carl", PscTest.createDate(1978, 4, 17))
    @subject3 = PscTest::Fixtures.createSubject("ID003", "Art", "Kelly", PscTest.createDate(1975, 6, 12))

    #Assign studies to the subject coordinator
    application_context['templateService'].assignTemplateToSubjectCoordinator(@nu480, @site, erin)
    application_context['templateService'].assignTemplateToSubjectCoordinator(@ecog170, @site, erin)
    application_context['templateService'].assignTemplateToSubjectCoordinator(@nu480, @site, hannah)

    #create a study subject assignment
    @studySegment1 = @nu480.plannedCalendar.epochs.first.studySegments.first
    @studySegment2 = @ecog170.plannedCalendar.epochs.first.studySegments.first
    @studySubjectAssignments= [
      @studySubjectAssignment1 = application_context['subjectService'].assignSubject(@subject1, @studySite1, @studySegment1, PscTest.createDate(2008, 12, 26) , "SS001", erin),
      @studySubjectAssignment2 = application_context['subjectService'].assignSubject(@subject1, @studySite2, @studySegment2, PscTest.createDate(2008, 12, 28) , "SS002", erin),
      @studySubjectAssignment3 = application_context['subjectService'].assignSubject(@subject2, @studySite2, @studySegment2, PscTest.createDate(2008, 12, 28) , "SS003", erin),
      @studySubjectAssignment4 = application_context['subjectService'].assignSubject(@subject3, @studySite1, @studySegment1, PscTest.createDate(2008, 12, 26) , "SS004", hannah)
    ]
    @studySubjectAssignments.each do |ssa|
      application_context['studySubjectAssignmentDao'].save(ssa)
    end
  end
  describe "GET" do
    describe "subject coordinator own schedules" do
      describe "xml" do
        before do
          get "/users/erin/roles/subject-coordinator/schedules", :as => :erin
        end

        it "is successful" do
          response.should be_success
        end

        it "is XML" do
          response.content_type.should == 'text/xml'
        end

        it "has the right number of assignments" do
          response.xml_elements('//subject-assignment').should have(3).elements
        end

        it "has the right number of activities" do
          response.xml_elements('//scheduled-activity').should have(8).activities
        end
      end
      describe "json" do
        before do
          get "/users/erin/roles/subject-coordinator/schedules.json", :as => :erin
        end

        it "is successful" do
          response.should be_success
        end

        it "is XML" do
          response.content_type.should == 'application/json'
        end

        it "has the right number of activities" do
          response.json["days"].inject(0) { |sum, (_, day)| sum + day["activities"].size }.should == 8
        end

        it "[days][date][activities][each activity] contains subject name" do
          response.json["days"]['2008-12-28']['activities'][0]["subject"].should == "Alan Boyarski"
        end

        it "[study_segments] contains subject name" do
          response.json["study_segments"][0]["subject"].should == "Alan Boyarski"
        end

      end
      describe "ics" do
        before do
          get "/users/erin/roles/subject-coordinator/schedules.ics", :as => :erin
        end

        it "is successful" do
          response.should be_success
        end

        it "is ICS calendar" do
          response.content_type.should == 'text/calendar'
        end

        it "has the right number of events" do
          response.calendar.events.size.should == 8
        end

        describe "event" do
          it 'has the Summary property' do
            response.calendar.events[0].summary.should == "Alan Boyarski/ECOG170/Lab Test"
          end
        end
      end
    end
    describe "subject coordinator colleage schedules" do
        describe "xml" do
          before do
            get "/users/hannah/roles/subject-coordinator/schedules", :as => :erin
          end

          it "has the right number of assignments" do
            response.xml_elements('//subject-assignment').should have(1).elements
          end

          it "has the right number of activities" do
            response.xml_elements('//scheduled-activity').should have(4).activities
          end
        end
        describe "json" do
          before do
            get "/users/hannah/roles/subject-coordinator/schedules.json", :as => :erin
          end

          it "has the right number of activities" do
            response.json["days"].inject(0) { |sum, (_, day)| sum + day["activities"].size }.should == 4
          end
        end
        describe "ics" do
          before do
            get "/users/hannah/roles/subject-coordinator/schedules.ics", :as => :erin
          end

          it "has the right number of events" do
            response.calendar.events.size.should == 4
          end

          describe "event" do
            it 'has the Summary property' do
              response.calendar.events[0].summary.should == "Art Kelly/NU480/Rituximab"
            end
          end
        end
    end
    it "forbids access to schedules other than subject coordinator" do
      get "/users/erin/roles/subject-coordinator/schedules", :as => :frieda
      response.status_code.should == 403
    end

    it "404s for non-existent users" do
      get "/users/unknown/roles/subject-coordinator/schedules", :as => :erin
      response.status_code.should == 404
    end
  end
end
