<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security"
           uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="commons" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
    <head>
        <title>Template</title>
        <tags:stylesheetLink name="main"/>
        <tags:includeScriptaculous/>
        <tags:javascriptLink name="main"/>
        <style type="text/css">
            #epochs {
                clear: both;
            }

            .epochs-and-studySegments {
                margin: 1em;
            }

            #epochs-indicator {
                margin: 0.5em 0.5em 0 0;
                float: left;
            }

            #cycle-form {
                border-width: 0 0 1px 0;
                font-size: inherit;
                font-weight: normal;
            }
            table.periods, table.periods tr, table.periods td, table.periods th {
                border-spacing: 0;
                border: 0 solid #666;
                margin: 1em;
            }
            table.periods td, table.periods th {
                width: 2.8em;
            }
            table.periods th {
                padding: 2px;
                border-right-width: 1px;
            }
            table.periods th.row {
                padding-right: 0.5em;
                text-align: right;
                white-space: nowrap;
            }
            table.periods th.column {
                border-top-width: 1px;
                font-weight: normal
            }
            table.periods th.no-cycle, table.periods th .cycle-number {
                font-weight: bold
            }
            table.periods th .day-number {
                color: #444
            }
            table.periods tr.resume th {
                border-right: 1px solid #ddd;
            }
            table.periods td {
                padding: 0;
                border-width: 1px 1px 0 0;
                text-align: center;
            }
            table.periods td.repetition a, table.periods td.repetition span {
                text-decoration: none;
                margin: 0;
                padding: 2px;
                display: block;
                color: #444;
                /* For IE */
                height: 1%;
            }
            table.periods a:hover {
                color: #000;
            }
            table.periods td.repetition.editable:hover {
                background-color: #ccc;
            }
            table.periods td.repetition {
                background-color: #ddd;
                border-right-width: 0;
            }
            table.periods td.empty {
                background-color: #fff;
                border-right-width: 0;
            }
            table.periods td.last {
                border-right-width: 1px;
            }
            table.periods tr.last td {
                border-bottom-width: 1px;
            }
            table.periods td.controls {
                border-width: 0 !important;
                padding: 3px;
            }
            table.periods tr.arrows td {
                border-width: 0 !important;
                border-top-width: 1px;
            }
            .days {
                margin: 0 3em 3em 5em;
            }
            li.studySegment, #epochs h4 {
                position: relative;

            }
            /* For IE */
            * html #epochs h4 { height: 1px; }
            .controls, table.periods a.control {
                /* TODO: this shouldn't be absolute */
                font-size: 7.5pt;
            }
            div.studySegment-controls, div.epoch-controls {
                bottom: 2px;
                top:2px;
                right: 2px;
                /*need the padding to display correctly the buttons in IE7*/
                padding-top:3px;
                padding-bottom:3px;

            }
            div.studySegment-controls {
                position: absolute;
                text-align: right;
            }

            div.epoch-controls{
                position: relative;
                text-align: left;
            }

            span.study-controls {
                margin-left: 4px;
            }

            .inplaceeditor-form a, form {
                font-size: 11pt;
                border: 1px solid #444;
                padding: 3px;
            }

            .no-border-form {
                border: 0px;
                margin-bottom:3px;
                width:60%;
                position:relative;

            }

            ul#admin-options {
                padding: 0.5em;
                margin: 0;
            }
            ul#admin-options li {
                display: inline;
                padding: 2px 4px;
                margin: 0;
                list-style-type: none;
            }

            #revision-changes {
                float: right;
                width: 29%;
            }

            #with-changes #selected-studySegment {
                width: 70%;
                float: left;
            }

            p#addEpoch{
                padding-left: 8px;
            }

            h1#enterStudyNameSentence {
                padding-top: 8px;
            }

            div.row div.label {
                text-align:left;
                display:inline;
                width:auto;
                margin-right:10px;
            }

            #populations{
                padding: 0; margin: 0;
                text-align:left;
                display:inline;
                width:auto;
                margin-right:10px;
                padding-right:10px;
            }

            div.row {
                padding-top: 2px;
                margin: 0;
            }
            div.controls-card {
                float: right;
                margin-left: 0;
                width: 68%;
            }

            #study-info ul {
                padding: 0; margin: 0;
            }

            #study-info ul li {
                list-style-type: none;
            }
            
            #outside-links {
                margin: 1em;
                text-align: right
            }
        </style>

        <c:if test="${not empty developmentRevision}">
            <script type="text/javascript" src="<c:url value="/pages/cal/template/edit.js?study=${study.id}&studyName=${study.assignedIdentifier}"/>"></script>
        </c:if>
        <script type="text/javascript">
            var lastRequest;
            var selectedStudySegmentId = ${studySegment.base.id};
			var days;
			var day;
			var showButton;
			var hideButton;
			var showDay;
			var showMonth;
			var hideMonth;
			var presentMonths;
			var hiddenMonths;
			var allDaysArePresent = false;
			var allDaysAreHidden = true;
			var init;

            function registerSelectStudySegmentHandlers() {
                $$('#epochs a').each(registerSelectStudySegmentHandler)
            }
			function initialize_arrows()
			{
				 days = $$(".days");
				 day = $$(".day");
				 showButton = $('show_button');
				 hideButton = $('hide_button');
				 showMonth =  $$(".showMonth");
				 hideMonth =  $$(".hideMonth");
				 presentMonths = [];
				 showDay = $$(".showDay");
				 hiddenMonths = [];
				 allDaysArePresent = false;
				 allDaysAreHidden = true;
				 init = true;
            }
			function check()
			{

				// Will figure out when the Show Month Arrows should be visible
			    days.each(function(dc, counter) {

		            if (isMonthPresent(counter)){
                        hideMonth[counter].reveal();
                        showMonth[counter].conceal();
						presentMonths[counter] = true;
                    }
 					else {
                        showMonth[counter].reveal();
						presentMonths[counter] = false;

                    }
				})


				// Will figure out when the Hide Month Arrows should be visible

				days.each(function(dc, counter) {
                   if (isMonthHidden(counter)){
                       hideMonth[counter].conceal();
					   if(!isMonthPresent(counter)) showMonth[counter].reveal();
					   hiddenMonths[counter] = true;

                   }
					else {
                       hideMonth[counter].reveal();
					   hiddenMonths[counter] = false;

                   }
			     })


				// Will figure out when the Show All button should be visible
				allDaysArePresent = true
				for (var i=0; i < presentMonths.length; i++)
				{
					if (allDaysArePresent && presentMonths[i])
						allDaysArePresent = true
					else allDaysArePresent = false

				}

				if (allDaysArePresent)	showButton.conceal()
				else showButton.reveal()

				// Will figure out when the Hide All button should be visible

				allDaysAreHidden = true
				for (var i=0; i < hiddenMonths.length; i++)
				{
					if (allDaysAreHidden && hiddenMonths[i])
						allDaysAreHidden = true
					else allDaysAreHidden = false

				}

				if (allDaysAreHidden) {
                    hideButton.conceal()
                }
				else {
                    hideButton.reveal()
                }

			}

			function isDayPresent(day1){
				var present
				if ($(day1).style.display == "none")
					present = false
				else present = true
				return present
			}
			function allDaysArePresent(){
				var continue1 = true

				day.each(function(e){

					if (isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false

				})
				return continue1
			}
			function allDaysAreHidden(){
				var continue1 = true

				day.each(function(e){

					if (!isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false

				})
				return continue1
			}
			function isMonthPresent(num)
			{
				var continue1 = true

				days[num].getElementsByClassName('day').each(function(e){

					if (isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false

				})
				return continue1
			}
			function isMonthHidden(num)
			{
				var continue1 = true

				days[num].getElementsByClassName('day').each(function(e){

					if (!isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false

				})
				return continue1
			}
			function quickSlideAndShow(elementArray, options)
			{
				var eA = elementArray
				var effects = []
				eA.each(function(e) {
					effects = effects.concat([
						new Effect.BlindDown(e, {sync:true}),
			        	new Effect.Appear(e, {sync:true})
					])
				})

			   	 	new Effect.Parallel(
				       	effects,
						$H(options).merge({
					         duration: 1.0
					    }).toObject()
					);
			}
			function quickSlideAndHide(elementArray, options)
			{
				var eA = elementArray
				var effects = []
				eA.each(function(e) {
					effects = effects.concat([
						new Effect.BlindUp(e, {sync:true}),
			        	new Effect.Fade(e, {sync:true})
					])
				})

			   	 	new Effect.Parallel(
				       	effects,
						$H(options).merge({
					         duration: 1.0
					    }).toObject()
					);
			}
            function registerSelectStudySegmentHandler(a) {
                var aElement = $(a)
                Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
                    $("epochs-indicator").reveal();
                    SC.slideAndHide('selected-studySegment-content', { afterFinish: function() {
                        // deselect current
                        var sel = $$("#epochs .selected")
                        if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                        var studySegmentId = aElement.id.substring('studySegment'.length+1)
                        selectedStudySegmentId = studySegmentId
                        aElement.href = '<c:url value="/pages/cal/template/select"/>?studySegment=' + studySegmentId

                        lastRequest = new Ajax.Request(aElement.href,
                            {
                                onComplete: function(req) {
                                    $("epochs-indicator").conceal()
                                },
                                onFailure: function() {
                                    Element.update('selected-studySegment-content', "<p class='error'>Loading failed</p>")
                                    Element.update('selected-studySegment-header', "Error")
                                    SC.slideAndShow('selected-studySegment-content')
                                }
                            }
                        );
                    } });
                })
            }
            function registerShowHandler(){
                if (showButton) {
                    Event.observe(showButton, "click", function(e) {
                        Event.stop(e);
                        day.each(SC.slideAndShow);
                        showMonth.each(function(e){e.conceal();});
                        hideMonth.each(function(e){e.reveal();});
                        showButton.conceal()
                        hideButton.reveal()

                        showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow"><b>&#45;</b></a>');});

                    });
                }
            }

			function showSetup(){
                if (init == null) {
                    initialize_arrows();
                }
				registerShowHandler()
			}

			function registerHideHandler(){
                if (hideButton) {
                    Event.observe(hideButton, "click", function(e) {
                        Event.stop(e);
                        day.each(SC.slideAndHide);
                        showMonth.each(function(e){e.reveal();});
                        hideMonth.each(function(e){e.conceal();});
                        showButton.reveal()
                        hideButton.conceal()

                        if ($('showArrow') != null && $('showArrow').innerHTML.toLowerCase() == '<b>-</b>'){
                            $('showArrow').innerHTML = "";
                        }

                        showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow">&#43;</a>');});
                    });
                }

			}

			function hideSetup(){
                if (init == null) {
                    initialize_arrows();
                }                
                registerHideHandler()
			}

			function registerArrowHandler(a, counter){
                var aElement = $(a)
                Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
                    var specificElement = document.getElementsByClassName('control showArrow')[counter];
                    //need to cast to LowerCase, as IE7 converts tags to upper case
                    if (specificElement != null && specificElement.innerHTML.toLowerCase() == '<b>-</b>'){
                        Element.update(this, '<a href="#" class="control showArrow" id="showArrow">&#43;</a>')
						SC.slideAndHide(day[counter], {afterFinish: check})

					} else{
                        Element.update(this, '<a href="#" class="control showArrow" id="showArrow"><b>&#45;</b></a>')
						SC.slideAndShow(day[counter], {afterFinish: check})
					}
                })
			}

            function registerArrowHandlers(){
				var counter = 0;
                showDay.each(function(num) {registerArrowHandler(num, counter); counter++;});
            }

			function registerShowMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)
					quickSlideAndShow(days[counter].getElementsByClassName('day'), {afterFinish: check})
                    showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow"><b>&#45;</b></a>');});
                });
			}

            function registerHideMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)
				    quickSlideAndHide(days[counter].getElementsByClassName('day'), {afterFinish: check})
                    showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow">&#43;</a>');});
                });
			}

            function epochsAreaSetup() {
                registerSelectStudySegmentHandlers()
                <c:if test="${not empty developmentRevision}">
                    createAllStudySegmentControls()
                    createAllEpochControls()
                </c:if>
            }
			function arrowSetup(){
                if (init == null) {
                    initialize_arrows();
                }
                registerArrowHandlers()
			}
			function showMonthSetup(){
				var counter = 0;
				showMonth.each(function(num) {registerShowMonthHandler(num, counter); counter++;});
			}
			function hideMonthSetup(){
				var counter = 0;
				hideMonth.each(function(num) {registerHideMonthHandler(num, counter); counter++;});

			}
			function initializeNewStudySegment(){
                if (init == null) {
                    initialize_arrows();
                }
                if ($$(".day").length != 0){
			    	initialize_arrows()
					showSetup()
					hideSetup()
					arrowSetup()
					showMonthSetup()
					hideMonthSetup()
				}

			}

            function studyManipulationSetup(){
                $('admin-options').show()
            }


            function deletePeriodPopup(periodId, periodName) {
                var message = "Are you sure you want to delete the period '" + periodName +
                        "'?  This will permanently remove it and its events. " +
                        "\n\nThis action cannot be undone."
                if (window.confirm(message)) {//Boolean variable. Sets to true if user pressed "OK" versus "Cancel."
                   deletePeriod(periodId)
                    return true;
                } else {
                  return false;
                }
            }

            // TODO: this method must go in edit.js with everything else
            function deletePeriod(periodId) {

                var formdata = "";
                formdata = formdata + "period=" + periodId + "&";
                formdata = formdata+ "studySegment=" + ${studySegment.base.id} + "&";

                href = '<c:url value="/pages/cal/deletePeriod"/>?period=' + periodId + "&studySegment=" + ${studySegment.base.id}
                var lastRequest = new Ajax.Request(href,
                {
                    postBody: formdata
                });

                return true;
            }
            // Temporary.  Validation should really be on the server side.
            function isCorrectCycleLength() {
                var cycleLength = document.getElementById("cycleLength").value;
                var isCorrectInput = true;
                if ((cycleLength != "" && cycleLength <=0) || (isNaN(cycleLength))
                     ||(cycleLength.indexOf(".")>0) || (cycleLength.indexOf(",") > 0)) {
                isCorrectInput = false;
                document.getElementById("cycleError").innerHTML = "Cycle Length must be a positive number.";
             }
            return isCorrectInput;
            }

            function updateCycleError() {
                 if (isCorrectCycleLength()){
                document.getElementById("cycleError").innerHTML = "";
                }
            }
            
            <c:if test="${not empty developmentRevision}">
            Element.observe(window, "load", function() {
                $$("#cycleLength").each(function(fn) {
                new Form.Element.Observer(fn, 1, updateCycleError);
                })
                updateCycleError();
            })

            $(document).observe("dom:loaded", function() {
                $('cycle-form').observe("submit", function(fn) {
                if (!isCorrectCycleLength()) {
                    Event.stop(fn);
                }
                })
            })
            </c:if>

            function generalSetup() {
                epochsAreaSetup();
                studyManipulationSetup();
            }

            function arrowsHideShowSetup(){
                showSetup();
                hideSetup();
                arrowSetup();
                showMonthSetup();
                hideMonthSetup();
            }

            function loadFunctionsForDevelopmentRevision() {
                generalSetup();
                createStudyControls();
                createAddEpochControl();
                addToBeginSentence();
                hideShowReleaseTemplateButton();
                arrowsHideShowSetup();
            }

            function loadFunctionsForStudySegment() {
                generalSetup();
                arrowsHideShowSetup();
            }


            <c:if test="${not empty developmentRevision}">
                Event.observe(window, "load", loadFunctionsForDevelopmentRevision)
            </c:if>
            <c:if test="${empty developmentRevision && not empty studySegment.months}">
                Event.observe(window, "load", loadFunctionsForStudySegment)
            </c:if>

        </script>
    </head>
    <body>
        <div id="study-info" class="title-card card">
            <div class="header">Study info</div>
            <div id="errors" style="margin-right:10px; margin-left:0.5em;"></div>
            <h1><span id="study-name">${study.assignedIdentifier}</span></h1>
            <div class="row odd">
                <div class="label">Amendment</div>
                <div class="value">
                    <a href="<c:url value="/pages/cal/template/amendments?study=${study.id}#amendment=${amendment.id}"/>">${amendment.displayName}</a>
                    <span class="controls"><a class="control" href="<c:url value="/pages/cal/template/amendments?study=${study.id}"/>">view all</a></span>
                </div>
            </div>
            <div class="row even" style="width:100%">
                <div class="label">Long title</div>
                <div class="value">${study.longTitle}</div>
            </div>
            <div class="row odd">
                <div class="label">Populations</div>
                <div class="value">
                    <ul id="populations">
                        <c:forEach items="${study.populations}" var="population">
                            <li>
                                <c:if test="${!canNotViewPopulations}">
                                    <a href="<c:url value="/pages/cal/template/population?study=${study.id}&population=${population.id}"/>">
                                            ${population.abbreviation}: ${population.name}
                                    </a>
                                </c:if>
                                <c:if test="${canNotViewPopulations}">${population.abbreviation}: ${population.name}</c:if>
                            </li>
                        </c:forEach>
                        <li class="controls">
                            <c:if test="${study.inInitialDevelopment || study.inAmendmentDevelopment}">
                                <a class="control" href="<c:url value="/pages/cal/template/population?study=${study.id}"/>">Add</a>
                            </c:if>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="row even">
                <div class="label">Other formats</div>
                <div class="value">
                    <ul>
                        <li><a href="<c:url value="/api/v1/studies/${empty developmentRevision ? study.assignedIdentifier : study.gridId}/template?download"/>">PSC XML</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <div id="study-manipulations" class="controls-card card">
            <div class="header">Manipulate study</div>
                <div id="enterStudyName" style="display:none;">
                    <h1 id="enterStudyNameSentence"></h1>
                </div>

                <div id="errorMessages" style="display:none;">
                    <tags:replaceErrorMessagesForTemplate/>
                </div>

                <ul id="admin-options" style="display:none;">

                    <div class="row">
                        <div class="value" style="margin:0px;">
                            <c:if test="${not empty developmentRevision}">
                                <tags:restrictedListItem url="/pages/cal/template/release" queryString="study=${study.id}" cssClass="control">
                                    Release this ${study.inInitialDevelopment ? 'template' : 'amendment'} for use
                                </tags:restrictedListItem>
                            </c:if>
                            <c:if test="${empty developmentRevision}">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
                                <%--<c:if test="${canAssignSubjects}">--%>
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleReconsent" queryString="study=${study.id}">
                                        Schedule reconsent
                                    </tags:restrictedListItem>

                                <%--</c:if>--%>
                                <c:if test="${empty disableAddAmendment}">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/amendment" queryString="study=${study.id}">
                                    Add amendment
                                </tags:restrictedListItem>
                               </c:if>
                            </div>
                        </div>

                        <c:forEach items="${study.studySites}" var="studySite" varStatus="studySiteStatus">
                            <div class="row">
                                <div class="label" >
                                    ${studySite.site.name}
                                </div>
                                <div class="value">
                                    <c:if test="${not empty studySite.unapprovedAmendments}">
                                        Waiting for approval at site ${studySite.site.name}. A <b>Site Coordinator</b> can do that
                                    </c:if>
                                    <c:if test="${empty studySite.unapprovedAmendments}">
                                        <c:set var="isSubjectCoordinatorAssigned" value="false"/>
                                        <c:forEach items="${studySite.userRoles}" var="userRole" varStatus="userRoleStatus">
                                            <c:if test="${userRole.role == 'SUBJECT_COORDINATOR'}">
                                                <c:set var="isSubjectCoordinatorAssigned" value="true"/>
                                            </c:if>
                                        </c:forEach>
                                        <c:if test="${!isSubjectCoordinatorAssigned}">
                                            Subject Coordinator has to be assigned to the study. A <b>Site Coordinator</b> can do this.
                                        </c:if>
                                        <c:if test="${isSubjectCoordinatorAssigned}">
                                            <c:if test="${configuration.map.enableAssigningSubject}">
                                                <tags:restrictedListItem url="/pages/cal/assignSubject" queryString="study=${study.id}&site=${studySite.site.id}" cssClass="control">
                                                    Assign Subject
                                                </tags:restrictedListItem>
                                            </c:if>
                                        </c:if>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>



                        <c:if test="${not empty onStudyAssignments}">
                            <security:secureOperation element="/pages/cal/schedule">
                            <li>View schedule (On Study) for
                                <select id="assigned-subject-selector">
                                    <c:forEach items="${onStudyAssignments}" var="assignment">
                                        <option value="${assignment.scheduledCalendar.id}">${assignment.subject.lastFirst}</option>
                                    </c:forEach>
                                </select>
                                <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="go-to-schedule-control">Go</a>
                            </li>
                            </security:secureOperation>
                        </c:if>
                        <c:if test="${not empty offStudyAssignments}">
                            <security:secureOperation element="/pages/cal/schedule">
                            <li>View schedule (Off Study) for
                                <select id="offstudy-assigned-subject-selector">
                                    <c:forEach items="${offStudyAssignments}" var="assignment">
                                        <option value="${assignment.scheduledCalendar.id}">${assignment.subject.lastFirst}</option>
                                    </c:forEach>
                                </select>
                                <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="offstudy-go-to-schedule-control">Go</a>
                            </li>
                            </security:secureOperation>
                        </c:if>
                    </c:if>
                </ul>
            
                <div id="outside-links">
                    <c:if test="${configuration.studyPageUrlConfigured}">
                        <c:set var="studyPageUrlAvail" value="${not empty configuration.map.studyPageUrl}"/>
                        <c:if test="${studyPageUrlAvail}">
                            <a href="<tags:urlFromTemplate property="studyPageUrl" />" class="control">${configuration.map.ctmsName} study record</a>
                        </c:if>

                    </c:if>
                </div>

        </div>
        <div id="epochs" class="section">
            <laf:box title="Epochs and study segments">
                <p class="controls" id="addEpoch"/>
              
                <laf:division>
                    <tags:epochsAndStudySegments id="epochs-container" plannedCalendar="${plannedCalendar}" selectedStudySegment="${studySegment.base}"/>
                </laf:division>
            </laf:box>
        </div>

        <c:set var="showChanges" value="${not empty developmentRevision and not study.inInitialDevelopment}"/>
        <c:if test="${showChanges}">
            <div id="with-changes">
                <div id="revision-changes" class="section">
                    <templ:changes revision="${developmentRevision}" changes="${revisionChanges}"/>
                </div>
            <%-- #with-changes is closed below --%>
        </c:if>

        <div id="selected-studySegment" class="section">
            <templ:studySegment studySegment="${studySegment}" developmentRevision="${developmentRevision}" visible="true"/>
        </div>

        <c:if test="${showChanges}"></div></c:if>

    </body>
</html>