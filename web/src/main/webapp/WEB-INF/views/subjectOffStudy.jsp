<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<jsp:useBean id="assignment" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment"/>

<html>
<body>
<head>
    <title>Take ${assignment.subject.fullName} off of study ${assignment.studySite.study.assignedIdentifier}</title>
    <style type="text/css">
        .value p {
            margin-top: 0;
        }
    </style>
</head>
<laf:box title="Take Off Study">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    Subject:
                </div>
                <div class="value">
                    ${assignment.subject.fullName}
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Study:
                </div>
                <div class="value">
                    ${assignment.studySite.study.assignedIdentifier}
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Site:
                </div>
                <div class="value">
                    ${assignment.studySite.site.name}
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Please Note
                </div>
                <div class="value">
                    <p class="instructions">
                        This action will remove this subject from the study and cancel any
                        incomplete activities after the Off Study Date.
                    </p><p class="instructions">
                        If you want to stop <em>treatment</em> for this subject but continue
                        monitoring or follow-up activities per the study protocol:
                    </p><ol class="instructions">
                        <li>Do not take the subject off the study.
                        <li>Go to the <a href="<c:url value="/pages/subject?subject=${assignment.subject.id}"/>">subject's schedule</a>.
                        <li>Select the "Next segment" section of the "Modify schedule" box.
                        <li>Choose the appropriate monitoring or followup segment.
                        <li>Click "Immediately" and enter the actual date that treatment should end and the next segment should begin.
                    </ol><p class="instructions">
                        If you do wish to remove this subject from the study and all
                        protocol-related follow-up, complete the off study date and submit below.
                    </p>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Off Study Date: (mm/dd/yyyy)
                </div>
                <div class="value">
                    <laf:dateInput path="expectedEndDate"/>
                </div>
            </div>
            <div class="row">
                <div class="label"></div>
                <div class="value"><input type="submit" value="Submit"/></div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>