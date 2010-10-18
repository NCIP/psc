require_spec('spec_helper.js');
require_main('jquery/jquery.tristate-checkbox.js');


Screw.Unit(function () {
  (function ($) {
    describe("jquery.tristate-checkbox.js", function () {

      function setup(state) {
        if (state) {
          return $('#frodo').tristate({initialState: state});
        } else {
          return $('#frodo').tristate();
        }
      }

      var c;

      before(function() {
        $('#magic').html('<input type="checkbox" id="frodo" value="hobbit"/>');
        c = setup();
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
          setup('unchecked').click();
          expect(c.attr('state')).to(equal, 'checked');
        });

        it("checked to unchecked", function () {
          setup('checked').click();
          expect(c.attr('state')).to(equal, 'unchecked');
        });

        it("intermediate to checked", function () {
          setup('intermediate').click();
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

        it("fires state change when clicked", function () {
          $(c).click();
          expect(receivedData.length).to(equal, 1);
          expect(receivedData[0]).to(equal, 'checked');
        });

        it("fires state change when state is manually changed", function () {
          $(c).tristate('state', 'checked');
          expect(receivedData.length).to(equal, 1);
          expect(receivedData[0]).to(equal, 'checked');
        });

        it("should not fire a state change when state is manually changed to the same state", function() {
          $(c).tristate('state', 'unchecked');
          expect(receivedData.length).to(equal, 0);
        });
      });

      describe("chaining", function() {
        it("should allow chaining", function() {
          $(c).tristate('state', 'unchecked').tristate('state', 'checked');;
          expect(c.attr('state')).to(equal, 'checked');
        });
      });

      describe("modifying multiple", function() {
        it("should allow changing multiple checkbox states at once", function() {
          $('#magic').html('<input type="checkbox" id="frodo" value="hobbit"/><input type="checkbox" id="sam" value="hobbit"/>');
          $('#frodo').tristate();
          $('#sam').tristate();
          $('#magic input').tristate('state', 'intermediate');
          expect($('#frodo').attr('state')).to(equal, 'intermediate');
          expect($('#sam').attr('state')).to(equal, 'intermediate');
        });
      });

    });
  }(jQuery));
});
