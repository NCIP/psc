require_spec("spec_helper.js");
require_main("psc-tools/async-updater.js");

(function ($) {
  Screw.Unit(function () {
    describe("AsyncUpdater", function () {
      var updater;
      var seen = [];
      
      before(function () {
        updater = new psc.tools.AsyncUpdater(function (newValue) {
          seen.push(newValue);
        });
      });
      
      after(function () {
        updater.stop();
      });
      
      it("has a default refresh rate of every 20ms", function () {
        expect(updater.refreshTime).to(equal, 20);
      });
      
      /* TODO: add yuitest-like wait feature to screw-unit
      it("sees distinct values separately", function () {
        updater.update(4);
        setTimeout(function () {
          expect(seen).to(have_length, 1)
        }, 500);
      });
      */
    });
  });  
}(jQuery))

