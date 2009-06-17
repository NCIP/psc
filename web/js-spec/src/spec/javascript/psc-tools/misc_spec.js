require_spec("spec_helper.js");
require_main("psc-tools/misc.js");

Screw.Unit(function () {
  describe("Dates", function () {
    describe("api date conversion", function () {
      it("converts 2009-05-01 to a UTC date", function () {
        expect(psc.tools.Dates.apiDateToUtc("2009-05-01")).to(
          equal_utc_date, new Date(Date.UTC(2009, 4, 1)));
      });
      
      it("converts a js date to the appropriate string", function () {
        expect(psc.tools.Dates.utcToApiDate(new Date(Date.UTC(2009, 3, 9)))).to(
          equal, "2009-04-09");
      });

      it("converts a utc date to a display date", function () {
        expect(psc.tools.Dates.utcToDisplayDate(new Date(Date.UTC(2009, 4, 5)))).to(
          equal, "05/05/2009");
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
    })
  });
});