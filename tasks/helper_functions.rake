###### HELPERS

require 'json'
require 'restclient'

def db_name
  set_db_name($db_name || ENV['DB'] || 'hsqldb')
end

def set_db_name(name)
  unless $db_name && $db_name == name
    info "#{$db_name ? 'Switching' : 'Setting'} datasource configuration name to #{name.inspect}"
  end
  $db_name = name
  Java.java.lang.System.setProperty("psc.config.datasource", $db_name)
  $db_name
end

def env_true?(name)
  ENV[name] && ENV[name] =~ /(^y)|(yes)|(true)/i
end

def emma?
  ENV['EMMA']
end

def hsqldb?
  db_name =~ /hsqldb/
end

def hsqldb
  hsqldb_dir = ENV['HSQLDB_DIR'] || _('hsqldb')
  {
    :dir => hsqldb_dir,
    :url => "jdbc:hsqldb:file:#{hsqldb_dir}/#{db_name}",
    :files => %w(script properties).collect { |ext| File.join(hsqldb_dir, "#{db_name}.#{ext}") } + [ "#{ENV['HOME']}/.#{APPLICATION_SHORT_NAME}/#{db_name}.properties" ]
  }
end

def ant_classpath(proj)
  [proj.compile.dependencies + LOGBACK.values].flatten.collect { |a| a.to_s }.join(':')
end

# Discovers and loads the datasource properties file into the target ant project
def datasource_properties(ant)
  ant.taskdef :name => 'datasource_properties',
    :classname => "gov.nih.nci.cabig.ctms.tools.ant.DataSourcePropertiesTask",
    :classpath => ant_classpath(project('psc:core'))
  ant.datasource_properties :applicationDirectoryName => APPLICATION_SHORT_NAME,
    :databaseConfigurationName => db_name
end

def db_deps
  [
    DB.postgresql,
    DB.hsqldb,
    (DB.oracle if env_true?('ORACLE'))
  ].compact
end

# building the wsrf directory name to be used by the grid services for deployment
def wsrf_dir
  [ENV['CATALINA_HOME'], "/webapps/", wsrf_dir_name].join('')
end

def wsrf_dir_name
  ENV['WSRF_DIR_NAME'] || "wsrf"
end

def java6?
  Java.java.lang.System.getProperty("java.specification.version").split('.')[1].to_i > 5
end

def user_settings
  if Buildr.settings.user['psc']
    Buildr.settings.user
  else
    YAML.load(open("#{File.dirname(__FILE__)}/../buildr-user-settings.yaml.default"))
  end
end

def psc_api_url(resource)
  "http://#{user_settings['psc']['dev_admin']['username']}:#{user_settings['psc']['dev_admin']['password']}@localhost:7200/psc/api/v1/#{resource}"
end

# starts up an OSGi bundle in the deployed webapp
def start_bundle(symbolic_name)
  bundles = 
    begin
      JSON.parse(RestClient.get(psc_api_url('osgi/bundles'), :accept => '*/*')).
        select { |b| symbolic_name === b['symbolic_name'] }
    rescue => e
      warn "Starting #{symbolic_name} failed: #{e}"
      e.backtrace.each { |l| trace "  #{l}" }
      return false
    end
  if bundles.empty?
    warn "No bundle matching #{symbolic_name.inspect}"
    return false
  end
  begin
    bundles.each { |b|
      trace "Attempting to start #{b.inspect}"
      RestClient.put(psc_api_url("osgi/bundles/#{b['id']}/state"),
        '{ state: STARTING }', :content_type => 'application/json')
    }
  rescue => e
    warn e
    return false
  end
end