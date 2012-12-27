#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

if Gem::Version.new(Buildr::VERSION.dup) < Gem::Version.new("1.3.5")
  # Monkeypatch OpenObject to fix BUILDR-247

  class OpenObject < Hash
    def initialize(source=nil, &block)
      super(&block)
      update(source) if source
    end
  
    %w([] []= delete to_hash).each do |n|
      class_eval <<-RUBY
        def #{n}(*args)
          super
        end
      RUBY
    end
  end
end
