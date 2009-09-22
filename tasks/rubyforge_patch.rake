# Monkey patch for this rubyforge gem bug:
#   http://rubyforge.org/tracker/index.php?func=detail&aid=21355&group_id=1024&atid=4025
# Can't just upgrade the rubyforge gem because buildr wants a specific version

require 'net/http'

class Net::HTTP
  def use_ssl= flag
    self.old_use_ssl = flag
    @ssl_context.tmp_dh_callback = proc {} if @ssl_context
  end
end if Net::HTTP.public_instance_methods.include? "old_use_ssl="