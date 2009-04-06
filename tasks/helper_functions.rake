###### HELPERS

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
