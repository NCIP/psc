log_level :DEBUG

classpath 'src/main/webapp/WEB-INF/classes'
classpath 'target/test-classes'
classpath Dir['{test/lib,src/main/webapp/WEB-INF/lib}/**/*.jar']

after do
  application_context['databaseInitializer'].afterAll
end

ignore Dir['test/restful-api/lib/**/*']
