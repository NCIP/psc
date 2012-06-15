require_spec("spec_helper.js");
require_main("psc-tools/misc.js");

psc.namespace('configuration');

Screw.Unit(function () {
  describe("Dates", function () {
    psc.configuration.calendarDateFormat = (function () {return "%m/%d/%y"; });

    describe("conversion", function () {
      it("converts 2009-05-01 to a UTC date", function () {
        expect(psc.tools.Dates.apiDateToUtc("2009-05-01")).to(
          equal_utc_date, new Date(Date.UTC(2009, 4, 1)));
      });

      it("returns null when converts null to a UTC date", function () {
        expect(psc.tools.Dates.apiDateToUtc(null)).to(
          equal, null);
      });

      it("converts display date 06/23/2009 to a UTC date", function () {

        expect(psc.tools.Dates.displayDateToUtc("06/23/2009")).to(
          equal_utc_date, new Date(Date.UTC(2009, 5, 23)));
      });
      
      it("converts a UTC date to an API date", function () {
        expect(psc.tools.Dates.utcToApiDate(new Date(Date.UTC(2009, 3, 9)))).to(
          equal, "2009-04-09");
      });

      it("converts a UTC date to a display date", function () {
        expect(psc.tools.Dates.utcToDisplayDate(new Date(Date.UTC(2009, 4, 5)))).to(
          equal, "05/05/2009");
      });
      
      it("converts a display date 11/23/2003 to the API date 2003-11-23", function () {
        expect(psc.tools.Dates.displayDateToApiDate("11/23/2003")).to(equal, "2003-11-23");
      });
      
      it("converts an API date 2005-11-03 to the display date 11/03/2005", function () {
        expect(psc.tools.Dates.apiDateToDisplayDate("2005-11-03")).to(equal, "11/03/2005");
      });

      it("converts 2009-05-01 to a local date", function () {
        expect(psc.tools.Dates.apiDateToLocal("2009-05-01")).to(
          equal_date, new Date(2009, 4, 1));
      });

      it("returns null when converts null to a local date", function () {
        expect(psc.tools.Dates.apiDateToLocal(null)).to(
          equal, null);
      });

      it("converts a local date to an API date", function () {
        expect(psc.tools.Dates.localToApiDate(new Date(2009, 3, 9))).to(
          equal, "2009-04-09");
      });
    });

    describe("day manipulation", function () {
      it("gives the UTC midnight instant for 2009-05-03 22:11:13", function () {
        expect(psc.tools.Dates.startOfUtcDay(new Date(Date.UTC(2009, 4, 3, 22, 11, 13)))).to(
          equal_utc_time, new Date(Date.UTC(2009, 4, 3)))
      });

      it("gives the UTC noon instant for 2009-05-03 22:11:13", function () {
        expect(psc.tools.Dates.middleOfUtcDay(new Date(Date.UTC(2009, 4, 3, 22, 11, 13)))).to(
          equal_utc_time, new Date(Date.UTC(2009, 4, 3, 12, 0, 0, 0)))
      });
    });

    describe("day shifting", function () {
      it("can shift into the future 9 days", function () {
        expect(psc.tools.Dates.shiftByDays(new Date(Date.UTC(2009, 4, 29, 10, 9, 8)), 9)).to(
          equal_utc_time, new Date(Date.UTC(2009, 5, 7, 10, 9, 8)));
      });

      it("can shift into the past 4 days", function () {
        expect(psc.tools.Dates.shiftByDays(new Date(Date.UTC(2009, 3, 6, 10, 11, 12)), -4)).to(
          equal_utc_time, new Date(Date.UTC(2009, 3, 2, 10, 11, 12)));
      });
    });
    
    describe("constants", function () {
      it("includes the number of milliseconds in a day", function () {
        expect(psc.tools.Dates.ONE_DAY).to(equal, 24 * 60 * 60 * 1000);
      });
    });
  });
  
  describe("Strings", function () {
    describe("leftpad", function () {
      it("leaves a proper length string alone", function () {
        expect(psc.tools.Strings.leftpad("ab", 2)).to(equal, "ab");
      });
      
      it("pads a short string", function () {
        expect(psc.tools.Strings.leftpad("a", 3, ' ')).to(equal, "  a");
      });
      
      it("pads a short integer", function () {
        expect(psc.tools.Strings.leftpad(3, 3, '_')).to(equal, "__3");
      });
      
      it("pads with zero by default", function () {
        expect(psc.tools.Strings.leftpad("b", 3)).to(equal, "00b");
      });
      
      it("leaves a long string alone", function () {
        expect(psc.tools.Strings.leftpad("whatever", 4)).to(equal, "whatever");
      });
    });
  });
  
  describe("Uris", function () {
    describe("relative", function () {
      before(function () {
        psc.tools.Uris.INTERNAL_URI_BASE_PATH = "/psc";
      });
      
      after(function () {
        psc.tools.Uris.INTERNAL_URI_BASE_PATH = null;
      })
      
      it("returns the entered URI if there's no context set", function () {
        psc.tools.Uris.INTERNAL_URI_BASE_PATH = null;
        expect(psc.tools.Uris.relative("/foo/etc")).to(equal, "/foo/etc");
      })
      
      it("prepends the prefix if present", function () {
        expect(psc.tools.Uris.relative("/foo/etc")).to(equal, "/psc/foo/etc");
      })
      
      it("includes a separating slash if necessary", function () {
        expect(psc.tools.Uris.relative("foo/etc")).to(equal, "/psc/foo/etc");
      })
      
      it("removes an extra separating slash if necessary", function () {
        psc.tools.Uris.INTERNAL_URI_BASE_PATH = "/psc/";
        expect(psc.tools.Uris.relative("/foo/etc")).to(equal, "/psc/foo/etc");
      })
    });

    describe("escapePathElement", function () {
      it("escapes slashes into 0x04", function () {
        expect(psc.tools.Uris.escapePathElement("A/B")).to(equal, "A%04B");
      });

      it("escapes all slashes into 0x04", function () {
        expect(psc.tools.Uris.escapePathElement("A/B/C/D")).to(equal, "A%04B%04C%04D");
      });

      it("escapes multiple slashes into separate 0x04s", function () {
        expect(psc.tools.Uris.escapePathElement("A///D")).to(equal, "A%04%04%04D");
      });
    });
  });

  describe("Arrays", function() {
    describe("minus", function() {
      it("should subtract the second array from the first", function() {
        expect(psc.tools.Arrays.minus(["foo", "bar"], ["bar", "stuff"])).to(
            equal, ["foo"]);
      });

      it("should return the first array if the second is null or undefined", function() {
        expect(psc.tools.Arrays.minus(["foo"], null)).to(equal, ["foo"]);
      });

      it("should return the an empty array if the first is null or undefined", function() {
        expect(psc.tools.Arrays.minus(null, ["foo"])).to(equal, []);
      });

      it("should return the an empty array if both arrays are null", function() {
        expect(psc.tools.Arrays.minus(null, null)).to(equal, []);
      });
    })
  });
});