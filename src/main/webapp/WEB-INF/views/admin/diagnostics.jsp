<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <style type="text/css">
        dignostics {
            width: 2em;
        }

        div.row div.label {
            width: 10em;
        }

        div.row div.value {
            margin-left: 12em;
        }

        div.tip {
            width: 50em;
            float: right;
        }


    </style>
</head>
<body>
<div class="dignostics">

<laf:box title="External Applications">
    <c:choose>
        <c:when test="${configuration.externalAppsConfigured}">

            <c:set var="caaersAvail" value="${not empty configuration.map.caAERSBaseUrl}"/>
            <c:set var="labViewerAvail" value="${not empty configuration.map.labViewerBaseUrl}"/>
            <c:set var="ctmsAvail" value="${not empty configuration.map.patientPageUrl}"/>

            <div class="row odd">

                <div class="label"> caAERS Url
                </div>
                <div class="tip">
                    example: http://localhost:8080/caaers/pages/ae/list?assignment={assignment_identifier}
                </div>

                <c:choose>
                    <c:when test="${caaersAvail}">
                        <div class="value">
                            <tags:urlFromTemplate property="caaersAvail"></tags:urlFromTemplate>
                        </div>

                    </c:when>
                    <c:otherwise>
                        <div class="value error">
                            caAERS URL is not configured.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="row even">

                <div class="label"> LabViewer base URL
                </div>
                <div class="tip">
                    example:
                    https://cbvapp-d1017.nci.nih.gov:28443/ctodslabviewer/studysubject.do?studySubjectGridId={assignment-identifier}
                </div>

                <c:choose>
                    <c:when test="${labViewerAvail}">
                        <div class="value">
                            <tags:urlFromTemplate property="labViewerBaseUrl"></tags:urlFromTemplate>
                        </div>

                    </c:when>
                    <c:otherwise>
                        <div class="value error">
                            LabViewer base URL is not configured.
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="value error">
                No external application is configured.
            </div>
        </c:otherwise>
    </c:choose>


</laf:box>
<laf:box title="Email Configurations">
        <c:choose>
            <c:when test="${command.smtpException ne null}">
<div class="row even">
<div class="level error"> Problem while sending email:   </div>

    <div class="value">${command.smtpException}</div>

                                                                                       </div>
            </c:when>
            <c:otherwise> Email Configuration is correct.</c:otherwise>
        </c:choose>
</laf:box>
</body>
</html>