jQuery.noConflict();
require_main("prototype.js");
require_main("common.js");

psc.namespace("test");

psc.test.envjs = function () {
  return navigator.userAgent.indexOf('EnvJS') >= 0;
};

(function ($) {
  Screw.Matchers["raise"] = {
    match: function (expected, actual) {
      try {
        actual();
        return false;
      } catch (e) {
        actual.actualException = e;
        return e == expected;
      }
    },

    failure_message: function (expected, actual, not) {
      var msg = 'expected to ' + (not ? 'not ' : '') + 'throw ' + jQuery.print(expected)
      if (actual.actualException) {
        msg += '; was ' + jQuery.print(actual.actualException);
      }
      return msg;
    }
  }

  Screw.Matchers["include"] = {
    match: function (expected, actual) {
      return $.inArray(expected, actual) >= 0;
    },

    failure_message: function (expected, actual, not) {
      return 'expected ' + $.print(actual) + ' to ' + (not ? 'not ' : '') +
        'include ' + $.print(expected);
    }
  }

  Screw.Matchers["equal_date"] = {
    match: function(expected, actual) {
      return expected.getFullYear() == actual.getFullYear() &&
        expected.getMonth() == actual.getMonth() &&
        expected.getDate() == actual.getDate();
    },

    failure_message: function(expected, actual, not) {
      return 'expected ' + actual + (not ? ' to not equal ' : ' to equal ') + expected;
    }
  }

  Screw.Matchers["equal_utc_date"] = {
    match: function (expected, actual) {
      return expected.getUTCFullYear() == actual.getUTCFullYear() &&
        expected.getUTCMonth() == actual.getUTCMonth() &&
        expected.getUTCDate() == actual.getUTCDate();
    },

    failure_message: function (expected, actual, not) {
      return "expected " + actual + " to " + (not ? 'not ' : '') +
        "be on same UTC day as " + expected;
    }
  }

  Screw.Matchers["equal_utc_time"] = {
    match: function (expected, actual) {
      return expected.getTime() == actual.getTime()
    },

    failure_message: function (expected, actual, not) {
      return "expected " + actual + " to " + (not ? 'not ' : '') +
        "be exactly the same as " + expected;
    }
  }
}(jQuery));

if (!window.console) {
  window.console = {
    log: function () { }
  }
}

// From http://www.webreference.com/programming/javascript/definitive2/index.html
// For simulating XML retrieval in XmlHttpRequests
if (!window.XML) { window.XML =  { } };

XML.newDocument = function(rootTagName, namespaceURL) {
  if (!rootTagName) rootTagName = "";
  if (!namespaceURL) namespaceURL = "";
  if (document.implementation && document.implementation.createDocument) {
    // This is the W3C standard way to do it
    return document.implementation.createDocument(namespaceURL, rootTagName, null);
  } else { // This is the IE way to do it
    // Create an empty document as an ActiveX object
    // If there is no root element, this is all we have to do
    var doc = new ActiveXObject("MSXML2.DOMDocument");
    // If there is a root tag, initialize the document
    if (rootTagName) {
      // Look for a namespace prefix
      var prefix = "";
      var tagname = rootTagName;
      var p = rootTagName.indexOf(':');
      if (p != -1) {
        prefix = rootTagName.substring(0, p);
        tagname = rootTagName.substring(p+1);
      }
      // If we have a namespace, we must have a namespace prefix
      // If we don't have a namespace, we discard any prefix
      if (namespaceURL) {
        if (!prefix) prefix = "a0"; // What Firefox uses
      }
      else prefix = "";
      // Create the root element (with optional namespace) as a
      // string of text
      var text = "<" + (prefix?(prefix+":"):"") +  tagname +
          (namespaceURL
           ?(" xmlns:" + prefix + '="' + namespaceURL +'"')
           :"") +
          "/>";
      // And parse that text into the empty document
      doc.loadXML(text);
    }
    return doc;
  }
}

XML.parse = function(text) {
  if (typeof DOMParser != "undefined") {
    // Mozilla, Firefox, and related browsers
    return (new DOMParser()).parseFromString(text, "application/xml");
  } else if (typeof ActiveXObject != "undefined") {
    // Internet Explorer.
    var doc = XML.newDocument();  // Create an empty document
    doc.loadXML(text);            // Parse text into it
    return doc;                   // Return it
  } else if (psc.test.envjs()) {
    // env.js
    // based on code in envjs's DOMParser
    return window['$domparser'].parseFromString(text);
  } else {
    // As a last resort, try loading the document from a data: URL
    // This is supposed to work in Safari. Thanks to Manos Batsis and
    // his Sarissa library (sarissa.sourceforge.net) for this technique.
    var url = "data:text/xml;charset=utf-8," + encodeURIComponent(text);
    var request = new XMLHttpRequest();
    request.open("GET", url, false);
    request.send(null);
    return request.responseXML;
  }
};
