<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
                                                    
<html>
  <head>
      <title>Site Coordinator Dashboard</title>
      <script type="text/javascript">
        function registerSelector() {
            var aElement = $('selector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = "${action}?selected=" + aElement.value;
            })
        }

        function assignmentButtonClicked(form) {
            $('assign-in-progress-indicator').reveal();

            SC.asyncSubmit(form, {
                onComplete: function() {
                    $('assign-in-progress-indicator').conceal();
                }
            })
        }

        Event.observe(window, "load", registerSelector);
      </script>
      <style type="text/css">
          ul.levelOne {border:1px solid #ccc; padding-left:0;}
          ul.levelTwo {padding-left:1.4em}
          ul.levelThree {padding-left:.5em; padding-top:0;}
          li.levelOne, .levelTwo, .levelThree {padding:3px}
          li.levelOne {list-style: none; border-bottom:1px solid #ccc;}
          li.levelTwo {padding-bottom:0}
          li.levelThree {list-style: none;}
          label {font-weight:bold;}
          div.row div.label{
              text-align:left;
          }
      </style>
  </head>
  <body>
  <laf:box title="Site Coordinator Dashboard">
      <laf:division>
          <c:url var="action" value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser" />
          <form:form id="PCSelectionForm" action="${action}">
              <form:errors path="*"/>
              <select id="selector">
                  <c:forEach items="${assignableUsers}" var="user">
                      <option value="${user.id}" <c:if test="${user.id == selectedId}">selected</c:if>>${user.name}</option>
                  </c:forEach>
              </select>
              <input type="button" value="Studies" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
              <input type="button" value="Participants" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
          </form:form>
          <br/>
          <c:choose>
              <c:when test="${fn:length(displayMap) < 1}">
                  No participants assigned to this participant coordinator.
              </c:when>
              <c:otherwise>
                  <ul class="levelOne">
                      <c:forEach items="${displayMap}" var="site">
                          <li class="levelOne"><label>${site.key.name}</label> </li>
                          <ul class="levelTwo">                                                                     
                              <c:forEach items="${site.value}" var="study">
                                  <form:form action="${action}">
                                      <input type="hidden" name="study" value="${study.key.id}"/>
                                      <input type="hidden" name="site" value="${site.key.id}"/>
                                      <input type="hidden" name="selected" value="${selectedId}"}

                                      <li class="levelTwo">
                                          <div class="row">
                                              <div class="label">
                                                 <label>${study.key.name}</label>
                                              </div>
                                              <div class="value">
                                                  <c:if test="${fn:length(participantCoordinatorStudySites[study.key][site.key]) < 1}">
                                                      There are no participant coordinators for this study site.
                                                  </c:if>
                                                  <c:if test="${fn:length(participantCoordinatorStudySites[study.key][site.key]) >= 1}">
                                                      <select name="participantCoordinator">
                                                          <option></option>
                                                          <c:forEach items="${participantCoordinatorStudySites[study.key][site.key]}" var="user">
                                                              <option value="${user.id}">${user.name}</option>
                                                          </c:forEach>
                                                      </select>
                                                      <input type="button" value="Assign Participant Coordinator" onclick="assignmentButtonClicked(this.form)"/>
                                                      <tags:activityIndicator id="assign-in-progress-indicator"/>
                                                  </c:if>
                                              </div>
                                          </div>
                                      </li>
                                      <ul id="${study.key.id}_${site.key.id}" class="levelThree">
                                          <sitecoord:displayParticipants study="${study.key}" site="${site.key}" participants="${study.value}"/>
                                      </ul>
                                  </form:form>
                              </c:forEach>
                          </ul>
                      </c:forEach>
                  </ul>
              </c:otherwise>
          </c:choose>

      </laf:division>
  </laf:box>
  </body>
</html>