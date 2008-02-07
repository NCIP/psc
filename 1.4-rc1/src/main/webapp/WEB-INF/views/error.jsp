<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
    <head>
        <title>Error - ${statusName}</title>
        <laf:stylesheetLink name="debug"/>
        <laf:stylesheetLink name="common"/>
        <tags:stylesheetLink name="common"/>
        <style type="text/css">
            body {
                margin: 1em ;
            }

            .stacktrace {
                font-family: 'Andale Mono', Monaco, monospace;
            }

            .stacktrace div {
                padding: 0 0 0 2em ;
                text-indent: -2em ;
                margin: 0 2em ;
            }

            .stacktrace .message {
                font-weight: bold ;
            }

            .stacktrace .trace {
                padding-left: 4em ;
            }

            .stacktrace .causedby {
                border-top: 1px dotted gray ;
                margin-top: 0.5em ;
            }
        </style>
    </head>
    <body>
    <c:set var="showDetail" value="${configuration.map.showFullExceptions}"/>
    <h1>${statusName}</h1>
    <p>${message}</p>

    <c:choose>
        <c:when test="${notified}">
            <p>
                ${showDetail ? 'The following information' : 'Information'} about this error has
                been mailed to the system administrator(s).  If you can provide any additional
                context, please contact them.
            </p>
        </c:when>
        <c:otherwise>
            <p>
                If you see this error repeatedly, please contact the system administrators.
            </p>
        </c:otherwise>
    </c:choose>

    <c:if test="${showDetail}">
        ${stackTrace}
    
        <laf:debugInfo/>
    </c:if>

    </body>
</html>