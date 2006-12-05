<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>Report Builder</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 35%;
        }

        div.submit {
            text-align: right;
        }

        form {
            width: 20em;
            margin: 10px;
        }
    </style>
    <script type="text/javascript">

        function registerSiteSelectorFormNext() {
            Event.observe("siteSelectorFormNext", "click", function() {
            	$('sites-indicator').reveal();
                SC.slideAndHide("siteSelectorForm", {
                    afterFinish: function() {
	                    SC.asyncSubmit("siteSelectorForm", {
	                        onComplete: function() {
	                            $('sites-indicator').conceal();                            
	                        }
	                    })
                    }
                })
        	})
        }
        
		function registerStudySelectorFormBack() {
            Event.observe("studySelectorFormBack", "click", function() {
                SC.slideAndHide("studySelectorForm")
                SC.slideAndShow('siteSelectorForm')
                $('sitesFilterList').innerHTML = "None selected"
            })
        }
        
		function registerStudySelectorFormNext() {
            Event.observe("studySelectorFormNext", "click", function() {
            	$('studies-indicator').reveal();
                SC.slideAndHide("studySelectorForm", {
                    afterFinish: function() {
	                    SC.asyncSubmit("studySelectorForm", {
	                        onComplete: function() {
	                            $('studies-indicator').conceal();
	                        }
	                    })
                    }
                })
        	})
        }

		function registerParticipantSelectorFormBack() {
            Event.observe("participantSelectorFormBack", "click", function() {
                SC.slideAndHide("participantSelectorForm")
                SC.slideAndShow('studySelectorForm')
                $('studiesFilterList').innerHTML = "None selected"
            })
        }
        
		function registerParticipantSelectorFormNext() {
            Event.observe("participantSelectorFormNext", "click", function() {
            	$('participants-indicator').reveal();
                SC.slideAndHide("participantSelectorForm", {
                    afterFinish: function() {
	                    SC.asyncSubmit("participantSelectorForm", {
	                        onComplete: function() {
	                            $('participants-indicator').conceal();
	                        }
	                    })
                    }
                })
        	})
        }

		function registerDateRangeSelectorFormBack() {
            Event.observe("dateRangeSelectorFormBack", "click", function() {
                SC.slideAndHide("dateRangeSelectorForm")
                SC.slideAndShow('participantSelectorForm')
                $('participantsFilterList').innerHTML = "None selected"
            })
        }

		function registerDateRangeSelectorFormFinish() {
            Event.observe("dateRangeSelectorFormFinish", "click", function() {
            	$('dateRange-indicator').reveal();
                SC.slideAndHide("dateRangeSelectorForm", {
                    afterFinish: function() {
	                    SC.asyncSubmit("dateRangeSelectorForm", {
	                        onComplete: function() {
	                            $('dateRange-indicator').conceal();
	                        }
	                    })
                    }
                })

            })
        }
           
    	Event.observe(window, "load", registerSiteSelectorFormNext)  	
    	Event.observe(window, "load", registerStudySelectorFormBack)
    	Event.observe(window, "load", registerStudySelectorFormNext)   	
    	Event.observe(window, "load", registerParticipantSelectorFormBack)   	
    	Event.observe(window, "load", registerParticipantSelectorFormNext)   	
    	Event.observe(window, "load", registerDateRangeSelectorFormBack)   	
    	Event.observe(window, "load", registerDateRangeSelectorFormFinish)   	
	</script>
</head>
<body>
<h1>Report Builder</h1>

<c:url value="/pages/reportBuilder/selectSites" var="siteSelectorFormAction"/>
<form method="post" action="${siteSelectorFormAction}" id="siteSelectorForm">
	<div class="row">
        <div class="label">
            <label>Sites</label>
        </div>
        <div class="value">
            <tags:activityIndicator id="sites-indicator"/>
            <select id="sites" name="sites" multiple="true">
            <c:forEach items="${sites}" var="site">
            	<option value="${site.id}"><c:out value="${site.name}"/></option>
            </c:forEach>
            </select>
        </div>
        <div class="value">
            <input type="button" id="siteSelectorFormNext" value="Next"/>
        </div>
    </div>    
</form>

<c:url value="/pages/reportBuilder/selectStudies" var="studySelectorFormAction"/>
<form method="post" action="${studySelectorFormAction}" id="studySelectorForm" style="display: none">
	<div class="row">
        <div class="label">
            <label>Studies</label>
        </div>
        <div class="value">
            <tags:activityIndicator id="studies-indicator"/>
            <select id="studies" name="studies" multiple="true">
            </select>
        </div>
        <div class="value">
            <input type="button" id="studySelectorFormBack" value="Back"/><input type="button" id="studySelectorFormNext" value="Next"/>            
        </div>
    </div>    
</form>

<c:url value="/pages/reportBuilder/selectParticipants" var="partipantSelectorFormAction"/>    
<form method="post" action="${partipantSelectorFormAction}" id="participantSelectorForm" style="display: none">
	<div class="row">
        <div class="label">
            <label>Participants</label>
        </div>
        <div class="value">
            <tags:activityIndicator id="participants-indicator"/>
            <select id="participants" name="participants" multiple="true">
            </select>
        </div>
        <div class="value">
            <input type="button" id="participantSelectorFormBack" value="Back"/><input type="button" id="participantSelectorFormNext" value="Next"/>            
        </div>
    </div>
</form>

<c:url value="/pages/reportBuilder/selectDateRange" var="dateRangeSelectorFormAction"/>
<form method="post" id="dateRangeSelectorForm" action="${dateRangeSelectorFormAction}" style="display: none">
	    <div class="row">
	        <div class="label">
	            <label for="startTimeSelector">Start (mm/dd/yyyy)</label>
	        </div>
	        <div class="value">
	            <input name="startDateInput"/>
	        </div>
	    </div>
	             
	    <div class="row">
	        <div class="label">
	            <label for="endTimeSelector">End (mm/dd/yyyy)</label>
	        </div>
	        <div class="value">
	            <input name="endDateInput"/>
				<tags:activityIndicator id="dateRange-indicator"/>
	        </div>
	    </div>
	
	    <div class="value">
	        <input type="button" id="dateRangeSelectorFormBack" value="Back"/><input type="button" id="dateRangeSelectorFormFinish" value="Finish"/>            
	    </div>
	</div>
</form>

<form id="reportBuilderForm" method="post">
	<div class="row">
		<div>You selected the following filters:</div>
		<div><strong>Sites:</strong></div>
			<div id="sitesFilterDisplay">None selected</div>
		<div><strong>Studies:</strong></div>
			<div id="studiesFilterDisplay">None selected</div>
		<div><strong>Participants:</strong></div>
			<div id="participantsFilterDisplay">None selected</div>
		<div>
			<div><strong>Occuring after (MM/DD/YYYY):</strong></div>
			<div id="startDateDisplay">Any date</div>
		</div>
		<div>
			<div><strong>and before (MM/DD/YYYY):</strong></div>
			<div id="endDateDisplay">Any date</div>
		</div>
        <div id="generateReport" class="submit" style="display: none">
            <input type="submit" value="Report"/>
        </div>
    </div>
</form>
</body>
</html>