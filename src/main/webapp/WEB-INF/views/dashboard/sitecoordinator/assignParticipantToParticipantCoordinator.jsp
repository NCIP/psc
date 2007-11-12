<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
                                                    
<html>
  <head>
      <title>Site Coordinator Dashboard</title>
      <tags:stylesheetLink name="main"/>
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

        function collapseList(studyId, siteId) {
            if($(studyId + '_' + siteId).decendants() < 1) {
                
            }
        }

        Event.observe(window, "load", registerSelector);
      </script>
      <style type="text/css">
          dl.levelOne {
              border:1px solid #000;
              width:80%
          }

          dt.site {
            border-bottom:1px solid #000;
            padding:.2em;
            font-weight:bold;
          }

          dd {
              margin: 0;
              padding-left:1em
          }

          div.row div.label div.inside {
              text-align:left;
          }

          dl.study {
              padding-bottom:1em
          }

          .site-coord-dash-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
         }
      </style>
  </head>
  <body>
  <laf:box title="Site Coordinator Dashboard">
      <laf:division>
          <c:url var="action" value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser" />
          <form:form id="PCSelectionForm" action="${action}">
              <form:errors path="*"/>
              <div class="links-row">
                  Assign By:
                      <span id="study-view" class="site-coord-dash-link" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByStudy"/>'">Study</span>,
                      Participant Coordinator
              </div>
              <br/>
              <div class="row">
                  <div class="label">
                      Participant Coordinator:
                  </div>
                  <div class="value">
                      <select id="selector">
                          <c:forEach items="${assignableUsers}" var="user">
                              <option value="${user.id}" <c:if test="${user.id == selectedId}">selected</c:if>>${user.name}</option>
                          </c:forEach>
                      </select>
                      <input type="button" value="Studies" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
                      <input type="button" value="Participants" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
                  </div>
              </div>
          </form:form>
          <br/>
          <c:choose>
              <c:when test="${fn:length(displayMap) < 1}">
                  No participants assigned to this participant coordinator.
              </c:when>

              <c:otherwise>
                  <div class="row">
                      <div class="label">
                          Re-assign Participant:
                      </div>
                      <div class="value">
                          <dl class="levelOne">
                              <c:forEach items="${displayMap}" var="site">
                                  <dt class="site">${site.key.name}</dt>

                                  <c:forEach items="${site.value}" var="study">
                                      <dd>
                                          <dl class="study">
                                              <form:form action="${action}">

                                                  <input type="hidden" name="study" value="${study.key.id}"/>
                                                  <input type="hidden" name="site" value="${site.key.id}"/>
                                                  <input type="hidden" name="selected" value="${selectedId}"/>

                                                  <dt class="study">
                                                      <div class="row">
                                                          <div class="label">
                                                              <div class="inside">${study.key.name}</div>
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
                                                  </dt>

                                                  <dd>
                                                      <dl id="${study.key.id}_${site.key.id}">
                                                          <sitecoord:displayParticipants study="${study.key}" site="${site.key}" participants="${study.value}"/>
                                                      </dl>
                                                  </dd>


                                              </form:form>
                                          </dl>
                                      </dd>
                                  </c:forEach>

                              </c:forEach>
                          </dl>
                      </div>
                  </div>
              </c:otherwise>
          </c:choose>

      </laf:division>
  </laf:box>
  </body>
</html>