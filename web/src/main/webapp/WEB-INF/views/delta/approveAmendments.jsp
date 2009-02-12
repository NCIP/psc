<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
<head>
    <title>Approve amendments</title>
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
            $$(".approval-checkbox").each(function(input) {
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
                warn("Unapproved amendments before " + amendmentName(event) + " automatically selected.")
            }
        }

        function uncheckLater(event) {
            var uncheckedInput = Event.element(event)
            if (uncheckedInput.checked) return;
            var uncheckedInputIndex = extractIndex(uncheckedInput)
            var anyChanged = false;
            $$(".approval-checkbox").each(function(input) {
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
            $$(".approval-checkbox").each(function(input) {
                Event.observe(input, "change", checkIntervening)
                Event.observe(input, "change", uncheckLater)
            })
        })
    </script>
</head>
<body>
<c:set var="title">Approve amendments for ${study.assignedIdentifier}</c:set>
<laf:box title="${title}">
    <laf:division>
        <p>
            You are approving the template<c:if test="${study.amended}"> and amendments</c:if>
            of <strong>${study.assignedIdentifier}</strong> for use at <strong>${site.name}</strong>.
        </p>
        <p>
            You cannot reverse any changes you make here.  In the case of mandatory amendments,
            changes will be immediately propagated to subject schedules.  Please be sure that you
            have all necessary regulatory approvals before proceeding.
        </p>
        <p id="warning">
            You must approve the template and amendments in chronological order.  You may
            approve more than one item at a time, but you may not leave any gaps.
            <strong id="warn-detail"></strong>
        </p>
        <form:form method="post">
            <table id="approvals" class="grid">
                <tr>
                    <th>Approve</th>
                    <th>Amendment</th>
                    <th>Approval date</th>
                </tr>
                <c:forEach items="${command.approvals}" var="approval" varStatus="status">
                    <tr>
                        <td class="approve">
                            <c:choose>
                                <c:when test="${approval.alreadyApproved}">
                                    <input disabled="disabled" type="checkbox" checked="checked" />
                                </c:when>
                                <c:otherwise>
                                    <form:checkbox cssClass="approval-checkbox" id="approval-checkbox-${status.index}" path="approvals[${status.index}].justApproved"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="amendment-name">
                            <!-- TODO: link to page describing the amendment -->
                            ${approval.amendment.displayName}
                        </td>
                        <td class="date">
                            <c:choose>
                                <c:when test="${approval.alreadyApproved}">
                                    <tags:formatDate value="${approval.date}"/>
                                </c:when>
                                <c:otherwise>
                                    <laf:dateInput path="approvals[${status.index}].date"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <div class="row">
                <div class="value submit">
                    <input type="submit" value="Approve selected amendments"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>