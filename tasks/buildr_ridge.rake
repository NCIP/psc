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

        def path_to(file=nil)
          File.dirname(__FILE__) + "/buildr-ridge/#{file}"
        end

        def rhino_command(*args)
          "java -jar #{path_to '/lib/js.jar'} -w -debug #{args.join(' ')}"
        end
      end
      
      def tests(dependencies)
        self.class.test_files(task.project)
      end
      
      def run(tests, dependencies)
        cd task.project._(:source, :spec, :javascript) do
          test_runner_command = self.class.rhino_command(self.class.path_to('lib/test_runner.js'))

          return tests.select { |test| system("#{test_runner_command} '#{self.class.path_to[0..-1]}' #{File.basename test}") }
        end
      end
    end
    
    module RidgeTasks
      include Buildr::Extension
      
      after_define do |project|
        if project.test.framework == :ridge
          shell_js = project._(:target, :ridge, "shell.js")
          shell_html = project._(:target, :ridge, "fixtures", "shell.html")

          file shell_html do |t|
            mkdir_p File.dirname(t.to_s)
            cp Ridge.path_to('lib/shell.html'), t.to_s
          end

          file shell_js => shell_html do
            Filter.new.clear.from(Ridge.path_to('lib')).
              include("shell.js").
              into(File.dirname(shell_js)).
              using(:buildr_ridge_root => Ridge.path_to[0..-2]).
              run
          end
          
          desc "JavaScript shell for #{project}"
          project.task("ridge:shell" => shell_js) do |t|
            cd project._ do
              rlwrap = `which rlwrap`.chomp
              cmd = "#{rlwrap} #{Ridge.rhino_command('-f', t.prerequisites.first.to_s, '-f', '-')}"
              trace "Starting shell with #{cmd}"
              system(cmd)
            end
          end
        end
      end
    end
  end
end

Buildr::TestFramework << Buildr::JavaScript::Ridge
class Buildr::Project
  include Buildr::JavaScript::RidgeTasks
end
