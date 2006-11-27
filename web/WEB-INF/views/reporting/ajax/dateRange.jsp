<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

$('fromFilter').value = ${fromSelected}
$('toFilter').value = ${toSelected}

SC.slideAndShow('generateReport')