/*global jQuery resigTemplate Screw describe it expect equal require_main require_spec */

require_spec('spec_helper.js');
require_main('resig-templates.js');

Screw.Unit(function () {
  (function ($) {
    function cleanActual(id, context) {
      return $.trim(resigTemplate(id, context));
    }
  
    describe("resigTemplate", function () {
      it("renders a value from the context object", function () {
        expect(cleanActual('a_equals', { a: 'alpha' })).
          to(equal, "a = alpha");
      });

      it("renders blank for a value not in the context object", function () {
        expect(cleanActual('a_equals', { })).to(equal, "a =");
      });

      it("renders a complex expression in the template", function () {
        expect(cleanActual('full_expression', { })).to(equal, "result = 32");
      });

      it("executes a function from the context object", function () {
        expect(cleanActual('fn_returns', { 
          fn: function () { return 'beta'; }
        })).to(equal, "fn() = beta");
      });

      // PENDING: Fix this if it is a problem
      /*
      it("executes a function from the context object exactly once", function () {
        var count = 0;
        expect(cleanActual('fn_returns', { 
          fn: function () { return ++count; }
        })).to(equal, "fn() = 1");
      });
      */
    });
  }(jQuery));
});
