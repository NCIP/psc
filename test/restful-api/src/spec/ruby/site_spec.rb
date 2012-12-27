#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe "/sites/{site-identifier}" do
  before do
    @massgeneral = PscTest::Fixtures.createSite("Massachussets General Hospital", "MA034")
    application_context['siteDao'].save(@massgeneral)
  end

  describe "GET" do
    it "forbids site access for unauthenticated users" do
      get "/sites/MA034", :as => nil
      response.status_code.should == 401
    end

    it "includes site attributes in XML response" do
      get "/sites/MA034", :as => :juno
      response.xml_attributes("site", "assigned-identifier").should include("MA034")
      response.xml_attributes("site", "site-name").should include("Massachussets General Hospital")
      response.xml_elements('//site').should have(1).elements
    end

    it "shows all sites to POIMs" do
      get "/sites/MA034", :as => :juno
      response.status_code.should == 200
    end

    it "gives 404 for unknown site" do
      get "/sites/MN009", :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "shows sites to affiliated users" do
      get "/sites/IL036", :as => :carla # carla is authorized for IL036
      response.status_code.should == 200
    end

    it "limits site visibility to associated users" do
      get "/sites/MN026", :as => :carla # carla is only authorized for IL036
      response.status_code.should == 403
    end
  end

  describe "PUT" do
    describe "new" do
      before do
        @tju_xml = psc_xml("site",
          'assigned-identifier' => "PA121", 'site-name' => "Thomas Jefferson University")
      end

      it "is forbidden for unauthorized users" do
        put '/sites/PA121', @tju_xml, :as => :gertrude
        response.status_code.should == 403
      end

      it "works for authorized user" do
        put '/sites/PA121', @tju_xml, :as => :juno
        response.status_code.should == 201
        response.xml_attributes("site", "assigned-identifier").should include("PA121")
        response.xml_attributes("site", "site-name").should include("Thomas Jefferson University")
        response.xml_elements('//site').should have(1).elements
      end
    end

    describe "update" do
      before do
        @update_mass = psc_xml("site",
          'assigned-identifier' => @massgeneral.assigned_identifier,
          'site-name' => "Massive Dynamic Cancer Center")
      end

      it "is forbidden for unauthorized users" do
        put '/sites/PA121', @update_mass, :as => :hannah
        response.status_code.should == 403
      end

      it "updates an existing site for an authorized sysadmin" do
        put '/sites/MA034', @update_mass, :as => :juno
        response.status_code.should == 200
        response.xml_attributes("site", "assigned-identifier").should include("MA034")
        response.xml_attributes("site", "site-name").should include("Massive Dynamic Cancer Center")
        response.xml_elements('//site').should have(1).elements
      end
    end
  end

  describe "DELETE" do
    it "deletes a site for an authorized user" do
      delete '/sites/MA034', :as => :juno
      response.status_code.should == 200
      response.status_message.should == "OK"

      get '/sites/MA034', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    it "gets 404 if site with given assigned identifier doesn't exist" do
      delete '/sites/IL000', :as => :juno
      response.status_code.should == 404
      response.status_message.should == "Not Found"
    end

    describe "site with assignment" do
      before do
        @site = PscTest::Fixtures.createSite("Test Site", "S001")
        application_context['siteDao'].save(@site)
        @ecog170 = create_study 'ECOG170' do |s|
          s.planned_calendar do |cal|
            cal.epoch "Followup" do |e|
              e.study_segment "B" do |a|
                a.period "P2", :start_day => 2, :duration => [3, :day], :repetitions => 1 do |p|
                  p.activity "Lab Test", 1
                  p.activity "Physical Test", 2
                end
              end
            end
          end
        end

        @studySite = PscTest::Fixtures.createStudySite(@ecog170, @site)
        application_context['studySiteDao'].save(@studySite)
        @approve_date = PscTest.createDate(2008, 12, 20)
        @studySite.approveAmendment(@ecog170.amendment, @approve_date)
        application_context['studySiteDao'].save(@studySite)
        @subject = PscTest::Fixtures.createSubject("ID001", "Alan", "Boyarski", PscTest.createDate(1983, 3, 23))
        @studySubjectAssignment = application_context['subjectService'].assignSubject(
          @studySite,
          Psc::Service::Presenter::Registration::Builder.new.
            subject(@subject).first_study_segment(@ecog170.plannedCalendar.epochs.first.studySegments.first).
            date(PscTest.createDate(2008, 12, 26)).manager(erin).
            to_registration)
      end

      it "gets 400" do
        delete '/sites/S001', :as => :juno
        response.status_code.should == 400
        response.status_message.should == "Bad Request"
      end
    end
  end
end
