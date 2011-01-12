psc.namespace("subject");

psc.subject.RealScheduleControls = (function ($) {
  var batchResource;
  var selectAndModifyTabIsOpen = false;
  var userActionUrl = psc.tools.Uris.relative('/api/v1/user-actions');

  function performDelay(evt, data) {
    var params = psc.subject.RealScheduleControls.computeDelayParameters();
    var count = 0;
    for each(var id in params)
      count = count + 1;
    var action = psc.subject.RealScheduleControls.createDelayUserAction(count);
    executeScheduleUpdateWithUserAction
            (params, action, batchResource, executePartialScheduleUpdate, data);
  }

  function performCheckedModifications(evt, data) {
    var params = psc.subject.RealScheduleControls.computeMarkParameters();
    var action = psc.subject.RealScheduleControls.createMarkUserAction();
    executeScheduleUpdateWithUserAction
            (params, action, batchResource, executePartialScheduleUpdate, data);
  }

  function performShowAction(evt, data) {
    var focusDate = psc.subject.ScheduleData.focusDate();
    var dateRange = psc.subject.ScheduleList.visibleDayRange();

    var parentElm = $(this).parent("li");
    var parentId = parentElm.attr('id');
    var activityType = parentId.split("-")[0];
    var activityClass = "li."+activityType;

    $.each(jQuery('.day'), function (index, dayElm) {
      var arrayOfLi = $(dayElm).find('li[class~='+activityType+']');
      if (arrayOfLi.size() > 0) {
        dayElm.show();
      }
    });

    var arrayOfActivities = jQuery(activityClass);
    arrayOfActivities.show();

    $('#schedule').trigger('focus-date-changed', {
        date: focusDate,
        source: "show",
        range: dateRange
    });

    $(this).removeClass('enableControl').addClass('disableControl');
    var hideControl = parentElm.children(".hideControl");
    hideControl.removeClass('disableControl').addClass('enableControl');
  }

  function performHideAction(evt, data) {
    var focusDate = psc.subject.ScheduleData.focusDate();
    var dateRange = psc.subject.ScheduleList.visibleDayRange();

    var parentElm = $(this).parent("li");
    var parentId = parentElm.attr('id');
    var activityType = parentId.split("-")[0];
    var activityClass = "li."+activityType;

    var arrayOfActivities = jQuery(activityClass);
    arrayOfActivities.hide();

    $.each(jQuery('.day'), function (index, dayElm) {
      var arrayOfLis = $(dayElm).find('li:visible');
      if (arrayOfLis.size() == 0) {
        dayElm.hide();
      }
    });

    $('#schedule').trigger('focus-date-changed', {
        date: focusDate,
        source: "hide",
        range: dateRange
      });

    $(this).removeClass('enableControl').addClass('disableControl');
    var showControl = parentElm.children(".showControl");
    showControl.removeClass('disableControl').addClass('enableControl');
  }

  function executeScheduleUpdateWithUserAction(updates, action, url, callback, data) {
    $.ajax({
      url: userActionUrl,
      type: 'POST',
      data: Object.toJSON(action),
      contentType: 'application/json',
        complete: function (xhr, status) {
          if (status === 'success') {
            if (xhr && xhr.getResponseHeader('Location')) {
              var userAction = xhr.getResponseHeader('Location');
              callback(updates, url, userAction, data);
            }
          }
        }
    });
  }

  function executePartialScheduleUpdate(updates, url, userAction) {
    $('#schedule-controls .indicator').css('visibility', 'visible');
    $.ajax({
      url: url,
      type: 'POST',
      data: Object.toJSON(updates),
      contentType: 'application/json',
      beforeSend: function(xhr) {
        xhr.setRequestHeader("X-PSC-User-Action", userAction);
      },
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
      if (selectAndModifyTabIsOpen !== true) {
        jQuery("#schedule-controls").accordion('activate', 4);
        selectAndModifyTabIsOpen = true;
      }
      $('#mark-activities-count').
        text('There is currently 1 activity checked.');
    } else {
      $('#mark-activities-count').
        text('There are currently ' + count + ' activities checked.');
    }
    jQuery('#apply-modified-activities-div').effect("highlight", {}, 2000);
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
    var assignmentName = $(this).attr('assignment-name');
    var params = {
      dismissed: true
    };
    var url = psc.tools.Uris.relative('/api/v1/subjects/'+psc.tools.Uris.escapePathElement(subjectId)+
                          '/assignments/'+psc.tools.Uris.escapePathElement(assignmentId)+'/notifications/'
                          +psc.tools.Uris.escapePathElement(notificationId))
    var list = $(this).parents('li:first')
    var subject = psc.subject.ScheduleData.subjectName();
    var desc = "notification dismiss for " + subject + " for " +assignmentName;
    var action = null;
    action = {
      description: desc,
      context: (psc.subject.ScheduleData.contextAPI())(),
      action_type: "dismiss"
    };
    executeScheduleUpdateWithUserAction(params, action, url, makeDismissNotificationRequest, list);
  }

  function makeDismissNotificationRequest(params, url, userAction, list) {
    $.ajax({
      url: url,
      type: 'PUT',
      data: Object.toJSON(params),
      contentType: 'application/json',
      beforeSend: function(xhr) {
        xhr.setRequestHeader("X-PSC-User-Action", userAction);
      },
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

  function getDelayOrAdvance(val) {
      if (val == 1) {
        return "delay";
      } else {
        return "advance";
      }
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
    },

    createDelayUserAction: function (count) {
      var params = null;
      var delayAmount = $('#delay-amount').val();
      var delayOrAdvance = $('#delay-or-advance').val();
      var subject = psc.subject.ScheduleData.subjectName();
      var actionType = getDelayOrAdvance(delayOrAdvance);
      var desc = getDelayOrAdvance(delayOrAdvance) + " " + count + " activities for " + subject + " by " + delayAmount + " days.";
      params = {
        description: desc,
        context: (psc.subject.ScheduleData.contextAPI())(),
        action_type: actionType
      };
      return params;
    },

    createMarkUserAction: function() {
      var delayAmount = $('#mark-delay-amount').val();
      var params = null;
      var delayOrAdvance = $('#mark-delay-or-advance').val();
      var count = $('input.event:checked').length;
      var newState = $('#mark-new-mode').val();
      var subject = psc.subject.ScheduleData.subjectName();
      var desc;
      if (newState == 'move-date-only') {
          newState = getDelayOrAdvance(delayOrAdvance);
          desc = getDelayOrAdvance(delayOrAdvance) + " " + count + " activities for " + subject +
                  " by " + delayAmount + " days."
      } else if (newState == 'scheduled') {
          desc = getDelayOrAdvance(delayOrAdvance) + " " + count + " " + newState + " activities for "
                  + subject + " by " + delayAmount + " days."
      } else {
          desc = count + " activities mark as " + newState.replace(/-/g, " ") + " for " + subject;
      }

      params = {
        description: desc,
        context: (psc.subject.ScheduleData.contextAPI())(),
        action_type: newState
      };
      return params;
    }
  }
}(jQuery));