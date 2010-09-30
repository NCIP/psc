describe "GET" do
  describe "/users" do
    def actual_user(username)
      response.json["users"].detect { |u| u["username"] == username }
    end

    def actual_usernames
      response.json["users"].collect { |u| u["username"] }
    end

    def actual_role(username, role_key)
      actual_user(username)["roles"].detect { |r| r["key"] == role_key }
    end

    describe "authorization" do
      it "is available to site-scoped user admins" do
        get "/users", :as => "yvette"
        response.status_code.should == 200
      end

      it "is available to global user admins" do
        get "/users", :as => "juno"
        response.status_code.should == 200
      end

      it "is not available to other users" do
        get "/users", :as => "alice"
        response.status_code.should == 403
      end
    end

    describe "with no params" do
      before do
        get "/users", :as => "yvette"
        @darlene = actual_user("darlene")
      end

      it "returns all the users" do
        actual_usernames.should ==
          %w(alice barbara carla darlene erin frieda gertrude hannah juno yvette zelda)
      end

      it "includes the correct total" do
        response.json["total"].should == 11
      end

      it "includes the correct offset" do
        response.json["offset"].should == 0
      end

      it "does not have a limit" do
        response.json["limit"].should be_nil
      end

      it "includes the first name" do
        @darlene["first_name"].should == "Darlene"
      end

      it "includes the last name" do
        @darlene["last_name"].should == "User"
      end

      it "does not include roles" do
        @darlene["roles"].should be_nil
      end
    end

    describe "?q" do
      before do
        get "/users?q=ar", :as => "yvette"
        @actual = response.json
      end

      it "returns the right users" do
        actual_usernames.should == %w(barbara carla darlene)
      end

      it "includes the correct total" do
        response.json["total"].should == 3
      end

      it "includes the correct offset" do
        response.json["offset"].should == 0
      end

      it "does not have a limit" do
        response.json["limit"].should be_nil
      end
    end

    describe "?brief=false" do
      describe "as a constrained user administrator" do
        before do
          get "/users?brief=false", :as => "yvette"
          @erin = actual_user("erin")
        end

        it "includes all users" do
          response.json["users"].size.should == 11
        end

        it "includes role information" do
          @erin["roles"].size.should == 2
        end

        it "does not include scope for sites the UA has no privs for" do
          # not PA015
          actual_role("erin", "subject_manager")["sites"].should ==
            [{
              "identifier" => "IL036",
              "name" => "Northwestern University Robert H. Lurie Comprehensive Cancer Center"
            }]
        end

        it "includes role-only information for users with no visible scope" do
          actual_role("hannah", "study_creator").should == {
            "key" => "study_creator",
            "display_name" => "Study Creator",
            "sites" => []
          }
        end

        it "includes all-study scope information" do
          actual_role("erin", "study_subject_calendar_manager")["all_studies"].should == true
          actual_role("erin", "study_subject_calendar_manager")["studies"].should be_nil
        end

        it "includes all-site scope information" do
          actual_role("barbara", "study_qa_manager")["all_sites"].should == true
          actual_role("barbara", "study_qa_manager")["sites"].should be_nil
        end
      end

      describe "as an unlimited user administrator" do
        before do
          get "/users?brief=false", :as => "juno"
          @erin = actual_user("erin")
        end

        it "include all users" do
          response.json["users"].size.should == 11
        end

        it "includes all scope information" do
          actual_role("erin", "subject_manager")["sites"].
            collect { |rs| rs["identifier"] }.should ==
            %w(IL036 PA015)
        end
      end
    end

    describe "?limit" do
      describe "(without offset)" do
        before do
          get "/users?limit=3", :as => "juno"
        end

        it "returns the first users" do
          actual_usernames.should == %w(alice barbara carla)
        end

        it "includes the correct offset" do
          response.json["offset"].should == 0
        end

        it "includes the correct limit" do
          response.json["limit"].should == 3
        end

        it "includes the correct total" do
          response.json["total"].should == 11
        end
      end

      describe "&offset" do
        before do
          get "/users?limit=4&offset=3", :as => "juno"
        end

        it "returns the requested users" do
          actual_usernames.should == %w(darlene erin frieda gertrude)
        end

        it "includes the correct offset" do
          response.json["offset"].should == 3
        end

        it "includes the correct limit" do
          response.json["limit"].should == 4
        end

        it "includes the correct total" do
          response.json["total"].should == 11
        end
      end

      describe "&offset&q" do
        before do
          get "/users?q=a&limit=3&offset=2", :as => "juno"
        end

        it "returns the matching users" do
          actual_usernames.should == %w(carla darlene frieda)
        end

        it "includes the correct offset" do
          response.json["offset"].should == 2
        end

        it "includes the correct limit" do
          response.json["limit"].should == 3
        end

        it "includes the correct total" do
          response.json["total"].should == 7
        end
      end

      describe "&offset (past end)" do
        before do
          get "/users?limit=4&offset=13", :as => "juno"
        end

        it "is a bad request" do
          response.status_code.should == 400
        end

        it "provides a useful error message" do
          response.entity.should =~
            /Offset 13 is too large.  There are 11 result\(s\), so the max offset is 10./
        end
      end
    end

    describe "?offset (without limit)" do
      before do
        get "/users?offset=5", :as => "juno"
      end

      it "is a bad request" do
        response.status_code.should == 400
      end

      it "gives a useful error message" do
        response.entity.should =~ /Offset does not make sense without limit./
      end
    end
  end
end
