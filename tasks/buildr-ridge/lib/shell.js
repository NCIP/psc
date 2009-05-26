(function(){
  var _old_quit = this.quit;
  this.__defineGetter__("exit", function(){ _old_quit() });
  this.__defineGetter__("quit", function(){ _old_quit() });
  
  print("=================================================");
  print(" Rhino JavaScript Shell");
  print(" To exit type 'exit', 'quit', or 'quit()'.");
  print("=================================================");

  var fixture_file = "target/ridge/fixtures/shell.html";

  load("${buildr_ridge_root}/lib/env.rhino.js");
  print(" - loaded env.js");

  window.location = fixture_file;
  print(" - sample DOM loaded");

  load("${buildr_ridge_root}/lib/jquery-1.3.2.js");
  print (" jQuery-1.3.2 loaded");
  // load(PLUGIN_PREFIX + "lib/jquery-1.2.6.js");
  // print(" - jQuery-1.2.6 loaded");

  print("=================================================");
})();
