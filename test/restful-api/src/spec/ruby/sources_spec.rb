describe "/activities" do
  describe "GET" do
    it "forbids access to sources for an unauthorized user" do
      get '/activities', :as => nil
      response.status_code.should == 401
      response.status_message.should == "Unauthorized"
      response.content_type.should == 'text/html'
    end

    it "allows access to sources for an authorized user" do
      get '/activities', :as => :juno

      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
      response.xml_attributes("source", "name").should include("Northwestern University")
    end

    it "allows access to a specific group of activity(s) by specifying a query parameter" do
      get '/activities/?q=x-ray', :as => :juno

      response.status_code.should == 200
      response.content_type.should == 'text/xml'
      response.xml_attributes("activity", "name").should include("X-Ray: Skeletal")
      response.xml_elements('//activity').should have(3).elements
    end

    describe "api /sources" do
      before do
        pending "#1211"
      end

      it "forbids access to sources for user other than system administrator" do
        get '/sources', :as => :hannah
        response.status_code.should == 403
        response.status_message.should == "Forbidden"
      end

      it "allows access to sources to system administrator"do
        get '/sources', :as => :zelda
        response.status_code.should == 200
        response.status_message.should == "OK"
        response.content_type.should == 'text/xml'
      end

      describe "xml" do
        before do
          get '/sources', :as => :zelda
        end
        it "is successful" do
          response.should be_success
        end
        it "is XML" do
          response.content_type.should == 'text/xml'
        end
        it "has the right number of sources" do
          response.xml_elements('//source').should have(3).sources
        end
        describe "source structure" do
          it "has name" do
            response.xml_attributes("source", "name").should include("Diabetes")
          end
          it "has manual flag" do
            response.xml_attributes("source", "manual-flag").should include("true")
          end
        end
      end

      describe "json" do
        before do
          get '/sources.json', :as => :zelda
        end
        it "is successful" do
          response.should be_success
        end
        it "is JSON" do
          response.content_type.should == 'application/json'
        end
        it "contains the right number of studies" do
          response.json["sources"].size.should == 3
        end
        describe "source structure" do
          before do
            @sources = response.json["sources"]
            @source = @sources[2]
          end
          it "has name" do
            @source["name"].should == "PSC - Manual Activity Creation"
          end
          it "has manual_flag" do
            @source["manual_flag"].should == true
          end
        end
      end
    end
  end

end
