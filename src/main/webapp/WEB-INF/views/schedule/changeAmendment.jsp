<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<c:set var="title">Change amendment for ${subject.fullName} on ${study.assignedIdentifier}</c:set>
<html>
<head>
    <title>Change amendment</title>
    <style type="text/css">
        #approvals .approval {
            text-align: right;
        }
    </style>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        function checkIntervening(event) {
            var checkedInput = Event.element(event)
            if (!checkedInput.checked) return;
            var checkedInputIndex = extractIndex(checkedInput)
            var anyChanged = false;
            $$(".amendment-checkbox").each(function(input) {
                if (!input.checked) {
                    var candidateIndex = extractIndex(input)
                    if (candidateIndex < checkedInputIndex) {
                        anyChanged = true;
                        input.checked = true
                        SC.highlight(input.parentNode)
                    }
                }
            })
            if (anyChanged) {
                warn("Amendments before " + amendmentName(event) + " automatically selected.")
            }
        }

        function uncheckLater(event) {
            var uncheckedInput = Event.element(event)
            if (uncheckedInput.checked) return;
            var uncheckedInputIndex = extractIndex(uncheckedInput)
            var anyChanged = false;
            $$(".amendment-checkbox").each(function(input) {
                if (input.checked) {
                    var candidateIndex = extractIndex(input)
                    if (candidateIndex > uncheckedInputIndex) {
                        anyChanged = true
                        input.checked = false
                        SC.highlight(input.parentNode)
                    }
                }
            })
            if (anyChanged) {
                warn("Checked amendments after " + amendmentName(event) + " automatically deselected.")
            }
        }

        function amendmentName(event) {
            return Event.findElement(event, 'tr').getElementsBySelector('.amendment-name')[0].innerHTML;
        }

        function warn(detail) {
            $('warn-detail').innerHTML = detail;
            SC.highlight($('warning'))
        }

        function extractIndex(approvalCheckbox) {
            return +(approvalCheckbox.id.split("-", 3)[2])
        }

        Event.observe(window, "load", function() {
            $$(".amendment-checkbox").each(function(input) {
                Event.observe(input, "change", checkIntervening)
                Event.observe(input, "change", uncheckLater)
            })
        })
    </script>
</head>
<body>
<laf:box title="${title}">
    <laf:division>
        <p>
            You are updating the amendment for ${subject.fullName} on ${study.name} at ${site.name}.
            This subject is currently on ${assignment.currentAmendment.displayName}.
        </p>
        <p>
            You cannot reverse any changes you make here.  Please be sure that you
            have the necessary consent before proceeding.
        </p>
        <p id="warning">
            You apply the amendments in chronological order.  You may
            apply more than one at a time, but you may not leave any gaps.
            <strong id="warn-detail"></strong>
        </p>
        <form:form method="post">
            <table id="amendments" class="grid">
                <tr>
                    <th>Select</th>
                    <th>Amendment</th>
                </tr>
                <c:forEach items="${command.amendments}" var="entry" varStatus="status">
                    <tr>
                        <td class="approve">
                            <form:checkbox cssClass="amendment-checkbox" id="amendment-checkbox-${status.index}" path="amendments[${entry.key.id}]" value="true"/>
                        </td>
                        <td class="amendment-name">
                            ${entry.key.displayName}
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <div class="row">
                <div class="value submit">
                    <input type="submit" value="Update to selected amendment level"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>