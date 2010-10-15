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


      describe("external methods", function() {
        it("should be able to get the state", function() {
          expect(c.tristate('state')).to(equal, 'unchecked');
        });

        it("should be able to set the state", function() {
          $(c).tristate('state', 'checked');
          expect(c.attr('state')).to(equal, 'checked');
        });
      });
      describe("firing events", function () {
        var receivedData;

        before(function () {
          receivedData = [];
          $(c).unbind('tristate-state-change');
          $(c).bind("tristate-state-change", function (evt, data) {
            receivedData.push(data);
          });
        });

//        it("fires state change when clicked", function () {
//          $(c).click();
//          console.log(receivedData)
//          expect(receivedData.length).to(equal, 1);
//          expect(receivedData[0]).to(equal, 'checked');
//        });

        it("fires state change when state is manually changed", function () {
          $(c).tristate('state', 'checked');
          expect(receivedData.length).to(equal, 1);
          expect(receivedData[0]).to(equal, 'checked');
        });
      });

    });
  }(jQuery));
});
