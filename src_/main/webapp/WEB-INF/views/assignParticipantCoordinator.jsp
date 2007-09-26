<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 35%;
        }

        div.submit {
            text-align: right;
        }

        form {
            width: 20em;
        }
    </style>
    <script type="text/javascript">

        function registerSiteSelector() {
            Event.observe("siteSelector", "change", function() {
                $('site-indicator').reveal();
                SC.slideAndHide("assignmentForm", {
                    afterFinish: function() {
                        var siteId = $("siteSelector").options[$("siteSelector").selectedIndex].value
                        if (siteId) {
                            SC.asyncSubmit("siteSelectorForm", {
                                onComplete: function() {
                                    $('site-indicator').conceal();
                                }
                            })
                        } else {
                            $('site-indicator').conceal();
                        }
                    }
                })
            })
        }

        function moveSelected(src, dst) {
            for (var i = 0; i < src.options.length; i++) {
                var srcOpt = src.options[i];
                if (srcOpt.selected) {
                    dst.options[dst.length] = new Option(srcOpt.text, srcOpt.value)
                    src.options[i] = null;
                    i--
                }
            }
            if (src.options[0]) {
                src.options[0].selected = true;
            }
        }

        function selectAll(selector) {
            var sel = $(selector)
            $A(sel.options).each(function(opt) {
                opt.selected = true;
            })
        }

        Event.observe(window, "load", registerSiteSelector)
        Event.observe(window, "load", function() {
            Event.observe("assign-button", "click", function() {
                moveSelected($('availableCoordinators'), $('assignedCoordinators'))
            })
            Event.observe("remove-button", "click", function() {
                moveSelected($('assignedCoordinators'), $('availableCoordinators'))
            })
        })

        Event.observe(window, "load", function() {
            Event.observe("assignmentForm", "submit", function() {
                selectAll("assignedCoordinators")
                selectAll("availableCoordinators")
            })
        })

    </script>
</head>
<body>
<laf:box title="Assign Participant Coordinators">
    <laf:division>
        <p>
            Study: ${study.name}
        </p>

        <form id="siteSelectorForm" action="<c:url value="/pages/assignParticipantCoordinator/selectSite"/>">
            <input type="hidden" name="study" value="${study.id}">
            <div class="row">
                <div class="label">
                    <label for="siteSelector">Site</label>
                </div>
                <div class="value">
                    <tags:activityIndicator id="site-indicator"/>
                    <select name="site" id="siteSelector">
                        <option value="">Select...</option>
                        <c:forEach items="${sites}" var="site">
                            <option value="${site.id}">${site.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </form>

        <form:form method="post" id="assignmentForm" cssStyle="display: none">
            <input type="hidden" name="studyId" value="${study.id}"/>
            <input type="hidden" name="siteId" id="site-id-forwarder" value="NOT SET"/>
            <div class="row">
                <div class="label">
                    <form:label path="availableCoordinators">Available Participant Coordinators</form:label>
                </div>
                <div class="value">
                    <form:select path="availableCoordinators" multiple="true">
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="value submit">
                    <input type="button" value="Assign" id="assign-button">
                    <input type="button" value="Remove" id="remove-button">
                </div>

            </div>
            <div class="row">
                <div class="label">
                    <form:label path="assignedCoordinators">Assigned Participant Coordinators</form:label>
                </div>
                <div class="value">
                    <form:select path="assignedCoordinators" multiple="true">
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="submit">
                    <input type="submit" value="Update Associations"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>