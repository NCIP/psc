<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<html>
<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="dashboards"/>

    <script type="text/javascript">
        SC.registerCurrentActivitiesUpdaters('<c:url value="/pages/dashboard/colleagueSubjectCoordinator?id=${userName.id}"/>')
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

        input.activity-type {
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
    <dash:currentActivities activityTypes="${activityTypes}" numberOfDays="7"/>
    <ul class="menu">
            <li class="colorAndPadding">
                ${extraSites}
            </li>
        </ul>
</body>
</html>