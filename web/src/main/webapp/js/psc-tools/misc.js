psc.namespace("tools");

/*
 * Utility functions for PSC.
 */

psc.tools.Dates = (function () {
  return {
    apiDateToUtc: function (apiDate) {
      var pieces = apiDate.split('-')
      return new Date(Date.UTC(pieces[0], pieces[1] - 1, pieces[2]));
    },

    displayDateToUtc: function (humanReadableDate) {
      var pieces = humanReadableDate.split('/')
      return new Date(Date.UTC(pieces[2], pieces[0] -1, pieces[1]));
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

    incrementDecrementDate: function(date, shiftNumber){
      var d = new Date();
      var time =  date + (parseInt(shiftNumber, 10)*this.ONE_DAY)
      return d.setTime(time)
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