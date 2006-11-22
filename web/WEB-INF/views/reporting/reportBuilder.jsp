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
                $('sitesFilter').innerHTML = "None"
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
                $('studiesFilter').innerHTML = "None"
            })
        }
        
		function registerParticipantSelectorFormNext() {
            Event.observe("participantSelectorFormNext", "click", function() {
                SC.slideAndHide("participantSelectorForm")
                SC.slideAndShow('dateRangeSelectorForm')
                var participantsFilterList = "";
				<c:forEach items="${participants}" var="participant">
					participantsFilterList = participantsFilterList + "${participant.lastName}, ${participant.firstName} <br>";
				</c:forEach>
				$('participantsFilter').innerHTML = participantsFilterList;	
            })
        }

		function registerDateRangeSelectorFormBack() {
            Event.observe("dateRangeSelectorFormBack", "click", function() {
                SC.slideAndHide("dateRangeSelectorForm")
                SC.slideAndShow('participantSelectorForm')
                $('participantsFilter').innerHTML = "None"
            })
        }

		function registerDateRangeSelectorFormFinish() {
            Event.observe("dateRangeSelectorFormFinish", "click", function() {
                SC.slideAndHide("dateRangeSelectorForm")
                SC.slideAndShow('generateReport')
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
<form:form method="post" action="${siteSelectorFormAction}" id="siteSelectorForm">
	<div class="row">
        <div class="label">
            <form:label path="sites">Sites</form:label>
        </div>
        <div class="value">
            <tags:activityIndicator id="sites-indicator"/>
            <form:select path="sites" multiple="true">
                <form:options items="${sites}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
        <div class="value">
            <input type="button" id="siteSelectorFormNext" value="Next"/>
        </div>
    </div>    
</form:form>

<c:url value="/pages/reportBuilder/selectStudies" var="studySelectorFormAction"/>
<form:form method="post" action="${studySelectorFormAction}" id="studySelectorForm" cssStyle="display: none">
	<div class="row">
        <div class="label">
            <form:label path="studies">Studies</form:label>
        </div>
        <div class="value">
            <tags:activityIndicator id="studies-indicator"/>
            <form:select path="studies" multiple="true">
                <form:options items="${studies}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
        <div class="value">
            <input type="button" id="studySelectorFormBack" value="Back"/><input type="button" id="studySelectorFormNext" value="Next"/>            
        </div>
    </div>    
</form:form>
    
<form:form method="post" id="participantSelectorForm" cssStyle="display: none">
	<div class="row">
        <div class="label">
            <form:label path="participants">Participants</form:label>
        </div>
        <div class="value">
            <form:select path="participants" multiple="true">
                <form:options items="${participants}" itemLabel="firstName" itemValue="id"/>
            </form:select>
        </div>
        <div class="value">
            <input type="button" id="participantSelectorFormBack" value="Back"/><input type="button" id="participantSelectorFormNext" value="Next"/>            
        </div>
    </div>
</form:form>
    
<form:form method="post" id="dateRangeSelectorForm" cssStyle="display: none">
	    <div class="row">
	        <div class="label">
	            <label for="startTimeSelector">Start (mm/dd/yyyy)</label>
	        </div>
	        <div class="value">
	            <form:input path="startDate"/>
	        </div>
	    </div>
	             
	    <div class="row">
	        <div class="label">
	            <label for="endTimeSelector">End (mm/dd/yyyy)</label>
	        </div>
	        <div class="value">
	            <form:input path="endDate"/>
	        </div>
	    </div>
	
	    <div class="value">
	        <input type="button" id="dateRangeSelectorFormBack" value="Back"/><input type="button" id="dateRangeSelectorFormFinish" value="Finish"/>            
	    </div>
	</div>
</form:form>

<c:url value="/pages/generateReport" var="formAction"/>
<form:form id="reportBuilderForm" method="post" action="${formAction}">
	<div class="row" >
		<div>You selected the following filters:</div>
		<div><strong>Sites:</strong></div>
		<div id="sitesFilter">None</div>
		<div><strong>Studies:</strong></div>
		<div id="studiesFilter">None</div>
		<div><strong>Participants:</strong></div>
		<div id="participantsFilter">None</div>
		<div><strong>Occuring from:</strong>
		<div id="fromFilter">None</div>
		<div><strong>to: </strong></div>		
		<div id="toFilter">None</div>
        <div id="generateReport" class="submit" style="display: none">
            <input type="submit" value="Report"/>
        </div>
    </div>
</form:form>
</body>
</html>