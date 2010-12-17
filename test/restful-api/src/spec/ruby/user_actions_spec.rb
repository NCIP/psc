describe "/user-actions" do
  describe "POST" do

    def user_action(description, context)
        "{description: #{description}, context: #{context}}"
    end

    it "should successfully create a user action" do
      action = user_action("Delayed 45 activities for Jo Carlson by 6 days", "http://fake.us/api/v1/subjects/0000001/schedules")

      post "/user-actions", action, :as => :juno, 'Content-Type' => 'application/json'
      response.status_code.should == 201
      response.meta['location'].should =~ %r{api/v1/user-actions/.+$}
    end
  end
end