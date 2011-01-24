<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<html>
<head>
    <tags:stylesheetLink name="admin"/>
    <title>Global configuration</title>
</head>
<body>
<laf:box title="Configure application">
    <form:form>
        <form:errors path="*"/>
        <c:forEach items="${command.conf}" var="entry" varStatus="status">
            <div class="row ${commons:parity(status.count)}">
                <div class="label">
                    <form:label path="conf[${entry.key}].value">${entry.value.property.name}</form:label>
                </div>
                <div class="value">
                    <admin:configurationInput configEntry="${entry.value}" configEntryPath="conf[${entry.key}].value"/>
                </div>
            </div>
        </c:forEach>
        <div class="row submit">
            <input type="submit" value="Save"/>
        </div>
    </form:form>
</laf:box>
</body>
</html>