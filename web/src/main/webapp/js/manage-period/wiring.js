(function($) {
  $(window).load(function () {
    var canEdit = psc.template.mpa.canEdit;
    if (canEdit) {
      psc.template.mpa.GridControls.init();
    }
    psc.template.mpa.Actions.init();
    psc.template.mpa.Presentation.init(location.hash);
    psc.template.mpa.ActivityNotes.init();
    psc.template.mpa.ActivityRows.init();
  })
}(jQuery))