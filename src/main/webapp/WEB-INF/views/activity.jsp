<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>


<%@taglib prefix="commons1" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>

<html>
<title>Activities</title>
<head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:stylesheetLink name="report"/>

    <%--<tags:stylesheetLink name="report" dynamic="true"/>--%>
    <style type="text/css">
        table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>) }
        table.query-results th.order1 a { background-image: url(<c:url value="/images/arrow_down.png"/>) }
        table.query-results th.order2 a { background-image: url(<c:url value="/images/arrow_up.png"/>) }


        a#newActivityLink {
            padding-left: 2em
        }

        #activities-input label {
            font-weight: bold;
        }

        #activities-input {
            margin: 1em 0;
            background-color: #ddd;
            padding: 0.5em;
        }

        #newActivity label {
            font-weight: bold;
        }

        #newActivity {
            /*margin: 1em 0;*/
            background-color: #ddd;
            /*padding: 0.5em;*/
        }

        #activityName label {
            margin-left:10em;    
        }

        #createNewActivity label {
            margin-bottom:1em;
        }


    </style>

    <script type="text/javascript">

        var activitiesAutocompleter;

        function initMethods() {
            registerEventHandlers()
        }


        function registerEventHandlers() {
            var input = $('addSourceButton')
            input.observe('click', function() {
                executeAddSource(input)
            })
        }


        function executeAddSource(input) {
            var data = ''
            data = data+"source"+"="+$('addSource').value+"&";
            var href = '<c:url value="/pages/activities/addSource"/>'
            console.log("=== data " + data)
            href= href+"?"+data
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post'
            });
            $('addSource').value =""
        }

        function loadActivities(){
            var input = $('sources').options[$('sources').selectedIndex].value
            var data = ''
            data = data+"sourceId"+"="+input+"&";
            var href = '<c:url value="/pages/activities/getActivityBySource"/>'
            href= href+"?"+data
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post'
            });
        }

        function editActivity(activityId) {
            var inputName = 'InputName'+activityId
            var labelName = 'Name'+activityId
            $(inputName).type = 'visible'
            $(labelName).hide();


            var inputCode = 'InputCode'+activityId
            var labelCode = 'Code'+activityId
            $(inputCode).type ='visible'
            $(labelCode).hide();


            var divType ='DivType'+activityId
            var labelType = 'Type'+activityId
            $(labelType).hide();
            $(divType).style.display='block'
            var selectType = 'SourceTypes'+activityId

            var editButton = 'Edit'+activityId
            $(editButton).style.display = 'none'

            var saveButton = 'Save'+activityId
            $(saveButton).style.display = "inline"
        }

        function saveActivity(activityId) {
            var inputName = 'InputName'+activityId
            var labelName = 'Name'+activityId
            var inputCode = 'InputCode'+activityId
            var labelCode = 'Code'+activityId
            var divType ='DivType'+activityId
            var selectType = 'SourceTypes'+activityId
            var labelType = 'Type'+activityId
            

            var data = ''
            data = data+"activityId"+"="+activityId+"&";
            data = data+"activityName"+"="+$(inputName).value+"&";
            data = data+"activityCode"+"="+$(inputCode).value+"&";
            data = data+"activityType"+"="+$(selectType).options[$(selectType).selectedIndex].value+"&";
            var href = '<c:url value="/pages/activities/saveActivity"/>'
            href= href+"?"+data
            var saveRequest = new Ajax.Request(href,
            {
                method: 'post',
                onComplete: function(t) {
                    $(labelName).innerHTML = $(inputName).value;
                    $(inputName).type ='hidden'
                    $(labelName).show()

                    $(labelCode).innerHTML = $(inputCode).value
                    $(inputCode).type ='hidden'
                    $(labelCode).show()

                    $(labelType).innerHTML = $(selectType).options[$(selectType).selectedIndex].innerHTML
                    $(labelType).show();
                    $(divType).style.display='none'

                    var editButton = 'Edit'+activityId
                    $(editButton).style.display="inline"
                    
                    var saveButton = 'Save'+activityId
                    $(saveButton).style.display="none"
                }
            });
        }


        function deleteActivity(activityId) {
            var labelName = 'Name'+activityId
            var labelCode = 'Code'+activityId
            var labelType = 'Type'+activityId

            var activityName = $(labelName).innerHTML
            var activityCode = $(labelCode).innerHTML
            var activityType = $(labelType).innerHTML

            var confirmMessage = "Are you sure you want to delete activity: [name= " + activityName +", type= " + activityType + ",code= " +
                                activityCode+ "] ? This will permanently remove it.  " +
                                "\n\nThis action cannot be undone."

            if (window.confirm(confirmMessage)) {
               deleteTheActivity(activityId)
                return true;
            } else {
              return false;
            }
        }


        function addNewActivity() {
            var activityName = $('addActivityName').value
            var activityType = $('types').options[$('types').selectedIndex].value
            var activityCode = $('addActivityCode').value
            var activityDescription = $('addActivityDescription').value
            var activitySource= $('sources').options[$('sources').selectedIndex].value

            var href = '<c:url value="/pages/activities/addNewActivity"/>'

            var data="";
            data= data+"activityName="+activityName+"&"
            data= data+"activityType="+activityType+"&"
            data= data+"activityCode="+activityCode+"&"
            data= data+"activityDescription="+activityDescription+"&"
            data= data+"activitySource="+activitySource+"&"
            href=href+"?"+data
            var newActivityReques = new Ajax.Request(href,
            {
                method: 'post'
            })

            return true;
        }

        function deleteTheActivity(activityId) {
            var inputName = 'InputName'+activityId
            var inputCode = 'InputCode'+activityId
            var selectType = 'SourceTypes'+activityId

            var href = '<c:url value="/pages/activities/deleteActivity"/>'
            var data = "";

            data = data+"activityId"+"="+activityId+"&";

            href= href+"?"+data
            var deleteRequest = new Ajax.Request(href,
            {
                method: 'post'
            });

            return true;
        }

        function importActivities() {
            var activitySource= $('sources').options[$('sources').selectedIndex].value
            var href = '<c:url value="/pages/activities/import"/>'
            var data = "";

            data = data+"sourceId"+"="+activitySource+"&";
            console.log("===== data " + data)
            href= href+"?"+data
//            var deleteRequest = new Ajax.Request(href,
//            {
//                method: 'post'
//            });
            return href;

        }


        Event.observe(window, "load", initMethods)


    </script>
</head>
<body>
    <laf:box title="Activities">
        <laf:division>
        <%-- this is needed to be uncomment for reconcileActivity --%>
        <%--<c:url value="/pages/activity/reconcileActivity" var="formAction"/>--%>
        <%--<form:form action="${formAction}" method="post">    --%>
             <div id="activities-input">
                <label for="source">Source:</label>
                <select id="sources" name="sources" onchange="loadActivities()">
                    <option value="select">Select... </option>
                    <c:forEach items="${sources}" var="source">
                        <option class="source" id="source" value="${source.id}">${source.name}</option>
                    </c:forEach>
                    <option value="selectAll">All sources</option>
                </select>

                <label id="add-new-source">Create new source:</label>
                <input id="addSource" type="text" class="addSource" value=""/>
                <input type="button" id="addSourceButton" name="addSourceButton" value="Add"/>
                <a id="importActivitiesLink" href="#" onclick="importActivities()">Import
                    activities from xml</a>

                 
            </div>

            <%--<br style="clear:both"/>--%>
            <div id="errors" style="margin-right:10px; margin-left:0.5em;">
                <form:errors path="*"/>
            </div>
            <div id="newActivity" style="margin-bottom:10px;"></div>
            <div id="myTable">
            </div>

        <%--</form:form>--%>

    </laf:division>
    </laf:box>
</body>
</html>