// The intent is that this file will eventually be automatically generated
// by the buildr-ridge test framework so that paths to the js files can be 
// filtered in.  This is unnecessary until buildr-ridge is factored out into 
// a separate gem.

//////

// Ported from blue-ridge.js in blue-ridge
function require(url, options) {
  //this function expects to be ran from the context of the spec/javascripts/fixtures or test/javascript/fixtures
  //directory, so add a '../' prefix to all Javascript paths
  url = "../" + url;
  
  var head = document.getElementsByTagName("head")[0];
  var script = document.createElement("script");
  script.src = url;
  
  options = options || {};
  
  if (options['onload']) {
    // Attach handlers for all browsers
    script.onload = script.onreadystatechange = options['onload'];
  }
  
  head.appendChild(script);
}

function debug(message) {
  document.writeln(message + " <br/>");
}

function derive_spec_name_from_current_file() {
  var file_prefix = new String(window.location).match(/.*\/(.*?)\.html/)[1];
  return file_prefix + "_spec.js";
}

require("../../../../../tasks/buildr-ridge/lib/jquery-1.3.2.js");
         
require("../../../../../tasks/buildr-ridge/lib/jquery.fn.js");
require("../../../../../tasks/buildr-ridge/lib/jquery.print.js");
require("../../../../../tasks/buildr-ridge/lib/screw.builder.js");
require("../../../../../tasks/buildr-ridge/lib/screw.matchers.js");
require("../../../../../tasks/buildr-ridge/lib/screw.events.js");
require("../../../../../tasks/buildr-ridge/lib/screw.behaviors.js");
require("../../../../../tasks/buildr-ridge/lib/smoke.core.js");
require("../../../../../tasks/buildr-ridge/lib/smoke.mock.js");
require("../../../../../tasks/buildr-ridge/lib/smoke.stub.js");
require("../../../../../tasks/buildr-ridge/lib/screw.mocking.js");

require(derive_spec_name_from_current_file());
