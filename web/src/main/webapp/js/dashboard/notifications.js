psc.namespace('dashboard');

(function ($) {
  function noteDismissed(id) {
    $('#notification-' + id + ' a.dismiss').replaceWith("<em>Dimissed</em>");
  }

  psc.dashboard.Notifications = {
    dismiss: function (id) {
      psc.dashboard.Main.loadStarted("notifications-" + id);
      $.ajax({
        // TODO: this needs a proper API
        url: psc.tools.Uris.relative("/pages/dashboard/dismiss-notification?notification=" + id),
        type: 'POST',
        success: function () {
          noteDismissed(id);
          psc.dashboard.Main.loadFinished("notifications-" + id);
        },
        error: function () {
          $('#notification-error').fadeIn(250).text("Error while dismissing notification");
          psc.dashboard.Main.loadFinished("notifications-" + id);
        }
      });
    },

    init: function () {
      var self = this;
      $('li.notification a.dismiss').click(function (evt) {
        var li = $(evt.target).parents('li.notification')[0];
        console.log(evt);
        console.log(li);
        self.dismiss(li.id.substring("notification-".length))
      });
    }
  }
}(jQuery));