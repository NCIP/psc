psc.namespace('template.mpa')

psc.template.mpa.Actions = (function ($) {
  function Action(evt, data) {
    var self = this;
    this.event = evt;
    this.data = data;

    if (this.data.action.name !== 'move' || this.data.action.step !== 0) {
      $(psc.template.mpa.Actions).queue(function () {
        self.go();
      });
    }
  }

  Action.prototype.go = function () {
    var self = this;
    $.ajax($.extend(
      {
        error: function (xhr, text, exception) {
          self.data.message = text;
          $('#days').trigger('action-error', self.data);
        },
        complete: function (xhr, status) {
          if (status === 'success') {
            if (xhr && xhr.getResponseHeader('Location')) {
              self.data.href = xhr.getResponseHeader('Location');
            }
            $('#days').trigger('action-completed', self.data);
          }
          $(psc.template.mpa.Actions).dequeue();
        }
      }, this.ajaxOptions()
    ));
  };

  Action.prototype.ajaxOptions = function () {
    return ({
      add: {
        type: 'POST',
        url: psc.template.mpa.Actions.collectionUri,
        data: this.plannedActivityRepresentation()
      },
      
      move: {
        type: 'PUT',
        url: this.data.href,
        data: this.plannedActivityRepresentation()
      }, 
      
      'update-notes': {
        type: 'PUT',
        url: this.data.href,
        data: this.plannedActivityRepresentation()
      }, 
      
      'delete': {
        type: 'DELETE',
        url: this.data.href
      }
    })[this.data.action.name];
  };

  Action.prototype.plannedActivityRepresentation = function () {
    var self = this;
    var rep = { };
    $.each([ 'day', 'population', 'details', 'condition', 'weight' ], function (i, k) {
      if (self.data[k]) {
        rep[k] = self.data[k];
      }
    });
    rep['activity-code'] = self.data.activity.code;
    rep['activity-source'] = self.data.activity.source;
    rep['label'] = self.data.labels.split(/ +/);
    return rep;
  };

  return {
    init: function () {
      $('#days').bind('action-started', function (evt, data) {
        new Action(evt, data);
      });
    }
  };
}(jQuery));
