psc.namespace("dashboard");

(function ($) {
  var loadsInProgress = [];

  function hideOrShowLoading() {
    if (loadsInProgress.length == 0) {
      $('#loading').fadeOut(250);
    } else {
      $('#loading').show();
    }
  }

  psc.dashboard.Main = {
    username: "NOT_SET",

    init: function (username) {
      this.username = username;
      hideOrShowLoading();
    },

    loadStarted: function(name) {
      loadsInProgress.unshift(name);
      hideOrShowLoading();
    },

    loadFinished: function(name) {
      loadsInProgress = _.reject(loadsInProgress, function (e) { return e == name });
      hideOrShowLoading();
    }
  }
}(jQuery));