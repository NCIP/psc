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
                SC.slideAndHide("siteSelectorForm")
                SC.slideAndShow('studySelectorForm')
            })
        }
        
		function registerStudySelectorFormBack() {
            Event.observe("studySelectorFormBack", "click", function() {
                SC.slideAndHide("studySelectorForm")
                SC.slideAndShow('siteSelectorForm')
            })
        }
        
		function registerStudySelectorFormNext() {
            Event.observe("studySelectorFormNext", "click", function() {
                SC.slideAndHide("studySelectorForm")
                SC.slideAndShow('participantSelectorForm')
            })
        }

		function registerParticipantSelectorFormBack() {
            Event.observe("participantSelectorFormBack", "click", function() {
                SC.slideAndHide("participantSelectorForm")
                SC.slideAndShow('studySelectorForm')
            })
        }
        
		function registerParticipantSelectorFormNext() {
            Event.observe("participantSelectorFormNext", "click", function() {
                SC.slideAndHide("participantSelectorForm")
                SC.slideAndShow('dateRangeSelectorForm')
            })
        }

		function registerDateRangeSelectorFormBack() {
            Event.observe("dateRangeSelectorFormBack", "click", function() {
                SC.slideAndHide("dateRangeSelectorForm")
                SC.slideAndShow('participantSelectorForm')
            })
        }

		function registerDateRangeSelectorFormFinish() {
            Event.observe("dateRangeSelectorFormFinish", "click", function() {
                SC.slideAndHide("dateRangeSelectorForm")
                SC.slideAndShow('reportBuilderFormSubmit')
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
<a href="<c:url value="/pages/studyList"/>">Calendar Menu</a>.<br>
<c:url value="/pages/generateReport" var="formAction"/>
<form:form id="reportBuilderForm" method="post" action="${formAction}">
    <div id="siteSelectorForm" class="row">
        <div class="label">
            <form:label path="sites">Sites</form:label>
        </div>
        <div class="value">
            <form:select path="sites" multiple="true">
                <form:options items="${sites}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
        <div class="value">
            <input type="button" id="siteSelectorFormNext" value="Next"/>
        </div>
    </div>    

    <div id="studySelectorForm" class="row" style="display: none">
        <div class="label">
            <form:label path="studies">Studies</form:label>
        </div>
        <div class="value">
            <form:select path="studies" multiple="true">
                <form:options items="${studies}" itemLabel="name" itemValue="id"/>
            </form:select>
        </div>
        <div class="value">
            <input type="button" id="studySelectorFormBack" value="Back"/><input type="button" id="studySelectorFormNext" value="Next"/>            
        </div>
    </div>    
    
    <div id="participantSelectorForm" class="row" style="display: none">
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
    
    <div id="dateRangeSelectorForm" style="display: none">
	    <div class="row">
	        <div class="label">
	            <label for="startTimeSelector">Start Time</label>
	        </div>
	        <div class="value">
	            <form:input path="startDate"/>
	        </div>
	    </div>
	             
	    <div class="row">
	        <div class="label">
	            <label for="endTimeSelector">End Time</label>
	        </div>
	        <div class="value">
	            <form:input path="endDate"/>
	        </div>
	    </div>
	
	    <div class="value">
	        <input type="button" id="dateRangeSelectorFormBack" value="Back"/><input type="button" id="dateRangeSelectorFormFinish" value="Finish"/>            
	    </div>
	</div>
	
	<div id="reportBuilderFormSubmit" class="row" style="display: none">
		<div>You selected the following filters:</div>
		<div>Sites:</div>
		<div>Studies:</div>
		<div>Participants:</div>
		<div>Occuring from: to: </div>		
        <div class="submit">
            <input type="submit" value="Report"/>
        </div>
    </div>
</form:form>
</body>
</html>