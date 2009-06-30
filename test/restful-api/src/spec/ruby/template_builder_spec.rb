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
end