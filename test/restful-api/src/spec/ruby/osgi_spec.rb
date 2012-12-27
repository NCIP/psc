#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

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

    def slf4j_bundle
      response.json.find { |b| b['symbolic_name'] == 'slf4j.api' }
    end

    it "has a symbolic name in the bundle description" do
      response.json.first['symbolic_name'].should_not be_nil
    end

    it "has an ID in the bundle description" do
      slf4j_bundle['id'].should_not be_nil
    end

    it "has a version in the bundle description" do
      slf4j_bundle['version'].should_not be_nil
    end

    it "has a state in the bundle description" do
      slf4j_bundle['state'].should_not be_nil
    end

    it "has a name in the bundle description" do
      slf4j_bundle['name'].should == "slf4j-api"
    end

    it "has a description in the bundle description" do
      slf4j_bundle['description'].should == "The slf4j API"
    end
  end

  describe "/{bundle-id}" do
    before do
      get "/osgi/bundles", :as => :zelda
      response.status_code.should == 200
      @bundles = response.json
    end

    describe "GET" do
      it "forbids access to non-sysadmins" do
        get "/osgi/bundles/#{@bundles[7]['id']}", :as => :barbara
        response.status_code.should == 403
      end

      it "404s for an unknown bundle ID" do
        get "/osgi/bundles/-63", :as => :zelda
        response.status_code.should == 404
      end

      it "returns the metadata for a valid bundle ID" do
        get "/osgi/bundles/#{@bundles[7]['id']}", :as => :zelda
        response.status_code.should == 200
        response.json['state'].should == @bundles[7]['state']
        response.json['name'].should == @bundles[7]['name']
        response.json['version'].should == @bundles[7]['version']
      end
    end

    describe "/state" do
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
          installed = @bundles.find { |b| b['symbolic_name'] =~ /psc-providers-mock/ }
          installed.should_not be_nil
          installed['state'].should == 'INSTALLED'
          put "/osgi/bundles/#{installed['id']}/state", "{ state: STARTING }",
            :as => :zelda, 'Content-Type' => 'application/json'
          response.status_code.should == 200
          response.json['state'].should == 'ACTIVE'
        end

        it "stops a started bundle" do
          active = @bundles.find { |b| b['symbolic_name'] =~ /websso-plugin/ }
          active.should_not be_nil
          active['state'].should == 'ACTIVE'
          put "/osgi/bundles/#{active['id']}/state", "{ state: STOPPING }",
            :as => :zelda, 'Content-Type' => 'application/json'
          response.status_code.should == 200
          %w(INSTALLED RESOLVED).should include(response.json['state'])
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
              response.json['state'].should == unputtable_state
            else
              pending "There is no bundle in the #{unputtable_state} state for testing"
            end
          end
        end
      end
    end
  end
end
