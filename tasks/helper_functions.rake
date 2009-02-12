###### HELPERS

def db_name
  ENV['DB'] || 'hsqldb'
end

def hsqldb
  hsqldb_dir = ENV['HSQLDB_DIR'] || _('hsqldb')
  {
    :dir => hsqldb_dir,
    :url => "jdbc:hsqldb:file:#{hsqldb_dir}/#{db_name}",
    :files => %w(script properties).collect { |ext| File.join(hsqldb_dir, "#{db_name}.#{ext}") }
  }
end

def ant_classpath(proj)
  [proj.compile.dependencies + LOGBACK].flatten.collect { |a| a.to_s }.join(':')
end

# Discovers and loads the datasource properties file into the target ant project
def datasource_properties(ant)
  ant.taskdef :name => 'datasource_properties', 
    :classname => "gov.nih.nci.cabig.ctms.tools.ant.DataSourcePropertiesTask",
    :classpath => ant_classpath(project('psc:core'))
  ant.datasource_properties :applicationDirectoryName => APPLICATION_SHORT_NAME,
    :databaseConfigurationName => db_name
  ant.echo :message => "Migrating ${datasource.url}"
end