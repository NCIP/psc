<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>


<%@taglib prefix="commons1" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>

<html>
<title>Activities</title>
<head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:stylesheetLink name="report"/>

    <%--<tags:stylesheetLink name="report" dynamic="true"/>--%>
    <style type="text/css">
        table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>);
         background-position: left ; display: block}
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
            background-color: #ddd;
        }

        #activityName label {
            margin-left:10em;
        }

        #createNewActivity label {
            margin-bottom:1em;
        }

        #exportOptions {
            padding: 0 1em 0 1em;
        }

        .underlined {
            text-decoration:underline;
            display:none
        }

        #bottomsTop input.pageNumberSelected, #bottomsBottom input.pageNumberSelected {
            background-color:lightskyblue;
        }

        #bottomsTop input.pageNumberDefault, #bottomsBottom input.pageNumberDefault {
            background-color:#3876C1;
        }

    </style>

    <script type="text/javascript">

        var activitiesAutocompleter;

        function initMethods() {
            var indicator = $('myIndicator')
            indicator.conceal()
            var input = $('sources').options[$('sources').selectedIndex].value
            if (input == null || input == "select") {
                $('sources').selectedIndex = 0
            }
            registerEventHandlers()
            enableExportOptions()
        }


        function registerEventHandlers() {
            var input = $('addSourceButton')
            input.observe('click', function() {
                executeAddSource(input)
            })

            var exportActivitiesLinkXML = $('exportActivitiesLinkXML')
            exportActivitiesLinkXML.observe('click', function(){
                exportActivitiesToXML(".xml")
            })

            var exportActivitiesLinkCSV = $('exportActivitiesLinkCSV')
            exportActivitiesLinkCSV.observe('click', function(){
                exportActivitiesToXML(".csv")
            })

            var exportActivitiesLinkXLS = $('exportActivitiesLinkXLS')
            exportActivitiesLinkXLS.observe('click', function(){
                exportActivitiesToXML(".xls")
            })
        }

        function displayErrorOnFailure(response, indicator){
            indicator.conceal()
            var divError = $('errors')
            var myDIV = document.createElement('div');
            myDIV.innerHTML=response.responseText;
            var errorFromResponse = Builder.node('h4', {id:'error'}, "Error occurs during the request process. Status is "+ response.getStatus() + ". Caused by " + myDIV.getElementsByClassName('message')[0].innerHTML);
            divError.appendChild(errorFromResponse)
        }

        function executeAddSource(input) {
            var data = '';
            data = data+"source"+"="+$('addSource').value+"&";
            var href = '<c:url value="/pages/activities/addSource"/>';
            href= href+"?"+data;
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post',
                onFailure: function(response) {
                    displayErrorOnFailure(response, $('myIndicator'))
                }
            });
            $('addSource').value =""
        }

        function loadActivities(){
            var indicator = $('myIndicator')
            indicator.reveal()
            var input = $('sources').options[$('sources').selectedIndex].value
            var data = ''
            if (input != "selectAll") {
             data = data+"sourceId"+"="+input;
            }
            data = data+"&index=0";
            var href = '<c:url value="/pages/activities/getActivityBySource"/>'
            href= href+"?"+data
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post',
                onSuccess: enableExportOptions(),
                onFailure: function(response) {
                    displayErrorOnFailure(response, indicator)
                }
            });
        }

        function enableExportOptions(){
            var selectedSourceId = $('sources').options[$('sources').selectedIndex].value
            if(selectedSourceId == "select" || selectedSourceId == "selectAll") {
                $('exportOptions').style.display = "none"
                $('exportActivitiesLinkXML').style.display = "none"
                $('exportActivitiesLinkCSV').style.display = "none"
                $('exportActivitiesLinkXLS').style.display = "none"
            } else {
                $('exportOptions').style.display = "inline"
                $('exportActivitiesLinkXML').style.display = "inline"
                $('exportActivitiesLinkCSV').style.display = "inline"
                $('exportActivitiesLinkXLS').style.display = "inline"
            }
        }

        function editActivity(activityId) {
            var inputName = 'InputName'+activityId
            var labelName = 'Name'+activityId
            $(inputName).show()
            $(labelName).hide();


            var inputCode = 'InputCode'+activityId
            var labelCode = 'Code'+activityId
            $(inputCode).show()
            $(labelCode).hide();

            var inputDescription = 'InputDescription'+activityId
            var labelDescription = 'Description'+activityId
            $(inputDescription).show()
            $(labelDescription).hide();

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
            var indicator = $('myIndicator')
            indicator.reveal()
            var inputName = 'InputName'+activityId
            var labelName = 'Name'+activityId
            var inputCode = 'InputCode'+activityId
            var labelCode = 'Code'+activityId
            var divType ='DivType'+activityId
            var selectType = 'SourceTypes'+activityId
            var labelType = 'Type'+activityId
            var labelDescription = "Description"+activityId
            var inputDescription = "InputDescription"+activityId
            var sourceId = $('sources').options[$('sources').selectedIndex].value

            var data = ''
            data = data+"activityId="+activityId+"&";
            data = data+"activityName="+$(inputName).value+"&";
            data = data+"activityCode="+$(inputCode).value+"&";
            data = data+"activityDescription="+$(inputDescription).value+"&";
            data = data+"activityType="+$(selectType).options[$(selectType).selectedIndex].value+"&";
            data = data+"sourceId"+"="+sourceId+"&";
            var href = '<c:url value="/pages/activities/saveActivity"/>'
            href= href+"?"+data
            if($(inputName).value !="" && $(inputCode).value !="") {
            var saveRequest = new Ajax.Request(href,
            {
                method: 'post',
                onFailure: function(response) {
                    displayErrorOnFailure(response, indicator)
                }
            });
            }
            else {
            $('errors').innerHTML = "Activity name or code can not be empty";
            }
            indicator.conceal()
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
            var indicator = $('myIndicator')
            indicator.reveal()
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
                method: 'post',
                onFailure: function(response) {
                    displayErrorOnFailure(response, indicator)
                }
            })

            return true;
        }

        function deleteTheActivity(activityId) {
            var indicator = $('myIndicator')
            indicator.reveal()
            var inputName = 'InputName'+activityId
            var inputCode = 'InputCode'+activityId
            var selectType = 'SourceTypes'+activityId

            var href = '<c:url value="/pages/activities/deleteActivity"/>'
            var data = "";

            data = data+"activityId"+"="+activityId+"&";

            href= href+"?"+data
            var deleteRequest = new Ajax.Request(href,
            {
                method: 'post',
                onFailure: function(response) {
                    displayErrorOnFailure(response, indicator)
                }
            });

            return true;
        }


        function displayNext(index) {
            var indicator = $('myIndicator')
            indicator.reveal()
            var input = $('sources').options[$('sources').selectedIndex].value
            var data = ''
            data = data+"sourceId"+"="+input+"&";

            data = data+"index="+ index+"&";

            //need to parse location.href to get sorting parameters...
            var arrayOfHrefParams = location.href.split('&');
            for (var i = 0; i< arrayOfHrefParams.length; i++){
                var pair = arrayOfHrefParams[i];
                if (pair.startsWith('d')){
                    data = data+ pair+"&"
                }
            }
            var href = '<c:url value="/pages/activities/getActivityBySource"/>'
            href= href+"?"+data
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post',
                onFailure: function(response) {
                    displayErrorOnFailure(response, indicator)
                }
            });
        }

        function exportActivitiesToXML(extension) {
            var activitySource= $('sources').options[$('sources').selectedIndex].value

            var href = '<c:url value="/pages/activities/display/"/>'
            var data = activitySource+extension;

            location.href = href+data;
        }

        Event.observe(window, "load", initMethods)


    </script>
</head>
<body>
    <laf:box title="Activities">
        <laf:division>
             <div id="activities-input">
                <img id="myIndicator" src="<c:url value="/images/indicator.white.gif"/>"/>
                <label for="source">Source:</label>
                <select id="sources" name="sources" onchange="loadActivities()">
                    <option value="select">Select... </option>
                    <c:forEach items="${sources}" var="source">
                        <c:if test="${sourceId == source.id}">
                            <option value="${source.id}" selected="true">${source.name}<c:if test="${source.manualFlag}"> (Manual Target) </c:if></option>
                        </c:if>
                        <c:if test="${sourceId != source.id}">
                            <option value="${source.id}">${source.name}<c:if test="${source.manualFlag}"> (Manual Target) </c:if></option>
                        </c:if>
                    </c:forEach>
                    <option value="selectAll">All sources</option>
                </select>

                <label id="add-new-source">Create new source:</label>
                <input id="addSource" type="text" class="addSource" value=""/>
                <input type="button" id="addSourceButton" name="addSourceButton" value="Add" />
                <a id="importActivitiesLink" href="<c:url value="/pages/activities/importActivities"/>" >Import activities from xml or from csv</a>

                <span id="exportOptions" style="display:none"> Export Options:</span>
                <a id="exportActivitiesLinkXML" class="underlined">XML</a>
                <a id="exportActivitiesLinkCSV" class="underlined">CSV</a>
                <a id="exportActivitiesLinkXLS" class="underlined">Excel</a>


            </div>

            <div id="errors" style="margin-right:10px; margin-left:0.5em;">
                <form:errors path="*"/>
            </div>
            <div id="newActivity" style="margin-bottom:10px;"></div>
            <div id="bottomsTop">

            </div>
            <div id="myTable">
                <tags:activitiesTable/>
                <script><tags:addNewActivityRow/></script>
            </div>

            <div id="bottomsBottom">
            </div>


    </laf:division>
    </laf:box>
</body>
</html>