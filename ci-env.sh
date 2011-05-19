##
# A script to set up the environment for running the tests in CI.
# This script should be sourced, not executed.

RUBY=ruby-1.8.7-p330
GEMSET=psc

set +xe
. ~/.rvm/scripts/rvm
rvm use "${RUBY}@${GEMSET}"
set -xe
ruby install_gems.rb

export JAVA_OPTS="-Xmx256M -XX:MaxPermSize=128M"
# For the integration test request report
export REQUEST_LOG_INCLUDE_ALL=true
