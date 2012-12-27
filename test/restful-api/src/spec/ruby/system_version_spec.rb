#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

describe '/system-version' do
  it 'is not available when not authenticated' do
    get '/system-version'
    response.status_code.should == 401
  end

  it 'is available to any authenticated user' do
    get '/system-version', :as => 'carla'
    response.status_code.should == 200
  end

  it 'includes the version number in the entity' do
    get '/system-version', :as => 'carla'
    response.json['psc_version'].should == application_context['buildInfo'].versionNumber
  end
end
