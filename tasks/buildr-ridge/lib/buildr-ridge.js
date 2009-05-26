// Ported from blue-ridge.js in blue-ridge
function require_spec(url, options) {
  require_absolute("/spec/" + url, options)
}

function require_main(url, options) {
  require_absolute("/main/" + url, options)
}

function require_absolute(url, options) {
  console.log("Attempting to load %o", url)
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

require_absolute("/lib/jquery-1.3.2.js");
require_absolute("/lib/jquery.fn.js");
require_absolute("/lib/jquery.print.js");
require_absolute("/lib/screw.builder.js");
require_absolute("/lib/screw.matchers.js");
require_absolute("/lib/screw.events.js");
require_absolute("/lib/screw.behaviors.js");
require_absolute("/lib/smoke.core.js");
require_absolute("/lib/smoke.mock.js");
require_absolute("/lib/smoke.stub.js");
require_absolute("/lib/screw.mocking.js");

require_spec(derive_spec_name_from_current_file());
