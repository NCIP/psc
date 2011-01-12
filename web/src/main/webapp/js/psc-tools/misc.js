psc.namespace("tools");

/*
 * Utility functions for PSC.
 */

psc.tools.Dates = (function () {
  return {
    apiDateToUtc: function (apiDate) {
      if (apiDate != null) {
        var pieces = apiDate.split('-')
        return new Date(Date.UTC(pieces[0], pieces[1] - 1, pieces[2]));
      } else {
        return null;
      }
    },

    displayDateToUtc: function (humanReadableDate) {
      var dateFormat = psc.configuration.calendarDateFormat();
      var pieces = humanReadableDate.split('/')
      if (dateFormat.startsWith("%m")) {
        return new Date(Date.UTC(pieces[2], pieces[0] -1, pieces[1]));
      } else {
        return new Date(Date.UTC(pieces[2], pieces[1] -1, pieces[0]));
      }
    },

    utcToApiDate: function (jsDate) {
      return jsDate.getUTCFullYear() + 
        "-" + psc.tools.Strings.leftpad(jsDate.getUTCMonth() + 1, 2) +
        "-" + psc.tools.Strings.leftpad(jsDate.getUTCDate(), 2);
    },

    utcToDisplayDate: function(jsDate) {
      var dateFormat = psc.configuration.calendarDateFormat();
      if (dateFormat.startsWith("%m")) {
        return psc.tools.Strings.leftpad(jsDate.getUTCMonth() + 1, 2)+
         '/'+psc.tools.Strings.leftpad(jsDate.getUTCDate(), 2)+
         '/'+jsDate.getUTCFullYear()
      } else {
        return psc.tools.Strings.leftpad(jsDate.getUTCDate(), 2)+
         '/'+psc.tools.Strings.leftpad(jsDate.getUTCMonth() + 1, 2)+
         '/'+jsDate.getUTCFullYear()
      }
    },

    apiDateToDisplayDate: function (apiDate) {
      return this.utcToDisplayDate(this.apiDateToUtc(apiDate));
    },

    displayDateToApiDate: function (displayDate) {
      return this.utcToApiDate(this.displayDateToUtc(displayDate));
    },

    startOfUtcDay: function (d) {
      return new Date(Math.floor(d.getTime() / this.ONE_DAY) * this.ONE_DAY);
    },
    
    middleOfUtcDay: function (d) {
      return new Date(Math.floor(d.getTime() / this.ONE_DAY) * this.ONE_DAY + this.ONE_DAY / 2);
    },

    shiftByDays: function (d, dayCount) {
      var newD = new Date();
      newD.setTime(d.getTime() +  dayCount * this.ONE_DAY);
      return newD;
    },

    weekdayName: function (d) {
      switch (d.getUTCDay()) {
        case 0: return "Sunday";
        case 1: return "Monday";
        case 2: return "Tuesday";
        case 3: return "Wednesday";
        case 4: return "Thursday";
        case 5: return "Friday";
        case 6: return "Saturday";
        default: return "" + d.getUTCDay() + "?";
      }
    },

    ////// CONSTANTS
    
    ONE_DAY: 86400000
  }
}());

psc.tools.Strings = (function () {
  return {
    leftpad: function (v, count, pad) {
      var s = v.toString();
      var pad = pad ? pad : "0";
      while (s.length < count) {
        s = pad + s;
      }
      return s;
    }
  };
}());

psc.tools.Uris = (function () {
  return {
    INTERNAL_URI_BASE_PATH: window.INTERNAL_URI_BASE_PATH || null,

    relative: function (uri) {
      if (this.INTERNAL_URI_BASE_PATH) {
        var pfx = this.INTERNAL_URI_BASE_PATH;
        if (pfx.endsWith('/')) {
          pfx = pfx.substring(0, pfx.length - 1);
        }
        if (!uri.startsWith('/')) {
          uri = '/' + uri;
        }
        return pfx + uri;
      } else {
        return uri;
      }
    },

    escapePathElement: function(element){
      return element.replace(/\//g, "%04");
    },

    applicationPath: function () {
      var LOCATION = window.location;
      if (LOCATION != null) {
        return  LOCATION.protocol + "//" + LOCATION.hostname + ":" + LOCATION.port;
      }
    },

    deployed: function (uri) {
       return this.applicationPath() + this.relative(uri);
    }
  };
}());

psc.tools.Arrays = (function() {
  return {
    minus: function(a1, a2) {
      a1 = a1 || [];
      a2 = a2 || [];

      var result = [];
      for (var i = 0; i < a1.length; i++) {
        var found = false;
        for (var j = 0; j < a2.length; j++) {
          if (a1[i] == a2[j]) {
            found = true;
            break;
          }
        }
        if (!found) {
          result.push(a1[i]);
        }
      }
      return result;
    }
  };
}());


psc.tools.CssSelectors = (function() {
  return {
    escapeComponent: function(raw) {
      return raw.replace(/(#|;|&|,|\.|\+|\*|~|'|:|"|!|\^|\$|\[|\]|\(|\)|=|>|\||\/|@|\s)/g,"\\$1");
    }
  }
})();