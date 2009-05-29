if (!window.SC) { window.SC = { } }
if (!SC.SP) { SC.SP = { } }

Object.extend(SC.SP, {
  selectStudySegmentsForPreview: function() {
     var studySegmentPreviewSelector = $F('studySegmentPreviewSelector')
     studySegmentPreviewSelector = studySegmentPreviewSelector.split("_")
     var studySegmentGridId = studySegmentPreviewSelector[0];
     var studySegmentName = studySegmentPreviewSelector[1];
     var studySegmentDayRange = studySegmentPreviewSelector[2];
     var Length = $('selectedStudySegments').options.length
     $('selectedStudySegments').options[Length] =  new Option(studySegmentName,
             Length +"_"+ studySegmentGridId +"_"+studySegmentDayRange )
  },

  generateSchedulePreview: function(uri) {
     var noOfSegments = $('selectedStudySegments').options.length
     var optionValues = new Array()
     var segments = new Array()
     var dayRanges =  new Array()
     for (var i=0; i<noOfSegments; i++) {
         var optionValue = $('selectedStudySegments').options[i].value.split("_")
         segments[i] = optionValue[1]
         dayRanges[i] = optionValue[2]
     }
     var start_dates = new Array();
     var inputDate = $F('previewStartDate')
     if (inputDate != "") {
        inputDate = inputDate.split("/")
        start_dates[0] = inputDate[2] + '-'+inputDate[0]+'-'+inputDate[1]
     } else {
        start_dates[0] = SC.SP.getDateInYYYYMMDDFormat(new Date())
     }
     for (var k=1; k<dayRanges.length; k++) {
         start_dates[k] = SC.SP.shiftDateByNumberOfDays(SC.SP.convertStringToDate(start_dates[k-1]).getTime() ,dayRanges[k-1])
     }
     var parameters = ""
     for (var index = 0; index <noOfSegments; index++) {
        parameters = parameters + "segment["+index+"]="+segments[index]+"&start_date["+index+"]="+start_dates[index]+"&"
     }
     SC.SP.makeSchedulePreviewRequest(uri, parameters)
     document.location.hash = parameters
  },

  generateIntialSchedulePreview : function(uri) {
     var parameters = document.location.hash.substr(1)
     SC.SP.makeSchedulePreviewRequest(uri, parameters)
  },

  makeSchedulePreviewRequest: function(uri, params) {
     SC.asyncRequest(uri, {
        method: 'GET',
        parameters:params
     })
  },

  shiftDateByNumberOfDays : function(dateToShiftInMilliseconds, numberOfDaysToShift) {
     var shiftedDate = new Date();
     var timeShifted =  dateToShiftInMilliseconds + (parseInt(numberOfDaysToShift, 10)*24*60*60*1000)
     shiftedDate.setTime(timeShifted)
     return SC.SP.getDateInYYYYMMDDFormat(shiftedDate)
  },

  convertStringToDate : function(dateString){
     var day = dateString.substring(8,10)
     var month = dateString.substring(5,7)
     month = parseInt(month) -1
     var year = dateString.substring(0,4)
     return new Date(year, month.toString(), day);
  },

  getDateInYYYYMMDDFormat: function(date){
     var year = date.getFullYear()
     var day = date.getDate()
     var month = date.getMonth()+1

     day = (day < 10 ) ? ("0" + day) : day
     month = (month < 10) ? ("0" + month): month
     return year+"-" + month+ "-" + day;
  },

  removeFromSelectedStudySegments : function() {
     var noOfOptions = $('selectedStudySegments').options.length
     for(var i=noOfOptions-1;i>=0;i--){
        if ($('selectedStudySegments').options[i].selected) {
           $('selectedStudySegments').remove(i)
        }
     }
  }
})
