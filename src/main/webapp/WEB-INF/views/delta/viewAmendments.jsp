<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
<head>
    <title>View amendments</title>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        var INITIAL_AMENDMENT_ID = "amendment-${amendment.id}"

        function selectAmendment(id) {
            $$('.amendment').each(function(div) {
                if (div.visible() && div.id != id) {
                    SC.slideAndHide(div)
                }
            })
            SC.slideAndShow(id)
            document.location.hash = id.sub('-', '=')
        }

        Event.observe(window, "load", function() {
            $$("#amendments-list a").each(function(a) {
                var idToSelect = a.getAttribute("href").substr(1)
                a.observe("click", function(evt) {
                    selectAmendment(idToSelect)
                    Event.stop(evt)
                })
            })
            if (document.location.hash && document.location.hash.startsWith("#amendment")) {
                selectAmendment(document.location.hash.substr(1).sub('=', '-'))
            } else {
                selectAmendment(INITIAL_AMENDMENT_ID)
            }
        })
    </script>
</head>
<body>
<h1>Amendments for ${study.name}</h1>
<p>
    Select an amendment to view the changes associated with it.  Amendments are listed in
    the reverse of the order in which they were applied (i.e., the most recent is listed
    first).
</p>
<ul id="amendments-list">
    <c:if test="${not empty dev}">
        <li><a href="#amendment-${dev.amendment.id}">${dev.amendment.displayName}</a> (in development)</li>
    </c:if>
    <c:forEach items="${amendments}" var="a">
        <li><a href="#amendment-${a.amendment.id}">${a.amendment.displayName}</a></li>
    </c:forEach>
</ul>

<c:if test="${not empty dev}">
    <tags:viewAmendment view="${dev}" style="display: none"/>
</c:if>

<c:forEach items="${amendments}" var="a">
    <tags:viewAmendment view="${a}" style="display: none"/>
</c:forEach>

</body>
</html>