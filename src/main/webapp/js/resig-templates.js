// Simple JavaScript Templating
// John Resig - http://ejohn.org/ - MIT Licensed
// See: http://ejohn.org/blog/javascript-micro-templating/
// Modifications for PSC:
//  - Split out compile function for debugging
//  - Switched template delimiters from ERB-style <% to vaguely freemarker-ish [#
//    since <% is also used by JSP
(function(){
  var cache = {};

  this.resigTemplate = function resigTemplate(str, data){
    // Figure out if we're getting a template, or if we need to
    // load the template - and be sure to cache the result.
    var fn = !/\W/.test(str) ?
      cache[str] = cache[str] ||
        resigTemplate(document.getElementById(str).innerHTML) : resigTemplateCompile(str)

    // Provide some basic currying to the user
    return data ? fn( data ) : fn;
  };
})();

function resigTemplateCompile(str) {
  // Generate a reusable function that will serve as a template
  // generator (and which will be cached).
  var src =
    "var p=[],print=function(){p.push.apply(p,arguments);};" +

    // Introduce the data as local variables using with(){}
    "with(obj){p.push('" +

    // Convert the template into pure JavaScript
    str
      .split("&lt;").join("<")
      .split("&gt;").join(">")
      .replace(/[\r\t\n]/g, " ")
      .split("[#").join("\t")
      .replace(/((^|#\])[^\t]*)'/g, "$1\r")
      .replace(/\t=(.*?)#\]/g, "',$1,'")
      .split("\t").join("');")
      .split("#]").join("p.push('")
      .split("\r").join("\\'")
  + "');}return p.join('');"

  console.log(src)

  return new Function("obj", src);
}