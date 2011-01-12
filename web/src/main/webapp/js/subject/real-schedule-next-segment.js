psc.namespace("subject");

psc.subject.RealScheduleNextSegment = (function () {

  function scheduleNextSegment() {
    var selectedElt = getSelectorValue();
    var studySegmentId = selectedElt.attr('studySegment');
    var studyId = selectedElt.attr('study');
    var assignmentId = selectedElt.attr('assignment');
    var startDay = selectedElt.attr('startday');
    var immediateOrPerProtocol = getModeValue().replace("_", "-").toLowerCase();
    var startDate = psc.tools.Dates.displayDateToApiDate($F('start-date-input'));
    var params = '<next-scheduled-study-segment study-segment-id="'+studySegmentId+'" start-date="'+startDate+
                 '" mode="'+immediateOrPerProtocol+'" start-day="'+startDay+'"/>';
    var url = psc.tools.Uris.relative('/api/v1/studies/'+psc.tools.Uris.escapePathElement(studyId)+
                                      '/schedules/'+psc.tools.Uris.escapePathElement(assignmentId));
    var segmentName = selectedElt.attr('segmentName');
    var subject = psc.subject.ScheduleData.subjectName();
    var desc = "segment " +segmentName+ " is scheduled from " +startDate+ " as "
            +immediateOrPerProtocol+ " mode for " + subject;
    var action = {
        description: desc,
        context: (psc.subject.ScheduleData.contextAPI())(),
        action_type: "segment"
    } ;
    makeRequestForNextSegmentWithUserAction(params, url, action);
  }

  function makeRequestForNextSegmentWithUserAction(params, url, action) {
    var userActionUrl = psc.tools.Uris.relative('/api/v1/user-actions');
    jQuery.ajax({
      url: userActionUrl,
      type: 'POST',
      data: Object.toJSON(action),
      contentType: 'application/json',
      complete: function (xhr, status) {
        if (status === 'success') {
          if (xhr && xhr.getResponseHeader('Location')) {
              var userAction = xhr.getResponseHeader('Location');
              makeRequestForNextSegment(params, url, userAction)
          }
        }
      }
    });
  }

  function makeRequestForNextSegment(parameters, resourceUrl, userAction) {
    $('next-studySegment-indicator').reveal();
    SC.asyncRequest(resourceUrl, Object.extend({
      method: 'POST',
      contentType: 'text/xml',
      postBody: parameters,
      requestHeaders: ['X-PSC-User-Action', userAction],
      onComplete: function(){
        $('next-studySegment-indicator').conceal();
        psc.subject.ScheduleData.refresh();
      }
    }));
  }

  function getModeValue() {
    return jQuery("input[name='mode']:checked").val();
  }

  function getSelectorValue() {
    return jQuery('#studySegmentSelector option:selected')
  }

  function createStartDatePerMode() {
    if (getModeValue() == "IMMEDIATE") {
      $('start-date-input').value = psc.tools.Dates.utcToDisplayDate(new Date());
    } else if (getModeValue() =="PER_PROTOCOL") {
      var stopDate;
      var segment_dates = psc.subject.ScheduleData.current()['study_segments'].select(function(studySegment){
        if (studySegment.planned.study.assigned_identifier == getSelectorValue().attr('study')) {
          return studySegment;
        }}).collect(function(studySegment) {
          return studySegment.range.stop_date;
      });

      var noOfSegments = segment_dates.length;
      if (noOfSegments==1) {
        stopDate = segment_dates[0];
      } else {
          for (var i=0; i<noOfSegments; i++) {
            if (segment_dates[i+1] > segment_dates[i]) {
              stopDate = segment_dates[i+1]
            }
         }
      }

      var start_date = psc.tools.Dates.shiftByDays(psc.tools.Dates.apiDateToUtc(stopDate),1)
      $('start-date-input').value = psc.tools.Dates.utcToDisplayDate(start_date);
    }
  }

  return {
    init: function () {
      Event.observe('next-study-segment-button', 'click', scheduleNextSegment)
      Event.observe('studySegmentSelector', 'click', createStartDatePerMode)
      $$('.mode-radio').each(function(radio) {
        Event.observe(radio, "click", function() {
          createStartDatePerMode()
        })
      })
    }
  };
}());

