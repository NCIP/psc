# If running in buildr-1.3.3 (which depends on an old version of
# rake), we need to provide this method so that iidea doesn't croak
# during initialization.
# This is a temporary kludge until PSC #1091 is fixed and we can drop
# support for buildr 1.3.x.
if Buildr::VERSION == '1.3.3'
  Rake::FileTask.class_eval <<-RUBY
    def clear_actions; end
  RUBY
end

require 'buildr_iidea'

task :iidea => 'iidea:generate'
