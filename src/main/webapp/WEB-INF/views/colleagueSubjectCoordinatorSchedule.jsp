<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<html>
<head>
    <tags:stylesheetLink name="main"/>

    <script type="text/javascript">

        function ajaxform() {
            var href = '<c:url value="/pages/dashboard/colleagueSubjectCoordinator?id=${userName.id}" />'
            // Set up data variable
            var formdata = "";
            var toDateTemp = 'toDate';
            formdata = formdata + toDateTemp+ "=" + $(toDateTemp).value+"&";

            var arrayOfCheckboxes = document.getElementsByName('activityTypes');

            for (var i = 0; i < arrayOfCheckboxes.length; i++) {
                formdata = formdata + 'activityTypes' + '[' +$(arrayOfCheckboxes[i]).value + ']'+  "=" + $(arrayOfCheckboxes[i]).checked + "&"
            }

            var lastRequest = new Ajax.Request(href,
            {
                postBody: formdata
            });
            return true;
        }

    </script>

    <style type="text/css">
        ul ul.controls {
            display: inline;
        }
        ul.controls li {
            display: inline;
        }
        ul li ul.controls li {
            margin: 0;
            padding: 3px;
        }
        ul.menu {
            margin: 0;
            padding: 0;
        }
        ul.menu li {
            padding: 0.5em;
            list-style-type: none;
            margin: 0.5em;
        }
        ul.menu li.subject {
            padding-left: 4em;
            padding-right: 0em;
            padding-top:0em;
            padding-bottom:0em;
            margin:0em;
            list-style-type: none;
            /*padding-bottom: 1em;*/
        }
        ul.menu li.controls {
            padding-left: 2em;
            list-style-type: none;
            margin: 0em;
            padding-bottom:0em;
            padding-top:0em;
        }
        h2 {
            margin-top: 2em;
        }

        input.checkboxes {
            margin-left:10px;
            vertical-align:middle;
        }
        .day h3{
            width:10%;
        }

        tr, td {
            vertical-align:top;
        }

        ul.menu li.colorAndPadding {
            padding: 0.5em;
            list-style-type: none;
            margin: 0.5em;
            color:red;
        }
    </style>
</head>
<body>
    <div class="main">
        <h1>Dashboard for the Colleague, ${userName.name}</h1>
    </div>
    <dash:pastDueActivities activities="${pastDueActivities}"/>
    <laf:box title="Current activities">
            <ul class="menu">
                <li class="autoclear">
                     Activities for the next <input value="7" path="toDate" id="toDate" size="5" onchange="ajaxform();" /> days
                </li>
                <li>
                    Filter by Activity Type:
                    <c:forEach items="${activityTypes}" var="activityType">
                        <input TYPE=checkbox class="checkboxes" value="${activityType.id}" id="checkboxId" name="activityTypes" checked="true" onchange="ajaxform();"> ${activityType.name} </input>
                    </c:forEach>
                </li>
                <li class="autoclear" id="subject-schedule">
                     <dash:subjectCoordinatorSchedule/>
                </li>
            </ul>
    </laf:box>
    <ul class="menu">
            <li class="colorAndPadding">
                ${extraSites}
            </li>
        </ul>
</body>
</html>