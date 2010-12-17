describe "/user-actions" do
  describe "POST" do

    def user_action(description, context)
        "{description: #{description}, context: #{context}}"
    end

    def retrieve(grid_id)
       application_context['userActionDao'].getByGridId(grid_id)
    end

    it "should successfully create a user action" do
      action = user_action("Delayed 45 activities for Jo Carlson by 6 days", "http://fake.us/api/v1/subjects/0000001/schedules")

      post "/user-actions", action, :as => :juno, 'Content-Type' => 'application/json'
      response.status_code.should == 201
      response.meta['location'].should =~ %r{api/v1/user-actions/.+$}

      grid_id = response.meta['location'].split('/').last
      retrieve(grid_id).should_not be_nil
    end
  end
end