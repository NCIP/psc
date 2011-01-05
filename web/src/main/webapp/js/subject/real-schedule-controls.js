psc.namespace("subject");

psc.subject.RealScheduleControls = (function ($) {
  var batchResource;

  function performDelay(evt, data) {
    var params = psc.subject.RealScheduleControls.computeDelayParameters();
    executePartialScheduleUpdate(params);
  }

  function performCheckedModifications(evt, data) {
    var params = psc.subject.RealScheduleControls.computeMarkParameters();
    executePartialScheduleUpdate(params);
  }

  function performShowAction(evt, data) {
    var parentElm = $(this).parent("li")
    var parentId = parentElm.attr('id')
    var activityType = parentId.split("-")[0];
    var activityClass = "li."+activityType

    var arrayOfActivities = jQuery(activityClass);
    arrayOfActivities.show()
    $(this).removeClass('enableControl').addClass('disableControl')
    var hideControl = parentElm.children(".hideControl")
    hideControl.removeClass('disableControl').addClass('enableControl')
  }

  function performHideAction(evt, data) {
    var parentElm = $(this).parent("li")
    var parentId = parentElm.attr('id')
    var activityType = parentId.split("-")[0];
    var activityClass = "li."+activityType

    var arrayOfActivities = jQuery(activityClass);
    arrayOfActivities.hide()
    $(this).removeClass('enableControl').addClass('disableControl')
    var showControl = parentElm.children(".showControl")
    showControl.removeClass('disableControl').addClass('enableControl')
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

  function checkScheduledActivitiesByClass() {
    var kind = $(this).attr('id').replace('mark-select-', '');
    var assign = $('#mark-select-assignment').val();
    var selector = "input.event";
    if ($.inArray(kind, ['all', 'none']) < 0) {
      selector += '.' + kind;
    }
    if (assign && assign !== '') {
      selector += '.assignment-' + assign.replace(/\W/g, '_');
    }
    $(selector).attr('checked', kind == 'none' ? '' : 'checked');
    return false;
  }

  function updateActivityCountMessage() {
    var count = $('input.event:checked').length;
    if (count === 0) {
      $('#mark-activities-count').
        text('There are currently no activities checked.');
    } else if (count === 1) {
      $('#mark-activities-count').
        text('There is currently 1 activity checked.');
    } else {
      $('#mark-activities-count').
        text('There are currently ' + count + ' activities checked.');
    }
  }

  function isShiftingMarkMode() {
    return $.inArray(
      $('#mark-new-mode').val(),
      ['move-date-only', 'scheduled']
    ) >= 0;
  }

  function mutateMarkForm() {
    if (isShiftingMarkMode()) {
      $('#mark-date-group').show();
    } else {
      $('#mark-date-group').hide();
    }
  }

  function shiftedDate(apiDate, shiftAmount) {
    return psc.tools.Dates.utcToApiDate(
      psc.tools.Dates.shiftByDays(
        psc.tools.Dates.apiDateToUtc(apiDate), shiftAmount));
  }

  // Map from mark modes to functions which determine the next state for an
  // SA.  If the function returns null, the SA won't be changed.
  var NEW_STATE_FNS = {
    'move-date-only': function (sa) {
      if (sa.isOpen()) {
        return sa.current_state.name;
      } else {
        return null;
      }
    },

    'scheduled': function (sa) {
      return 'scheduled';
    },
    
    'canceled-or-na': function (sa) {
      if ($.inArray(sa.current_state.name, ['NA', 'conditional']) >= 0) {
        return 'NA';
      } else {
        return 'canceled';
      }
    },
    
    'occurred': function (sa) {
      return 'occurred';
    },
    
    'missed': function (sa) {
      return 'missed';
    }
  }

   function dismissNotification() {
       var notificationId = $(this).attr('notification');
       var assignmentId = $(this).attr('assignment');
       var subjectId = $(this).attr('subject');
       var params = {
           dismissed: true
       };
       var url = psc.tools.Uris.relative('/api/v1/subjects/'+psc.tools.Uris.escapePathElement(subjectId)+
                                      '/assignments/'+psc.tools.Uris.escapePathElement(assignmentId)+'/notifications/'
                                       +psc.tools.Uris.escapePathElement(notificationId))
       var list = $(this).parents('li:first')
       $.ajax({
         url: url,
         type: 'PUT',
         data: Object.toJSON(params),
         contentType: 'application/json',
         complete: function() {
            updateNotificationList(list)
         }
       });
   }

   function updateNotificationList(li) {
       li.slideUp()
       li.removeClass("remove");
       if ($('li.'+li.attr('study')+'.remove:not(.removed)').length == 0) {
         var div = $('#div-'+li.attr('study'))
         div.slideUp()
       }
       if ($('li.remove:not(.removed)').length == 0) {
         $('#notification-message').
             text('No current notifications available.');
       }
   }

  function makeReportRequest() {
      var subjectId = $(this).attr('subject');
      var extension = $(this).attr('extension');
      var baseUrl = psc.tools.Uris.relative("/api/v1/reports/scheduled-activities")
      var url = baseUrl + extension + '?person-id=' +subjectId;
      var startDate = getReportStartDate();
      var endDate = getReportEndDate()
      if (startDate) {
          url = url + '&start-date=' + startDate
      }
      if (endDate) {
          url = url + '&end-date=' + endDate
      }
      location.href = url
  }

  function makeReportMoreOptionsRequest() {
      var subjectId = $(this).attr('subject');
      var baseUrl = psc.tools.Uris.relative("/pages/report/scheduledActivitiesReport")
      var url = baseUrl + '?personId=' +subjectId;
      var startDate = $('#actual-date-start').val()
      var endDate = $('#actual-date-stop').val()
      if (startDate) {
          url = url + '&startDate=' + startDate
      }
      if (endDate) {
          url = url + '&endDate=' + endDate
      }
      $('#report-options').attr("href", url);
  }

  function getReportStartDate() {
      var startDate = $('#actual-date-start').val() ?
            psc.tools.Dates.displayDateToApiDate($('#actual-date-start').val()) :
            null;
      return startDate;
  }

  function getReportEndDate() {
      var endDate = $('#actual-date-stop').val() ?
            psc.tools.Dates.displayDateToApiDate($('#actual-date-stop').val()) :
            null;
      return endDate;
  }

  return {
    init: function () {
      $('#xls-report').click(makeReportRequest)
      $('#csv-report').click(makeReportRequest)
      $('#report-options').click(makeReportMoreOptionsRequest)
      $('#delay-submit').click(performDelay);
      $('#mark-submit').click(performCheckedModifications);
      $('.showControl').click(performShowAction);
      $('.hideControl').click(performHideAction);
      $('a.mark-select').click(checkScheduledActivitiesByClass).
        click(updateActivityCountMessage);
      $('input.event').live('click', updateActivityCountMessage);
      $('#mark-new-mode').change(mutateMarkForm);
      $('#toggle-plan-days').click(function () {
        $('.event-details.plan-day').toggle();
        if ($(this).text().match(/Show/)) {
          $(this).text($(this).text().replace(/Show/, 'Hide'));
        } else {
          $(this).text($(this).text().replace(/Hide/, 'Show'));
        }
        return false;
      });
      $('a.notification-control').click(dismissNotification)
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
      var onlyAssign = $('#delay-assignment').val() || null;
      $.each(psc.subject.ScheduleData.current()['days'], function (day, value) {
        if (asOf) {
          var date = psc.tools.Dates.apiDateToUtc(day);
          if (date < asOf) return true; // continue
        }
        $.each(value['activities'], function () {
          if (onlyAssign && onlyAssign !== this.assignment.id) {
            return true; // continue
          }
          if (this.isOpen()) {
            params[this['id']] = {
              state: this.current_state.name,
              date: shiftedDate(this.current_state.date, delayAmount),
              reason: $('#delay-reason').val()
            };
          }
        });
      });
      return params;
    },
    
    // public for testing
    computeMarkParameters: function () {
      var delayAmount = 
        $('#mark-delay-amount').val() * $('#mark-delay-or-advance').val();
      var params = null;
      var saIds = $('input.event:checked').
        collect(function () { return this.value; });
      $.each(psc.subject.ScheduleData.current()['days'], function (day, value) {
        $.each(value['activities'], function () {
          if ($.inArray(this.id, saIds) >= 0) {
            var newState = NEW_STATE_FNS[$('#mark-new-mode').val()](this);
            if (newState) {
              if (!params) params = {};
              params[this.id] = {
                state: newState,
                date: isShiftingMarkMode() ? 
                  shiftedDate(this.current_state.date, delayAmount) : 
                  this.current_state.date,
                reason: $('#mark-reason').val()
              }
            }
          }
        });
      });
      return params;
    }
  }
}(jQuery));