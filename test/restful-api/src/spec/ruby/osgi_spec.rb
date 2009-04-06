describe "/osgi/bundles" do
  it "is not permitted to regular users" do
    get "/osgi/bundles", :as => :erin
    response.status_code.should == 403
  end
  
  describe "GET by system admin" do
    before do
      get "/osgi/bundles", :as => :zelda
      response.status_code.should == 200
    end
    
    it "returns an array of objects" do
      response.json.should be_kind_of(Array)
      response.json.first.should be_kind_of(Hash)
    end
    
    it "has a symbolic name in the bundle description" do
      response.json.first['symbolic-name'].should_not be_nil
    end
    
    it "has an ID in the bundle description" do
      response.json.first['id'].should_not be_nil
    end
    
    it "has a version in the bundle description" do
      response.json.first['version'].should_not be_nil
    end
    
    it "has a state in the bundle description" do
      response.json.first['state'].should_not be_nil
    end
  end
  
  describe "/{bundle-id}/state" do
    before do
      pending
      get "/osgi/bundles", :as => :zelda
      response.status_code.should == 200
      @bundles = response.json
    end
    
    describe "GET" do
      it "forbids access to non-sysadmins" do
        get "/osgi/bundles/#{@bundles[7]['id']}/state", :as => :barbara
        response.status_code.should == 403
      end
      
      it "returns the state for a valid bundle ID" do
        get "/osgi/bundles/#{@bundles[7]['id']}/state", :as => :zelda
        response.status_code.should == 200
        response.json['state'].should == @bundles[7]['state']
      end
      
      it "404s for an invalid bundle ID" do
        get "/osgi/bundles/-5/state", :as => :zelda
        response.status_code.should == 404
      end
    end
    
    describe "PUT" do
      it "starts a non-started bundle" do
        installed = @bundles.find { |b| b['state'] == 'INSTALLED' }
        installed.should_not be_nil
        put "/osgi/bundles/#{installed['id']}/state", "{ state: STARTING }", 
          :as => :zelda, 'Content-Type' => 'application/json'
        response.status_code.should == 200
        response.json['status'].should == 'ACTIVE'
      end
      
      it "stops a started bundle" do
        active = @bundles.find { |b| b['state'] == 'ACTIVE' }
        active.should_not be_nil
        put "/osgi/bundles/#{active['id']}/state", "{ state: STOPPING }", 
          :as => :zelda, 'Content-Type' => 'application/json'
        response.status_code.should == 200
        %w(INSTALLED RESOLVED).should include(response.json['status'])
      end
      
      %w(UNINSTALLED INSTALLED RESOLVED ACTIVE).each do |unputtable_state|
        it "cannot change bundle into the #{unputtable_state} state" do
          active = @bundles.find { |b| b['state'] != unputtable_state }
          active.should_not be_nil
          put "/osgi/bundles/#{active['id']}/state", "{ state: #{unputtable_state} }", 
            :as => :zelda, 'Content-Type' => 'application/json'
          response.status_code.should == 422
        end
        
        # Note that this spec will be a no-op if there isn't a bundle in the 
        # spec state already
        it "leaves a bundle in #{unputtable_state} if it is already there" do
          active = @bundles.find { |b| b['state'] == unputtable_state }
          if active
            put "/osgi/bundles/#{active['id']}/state", "{ state: #{unputtable_state} }", 
              :as => :zelda, 'Content-Type' => 'application/json'
            response.status_code.should == 200
            response.json['status'].should == unputtable_state
          end
        end
      end
    end
  end
end