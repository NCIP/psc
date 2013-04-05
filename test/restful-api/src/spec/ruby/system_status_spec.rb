#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

describe 'GET /system-status' do
  # Can't practically check failures, so these are just the happy scenarios
  before do
    @actual = get '/system-status'
  end

  it 'is available when not authenticated' do
    response.status_code.should == 200
  end

  it 'is JSON' do
    response.content_type.should == 'application/json'
  end

  describe 'datasource' do
    it 'has an ok/not-ok flag' do
      response.json['system-status']['datasource']['ok'].should == true
    end

    it 'has a message' do
      response.json['system-status']['datasource']['message'].should == 'Domain query successful'
    end
  end

  describe 'csm' do
    it 'has a ok/not-ok flag' do
      response.json['system-status']['csm']['ok'].should == true
    end

    it 'has a message' do
      response.json['system-status']['csm']['message'].
        should == 'CSM available'
    end
  end
end
