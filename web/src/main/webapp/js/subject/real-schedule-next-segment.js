psc.namespace("subject");

psc.subject.RealScheduleNextSegment = (function () {

  function scheduleNextSegment() {
    var selectedElt = getSelectorValue()
    var studySegmentId = selectedElt.attr('studySegment')
    var studyId = selectedElt.attr('study')
    var assignmnentId = selectedElt.attr('assignment')
    var startDay = selectedElt.attr('startday')
    var immediateOrPerProtocol = getModeValue().replace("_", "-").toLowerCase()
    var startDate = psc.tools.Dates.displayDateToApiDate($F('start-date-input'));
    var params = '<next-scheduled-study-segment study-segment-id="'+studySegmentId+'" start-date="'+startDate+
                 '" mode="'+immediateOrPerProtocol+'" start-day="'+startDay+'"/>'
    var url = psc.tools.Uris.relative('/api/v1/studies/'+psc.tools.Uris.escapePathElement(studyId)+
                                      '/schedules/'+psc.tools.Uris.escapePathElement(assignmnentId))
    makeRequestForNextSegment(params, url)
  }

  function makeRequestForNextSegment(parameters, resourceUrl) {
    $('next-studySegment-indicator').reveal()
    SC.asyncRequest(resourceUrl, Object.extend({
                method: 'POST',
                contentType: 'text/xml',
                postBody: parameters,
                onComplete: function(){
                    $('next-studySegment-indicator').conceal()
                    psc.subject.ScheduleData.refresh()
                }
            }))
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

