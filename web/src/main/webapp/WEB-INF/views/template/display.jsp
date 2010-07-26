<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security"
           uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="commons" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<jsp:useBean scope="request" id="canEdit" type="java.lang.Boolean"/>
<jsp:useBean scope="request" id="study" type="edu.northwestern.bioinformatics.studycalendar.domain.Study"/>
<jsp:useBean scope="request" id="relationship" type="edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship"/>
<jsp:useBean scope="request" id="amendment" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment"/>
<jsp:useBean scope="request" id="plannedCalendar" type="edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar"/>
<jsp:useBean scope="request" id="epochs" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.domain.Epoch>"/>
<jsp:useBean scope="request" id="studySegment" type="edu.northwestern.bioinformatics.studycalendar.web.template.StudySegmentTemplate"/>
<c:if test="${not empty requestScope['developmentRevision']}">
    <jsp:useBean scope="request" id="developmentRevision" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision"/>
</c:if>
<c:if test="${not empty requestScope['revisionChanges']}">
    <jsp:useBean scope="request" id="revisionChanges" type="edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges"/>
</c:if>
<jsp:useBean scope="request" id="todayForApi" type="java.lang.String"/>

<jsp:useBean scope="request" id="user" type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser"/>
<jsp:useBean scope="request" id="configuration" type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"/>

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
            ul#admin-options li, .study-site ul li {
                display: inline;
                padding: 2px 4px;
                margin: 0;
                list-style-type: none;
            }
            #study-sites {
                border-top: 1px solid #444;
            }
            #study-sites ul {
                padding: 0;
                margin: 0;
            }
            #study-sites div.row {
                padding: 0.3em;
            }
            #study-sites div.label {
                width: 10em;
                text-align: right;
            }
            #study-sites div.value {
                margin-left: 10em;
            }

            #revision-changes {
                float: right;
                width: 5%;
            }

            #selected-studySegment.with-changes {
                width: 93%;
                float: left;
            }

            #changesTable {
                width: 300%;
                position:relative;
                float:inherit;
                margin-top:25px;
            }

            p#addEpoch{
                padding-left: 8px;
            }

            h1#enterStudyNameSentence {
                padding-top: 8px;
            }

            div.row div.label {
                text-align:right;
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

            .h2ForCycle {
                font-weight:normal;
            }

            .error .epochAndSegmentErrors {
                margin-right:10px;
                margin-left:0.5em;
            }
        </style>

        <c:if test="${canEdit}">
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

				var array = days[num].getElementsByClassName('day')
                for (var i= 0; i< array.length; i++){
                    var elem = array[i];
                    if (isDayPresent($(elem)) && continue1){
					continue1 = true
					}
					else continue1 = false
                }

//              days[num].getElementsByClassName('day').each(function(e){
//
//					if (isDayPresent($(e)) && continue1){
//					continue1 = true
//					}
//					else continue1 = false
//
//				})
				return continue1
			}
			function isMonthHidden(num)
			{
				var continue1 = true
                var array = days[num].getElementsByClassName('day')
                for (var i=0; i< array.length; i++) {
                    var elem = array[i]
                    if (!isDayPresent($(elem)) && continue1){
					continue1 = true
					}
					else continue1 = false
                }
//				days[num].getElementsByClassName('day').each(function(e){
//
//					if (!isDayPresent($(e)) && continue1){
//					continue1 = true
//					}
//					else continue1 = false
//
//				})
				return continue1
			}
			function quickSlideAndShow(elementArray, options)
			{
				var eA = elementArray
				var effects = []
                for (var i = 0; i < eA.length; i++) {
                    var elem = eA[i];


                    effects = effects.concat([
                        new Effect.BlindDown(elem, {sync:true}),
                        new Effect.Appear(elem, {sync:true})
                    ]);
                }

//				eA.each(function(e) {
//					effects = effects.concat([
//						new Effect.BlindDown(e, {sync:true}),
//			        	new Effect.Appear(e, {sync:true})
//					]);
//				});

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
                for (var i=0; i< eA.length; i++) {
                    var elem = eA[i]
                    effects = effects.concat([
						new Effect.BlindUp(elem, {sync:true}),
			        	new Effect.Fade(elem, {sync:true})
					])
                }
//				eA.each(function(e) {
//					effects = effects.concat([
//						new Effect.BlindUp(e, {sync:true}),
//			        	new Effect.Fade(e, {sync:true})
//					])
//				})

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
                        var href=   '<c:url value="/pages/cal/template/select"/>?studySegment=' + studySegmentId;

                        <c:if test="${not empty developmentRevision}">
                             href = href + '&development=true';
                        </c:if>
                        aElement.href = href;

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

            function showChangesSetup() {
                var show = $("showChanges")
                if (show) {
                    Event.observe(show, "click", function(e) {
                        var mytext = show.innerHTML
                        Event.stop(e)
                        if (mytext.indexOf("Show changes", 0) == 0) {
                            show.innerHTML = "Hide changes"
                            $('changesTable').style.display='block'
                        } else if (mytext.indexOf("Hide changes", 0) == 0) {
                            show.innerHTML = "Show changes"
                            $('changesTable').style.display='none'
                        }

                    });
                }
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

                    }
                    );
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
                    }
                    );
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
                <c:if test="${canEdit}">
                createAllStudySegmentControls()
                epochControls()
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

            // Temporary.  Validation should really be on the server side.
            function isCorrectCycleLength() {
                var isCorrectInput = true;
                if ($('cycleLength') != null) {
                    var cycleLength = document.getElementById("cycleLength").value;

                    if ((cycleLength != null && (cycleLength != "" && cycleLength <=0) || (isNaN(cycleLength)) ||(cycleLength.indexOf(".")>0) || (cycleLength.indexOf(",") > 0))) {
                        isCorrectInput = false;
                        document.getElementById("cycleError").innerHTML = "Cycle Length must be a positive number.";
                    }
                }
            return isCorrectInput;
            }

            function updateCycleError() {
                 if (isCorrectCycleLength()){
                    if ($('cycleLength') != null) {
                        document.getElementById("cycleError").innerHTML = "";
                    }
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
            }

            function arrowsHideShowSetup(){
                showSetup();
                hideSetup();
                arrowSetup();
                showMonthSetup();
                hideMonthSetup();
            }

            function loadFunctionsForDevelopmentRevision() {
                <c:choose>
                    <c:when test="${anyProvidersAvailable == null || anyProvidersAvailable == true}">
                      var anyProvidersAvailable = true;
                    </c:when>
                    <c:otherwise>
                        var anyProvidersAvailable = false;
                    </c:otherwise>
                </c:choose>

                generalSetup();
                if(typeof createStudyControls == 'function')
                    createStudyControls(anyProvidersAvailable);
                if(typeof createAddEpochControl == 'function')
                    createAddEpochControl();
                if(typeof addToBeginSentence == 'function')
                    addToBeginSentence();
                if(typeof hideShowReleaseTemplateButton == 'function')
                    hideShowReleaseTemplateButton();
                arrowsHideShowSetup();
                showChangesSetup()
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
            <div id="errors" class="error"></div>
            <h1><span id="study-name">${study.assignedIdentifier}</span></h1>
            <div class="row odd">
                <div class="label">Amendment</div>
                <div class="value">
                    <a href="<c:url value="/pages/cal/template/amendments?study=${study.id}#amendment=${amendment.id}"/>">${amendment.displayName}</a>
                    <span class="controls"><a class="control" href="<c:url value="/pages/cal/template/amendments?study=${study.id}"/>">view all</a></span>
                </div>
            </div>
            <c:choose>
                <c:when test="${not empty study.longTitle}">
                    <div class="row even" style="width:100%">
                        <div class="label">Long title</div>
                        <div class="value">${study.longTitle}</div>
                    </div>
                    <div class="row odd">
                </c:when>
                <c:otherwise>
                    <div class="row even">
                </c:otherwise>
            </c:choose>
                <div class="label">Populations</div>
                <div class="value">
                    <ul>
                        <c:forEach items="${study.populations}" var="population">
                            <li>
                                <c:if test="${canEdit}">
                                    <a href="<c:url value="/pages/cal/template/population?study=${study.id}&population=${population.id}"/>">
                                            ${population.abbreviation}: ${population.name}
                                    </a>
                                </c:if>
                                <c:if test="${!canEdit}">${population.abbreviation}: ${population.name}</c:if>
                            </li>
                        </c:forEach>
                        <li class="controls addPopulationButton" studyId='${study.id}' studyInInitialDevelopment='${study.inInitialDevelopment}' studyInAmendmentDevelopment='${study.inAmendmentDevelopment}'></li>
                    </ul>
                </div>
            </div>
            <c:choose>
                <c:when test="${not empty study.longTitle}">
                    <div class="row even">
                </c:when>
                <c:otherwise>
                    <div class="row odd">
                </c:otherwise>
            </c:choose>
                <div class="label">Other formats</div>
                <div class="value">
                    <ul>
                        <li><a href="<c:url value="/api/v1/studies/${empty developmentRevision ? study.assignedIdentifier : study.gridId}/template?download"/>">PSC XML</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <div id="study-manipulations" class="controls-card card">
            <div class="header">Study controls</div>
            <div id="enterStudyName" style="display:none;">
                <h1 id="enterStudyNameSentence"></h1>
            </div>

            <div id="errorMessages" class="error" style="display:none;">
                <tags:replaceErrorMessagesForTemplate/>
            </div>

            <ul id="admin-options">
                <li><a class="control" href="<c:url value="/pages/cal/template/preview?study=${study.id}&amendment=${amendment.id}#segment[0]=${studySegment.base.gridId}&start_date[0]=${todayForApi}"/>">
                    Preview schedule
                </a></li>
                <tags:conditionalListItemLink
                    showIf="${relationship.canRelease && not empty developmentRevision}"
                    url="/pages/cal/template/release?study=${study.id}"
                    cssClass="control">
                    Release this ${study.inInitialDevelopment ? 'template' : 'amendment'} for use
                </tags:conditionalListItemLink>
                <tags:conditionalListItemLink
                    showIf="${relationship.canSetParticipation && empty developmentRevision}"
                    url="/pages/cal/assignSite?id=${study.id}"
                    cssClass="control">
                    Assign sites
                </tags:conditionalListItemLink>
                <tags:conditionalListItemLink
                    showIf="${relationship.canScheduleReconsent && empty developmentRevision}"
                    url="/pages/cal/scheduleReconsent?study=${study.id}"
                    cssClass="control">
                    Schedule reconsent
                </tags:conditionalListItemLink>
                <tags:conditionalListItemLink
                    showIf="${relationship.canStartAmendment}"
                    url="/pages/cal/amendment?study=${study.id}"
                    cssClass="control">
                    Add amendment
                </tags:conditionalListItemLink>
                <c:set var="studyUrl"><tags:urlFromTemplate property="studyPageUrl"/></c:set>
                <tags:conditionalListItemLink
                    showIf="${configuration.studyPageUrlConfigured}"
                    url="${studyUrl}">
                    ${configuration.map.ctmsName} study record
                </tags:conditionalListItemLink>
            </ul>

            <c:if test="${empty developmentRevision}">
                <div id="study-sites">
                    <c:forEach items="${relationship.visibleStudySites}" var="ssRelationship" varStatus="ssStatus">
                        <div class="row study-site ${ssStatus.count % 2 == 0 ? 'even' : 'odd'}">
                            <div class="label" >
                                ${ssRelationship.studySite.site.name}
                            </div>
                            <div class="value">
                                <ul>
                                    <tags:conditionalListItemLink
                                        showIf="${ssRelationship.canAssignSubjects}"
                                        url="/pages/cal/assignSubject?study=${study.id}&site=${ssRelationship.studySite.site.id}"
                                        cssClass="control">
                                        Assign subject
                                    </tags:conditionalListItemLink>
                                    <tags:conditionalListItemLink
                                        showIf="${ssRelationship.canApproveAmendments}"
                                        url="/pages/cal/template/approve?studySite=${ssRelationship.studySite.id}"
                                        cssClass="control">
                                        Approve amendment
                                    </tags:conditionalListItemLink>
                                    <c:if test="${ssRelationship.canSeeSubjectInformation}">
                                        <li>
                                            <label>
                                                Schedule for
                                                <select id="assigned-subject-selector-${ssRelationship.studySite.id}" class="assigned-subject-selector">
                                                    <c:forEach items="${ssRelationship.visibleAssignments}" var="aRel">
                                                        <option value="${aRel.assignment.scheduledCalendar.id}"
                                                            assignment="${aRel.assignment.id}"
                                                            off="${aRel.assignment.off}"
                                                            >
                                                            ${aRel.assignment.subject.lastFirst}
                                                            <c:if test="${aRel.assignment.off}">(off study)</c:if>
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <a id="go-to-schedule-control-${ssRelationship.studySite.id}" class="control go-to-schedule-control"
                                                   href="<c:url value="/pages/subject"/>">View</a>
                                                <a id="take-subject-off-study-${ssRelationship.studySite.id}" class="control take-subject-off-study"
                                                   href="<c:url value="/pages/cal/takeSubjectOffStudy"/>">
                                                    Take off study
                                                </a>
                                            </label>
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <div id="epochs" class="section">
            <laf:box title="Epochs and study segments">
                <p class="controls" id="addEpoch"></p>

                <laf:division>
                    <div id="epochAndSegmentErrors" class="error"></div>
                    <tags:epochsAndStudySegments id="epochs-container" plannedCalendar="${plannedCalendar}" selectedStudySegment="${studySegment.base}"/>
                </laf:division>
            </laf:box>
        </div>

        <c:set var="showChanges" value="${not empty developmentRevision and not study.inInitialDevelopment}"/>
        <c:if test="${showChanges}">
            <div id="revision-changes" class="section">
                <a id="showChanges" class="showChanges" href="#" name="showChanges" style="visibility: visible;">Show changes</a>
                <templ:changes revision="${developmentRevision}" changes="${revisionChanges}"/>
            </div>
        </c:if>
        <div id="selected-studySegment" class="section ${showChanges ? 'with-changes' : ''}">
            <templ:studySegment studySegment="${studySegment}" developmentRevision="${developmentRevision}" visible="true" canEdit="${canEdit}"/>
        </div>
    </body>
</html>