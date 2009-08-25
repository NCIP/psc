require_spec('spec_helper.js');
require_main('jquery/jquery.enumerable.js');
require_main('manage-period/rows.js');

Screw.Unit(function () {
  describe("psc.template.mpa.ActivityRows", function () {
    describe("objectifyActivityXml", function () {
      var actual;
      
      before(function () {
        var doc = XML.parse(
          "<sources xmlns='http://bioinformatics.northwestern.edu/ns/psc'>\n" +
          "  <source name='one'>\n" +
          "    <activity code='a0' name='a' type='Normal'/>\n" +
          "    <activity code='b0' name='b' type='One Space'/>\n" +
          "    <activity code='c0' name='c' type='More Than One Space'/>\n" +
          "  </source>\n" +
          "</sources>\n"
        );
        console.log(doc);
        actual = psc.template.mpa.ActivityRows.objectifyActivityXml(doc);
      });

      it("creates one object per activity", function () {
        expect(actual.length).to(equal, 3);
      });

      it("adds a property for the activity name", function () {
        expect(actual[0].name).to(equal, 'a');
      });

      it("adds a property for the activity code", function () {
        expect(actual[2].code).to(equal, 'c0');
      });

      describe("activity type", function () {
        it("exists", function () {
          expect(actual[0].type).to_not(equal, undefined);
        });

        it("has a name", function () {
          expect(actual[0].type.name).to(equal, 'Normal');
        });

        it("has a selector", function () {
          expect(actual[0].type.selector).to(equal, 'activity-type-normal');
        });
        
        it("converts the name into an HTML class-compatible version for the selector", function () {
          expect(actual[2].type.selector).to(equal, 'activity-type-more_than_one_space');
        });
      });
    });
  });
});
