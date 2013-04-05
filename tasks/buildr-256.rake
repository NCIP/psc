#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.io/psc/LICENSE.txt for details.
#L

require 'buildr/java/bdd'

module Buildr::TestFramework::JRubyBased
  protected

  def jruby_gem
    %{
     require 'jruby'
     def JRuby.gem(name, version = '>0', *args)
        require 'rbconfig'
        jruby_home = Config::CONFIG['prefix']
        expected_version = '#{TestFramework::JRubyBased.version}'
        unless JRUBY_VERSION >= expected_version
          fail "Expected JRuby version \#{expected_version} installed at \#{jruby_home} but got \#{JRUBY_VERSION}"
        end
        require 'rubygems'
        begin
          Kernel.send :gem, name, version
        rescue LoadError, Gem::LoadError => e
          #{'puts "Gem #{name} (#{version}) not found.  Installing.  (Message: #{e.message.strip}.)"' if Buildr.application.options.trace}
          require 'rubygems/gem_runner'
          args = ['install', name, '--version', version, '--no-ri', '--no-rdoc'] + args
          begin
            Gem::GemRunner.new.run(args)
          rescue Gem::SystemExitException=>e
            if e.exit_code == 0
              #{'puts "Successfully installed.  #{e.message}"' if Buildr.application.options.trace}
            else
              puts "Install of \#{name} (\#{version}) failed. \#{e.message}"
              raise e
            end
          end
          Kernel.send :gem, name, version
        end
     end
    }
  end
end
