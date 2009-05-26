# buildr-ridge is an adaption of blue-ridge, a javascript testing plugin for
# rails applications.  http://github.com/relevance/blue-ridge/tree/master

require 'buildr'
require 'sinatra'
require 'haml'

module Buildr
  module JavaScript
    class Ridge < TestFramework::Base
      class << self
        def applies_to?(project)
          !test_files(project).empty?
        end
        
        def test_files(project)
          Dir[spec_path(project) + '/**/*_spec.js']
        end
        
        def path_to(file=nil)
          File.dirname(__FILE__) + "/buildr-ridge/#{file}"
        end

        def rhino_command(*args)
          "java -jar #{path_to 'lib/js.jar'} -w -debug #{args.join(' ')}"
        end
        
        def main_path(project)
          project.test.options[:main_path] || project.path_to(:source, :main, :javascript)
        end
        
        def spec_path(project)
          project.path_to(:source, :spec, :javascript)
        end
      end
      
      def tests(dependencies)
        self.class.test_files(task.project)
      end
      
      def run(tests, dependencies)
        cd self.class.spec_path(task.project) do
          runner = self.class.rhino_command(self.class.path_to('lib/test_runner.js'))
          args = [
            self.class.path_to[0..-2],
            self.class.main_path(task.project),
            self.class.spec_path(task.project)
          ]
          
          return tests.select { |test| system("#{runner} '#{args.join("' '")}' '#{test.sub(%r{^#{self.class.spec_path(task.project)}/}, '')}'") }
        end
      end
    end
    
    module RidgeTasks
      include Buildr::Extension

      after_define do |project|
        if project.test.framework == :ridge
          RidgeTasks.define_shell_tasks(project)
          RidgeTasks.define_server_task(project)
        end
      end

      private

      def self.define_server_task(project)
        task "ridge:serve" do
          Server.set :tests, Ridge.test_files(project)
          Server.set :main_path, Ridge.main_path(project)
          Server.set :spec_path, Ridge.spec_path(project)
          Server.set :project_name, project.to_s
          Server.run!
        end
      end

      def self.define_shell_tasks(project)
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
      
      class Server < Sinatra::Base
        set :port, 2702
        set :root, File.dirname(__FILE__) + "/buildr-ridge/server"
        set :static, true
        set :public, File.dirname(__FILE__) + "/buildr-ridge"
        
        get '/' do
          section_map = options.tests.
            collect { |t| t.sub(%r{^#{options.spec_path}/?}, '') }.
            collect { |t| [File.dirname(t), 'fixtures/' + File.basename(t).sub(/_spec.js$/, '.html')] }.
            inject({}) { |h, (dir, file)| h[dir] ||= []; h[dir] << file; h }
          @sections = section_map.collect { |dir, files| [dir, files.sort] }.sort
          
          haml :index
        end

        get '/spec/*' do
          map_file(options.spec_path, params[:splat].first, "Spec")
        end

        get '/main/*' do
          map_file(options.main_path, params[:splat].first, "Main")
        end
        
        get '/screw.css' do
          screw_css = File.join(options.spec_path, 'screw.css')
          unless File.exist?(screw_css)
            screw_css = File.join(options.public, 'lib', 'screw.css')
          end

          content_type 'text/css'
          File.read screw_css
        end

        def map_file(path, name, desc)
          file = File.join(path, name)
          if File.exist?(file)
            case file
            when /.js$/
              content_type 'text/javascript'
            when /.css$/
              content_type 'text/css'
            end
            File.read(file)
          else
            halt 404, "#{desc} file not found: #{file}"
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
