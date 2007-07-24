<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security"
           uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<html>
    <head>
        <title>Template for ${study.name}</title>
        <tags:stylesheetLink name="main"/>
        <tags:includeScriptaculous/>
        <tags:javascriptLink name="main"/>
        <style type="text/css">
            .epochs-and-arms {
                margin: 1em;
            }

            #epochs-indicator {
                margin: 0.5em 0.5em 0 0;
                float: left;
            }

            table.periods, table.periods tr, table.periods td, table.periods th {
                border-spacing: 0;
                border: 0 solid #666;
                margin: 1em;
            }
            table.periods td, table.periods th {
                width: 2em;
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
            }
            table.periods tr.resume th {
                border-right: 1px solid #ddd;
            }
            table.periods td {
                padding: 0;
                border-width: 1px 1px 0 0;
                text-align: center;
            }
            table.periods td.repetition a {
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
            table.periods td.repetition:hover {
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

            li.arm, #epochs h4 {
                position: relative;

            }
            /* For IE */
            * html #epochs h4 { height: 1px; }
            .controls, table.periods a.control {
                font-family: Arial, sans-serif;
                font-size: 7.5pt;
            }
            div.arm-controls, div.epoch-controls {
                position: absolute;
                bottom: 4px;
                right: 2px;
                text-align: right;
            }
            span.study-controls {
                margin-left: 4px;
            }

            .inplaceeditor-form a {
                font-size: 11pt;
                border: 1px solid #444;
                padding: 3px;
            }

            ul#admin-options {
                padding: 0;
                margin: 0;
            }
            ul#admin-options li {
                display: inline;
                padding: 2px 4px;
                margin: 0;
                list-style-type: none;
            }
        </style>
        <c:if test="${not plannedCalendar.complete}">
        <script type="text/javascript" src="<c:url value="/pages/template/edit.js?study=${study.id}"/>"></script>
        </c:if>
        <script type="text/javascript">
            var lastRequest;
            var selectedArmId = ${arm.base.id};

            function registerSelectArmHandlers() {
                $$('#epochs a').each(registerSelectArmHandler)
            }
			function check()
			{
				var days = $$(".days")
				var showButton = $('show_button')
				var hideButton = $('hide_button')
				var showMonth =  $$(".showMonth")
				var hideMonth =  $$(".hideMonth")
				
				// Will figure out when the Show All button should be visible
				if (areAllDaysPresent()){
					showButton.conceal()
				}
				else{
					showButton.reveal()
				}
				
				// Will figure out when the Hide All button should be visible
				if (!areAllDaysHidden()){
					hideButton.reveal();
				}
				else{
					hideButton.conceal()
				}
			
				// Will figure out when the Show Month Arrows should be visible
			    days.each(function(dc, counter) {
			                    if (isMonthPresent(counter)){
			                        hideMonth[counter].reveal();
			                        showMonth[counter].conceal();
			                    }
			 					else {
			                        showMonth[counter].reveal();
			                    }
			     })

				
				// Will figure out when the Hide Month Arrows should be visible
				
				days.each(function(dc, counter) {
			                    if (isMonthPresent(counter)){
			                        hideMonth[counter].conceal();
			                        showMonth[counter].reveal();
			                    }
			 					else {
			                        showMonth[counter].reveal();
			                    }
			     })
			}

			function isDayPresent(day){
				var present
				if ($(day).style.display == "none")
					present = false
				else present = true
				return present
			}
			function areAllDaysPresent(){
				var continue1 = true
				
				$$(".day").each(function(e){ 
				
					if (isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}
			function areAllDaysHidden(){
				var continue1 = true
				
				$$(".day").each(function(e){ 
				
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
				
				$$(".days")[num].getElementsByClassName('day').each(function(e){ 
				
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

				$$(".days")[num].getElementsByClassName('day').each(function(e){ 
				
					if (!isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}

            function registerSelectArmHandler(a) {
                var aElement = $(a)
                Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
                    $("epochs-indicator").reveal();
                    SC.slideAndHide('selected-arm-content', { afterFinish: function() {
                        // deselect current
                        var sel = $$("#epochs .selected")
                        if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                        var armId = aElement.id.substring(4)
                        selectedArmId = armId
                        aElement.href = '<c:url value="/pages/template/select"/>?arm=' + armId

                        lastRequest = new Ajax.Request(aElement.href,
                            {
                                onComplete: function(req) {
                                    $("epochs-indicator").conceal()
                                },
                                onFailure: function() {
                                    Element.update('selected-arm-content', "<p class='error'>Loading failed</p>")
                                    Element.update('selected-arm-header', "Error")
                                    SC.slideAndShow('selected-arm-content')
                                }
                            }
                        );
                    } });
                })
            }
			function registerShowHandler(){
				var show = $("show_button")
				Event.observe(show, "click", function(e) {
		            Event.stop(e);
				    $$(".day").each(function(e){SC.slideAndShow(e, { afterFinish: check});});
					
					$$(".showDay").each(function (e){$(e).update('<a href="#" class="control" id="showArrow">&#9650;</a>');});
					
				}							
				);				
			}
			
			function showSetup(){
				registerShowHandler()
			}
			
			function registerHideHandler(){
				var hide = $("hide_button")
				Event.observe(hide, "click", function(e) {
		            Event.stop(e);
				    $$(".day").each(function(e){SC.slideAndHide(e, { afterFinish: check});});
					$$(".showDay").each(function (e){$(e).update('<a href="#" class="control" id="showArrow">&#9660;</a>');});
				}							
				);	
			
			}
			function hideSetup(){
				registerHideHandler()
			}
			
			function registerArrowHandler(a, counter){
				var aElement = $(a)
				
				Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
					if ($('showArrow').innerHTML == '▲'){
                		Element.update(this, '<a href="#" class="control" id="showArrow">&#9660;</a>')
						SC.slideAndHide($$(".day")[counter], {afterFinish: check})
				
						
					}
                	else{
						Element.update(this, '<a href="#" class="control" id="showArrow">&#9650;</a>')
						SC.slideAndShow($$(".day")[counter], {afterFinish: check})
					
				
						
					}
                })
			}
			function registerArrowHandlers(){
				var counter = 0;
                $$(".showDay").each(function(num) {registerArrowHandler(num, counter); counter++;});
            }
			
			function registerShowMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)          		
						$$(".days")[counter].getElementsByClassName('day').each(function(e){SC.slideAndShow(e, { afterFinish: check});})
				})
			}
			function registerHideMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)          		
						$$(".days")[counter].getElementsByClassName('day').each(function(e){SC.slideAndHide(e, { afterFinish: check});})
						$$(".hideMonth")[counter].conceal()
						$$(".showMonth")[counter].reveal()
				})
			}

            function epochsAreaSetup() {
                registerSelectArmHandlers()
                <c:if test="${not plannedCalendar.complete}">
                createAllArmControls()
                createAllEpochControls()
                </c:if>
            }
			function arrowSetup(){
				registerArrowHandlers()
			}
			function showMonthSetup(){
				var counter = 0;
				$$(".showMonth").each(function(num) {registerShowMonthHandler(num, counter); counter++;});
			}
			function hideMonthSetup(){
				var counter = 0;
				$$(".hideMonth").each(function(num) {registerHideMonthHandler(num, counter); counter++;});
			}
			

            <c:if test="${not plannedCalendar.complete}">
            Event.observe(window, "load", createStudyControls)
            </c:if>
            Event.observe(window, "load", epochsAreaSetup)
			Event.observe(window, "load", showSetup)
			Event.observe(window, "load", hideSetup)
			Event.observe(window, "load", arrowSetup)
			Event.observe(window, "load", showMonthSetup)
			Event.observe(window, "load", hideMonthSetup)
			
			
        </script>
    </head>
    <body>
    <h1>Template for <span id="study-name">${study.name}</span></h1>

        <ul id="admin-options">
            <c:if test="${not plannedCalendar.complete}">
                <tags:restrictedListItem url="/pages/markComplete" queryString="study=${study.id}" cssClass="control">Mark this template complete</tags:restrictedListItem>
            </c:if>
            <tags:restrictedListItem cssClass="control" url="/pages/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
            <c:if test="${not empty study.studySites}">
                <tags:restrictedListItem url="/pages/assignParticipantCoordinator" queryString="id=${study.id}" cssClass="control"
                    >Assign Participant Coordinators</tags:restrictedListItem>
                <tags:restrictedListItem url="/pages/assignParticipant" queryString="id=${study.id}" cssClass="control"
                    >Assign Participant</tags:restrictedListItem>
            </c:if>
            <c:if test="${not empty assignments}">
                <security:secureOperation element="/pages/schedule" operation="ACCESS">
                <li>View schedule for
                    <select id="assigned-participant-selector">
                        <c:forEach items="${assignments}" var="assignment">
                            <option value="${assignment.scheduledCalendar.id}">${assignment.participant.lastFirst}</option>
                        </c:forEach>
                    </select>
                    <a class="control" href="<c:url value="/pages/schedule"/>" id="go-to-schedule-control">Go</a>
                </li>
                </security:secureOperation>
            </c:if>
        </ul>

        <div id="epochs" class="section">
            <h2>Epochs and arms</h2>
            <tags:epochsAndArms id="epochs-container" plannedCalendar="${plannedCalendar}" selectedArm="${arm.base}"/>
        </div>

        <div id="selected-arm" class="section">
            <templ:arm arm="${arm}" visible="true"/>
        </div>

    </body>
</html>