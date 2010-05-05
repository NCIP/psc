require_spec('spec_helper.js');
require_main('activity-property/activity-property.js');
require_main('lightbox.js')

Screw.Unit(function () {
    (function ($) {
        describe("activity property", function () {
            it("edits property", function () {
                var row = $('oldUri list-0')
                SC.AP.editProperty(row);
            });

            it("create new property", function () {
                var row = SC.AP.addNewRowToUriTable();
                SC.AP.editProperty(row)
            });
        });
     }(jQuery));
});
