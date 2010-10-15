require_spec('spec_helper.js');
require_main('jquery/jquery.tristate-checkbox.js');


Screw.Unit(function () {
  (function ($) {
    describe("jquery.tristate-checkbox.js", function () {

      function setup(state) {
        if (state) {
          c.tristate({initialState: state});
        } else {
          c.tristate();
        }
      }

      var c;

      before(function() {
        c = $('#magic').html('<input type="checkbox" id="frodo" value="hobbit"/>');
        setup();
      });

      describe("initial state", function() {
        it("should be unchecked", function() {
          expect(c.attr('state')).to(equal, 'unchecked');
        });
      });

      describe("configuration", function () {
        it("should allow intermediate as the initial state", function () {
          setup('intermediate');
          expect(c.attr('state')).to(equal, 'intermediate');
        });

        it("should allow Intermediate as the initial state", function() {
          setup('Intermediate');
          expect(c.attr('state')).to(equal, 'intermediate');
        });

        it("should default to unchecked when invalid state", function() {
          setup('zap');
          expect(c.attr('state')).to(equal, 'unchecked');
        })
      });


      describe("state changes", function() {
        it("unchecked to checked", function () {
          setup('unchecked'); $('#frodo').trigger('click');
          expect(c.attr('state')).to(equal, 'checked');
        });

        it("checked to unchecked", function () {
          setup('checked'); $('#frodo').trigger('click');
          expect(c.attr('state')).to(equal, 'unchecked');
        });

        it("intermediate to checked", function () {
          setup('intermediate'); $('#frodo').trigger('click');
          expect(c.attr('state')).to(equal, 'checked');
        });
      });

    });
  }(jQuery));
});
