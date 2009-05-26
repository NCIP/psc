// NOTE: This script expects to be ran from the #{RAILS_ROOT}/[test|spec|examples]/javascript directory!

if(arguments.length == 0) {
  print("Usage: test_runner.js /path/to/buildr-ridge /path/to/mainfiles /path/to/specfiles file_spec.js");
  quit(1);
}

print("test_runner.js " + arguments.join(" "))

var BUILDR_RIDGE = arguments[0]
var MAIN_PATH = arguments[1]
var SPEC_PATH = arguments[2]
var SPEC_FILE = arguments[3]

function require_main(file, options) { 
  require_absolute(MAIN_PATH + '/' + file)
}

function require_spec(file, options) { 
  require_absolute(SPEC_PATH + '/' + file)
}

function require_absolute(file, options) {
  load(file)

  options = options || {};
  if(options['onload']) {
    options['onload'].call();
  }
}

function debug(message){
  print(message);
}

// Mock up the Firebug API for convenience.
var console = {debug:debug};

var fixture = "fixtures/" + SPEC_FILE.replace(/^(.*?)_spec\.js$/, "$1.html");
print("Running " + SPEC_FILE + " with fixture '" + fixture + "'...");

load(BUILDR_RIDGE + "/lib/env.rhino.js");
window.location = fixture;

load(BUILDR_RIDGE + "/lib/jquery-1.3.2.js");
load(BUILDR_RIDGE + "/lib/jquery.fn.js");
load(BUILDR_RIDGE + "/lib/jquery.print.js");
load(BUILDR_RIDGE + "/lib/screw.builder.js");
load(BUILDR_RIDGE + "/lib/screw.matchers.js");
load(BUILDR_RIDGE + "/lib/screw.events.js");
load(BUILDR_RIDGE + "/lib/screw.behaviors.js");
load(BUILDR_RIDGE + "/lib/smoke.core.js");
load(BUILDR_RIDGE + "/lib/smoke.mock.js");
load(BUILDR_RIDGE + "/lib/smoke.stub.js");
load(BUILDR_RIDGE + "/lib/screw.mocking.js");
load(BUILDR_RIDGE + "/lib/consoleReportForRake.js");

load(SPEC_PATH + '/' + SPEC_FILE);
jQuery(window).trigger("load");
