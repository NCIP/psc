describe "/user-actions" do
  describe "POST" do

    def user_action(description, context)
      <<-JSON
        {
          description: "Delayed 45 activities for Jo Carlson by 6 days",
          context: "http://fake.us/api/v1/subjects/0000001/schedules"
        }
      JSON
    end

    it "should successfully create a user action" do
      action = user_action("Delayed 45 activities for Jo Carlson by 6 days", "http://fake.us/api/v1/subjects/0000001/schedules")

      post "/user-actions", action, :as => :juno, 'Content-Type' => 'application/json'
      response.status_code.should == 201

      # response.status_message.should == "Created"
      # response.meta['location'].should =~ %r{api/v1/studies/NU480/schedules/#{@studySubjectAssignment.gridId}/activities/#{@scheduled_activity.gridId}$}
    end
  end
end