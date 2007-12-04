<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
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

<c:if test="${not empty notices}">
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
    <a class="control" href="<c:url value="pages/dashboard/siteCoordinator/assignSubjectToSubjectCoordinatorByUser"/>">change</a>
    the primary subject coordinator for individual subjects.
</laf:box>

<laf:box title="Studies" autopad="true">
    <p>
        This table contains all the studies that are have been released to the sites of which
        you are a site coordinator.
    </p>
    <table id="studies" class="grid">
        <tr>
            <th class="header-of-headers">Studies / Sites</th>
            <c:forEach items="${sites}" var="site">
                <th>${site.name}</th>
            </c:forEach>
        </tr>
        <c:forEach items="${studiesAndSites}" var="studyToStudySite" varStatus="status">
            <tr class="${commons:parity(status.index)}">
                <th>${studyToStudySite.key.name}</th>
                <c:forEach items="${studyToStudySite.value}" var="siteToStudySite">
                    <td>
                    <c:if test="${not empty siteToStudySite.value}">
                        <a class="control" href="<c:url value="/pages/cal/template/approve?studySite=${siteToStudySite.value.id}"/>">Approve amendments</a>
                    </c:if>
                    <c:if test="${empty siteToStudySite.value}">
                        <em>Not available at this site</em>
                    </c:if>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
</laf:box>

</body>
</html>