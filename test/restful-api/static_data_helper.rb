require 'yaml'

helper_for Spec::Example::ExampleGroup do
  def load_site(assigned_identifier)
    application_context['siteDao'].getByAssignedIdentifier(assigned_identifier)
  end

  # creates literate accessors for each of the sites defined in sites.yml
  YAML.load_file("#{File.dirname(__FILE__)}/static-data/sites.yml").each do |short_name, attributes|
    define_method(short_name) do
      load_site(attributes['assignedIdentifier'])
    end
  end
  
  def load_user(username)
    application_context['userService'].getUserByName(username)
  end
  
  # creates literate accessors for each of the users defined in users.yml
  YAML.load_file("#{File.dirname(__FILE__)}/static-data/users.yml").each do |username, _|
    define_method(username) do
      load_user(username)
    end
  end
end