#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

# This isn't actually a spec for the API -- it's a spec for one of the helpers

describe TemplateBuilder do
  describe "#create_study" do
    before do
      @study = create_study "NU 04B8" do |s|
        s.planned_calendar do |t|
          t.epoch "Run-in"
          t.epoch "Treatment" do |e|
            e.study_segment "A" do |a|
              a.period "P1", :duration => [14, :days], :repetitions => 4, :start_day => 8 do |p|
                p.activity "CBC", 4
                p.activity "CBC", 8
              end

              a.period "P2"
            end
            e.study_segment "B"
            e.study_segment "C"
          end
          t.epoch "Follow up"
        end
        s.population "W", "Women of child-bearing potential"
      end
    end

    it "gives you a study" do
      @study.assigned_identifier.should == "NU 04B8"
    end

    it "gives you a saved study" do
      @study.getId.should_not be_nil # use getId b/c id is in Ruby's Object
      @study.grid_id.should_not be_nil
    end

    it "gives you a released template" do
      @study.amendment.should_not be_nil
      @study.development_amendment.should be_nil
    end

    describe "the created population" do
      before do
        @pop = @study.populations.first
      end

      it "exists" do
        @pop.should_not be_nil
      end

      it "is alone" do
        @study.populations.size.should == 1
      end

      it "has the correct abbreviation" do
        @pop.abbreviation.should == 'W'
      end

      it "has the correct name" do
        @pop.name.should == 'Women of child-bearing potential'
      end
    end

    it "builds the right epochs" do
      @study.planned_calendar.epochs.collect { |e| e.name }.should == ['Run-in', 'Treatment', 'Follow up']
    end

    describe "for a default epoch" do
      before do
        @epoch = @study.planned_calendar.epochs.first
      end

      describe "the default segment" do
        before do
          @segment = @epoch.study_segments.first
          @epoch.name.should == 'Run-in'
        end

        it "exists" do
          @segment.should_not be_nil
        end

        it "is alone" do
          @epoch.study_segments.should have(1).item
        end

        it "is named the same as the epoch" do
          @segment.name.should == @epoch.name
        end
      end

      describe "the default period" do
        before do
          @period = @epoch.study_segments.first.periods.first
        end

        it "exists" do
          @period.should_not be_nil
        end

        it "lasts for 21 days" do
          @period.duration.unit.to_s.should == "day"
          @period.duration.quantity.should == 21
        end

        it "is named 'default'" do
          @period.name.should == 'default'
        end

        it "starts on day 1" do
          @period.start_day.should == 1
        end

        it "has one repetition" do
          @period.repetitions.should == 1
        end

        it "has 4 activities" do
          @period.planned_activities.size.should == 4
        end
      end
    end

    describe "for a detailed epoch" do
      before do
        @epoch = @study.planned_calendar.epochs[1]
      end

      it "builds the requested segments" do
        @epoch.study_segments.collect { |ss| ss.name }.should == %w(A B C)
      end

      describe "a detailed segment" do
        before do
          @segment = @epoch.study_segments.first
          @segment.name.should == 'A'
        end

        it "has the requested periods" do
          @segment.periods.collect { |p| p.name }.should == %w(P2 P1)
        end

        describe "a detailed period" do
          before do
            @period = @segment.periods.find { |p| p.name == 'P1' }
          end

          it "has the requested duration" do
            @period.duration.quantity.should == 14
            @period.duration.unit.to_s.should == 'day'
          end

          it "has the requested start day" do
            @period.start_day.should == 8
          end

          it "has the requested repetitions" do
            @period.repetitions.should == 4
          end

          it "has the requested activities" do
            @period.planned_activities.size.should == 2
          end
        end
      end
    end
  end

  describe "#update_study" do
    before do
      @nu481 = create_study "NU481" do |s|
        s.planned_calendar do |cal|
          cal.epoch "Treatment" do |e|
            e.study_segment "A"
            e.study_segment "B"
          end
          cal.epoch "Follow up"
        end
      end
    end

    describe ":development" do
      def first_delta
        @nu481.development_amendment.deltas.first
      end

      def first_change
        first_delta.changes.first
      end

      before do
        @nu481.development_amendment.should be_nil
      end

      describe "with an epoch" do
        before do
          update_study @nu481, :development do |s|
            s.add_epoch "LTFU"
          end
        end

        it "has a PC delta" do
          first_delta.node.should == @nu481.planned_calendar
        end

        describe "the sole change" do
          it "is an add" do
            first_change.action.code.should == 'add'
          end

          it "has the right name" do
            first_change.child.name.should == 'LTFU'
          end
        end
      end

      describe "with a study segment" do
        def add(name, opts=nil)
          update_study @nu481, :development do |s|
            s.add_study_segment name, opts
          end
        end

        it "fails without in" do
          lambda { add("C") }.should raise_error("Please specify the name of the target epoch.  E.g. :in => 'Treatment'.")
        end

        describe "and correct options" do
          before { add("C", :in => "Treatment") }

          it "adds to the correct epoch" do
            first_delta.node.should == @nu481.planned_calendar.epochs.first
          end

          it "uses the correct name" do
            first_change.child.name.should == "C"
          end
        end
      end

      describe "with a period" do
        def add(name, opts=nil)
          update_study @nu481, :development do |s|
            s.add_period name, opts
          end
        end

        it "fails without in" do
          lambda { add("P1") }.should raise_error("Please specify the name of the target study segment.  E.g. :in => 'Treatment: B'.")
        end

        describe "and minimum options" do
          before { add("P1", :in => "Treatment: B") }

          it "adds to the correct segment" do
            first_delta.node.should == @nu481.planned_calendar.epochs.first.study_segments[1]
          end

          it "uses the correct name" do
            first_change.child.name.should == "P1"
          end
        end

        describe "and period options" do
          it "uses the specified duration" do
            add("P4", :in => "Treatment: B", :duration => [1, :month])

            first_change.child.duration.unit.to_s.should == 'month'
            first_change.child.duration.quantity.should == 1
          end

          it "uses the specified reps" do
            add("P7", :in => "Treatment: B", :repetitions => 5)
            first_change.child.repetitions.should == 5
          end

          it "uses the specified start day" do
            add("P11", :in => "Treatment: B", :start_day => 99)
            first_change.child.start_day.should == 99
          end
        end

        describe "with a planned activity" do
          def add(name, day, opts=nil)
            update_study @nu481, :development do |s|
              s.add_planned_activity name, day, opts
            end
          end

          it "fails without in" do
            lambda { add("CBC", 4) }.should raise_error("Please specify the name of the target period.  E.g. :in => 'Treatment: B: P4'.")
          end

          describe "and correct options" do
            before { add("CBC", 2, :in => "Treatment: B: default") }

            it "adds to the correct period" do
              first_delta.node.should == @nu481.planned_calendar.epochs.first.study_segments[1].periods.to_a.first
            end

            it "uses the correct name" do
              first_change.child.activity.name.should == "CBC"
            end

            it "uses the correct day" do
              first_change.child.day.should == 2
            end
          end
        end
      end
    end

  end
end
