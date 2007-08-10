<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
<head>
</head>
<body>
<laf:box title="Mark template for ${study.name} complete">
<p>You are about to mark the ${study.name} template complete. Are you sure you want to do this?</p>
<form:form method="post">
   <input type="hidden" name="studyId" value="${study.id}"/>
   <div class="row">
        <div class="label">
            Mark complete?
        </div>
        <div class="value">
            <label><input type="radio" id="complete-yes" name="completed" value="true"  <c:if test="${    command.study.plannedCalendar.complete}">checked="checked"</c:if>/> Yes</label>
            <label><input type="radio" id="complete-no"  name="completed" value="false" <c:if test="${not command.study.plannedCalendar.complete}">checked="checked"</c:if>/> No</label>
        </div>
    </div>
    <div class="row">
        <div class="value submit">
            <input type="submit" value="Submit"/>
        </div>
    </div>
</form:form>
</laf:box>
</body>
</html>