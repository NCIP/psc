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
        return psc.tools.Strings.leftpad(jsDate.getUTCMonth() + 1, 2)+
         '/'+psc.tools.Strings.leftpad(jsDate.getUTCDate(), 2)+
         '/'+jsDate.getUTCFullYear()
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
    }
  };
}());