# buildr-ridge is an adaption of blue-ridge, a javascript testing plugin for
# rails applications.  http://github.com/relevance/blue-ridge/tree/master

require 'buildr'

module Buildr
  module JavaScript
    class Ridge < TestFramework::Base
      class << self
        def applies_to?(project)
          !test_files(project).empty?
        end
        
        def test_files(project)
          Dir[project.path_to(:source, :spec, :javascript, '**/*_spec.js')]
        end
      end
      
      def tests(dependencies)
        self.class.test_files(task.project)
      end
      
      def run(tests, dependencies)
        cd task.project._(:source, :spec, :javascript) do
          plugin_prefix = File.dirname(__FILE__) + "/buildr-ridge"
          rhino_command = "java -jar #{plugin_prefix}/lib/js.jar -w -debug"
          test_runner_command = "#{rhino_command} #{plugin_prefix}/lib/test_runner.js"

          return tests.select { |test| system("#{test_runner_command} '#{plugin_prefix}' #{File.basename test}") }
        end
      end
    end
  end
end

Buildr::TestFramework << Buildr::JavaScript::Ridge
