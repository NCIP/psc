##
# A script to set up the environment for running the tests in CI.
# This script should be sourced, not executed.

RUBY=ruby-1.8.7-p330
GEMSET=psc

set +x
. ~/.rvm/scripts/rvm
rvm use "${RUBY}@${GEMSET}"
set -x
ruby install_gems.rb

export JAVA_OPTS="-Xmx256M -XX:MaxPermSize=128M"
