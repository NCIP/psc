log_level :DEBUG

classpath 'src/main/webapp/WEB-INF/classes'
classpath 'target/test-classes'
classpath Dir['{test/lib,src/main/webapp/WEB-INF/lib}/**/*.jar']

after do
  application_context['databaseInitializer'].afterAll
end

ignore Dir['test/restful-api/lib/**/*']

# simulate rubygems by unpacking gems into test/restful-api/lib
# a la http://ola-bini.blogspot.com/2008/06/jtestr-rubygems-and-external-code.html
Dir["test/restful-api/lib/*/lib"].each do |dir|
  $LOAD_PATH << dir
end