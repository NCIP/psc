require 'facets/dictionary'
require 'buildr/java/bdd'

class Buildr::RSpec < TestFramework::JavaBDD
  def runner_config
    runner = super
    # use facets' Dictionary to preserve insertion order so that the deps
    # get installed first
    runner.gems = Dictionary.new.merge runner.gems
    
    # explicity include rspec deps for auto-install
    runner.gems.update 'hoe' => '>0'
    runner.gems.update 'cucumber' => '>=0.1.13'

    runner.gems.update 'rspec' => '=1.1.12'
    runner.requires.unshift 'spec'
    runner
  end
end