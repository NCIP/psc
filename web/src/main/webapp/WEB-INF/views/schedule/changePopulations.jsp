<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="commons" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions" %>
<html>
<head>
    <title>Change populations</title>
    <style type="text/css">
        #population-checkboxes li {
            list-style-type: none
        }
    </style>
</head>
<body>
<c:set var="title">Change populations for ${subject.fullName} on ${study.assignedIdentifier}</c:set>
<laf:box title="${title}">
    <laf:division>
        <p>
            You are selecting the special populations of which ${subject.fullName} should be considered
            a part.  When you add a subject to a new population, any activities for the current study
            segment will be added to the subject's schedule.  As new study segments are added, the
            activities for the selected populations will be added in addition to the activities that
            are shared by all subjects.
        </p><p>
            If you remove a subject from a population, any outstanding activities that are
            special to that population will be canceled and no future ones will be scheduled.
        </p>
    </laf:division>

    <h3>Populations</h3>

    <laf:division>
        <form:form>
            <ul id="population-checkboxes">
            <c:forEach items="${study.populations}" var="pop">
                <li><label>
                    <input name="populations" type="checkbox" value="${pop.id}" ${commons:contains(command.populations, pop) ? 'checked="checked"' : ''} />
                    <input name="_populations" type="hidden" value="on"/>
                    ${pop.name}
                </label></li>
            </c:forEach>
            </ul>

            <input type="submit" value="Save"/>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>