describe "/user-actions" do
    def user_action(description, context)
      "{description: #{description}, context: #{context}}"
    end

    def retrieve(grid_id)
      application_context['userActionDao'].getByGridId(grid_id)
    end

  describe "POST" do
    describe "success" do
      before(:each) do
        action = user_action("Delayed 45 activities", "http://fake.psc/api/v1/subjects/0000001/schedules")
        post "/user-actions", action, :as => :juno, 'Content-Type' => 'application/json'
      end

      it "should respond with a 201 for success" do
        response.status_code.should == 201
      end

      it "should respond with a valid location header" do
        response.meta['location'].should =~ %r{api/v1/user-actions/.+$}
      end

      it "should successfully create a user action" do
        grid_id = response.meta['location'].split('/').last
        actual = retrieve(grid_id)
        actual.description.should == "Delayed 45 activities"
        actual.context.should == "http://fake.psc/api/v1/subjects/0000001/schedules"
      end
    end
  end
end