require_spec('spec_helper.js');
require_main('activity-property/activity-property.js');
require_main('lightbox.js')

Screw.Unit(function () {
  describe("SC.AP", function () {
    describe("editProperty", function () {
      it("edits the property", function () {
        var row = SC.AP.addNewRowToUriTable();
        SC.AP.editProperty(row)

      });
    });
  });
});
