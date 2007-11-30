<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <%--<title>${action} Subject</title>--%>
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
</head>
<body>
<laf:box title="Assign Subject">
    <laf:division>
        <p>
            Study: ${study.name}
        </p>

        <c:url value="/pages/cal/assignSubject" var="action"/>
        <form:form method="post" action="${action}">
            <form:errors path="*"/>
            <input type="hidden" name="study" value="${study.id}"/>
            <div class="row">
                <div class="label">
                    <form:label path="subject">Subject</form:label>
                </div>
                <div class="value">
                    <form:select path="subject">
                        <form:options items="${subjects}" itemLabel="fullName" itemValue="id"/>
                    </form:select>
                </div>
            </div>
            <p><a href="<c:url value="/pages/cal/createSubject?id=${study.id}"/>">Create New Subject</a></p>
                <div class="row">
                    <div class="label">
                        <form:label path="site">Site</form:label>
                    </div>
                    <div class="value">
                        <c:if test="${fn:length(sites) gt 1}">
                            <form:select path="site">
                                <form:options items="${sites}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </c:if>
                        <c:if test="${fn:length(sites) eq 1}">
                            ${sites[0].name}
                            <input type="hidden" name="site" value="${sites[0].id}"/>
                        </c:if>
                    </div>
                </div>

            <c:if test="${not empty studySegments}">
                <div class="row">
                    <div class="label">
                        <form:label path="studySegment">Select studySegment for first epoch</form:label>
                    </div>
                    <div class="value">
                        <form:select path="studySegment">
                            <form:options items="${studySegments}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    </div>
                </div>
            </c:if>

            <div class="row">
                <div class="label">
                    <form:label path="startDate">Start date of first epoch (mm/dd/yyyy)</form:label>
                </div>
                <div class="value">
                    <form:input path="startDate"/>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Assign"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>