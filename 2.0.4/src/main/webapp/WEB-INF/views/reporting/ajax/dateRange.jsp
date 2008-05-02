<%@page contentType="text/javascript"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>

$('startDateDisplay').innerHTML = "${fromSelected}"
$('endDateDisplay').innerHTML = "${toSelected}"

<jsgen:insertHtml targetElement="reportBuilderForm" position="bottom">
<input type="hidden" name="startDate" value="${fromSelected}"/> 
<input type="hidden" name="endDate" value="${toSelected}"/> 
</jsgen:insertHtml>

SC.slideAndShow('generateReport')