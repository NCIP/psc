describe "/studies/{study}/template/{as_of}/schedule-preview" do
  before do
    # The released (current) version of NU480
    @nu480 = create_study 'NU480' do |s|
      s.planned_calendar do |cal|
        cal.epoch "Treatment" do |e|
          e.study_segment "A" do |a|
            a.period "P1", :start_day => 2, :duration => [7, :day], :repetitions => 4 do |p|
              p.activity "Rituximab", 1
              p.activity "Alcohol", 4
            end
          end
          
          e.study_segment "B"
        end
      end
    end

    @nu480_a, @nu480_b = @nu480.planned_calendar.epochs.first.study_segments.to_a
  end

  describe "GET" do
    describe "xml" do
      before do
        get "/studies/NU480/template/current/schedulePreview",
          :params => { "segment[0]" => @nu480_a.grid_id, "start_date[0]" => "2009-05-04" },
          :as => :erin
      end

      it "is successful" do
        response.should be_success
      end

      it "is XML" do
        response.content_type.should == 'text/xml'
      end

      it "has the right number of activities" do
        response.xml_elements('//scheduled-activity').should have(8).activities
      end
    end

    describe "json" do
      describe "for released version" do
        describe "basic functioning" do
          before do
            get "/studies/NU480/template/current/schedulePreview.json",
              :params => { "segment[0]" => @nu480_a.grid_id, "start_date[0]" => "2009-05-01" },
              :as => :erin
          end

          it "is successful" do
            response.should be_success
          end

          it "is JSON" do
            response.content_type.should == 'application/json'
          end

          it "contains the right number of activities" do
            response.json["days"].inject(0) { |sum, (_, day)| sum + day["activities"].size }.should == 8
          end
        end

        it "cannot refer to an unreleased segment" do
          pending
          get "/studies/NU480/template/current/schedulePreview.json",
            "segment[0]" => @nu480_c.grid_id, "start_date[0]" => "2009-05-01"
          response.status_code.should == 400
        end
      end

      describe "for development version" do
        describe "basic functioning" do
          before do
            pending
            get "/studies/NU480/template/development/schedulePreview.json",
              :params => { "segment[0]" => @nu480_c.grid_id, "start_date[0]" => "2009-05-15" },
              :as => :erin
          end

          it "is successful" do
            response.should be_success
          end

          it "is JSON" do
            response.content_type.should == 'application/json'
          end

          it "contains the right number of activities" do
            pending
            response.json["days"].inject(0) { |sum, day| sum + day["activities"].size }.should == 8
          end
        end
      end

      describe "the structure" do
        before do
          get "/studies/NU480/template/current/schedulePreview.json",
            :params => { "segment[0]" => @nu480_a.grid_id, "start_date[0]" => "2009-05-02" },
            :as => :erin
        end

        describe "[days]" do
          before do
            @days = response.json["days"]
          end

          it "has the correct days" do
            @days.keys.sort.should == %w(2009-05-02 2009-05-05 2009-05-09 
              2009-05-12 2009-05-16 2009-05-19 2009-05-23 2009-05-26)
          end

          it "includes the hidden_activities flag"

          describe "[2009-05-05][activities][0]" do
            before do
              @activity = @days['2009-05-05']['activities'][0]
            end

            it "refers to the study" do
              @activity["study"].should == "NU480"
            end

            it "refers to the segment" do
              @activity["study_segment"].should == "Treatment: A"
            end

            it "refers to the plan day" do
              @activity["plan_day"].should == "5"
            end

            describe "[current_state]" do
              it "has the name" do
                @activity["current_state"]["name"].should == "scheduled"
              end

              it "has the date" do
                @activity["current_state"]["date"].should == "2009-05-05"
              end
            end

            describe "[activity]" do
              it "has the name" do
                @activity["activity"]["name"].should == "Alcohol"
              end

              it "has the type" do
                @activity["activity"]["type"].should == "Lab Test"
              end
            end

            describe "[state_history]" do
              it "has one item" do
                @activity["state_history"].should have(1).item
              end

              it "has the current state" do
                @activity["state_history"][0]["name"].should == "scheduled"
                @activity["state_history"][0]["date"].should == "2009-05-05"
              end
            end
          end
        end

        describe "[study_segments]" do
          before do
            response.json["study_segments"].should have(1).segment
            @segment = response.json["study_segments"][0]
          end

          it "includes the name" do
            @segment['name'].should == "Treatment: A"
          end

          describe "[range]" do
            it "includes the start date"
            it "includes the stop date"
          end

          describe "[planned]" do
            describe "[segment]" do
              it "has the name" do
                @segment["planned"]["segment"]["name"].should == "A"
              end

              it "has the ID" do
                @segment["planned"]["segment"]["id"].should == @nu480_a.grid_id
              end
            end

            describe "[epoch]" do
              it "has the name" do
                @segment["planned"]["epoch"]["name"].should == "Treatment"
              end

              it "has the ID" do
                @segment["planned"]["epoch"]["id"].should == @nu480_a.epoch.grid_id
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

    it "returns 400 for unparsable date" do
      get "/studies/NU480/template/current/schedulePreview.json",
        :params => { "segment[0]" => @nu480_a.grid_id, "start_date[0]" => "200905-04" },
        :as => :erin
      response.status_code.should == 400
    end

    it "returns 400 for no pair for segment & date" do
      get "/studies/NU480/template/current/schedulePreview.json",
        :params => { "segment[0]" => @nu480_a.grid_id }, :as => :erin
      response.status_code.should == 400
    end

    it "returns 404 for no study exist" do
      get "/studies/NoStudy/template/current/schedulePreview.json",
        :params => { "segment[0]" => @nu480_a.grid_id, "start_date[0]" => "2009-05-07" },
        :as => :erin
      response.status_code.should == 404
    end
  end
end