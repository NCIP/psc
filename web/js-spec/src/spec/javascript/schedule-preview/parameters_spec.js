/*global Screw before after describe it expect equal have_length */

require_spec('spec_helper.js');

if (psc.test.envjs()) {
  print("SKIPPING parameters_spec until I can figure out why jquery.query won't load in Rhino");
} else {

require_main('jquery/jquery.query.js')
require_main('schedule-preview/parameters.js')

Screw.Unit(function () {
  (function ($) {
    describe("Preview parameters", function () {
      var $Parameters;  // alias

      before(function () {
        $Parameters = psc.schedule.preview.Parameters
      })

      describe("parsing", function () {
        before(function () {
          $Parameters.init(
            "segment[z]=1457&start_date[z]=2009-04-02&start_date[4]=2008-08-02&segment[4]=9322");
        });

        it("finds all pairs", function () {
          expect($Parameters.size()).to(equal, 2);
        });

        it("yields the pairs in order by date", function () {
          expect($Parameters.requestedSegments()).to(equal, [
            { segment: "9322", start_date: '2008-08-02' },
            { segment: "1457", start_date: '2009-04-02' }
          ]);
        });

        it("can clear the default info", function () {
          $Parameters.clear()
          expect($Parameters.size()).to(equal, 0);
        });

        it("can reconstruct the query string", function () {
          var qs = $Parameters.toQueryString();
          expect(qs).to(match, "segment%5Bz%5D=1457");
          expect(qs).to(match, "start_date%5Bz%5D=2009-04-02");
          expect(qs).to(match, "segment%5B4%5D=9322");
          expect(qs).to(match, "start_date%5B4%5D=2008-08-02");
        });
      });

      describe("updating", function () {
        describe("add", function () {
          describe("when there's a single existing parameter", function () {
            before(function () {
              $Parameters.init(
                "segment[0]=78&start_date[0]=2009-01-02")
              $Parameters.add({ segment: "3224", start_date: "2008-03-07" });
            });
            
            it("works", function () {
              expect($Parameters.size()).to(equal, 2);
            });
          });
          
          describe("when there are multiple other parameters", function () {
            before(function () {
              $Parameters.init(
                "segment[b]=45&segment[11]=78&start_date[11]=2009-01-02&start_date[b]=2009-02-10")
              $Parameters.add({ segment: "3224", start_date: "2008-03-07" });
            })

            it("is reflected in the visible parameters", function () {
              expect($Parameters.requestedSegments()).to(equal, [
                { segment: "3224", start_date: "2008-03-07" },
                { segment: "78", start_date: "2009-01-02" },
                { segment: "45", start_date: "2009-02-10" }
              ]);
            });

            it("is reflected in the count", function () {
              expect($Parameters.size()).to(equal, 3)
            });

            it("is reflected in the generated query string", function () {
              var qs = $Parameters.toQueryString();
              expect(qs).to(match, "segment%5B12%5D=3224")
              expect(qs).to(match, "start_date%5B12%5D=2008-03-07")
            });
          
            describe("of a duplicate segment", function () {
              before(function () {
                $Parameters.add({ segment: "3224", start_date: "2008-03-09" });
              });
            
              it("is reflected in the request", function () {
                expect($Parameters.requestedSegments()).to(equal, [
                  { segment: "3224", start_date: "2008-03-07" },
                  { segment: "3224", start_date: "2008-03-09" },
                  { segment: "78", start_date: "2009-01-02" },
                  { segment: "45", start_date: "2009-02-10" }
                ]);
              });
            
              it("is reflected in the count", function () {
                expect($Parameters.size()).to(equal, 4);
              });
            
              it("is reflected in the QS", function () {
                var qs = $Parameters.toQueryString();
                expect(qs).to(match, "segment%5B13%5D=3224")
                expect(qs).to(match, "start_date%5B13%5D=2008-03-09")
              });
            });
          });
        });

        describe("remove", function () {
          before(function () {
            $Parameters.init(
              "segment[b]=45&segment[11]=78&start_date[11]=2009-01-02&start_date[b]=2009-02-10")
          });
          
          describe("of a valid entry", function () {
            before(function () {
              $Parameters.remove({ segment: "78", start_date: "2009-01-02" });
            });

            it("is reflected in the visible parameters", function () {
              expect($Parameters.requestedSegments()).to(equal, [
                { segment: "45", start_date: "2009-02-10" }
              ]);
            });

            it("is reflected in the count", function () {
              expect($Parameters.size()).to(equal, 1)
            });

            it("is reflected in the query string", function () {
              expect($Parameters.toQueryString()).to_not(match, "segment%5B11%5D")
              expect($Parameters.toQueryString()).to_not(match, "start_date%5B11%5D")
            });
          });
          
          describe("with a mismatched date", function () {
            before(function () {
              $Parameters.remove({ segment: "78", start_date: "2009-01-01" });
            });
            
            it("does nothing", function () {
              expect($Parameters.size()).to(equal, 2);
            });
          });
          
          describe("of a repeated segment", function () {
            before(function () {
              $Parameters.add({ segment: "18", start_date: "2009-03-08" });
              $Parameters.add({ segment: "18", start_date: "2009-04-08" });
              expect($Parameters.size()).to(equal, 4);
            });
            
            it("can remove the earlier one", function () {
              $Parameters.remove({ segment: "18", start_date: "2009-03-08" });

              expect($Parameters.requestedSegments()).to(equal, [
                { segment: "78", start_date: "2009-01-02" },
                { segment: "45", start_date: "2009-02-10" },
                { segment: "18", start_date: "2009-04-08" }
              ]);
            });
            
            it("can remove the later one", function () {
              $Parameters.remove({ segment: "18", start_date: "2009-04-08" });

              expect($Parameters.requestedSegments()).to(equal, [
                { segment: "78", start_date: "2009-01-02" },
                { segment: "45", start_date: "2009-02-10" },
                { segment: "18", start_date: "2009-03-08" }
              ]);
            })
          });
        });
      });

      describe("status", function () {
        before(function () {
          $Parameters.init(
            "segment[z]=1457&start_date[z]=2009-04-02&start_date[4]=2008-08-02&segment[4]=9322");
        });

        it("is up-to-date after init", function () {
          expect($Parameters.pending()).to(be_false);
        });

        it("is not up-to-date after an add", function () {
          $Parameters.add({ segment: "9000", date: "2001-01-12" })
          expect($Parameters.pending()).to(be_true);
        });

        it("is not up-to-date after a remove", function () {
          $Parameters.remove("1457");
          expect($Parameters.pending()).to(be_true);
        });

        it("is not up-to-date after a clear", function () {
          $Parameters.clear();
          expect($Parameters.pending()).to(be_true);
        });

        it("is up-to-date after a modification and then a successful refresh", function () {
          $Parameters.clear();
          $('#schedule').trigger('schedule-ready');
          expect($Parameters.pending()).to(be_false);
        });

        it("is not up-to-date after a modification and then a failed refresh", function () {
          $Parameters.clear();
          $('#schedule').trigger('schedule-error');
          expect($Parameters.pending()).to(be_true);
        });
      });
    });
  }(jQuery));
});

} // end env check