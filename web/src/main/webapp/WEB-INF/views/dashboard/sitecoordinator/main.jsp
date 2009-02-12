<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>

<html>
<head>
    <title>Site coordinator dashboard</title>
    <tags:stylesheetLink name="main"/>

    <style type="text/css">
        #notices {
            padding: 0; margin: 0 2em;
        }
        #notices li {
            padding: 0.5em 0;
        }

        th.header-of-headers {
            font-weight: normal;
        }
    </style>
</head>
<body>

<h1>Welcome, ${user.name}</h1>

<c:if test="${not empty notices['approvals']}">
    <laf:box title="Please note" autopad="true">
        <ul id="notices">
        <c:forEach items="${notices['approvals']}" var="notice">
            <li>
                ${notice.amendment.initialTemplate ? '' : 'Amendment'} ${notice.amendment.displayName} of ${notice.studySite.study.assignedIdentifier} needs to be
                <a class="control" href="<c:url value="/pages/cal/template/approve?studySite=${notice.studySite.id}"/>">approved</a>
                for ${notice.studySite.site.name}.
            </li>
        </c:forEach>
        </ul>
    </laf:box>
</c:if>

<laf:box title="Manage subject coordinators" autopad="true">
    <%-- TODO: merge the view for this into this box, instead of linking out --%>
    As a site coordinator, you can <a class="control" href="<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy"/>">manage</a>
    the visibility of studies to individual subject coordinators.  You can also
    <a class="control" href="<c:url value="/pages/dashboard/siteCoordinator/assignSubjectToSubjectCoordinatorByUser"/>">change</a>
    the primary subject coordinator for individual subjects.
</laf:box>


</body>
</html>