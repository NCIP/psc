<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
    <title>${action} Participant Coordinators</title>
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
    <script> 
    function selSwitch(btn)
		{
		   var i= btnType = 0;
		   var isavailableIds = doIt = false;
		
		   if (btn.value == "Assign" || btn.value == "Remove") 
		      btnType = 1;
		   else if (btn.value == "Assign All" || btn.value == "Remove All") 
		      btnType = 2;
		   else
		      btnType = 3;
		
	      isavailableIds = (btn.value.indexOf('Assign') != -1) ? true : false;     
	
	      with ( ((isavailableIds)? document.forms[0].availableCoordinators: document.forms[0].assignedCoordinators) )
	      {
	         for (i = 0; i < length; i++)
	         {
	            doIt = false;
	            if (btnType == 1)
	            { 
	               if(options[i].selected) doIt = true;
	            }
	            else if (btnType == 2)
	            {
	               doIt = true;
	            } 
	            else 
	               if (!options[i].selected) doIt = true;
	             
	            if (doIt)
	            {
	               with (options[i])
	               {
	                  if (isavailableIds)
	                     document.forms[0].assignedCoordinators.options[document.forms[0].assignedCoordinators.length] = new Option( text, value );
	                  else
	                     document.forms[0].availableCoordinators.options[document.forms[0].availableCoordinators.length] = new Option( text, value );
	               } 
	               options[i] = null;
	               i--;
	            } 
	         } // end for loop
	         if (options[0] != null)
	            options[0].selected = true;
	      } // end with isavailableIds
		}    // -->
	</script>	
</head>
<body>
<h1>${action} Participant Coordinators</h1>
<p>
    Study: ${study.name}
</p>
<c:url value="/pages/assignParticipantCoordinator?id=${study.id}" var="formAction"/>

<form:form action="${formAction}" method="post">
<input type="hidden" name="studyId" value="${study.id}"/>
    <div class="row">
        <div class="label">
            <form:label path="availableCoordinators">Available Participant Coordinators</form:label>
        </div>
        <div class="value">
            <form:select path="availableCoordinators" multiple="true">
                <form:options items="${availableUsers}" itemLabel="firstName" itemValue="userId"/>
            </form:select>
        </div>
    </div>
    <div class="row">
        <div class="value submit">
            <input type="button" value="Assign" style="width:75px;" onclick="selSwitch(this);">  
            <input type="button" value="Remove" style="width:75px;" onclick="selSwitch(this);">
        </div>

    </div>
    <div class="row">
        <div class="label">
            <form:label path="assignedCoordinators">Assigned Participant Coordinators</form:label>
        </div>
        <div class="value">
            <form:select path="assignedCoordinators" multiple="true">
                <form:options items="${assignedUsers}" itemLabel="firstName" itemValue="userId" />
            </form:select>
        </div>
    </div>
    <div class="row">
        <div class="submit">
            <input type="submit" value="Update Associations"/>
        </div>
    </div>
</form:form>
</body>
</html>