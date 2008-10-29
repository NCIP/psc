require File.expand_path("rspec_helper.rb", File.dirname(__FILE__))

describe "/studies" do
  before do
    puts application_context['studyService']
  end

  describe "GET" do
    it "" do
      (2 + 2).should == 4
    end
  end
end