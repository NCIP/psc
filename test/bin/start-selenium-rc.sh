#!/bin/sh

DIR=`dirname $0`

# TODO: detect whether firefox-bin is on the path first

echo "Please make sure firefox-bin is on the path. Otherwise you will have problems along the lines of:"
echo "     SeleniumCommandError: ERROR: No launcher found for sessionId null."
echo "when you run your selenium tests."

# choose a higher port than the default 4444, I was having conflicts with 4444
java -jar $DIR/../lib/selenium-server-0.8.1.jar -port 12452

