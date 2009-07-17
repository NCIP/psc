(function($) {
  $(window).load(function () {
    psc.template.mpa.GridControls.init();
    psc.template.mpa.Actions.init();
    psc.template.mpa.Presentation.init(location.hash);
    psc.template.mpa.ActivityNotes.init();
    psc.template.mpa.ActivityRows.init();
  })
}(jQuery))