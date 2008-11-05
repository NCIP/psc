require 'yaml'

helper_for Spec::Example::ExampleGroup do
  def load_site(assigned_identifier)
    application_context['siteDao'].getByAssignedIdentifier(assigned_identifier)
  end

  # creates literate accessors for each of the sites defined in sites.yml
  YAML.load_file("#{File.dirname(__FILE__)}/static-data/sites.yml").each do |k, v|
    define_method(k) do
      load_site(v['assignedIdentifier'])
    end
  end
end