# Monkeypatch OpenObject to fix BUILDR-247

class OpenObject < Hash
  def initialize(source=nil, &block)
    super(&block)
    update(source) if source
  end
  
  %w([] []= delete to_hash).each do |n|
    define_method n do
      super
    end
  end
end
