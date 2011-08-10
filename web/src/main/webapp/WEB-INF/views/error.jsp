<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
    <head>
        <title>Error - ${statusName}</title>
        <tags:stylesheetLink name="debug"/>
        <tags:stylesheetLink name="common"/>
        <tags:javascriptLink name="prototype"/>
        <style type="text/css">
            body {
                margin: 1em ;
            }

            #main {
                margin: auto;
                max-width: 720px
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

            #links {
                position: absolute; top: 0; right: 0;
                padding: 6px 10px 0 20px;
                white-space: nowrap;
            }

            #links a { font-weight: bold; }
        </style>
        <script type="text/javascript">
            document.observe("dom:loaded", function() {
                $('show-details').observe('click', function(e) {
                    Event.stop(e)
                    $('error-details').show()
                    $('show-details').hide();
                })
            })
        </script>

        <c:if test="${not empty redirectToSwitchboardAfter}">
            <meta http-equiv="Refresh" content="${redirectToSwitchboardAfter}; <c:url value="/pages/switchboard"/>"/>
        </c:if>
    </head>
    <body>
    <div id="links">
        <a href="<c:url value="/"/>" id="home">Home</a>
        | <a href="<c:url value="/auth/logout"/>" id="logout">Log out</a>
    </div>
    <c:set var="showDetail" value="${configuration.map.showFullExceptions}"/>
    <div id="main">
        <h1>${statusName}</h1>
        <p>${message}</p>

        <c:if test="${not empty redirectToSwitchboardAfter}">
            <p>
                You don't have permission to see the requested page. You will be <strong>returned to your
                <a href="<c:url value="/pages/switchboard"/>">default page</a></strong> in 20 seconds.
                Alternatively, you may be able to use your browser's Back button to return to the
                page you came from.
            </p>
            <p>
                If you arrived at this page by following a link from within PSC, please contact the
                system administrators and tell them what roles you have and what page you were trying to
                access. If you are a system administrator, you can look in PSC's log to see what
                role is necessary to access this page and which roles PSC thinks you have.
            </p>
        </c:if>

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

        <a href="#" id="show-details">Show technical details</a>
    </div>

    <div id="error-details" style="display: none">
        ${stackTrace}

        <laf:debugInfo/>
    </div>

    </body>
</html>