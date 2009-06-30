require_spec("spec_helper.js");

if (psc.test.envjs()) {
  print("SKIPPING async-updater_spec until I make env.js's setTimeout behavior non-pathological");
} else {

require_main("psc-tools/async-updater.js");

(function ($) {
  Screw.Unit(function () {
    describe("AsyncUpdater", function () {
      it("has a default refresh rate of every 20ms", function () {
        var updater = new psc.tools.AsyncUpdater(function (newValue) { });
        expect(updater.refreshTime).to(equal, 20);
        updater.stop();
      });

      describe("updating", function () {
        var updater, seen;

        before(function () {
          seen = [];
          updater = new psc.tools.AsyncUpdater(function (newValue) {
            seen.push(newValue);
          });
          updater.refreshTime = 120; // Rhino can't handle 20ms updates
        });

        after(function () {
          updater.stop();
        });

        it("doesn't update immediately", function () {
          updater.update(4);
          expect(seen).to(have_length, 0)
        });

        it("eventually sees the update", function () {
          updater.update(4);

          wait(function () {
            expect(seen).to(have_length, 1)
          }, 300);
        });

        it("coalesces updates", function () {
          updater.update(4);

          wait(function () {
            updater.update(4);
          
            wait(function () {
              expect(seen).to(have_length, 1)
            }, 300);
          }, 300);
        });

        it("sees multiple updates", function () {
          updater.update(4);

          wait(function () {
            updater.update(7);
          
            wait(function () {
              expect(seen).to(have_length, 2)
            }, 300);
          }, 300);
        });
      });
    });
  });  
}(jQuery))

} // end env detect