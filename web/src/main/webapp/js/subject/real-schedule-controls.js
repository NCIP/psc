psc.namespace("subject");

psc.subject.RealScheduleControls = (function ($) {
  var batchResource;

  function performDelay(evt, data) {
    var params = psc.subject.RealScheduleControls.computeDelayParameters();
    executePartialScheduleUpdate(params);
  }

  function executePartialScheduleUpdate(updates) {
    $('#schedule-controls .indicator').css('visibility', 'visible');
    $.ajax({
      url: batchResource,
      type: 'POST',
      data: Object.toJSON(updates),
      contentType: 'application/json',
      complete: function() {
        $('#schedule-controls .indicator').css('visibility', 'hidden');
        psc.subject.ScheduleData.refresh();
      }
    });
  }

  return {
    init: function () {
      $('#delay-submit').click(performDelay);
    },

    batchResource: function (uri) {
      batchResource = uri;
    },

    // public for testing
    computeDelayParameters: function () {
      var delayAmount = $('#delay-amount').val() * $('#delay-or-advance').val();
      var params = {};
      var asOf = $('#delay-as-of').val() ?
        psc.tools.Dates.displayDateToUtc($('#delay-as-of').val()) :
        null;
      var onlyStudy = $('#delay-study').val() || null;
      $.each(psc.subject.ScheduleData.current()['days'], function (day, value) {
        if (asOf) {
          var date = psc.tools.Dates.apiDateToUtc(day);
          if (date < asOf) return true; // continue
        }
        $.each(value['activities'], function () {
          if (onlyStudy && onlyStudy !== this.study) {
            return true; // continue
          }
          if (this.isOpen()) {
            params[this['id']] = {
              state: this.current_state.name,
              date: psc.tools.Dates.utcToApiDate(
                psc.tools.Dates.shiftByDays(
                  psc.tools.Dates.apiDateToUtc(this.current_state.date), delayAmount)),
              reason: 'foo'
            };
            var reason = $('#delay-reason').val();
            if (reason) {
              params[this['id']].reason = reason;
            }
          }
        });
      });
      return params;
    }
  }
}(jQuery));