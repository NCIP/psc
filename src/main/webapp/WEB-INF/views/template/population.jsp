<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<c:set var="title">${command.edit ? 'Edit' : 'Create'} population ${command.edit ? command.population.name : ''}</c:set>
<html>
<head>
    <title>${title}</title>
    <tags:stylesheetLink name="main"/>
    <script type="text/javascript">
        function suggest(e) {
            Event.stop(e)
            var name = $('population.name').value
            if (name.empty()) {
                alert("Please enter a name first")
                return;
            }
            var href = $('suggest-control').href + '&populationName=' + name
            $('suggest-indicator').reveal()
            new Ajax.Request(href, {
                asynchronous: true,
                onComplete: function() {
                    $('suggest-indicator').conceal()
                }
            })
        }

        Event.observe(window, 'load', function() {
            Event.observe('suggest-control', 'click', suggest)
        })
    </script>
</head>
<body>
<laf:box title="${title}" autopad="true">
    <c:if test="${!study.released}">

        <p>
            You are ${command.edit ? 'editing' : 'creating'} a new subject population for
                ${command.population.study.assignedIdentifier}. A <strong>population</strong> is
            a group of subjects who need one or more additional activities automatically added
            to their schedules. A subject can become a member of a population (or stop being
            a member of a population) at any point in their participation in the study &mdash;
            either at enrollment or any later time.
        </p>
        <form:form>
            <tags:errors/>
            <div class="row">
                <div class="label"><form:label path="population.name">Name</form:label></div>
                <div class="value">
                    <form:input path="population.name" size="30"/>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="population.abbreviation">Abbreviation</form:label></div>
                <div class="value">
                    <form:input path="population.abbreviation" size="6"/>&nbsp;<a id="suggest-control" class="control"
                                                                                  href="<c:url value="/pages/cal/template/population/suggest?study=${command.population.study.id}"/>">Suggest</a>&nbsp;<tags:activityIndicator
                        id="suggest-indicator"/>
                    <p class="tip">
                        The abbreviation can be up to 5 characters long, but 1 or 2 is best.
                        It can't be one that's already used for another population in this study.
                        If you don't specify an abbreviation, PSC will assign one for you.
                        (The one it assigns will be the same as the one you get by pressing
                        the suggest button.)
                    </p>
                </div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </c:if>
    <c:if test="${study.released}">
        You can not edit this population as population can only be edited when the template is "in design".
        <form:form>
            <div class="row">
                <div class="label"><form:label path="population.name">Name</form:label></div>
                <div class="value">${population.name}</div>
            </div>
            <div class="row">
                <div class="label"><form:label path="population.abbreviation">Abbreviation</form:label></div>
                <div class="value">${population.abbreviation}
                </div>
            </div>
        </form:form>
    </c:if>

</laf:box>
</body>
</html>