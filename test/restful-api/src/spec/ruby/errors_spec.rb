describe "Error responses" do
  describe "for XML" do
    describe "which is structurally invalid" do
      before do
        put '/sites/PA121', "<site hello/>", :as => :juno
      end

      it "is a bad request" do
        response.status_code.should == 400
      end

      it "includes a detailed error" do
        response.entity.should include(
          "Could not parse the provided XML: Error on line 1 of document");
      end
    end
  end
end
