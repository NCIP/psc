require 'rspec'
require 'date'
require 'fileutils'
require 'net/http'
require 'faraday'
require 'webmock/rspec'

require File.expand_path('../../../lib/psc/state', __FILE__)

describe Psc::State do
  describe "a loaded sample" do
    let (:state) { Psc::State.from_file(File.expand_path('../../../sample-state.xml', __FILE__)) }

    it "has two sites" do
      state.should have(2).sites
    end

    describe "the first site" do
      it "has a name" do
        state.sites.first.name.should == "Northwestern University"
      end

      it "has an identifier" do
        state.sites.first.assigned_identifier.should == "IL036"
      end
    end

    it "has two templates" do
      state.should have(2).templates
    end

    describe "the first template" do
      let (:template) { state.templates[0] }

      it "has the filename resolved relative to the state file" do
        template.filename.should == File.expand_path('../../../foo/1234.xml', __FILE__)
      end

      it "has two participating sites" do
        template.should have(2).participating_sites
      end

      describe "the first participating site" do
        let (:site) { template.participating_sites[0] }

        it "has the correct ident" do
          site.assigned_identifier.should == 'IL036'
        end

        it "has the correct approval status" do
          site.approval.should == false
        end
      end

      describe "the second participating site" do
        let (:site) { template.participating_sites[1] }

        it "has the correct ident" do
          site.assigned_identifier.should == 'PA015'
        end

        it "has the correct approval status" do
          site.approval.should == Date.new(2010, 1, 2)
        end
      end
    end

    describe "the second template" do
      let(:template) { state.templates[1] }

      it "has the correct default filename" do
        template.filename.should == File.expand_path('../../../ABC 2345.xml', __FILE__)
      end

      it "has one participating site" do
        template.should have(1).participating_sites
      end

      describe "the first participating site" do
        it "has the correct default approval date" do
          template.participating_sites.first.approval.should == Psc::RelativeDate.new(0)
        end
      end
    end

    it "has one registration" do
      state.should have(1).registrations
    end

    describe "the one registration" do
      let (:registration) { state.registrations.first }

      describe "the subject" do
        let (:subject) { registration.subject }

        it "has the correct first name" do
          subject.first_name.should == "Jo"
        end

        it "has the correct last name" do
          subject.last_name.should == 'Fredricksson'
        end

        it "has the correct birth date" do
          subject.birth_date.should == Date.new(1950, 6, 1)
        end

        it "has the correct gender" do
          subject.gender.should == "Female"
        end

        it "has the correct person ID" do
          subject.person_id.should == "XC56700077"
        end

        it "has the correct property" do
          subject.properties.first.should == ['Hat size', '7']
        end
      end

      it "has one study site" do
        registration.should have(1).study_sites
      end

      describe "the one study site" do
        let (:study) { registration.study_sites.first }

        it "has the desired primary coordinator" do
          study.primary_coordinator.should == 'sam'
        end

        it "has the correct template ident" do
          study.template.should == 'ABC 1234'
        end

        it "has the correct study subject id" do
          study.study_subject_identifier.should == 'A0001'
        end

        it "has the correct assignment id" do
          study.desired_assignment_identifier.should == 'EXT-4563'
        end

        it "has the correct site id" do
          study.site.should == 'IL036'
        end

        it "has three segments" do
          study.should have(3).scheduled_segments
        end

        describe "the first segment" do
          let (:segment) { study.scheduled_segments[0] }

          it "has the right segment name" do
            segment.identifier.should == "Run-in"
          end

          it "has the right segment start" do
            segment.start.should == Psc::RelativeDate.new(14)
          end

          it "has the correct (default) mode" do
            segment.mode.should == "per-protocol"
          end
        end

        describe "the second segment" do
          let (:segment) { study.scheduled_segments[1] }

          it "has the right segment name" do
            segment.identifier.should == "Treatment: A"
          end

          it "has nil indicating automatic segment start" do
            segment.start.should be_nil
          end

          it "has the correct mode" do
            segment.mode.should == "per-protocol"
          end
        end

        describe "the third segment" do
          let (:segment) { study.scheduled_segments[2] }

          it "has the right segment name" do
            segment.identifier.should == "Followup"
          end

          it "has the right segment start" do
            segment.start.should == Psc::RelativeDate.new(56)
          end

          it "has the correct mode" do
            segment.mode.should == "immediate"
          end
        end
      end
    end
  end

  describe '#apply' do
    let (:base_url) { 'https://psc.example.org/api/v1' }

    let (:connection) {
      Faraday.new(:url => base_url) do |builder|
        builder.adapter :net_http
      end
    }

    let (:state) { Psc::State.new }

    before do
      stub_request(:any, /#{base_url}.*/)

      state.templates = [ Psc::Template.new('YUV 1234').tap { |t|
          t.filename = File.expand_path("../YUV 1234.xml", __FILE__)
        } ]
    end

    def do_apply
      Psc::StateApplier.new(state, QuietOutput.new).apply(connection)
    end

    class QuietOutput
      def messages
        @messages ||= []
      end

      def monitor(msg)
        messages << msg
        yield
        nil
      end

      def trace(msg)
        messages << msg
      end
    end

    describe "when importing sites" do
      before do
        state.sites = [ Psc::Site.new('Northwestern', 'IL036') ]

        do_apply
      end

      it "includes the name" do
        a_request(:put, File.join(base_url, '/sites/IL036')).
          with(:body => %r{<site[^>]*name="Northwestern"}).
          should have_been_made.once
      end

      it "includes the assigned identifier" do
        a_request(:put, File.join(base_url, '/sites/IL036')).
          with(:body => %r{<site[^>]*assigned-identifier="IL036"}).
          should have_been_made.once
      end
    end

    describe "when importing templates" do
      it "PUTs the contents of the specified file" do
        do_apply

        a_request(:put, File.join(base_url, '/studies/YUV 1234/template')).
          with(:body => %r{<planned-calendar id="84b52b6a-4e04-44a1-9b95-bd2c0367cf26"/>}).
          should have_been_made.once
      end

      describe "with participating sites" do
        before do
          state.templates.first.participating_sites = [ Psc::ParticipatingSite.new('IL036') ]
        end

        it "links the study to the site" do
          do_apply

          a_request(:put, File.join(base_url, '/studies/YUV 1234/sites/IL036')).
            with(:body => %r{<study-site-link}).
            should have_been_made.once
        end

        describe "and an approval date" do
          shared_examples_for 'an approved site for YUV 1234' do
            it "approves the original amendment" do
              a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/approvals')).
                with(:body => %r{<amendment-approval[^>]*amendment="2008-01-28~\[Original\]"}).
                should have_been_made.once
            end

            it "approves the second amendment" do
              a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/approvals')).
                with(:body => %r{<amendment-approval[^>]*amendment="2010-10-01~Empty"}).
                should have_been_made.once
            end
          end

          describe "that is relative" do
            it_behaves_like 'an approved site for YUV 1234'

            before do
              @expected_date = (Date.today + 4).to_s
              state.templates.first.participating_sites.first.approval = Psc::RelativeDate.new(4)
              do_apply
            end

            it "approves all the amendments with the correct date" do
              a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/approvals')).
                with(:body => %r{<amendment-approval[^>]*date="#{@expected_date}"}).
                should have_been_made.times(2)
            end
          end

          describe "that is exact" do
            it_behaves_like 'an approved site for YUV 1234'

            before do
              state.templates.first.participating_sites.first.approval = Date.new(2010, 3, 4)
              do_apply
            end

            it "approves all the amendments with the correct date" do
              a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/approvals')).
                with(:body => %r{<amendment-approval[^>]*date="2010-03-04"}).
                should have_been_made.times(2)
            end
          end
        end

        describe "and no approval date" do
          before do
            state.templates.first.participating_sites.first.approval = false
            do_apply
          end

          it "does not issue any approvals" do
            a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/approvals')).
              should_not have_been_made
          end
        end
      end
    end

    describe "when registering subjects" do
      before do
        state.registrations = [
          Psc::Registration.create(
            :subject => Psc::Subject.create(
              :first_name => 'Ben',
              :last_name => 'Fredricksson',
              :person_id => '2702',
              :birth_date => Date.new(1960, 4, 3),
              :gender => 'Male'
            ),

            :study_sites => [
              Psc::StudySite.create(
                :template => 'YUV 1234',
                :site => 'IL036',
                :primary_coordinator => 'jeff',
                :study_subject_identifier => 'D7830',
                :desired_assignment_identifier => '18A',
                :scheduled_segments => [
                  Psc::ScheduledSegment.new("Treatment: Regimen B")
                ]
              ),
              Psc::StudySite.create(
                :template => 'YUV 1234',
                :site => 'PA015',
                :scheduled_segments => [
                  Psc::ScheduledSegment.new("Treatment: Regimen A")
                ]
              )
            ]
          )
        ]
      end

      let(:reg) { state.registrations.first }

      it "issues one registration request per study" do
        do_apply
        a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/subject-assignments')).
          should have_been_made
        a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/PA015/subject-assignments')).
          should have_been_made
      end

      def expect_registration_matching(re)
        do_apply
        a_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/subject-assignments')).
          with(:body => re).should have_been_made.once
      end

      describe "each registration" do
        it "includes the subject's first name" do
          expect_registration_matching(/first-name="Ben"/)
        end

        it "includes the subject's last name" do
          expect_registration_matching(/last-name="Fredricksson"/)
        end

        it "includes the subject's person ID" do
          expect_registration_matching(/person-id="2702"/)
        end

        it "includes the subject's gender" do
          expect_registration_matching(/gender="Male"/)
        end

        it "includes the subject's birth date (relative)" do
          d = Psc::RelativeDate.new(-5)
          reg.subject.birth_date = d

          expect_registration_matching(/birth-date="#{d}"/)
        end

        it "includes the subject's birth date (exact)" do
          expect_registration_matching(/birth-date="1960-04-03"/)
        end

        it "includes the subject's properties" do
          reg.subject.properties = [ ["Hat size", "6 3/4"] ]
          expect_registration_matching(/<property[^>]*name="Hat size"/)
        end

        it "includes the study subject ID" do
          expect_registration_matching(/<registration[^>]*study-subject-id="D7830"/)
        end

        it "includes the desired assignment ID" do
          expect_registration_matching(/<registration[^>]*desired-assignment-id="18A"/)
        end

        it "includes the primary coordinator" do
          expect_registration_matching(/<registration[^>]*subject-coordinator-name="jeff"/)
        end

        describe "the initial segment ID" do
          it "is correctly derived for an Epoch: Segment name" do
            expect_registration_matching(
              /<registration[^>]*first-study-segment-id="a6c9a1ef-1cc7-4194-abf7-358d8b76b565"/)
          end

          it "is correctly derived for a segment name only" do
            reg.study_sites.first.scheduled_segments.first.identifier = 'Short term'
            expect_registration_matching(
              /<registration[^>]*first-study-segment-id="540926c7-391e-4aa8-984e-68434d241f8a"/)
          end

          it "is correct when exactly specified" do
            reg.study_sites.first.scheduled_segments.first.identifier =
              '97f34c10-41f3-4ca1-81d5-328cea34ee2a'
            expect_registration_matching(
              /<registration[^>]*first-study-segment-id="97f34c10-41f3-4ca1-81d5-328cea34ee2a"/)
          end
        end

        describe "the start date" do
          it "is today if not specified" do
            expect_registration_matching(/<registration[^>]*date="#{Date.today}"/)
          end

          it "is correct if relative" do
            reg.study_sites.first.scheduled_segments.first.start = Psc::RelativeDate.new(5)
            expect_registration_matching(/<registration[^>]*date="#{Date.today + 5}"/)
          end

          it "is correct if exact" do
            reg.study_sites.first.scheduled_segments.first.start = Date.new(2010, 5, 11)
            expect_registration_matching(/<registration[^>]*date="2010-05-11"/)
          end
        end
      end

      describe "with multiple segments in a study" do
        let(:second_segment) { reg.study_sites.first.scheduled_segments[1] }

        before do
          stub_request(:post, File.join(base_url, '/studies/YUV 1234/sites/IL036/subject-assignments')).
            to_return(:headers => {
              'Location' => File.join(base_url, '/studies/YUV 1234/schedules/18927')
            })

          reg.study_sites.first.scheduled_segments <<
            Psc::ScheduledSegment.create('Follow up: Long term', :start => Psc::RelativeDate.new(4))
        end

        it "does not append the first segment after the registration" do
          a_request(:post, File.join(base_url, '/studies/YUV 1234/schedules/18927')).
            with(:body => /<next-scheduled-study-segment[^>]*study-segment-id="a6c9a1ef-1cc7-4194-abf7-358d8b76b565"/).
            should_not have_been_made
        end

        describe "a subsequent segment's start" do
          it "is the 'natural' day by default" do
            pending "TODO"
          end

          it "is the specified day when relative" do
            do_apply
            a_request(:post, File.join(base_url, '/studies/YUV 1234/schedules/18927')).
              with(:body => /<next-scheduled-study-segment[^>]*start-date="#{Date.today + 4}"/).
              should have_been_made
          end

          it "is the specified day when exact" do
            second_segment.start = Date.new(2011, 8, 2)
            do_apply
            a_request(:post, File.join(base_url, '/studies/YUV 1234/schedules/18927')).
              with(:body => /<next-scheduled-study-segment[^>]*start-date="2011-08-02"/).
              should have_been_made
          end

          it "has the correct segment id" do
            do_apply
            a_request(:post, File.join(base_url, '/studies/YUV 1234/schedules/18927')).
              with(:body => /<next-scheduled-study-segment[^>]*study-segment-id="97f34c10-41f3-4ca1-81d5-328cea34ee2a"/).
              should have_been_made
          end

          it "has the correct mode" do
            do_apply
            a_request(:post, File.join(base_url, '/studies/YUV 1234/schedules/18927')).
              with(:body => /<next-scheduled-study-segment[^>]*mode="per-protocol"/).
              should have_been_made
          end
        end
      end
    end
  end
end

describe Psc::RelativeDate do
  describe "#to_date" do
    it "is today for 0" do
      Psc::RelativeDate.new(0).to_date.should == Date.today
    end

    it "is in the past for negative" do
      Psc::RelativeDate.new(-5).to_date.should < Date.today
    end

    it "is in the future for positive" do
      Psc::RelativeDate.new( 5).to_date.should > Date.today
    end
  end

  describe "#to_s" do
    it "is the yyyy-mm-dd version of the date" do
      Psc::RelativeDate.new(7).to_s.should == (Date.today + 7).to_s
    end
  end
end
