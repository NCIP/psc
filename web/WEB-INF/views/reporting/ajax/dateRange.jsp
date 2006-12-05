<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

$('startDateDisplay').innerHTML = "${fromSelected}"
$('endDateDisplay').innerHTML = "${toSelected}"

$('startDate').value = "${fromSelected}"
$('endDate').value = "${toSelected}"

SC.slideAndShow('generateReport')