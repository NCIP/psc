<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@attribute name="value" type="java.util.Date" required="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="origPattern" value="${configuration.map.displayDateFormat}"/>
<c:set var="pattern" value="${fn:replace(fn:replace(origPattern, 'YYYY', 'yyyy'), 'DD', 'dd')}"/>

<fmt:formatDate value="${value}" pattern="${pattern}"/>