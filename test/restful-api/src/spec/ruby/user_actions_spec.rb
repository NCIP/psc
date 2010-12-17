describe "/user-actions" do
    def user_action(action_type, description, context, show_null_attributes = false)
      attrs = {}
      attrs['context'] = context
      attrs['actionType'] = action_type
      attrs['description'] = description

      attrs.reject!{|a,b| b.nil?} unless show_null_attributes

      <<-JSON
        {
          #{attrs.collect{|a,b| "#{a}: " + (b ? "\"#{b}\"" : "null")}.join(', ')}
        }
      JSON
    end

    def retrieve(grid_id)
      application_context['userActionDao'].getByGridId(grid_id)
    end

  describe "POST" do
    describe "success" do
      before(:each) do
        action = user_action("Delay", "Delayed 45 activities", "http://fake/psc/api/v1/subjects")
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
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
        actual.actionType.should == "Delay"
        actual.description.should == "Delayed 45 activities"
        actual.context.should == "http://fake/psc/api/v1/subjects"
        actual.undone.should be(false)
        actual.csm_user_id.should_not be_nil
      end
    end

    describe "failure" do
      it "should respond with a 400 when there is no description property " do
        action = user_action("Delay", nil, "http://fake/psc/api/v1/subjects")
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.entity.should include("Missing attribute: description")
      end

      it "should respond with a 400 when description is null" do
        action = user_action("Delay", nil, "http://fake/psc/api/v1/subjects", true)
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.entity.should include("Blank attribute: description")
      end

      it "should respond with a 400 when description is blank" do
        action = user_action("Delay", "", "http://fake/psc/api/v1/subjects")
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.entity.should include("Blank attribute: description")
      end

      it "should respond with a 400 when there is no action type" do
        action = user_action(nil, "Delayed 45 activities", "http://fake/psc/api/v1/subjects")
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.entity.should include("Missing attribute: actionType")
      end

      it "should respond with a 400 when there is no contexts type" do
        action = user_action("Delay", "Delayed 45 activities", nil)
        post "/user-actions", action, :as => :alice, 'Content-Type' => 'application/json'
        response.status_code.should == 400
        response.entity.should include("Missing attribute: context")
      end
    end
  end
end