require_spec("spec_helper.js");
require_main("psc-tools/async-updater.js");

(function ($) {
  Screw.Unit(function () {
    describe("AsyncUpdater", function () {
      function in_isolation_it(desc, fn) {
        describe("in isolation", function () {
          var updater, seen;

          before(function () {
            seen = [];
            updater = new psc.tools.AsyncUpdater(function (newValue) {
              seen.push(newValue);
            });
          });

          after(function () {
            updater.stop();
          });
          
          it(desc, function () {
            fn(updater, seen);
          });
        });
      }

      in_isolation_it("has a default refresh rate of every 20ms", function (updater, seen) {
        expect(updater.refreshTime).to(equal, 20);
      });

      in_isolation_it("doesn't update immediately", function (updater, seen) {
        updater.update(4);
        expect(seen).to(have_length, 0)
      });

      in_isolation_it("eventually sees the update", function (updater, seen) {
        updater.update(4);

        wait(function () {
          expect(seen).to(have_length, 1)
        }, 300);
      });

      in_isolation_it("coalesces updates", function (updater, seen) {
        updater.update(4);

        wait(function () {
          updater.update(4);
          
          wait(function () {
            expect(seen).to(have_length, 1)
          }, 300);
        }, 300);
      });

      in_isolation_it("sees multiple updates", function (updater, seen) {
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
}(jQuery))

