<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<jsp:useBean id="inDevelopmentTemplates" scope="request" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>
<jsp:useBean id="pendingTemplates" scope="request" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>
<jsp:useBean id="availableTemplates" scope="request" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>
<jsp:useBean id="configuration" scope="request" type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"/>

<html>
<title>Calendars</title>
<head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <style type="text/css">
        ul ul.controls {
            display: inline;
        }
        ul.controls li {
            display: inline;
        }
        ul li ul.controls li {
            margin: 0;
            padding: 3px;
        }
        ul.menu {
            margin: 0;
            padding: 0;
        }
        ul.menu li {
            padding: 0.5em;
            list-style-type: none;
            margin: 0.5em;
            height:1% /* For IE */
        }
        ul.menu li .primary {
            display: block;
            float: left;
            width: 20%;
        }
        h2 {
            margin-top: 2em;
        }

        div.mainDiv {
            width:100%;
            margin-top: 0%;
        }
        div #columnLeft {
            width:50%;
            float:left;
        }
        div #columnRight{
            width:50%;
            float:left;
        }

            div.row div.label {
                text-align:left;
                width:20%;
            }

            div.row {
                width: 20%;
                padding-top:2px;
                margin:0px;
                /*text-align:right;*/
               /*line-height:1px;*/
            }

        ul ul.controls1 {
            display: block;
        }
        ul.controls li {
            display: inline;
        }
        ul li ul.controls li {
            margin: 0;
            padding: 3px;
        }

    </style>

    <script type="text/javascript">




        function createAutocompleter() {
             new SC.FunctionalAutocompleter(
                 'studies-autocompleter-input', 'studies-autocompleter-div', studyAutocompleterChoices, {
                     afterUpdateElement: function(input, selected) {
                         $('selected-study').style.display = 'inline';
                          $('selected-study-itself').innerHTML= selected.innerHTML;
                          $('selected-study-itself').href= '<c:url value="/pages/cal/template?study="/>' + selected.id;

                         input.value = ""
                         input.focus()
                     }
                 }
             );
         }

        function studyAutocompleterChoices(str, callback) {
              studyAutocompleterChoiceProcessing(function(data) {
              var lis = data.map(function(study) {
                        var id = study.id
                        var name = study.assigned_identifier
                        var listItem = "<li id='"  + id + "'>" + name + "</li>";
                        return listItem
              }).join("\n");

              callback("<ul>\n" + lis + "\n</ul>");
            });
        }

       function studyAutocompleterChoiceProcessing(callback) {
            var searchString = $F("studies-autocompleter-input")
            if (searchString == "Search for study") {
                searchString = ""
            }

            var uri = SC.relativeUri("/api/v1/studies")
            if (searchString.blank()) {
                return;
            }

            var params = {};
            if (!searchString.blank()) {
                params.q = searchString;
            }

            SC.asyncRequest(uri+".json", {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                    callback(response.responseJSON.studies)
                }
            })
        }
        
        function disableDivs() {
            var L1 = $('L1');
            if (L1 != null) {
                $(L1).style.display = "inline"
                var arrayOfSiblings = $('L1').siblings();
                for (var i = 0; i < arrayOfSiblings.length; i++){
                    $(arrayOfSiblings[i]).style.display = "none";
                }
            }

            var R2 = $('R2');
            if (R2 != null) {
                $(R2).style.display = "inline"
                var arrayOfSiblings = $('R2').siblings();
                for (var i = 0; i < arrayOfSiblings.length; i++){
                    $(arrayOfSiblings[i]).style.display = "none";
                }
            } else {
                var R1 = $('R1');
                if (R1 != null) {
                    $(R1).style.display = "none"
                    var arrayOfSiblings = $('R1').siblings();
                    for (var i = 0; i < arrayOfSiblings.length; i++){
                        $(arrayOfSiblings[i]).style.display = "none";
                    }
                }
                if ($('nextTenStudies') != null) {
                    $('nextTenStudies').hide()
                }
            }
        }

        function nextTenStudies() {
            var L1 = $('L1');
            if ($(L1).style.display == "none") {
                var arrayOfLeftSiblings = $('L1').siblings();
                for (var i = 0; i < arrayOfLeftSiblings.length; i++) {
                    if ($(arrayOfLeftSiblings[i]).style.display == "inline") {
                        $(arrayOfLeftSiblings[i]).style.display = "none";
                        if (arrayOfLeftSiblings[i+1] != null) {
                            $(arrayOfLeftSiblings[i+1]).style.display = "inline";
                        } else {
                            $(L1).style.display ="inline"
                        }
                        break;
                    }
                }
            } else {
               $(L1).style.display = "none";
                var arrayOfLeftSiblings = $('L1').siblings();
                if (arrayOfLeftSiblings[0] != null) {
                    $(arrayOfLeftSiblings[0]).style.display = "inline";
                } else {
                    $(arrayOfLeftSiblings[0]).style.display ="inline"
                }
            }


            var R1 = $('R1');
            if ($(R1).style.display == "none") {
                var arrayOfRightSiblings = $('R1').siblings();
                for (var i = 0; i < arrayOfRightSiblings.length; i++) {
                    if ($(arrayOfRightSiblings[i]).style.display == "inline") {
                        $(arrayOfRightSiblings[i]).style.display = "none";
                        if (arrayOfRightSiblings[i+1] != null) {
                            $(arrayOfRightSiblings[i+1]).style.display = "inline";
                        } else {
                            $(R1).style.display ="inline"
                        }
                        break;
                    }
                }
            } else {
                $(R1).style.display = "none";
                var arrayOfRightSiblings = $('R1').siblings();
                if (arrayOfRightSiblings[0] != null) {
                    $(arrayOfRightSiblings[0]).style.display = "inline";
                } else {
                    $(arrayOfRightSiblings[0]).style.display ="inline"
                }
            }

        }

        Event.observe(window, "load", createAutocompleter)
        Event.observe(window, "load", disableDivs)

    </script>
</head>
<body>
<laf:box title="Calendars">
    <laf:division>
        <security:secureOperation element="/pages/cal/newStudy">
            <c:if test="${configuration.map.enableCreatingTemplate}">
                <p><a href="<c:url value="/pages/cal/newStudy"/>">Create a new template</a></p>
            </c:if>
            <p><a href="<c:url value="/pages/cal/template/import"/>">Import an existing template</a></p>
        </security:secureOperation>
     </laf:division>
        <c:if test="${not empty inDevelopmentTemplates}">
            <h3>Templates in design</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${inDevelopmentTemplates}" var="template" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${template.study.id}&amendment=${template.study.developmentAmendment.id}"/>" class="primary">
                                ${template.study.developmentDisplayName}
                            </a>
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/template/deleteDevelopmentAmendment" queryString="study=${template.study.id}" >
                                    Delete
                                </tags:restrictedListItem>
                            </ul>
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/copyStudy" queryString="study=${template.study.id}&amendment=${template.study.developmentAmendment.id}" >
                                    Copy
                                </tags:restrictedListItem>
                            </ul>
                        </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>
        <c:if test="${not empty pendingTemplates}">
            <h3>Pending</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${pendingTemplates}" var="template" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${template.study.id}"/>" class="primary">
                                ${template.study.releasedDisplayName}
                            </a>

                            <%-- Removed old workflow bits pending fixup in #1078
                             <c:if test="${empty template.study.studySites}">
                                <div style="width:80%;float:right;">
                                    <c:set var="canAssignTemplateToStudy" value="false"/>
                                    Must
                                    <tags:restrictedItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${template.id}">
                                        <c:set var="canAssignTemplateToStudy" value="true"/> assign
                                    </tags:restrictedItem>
                                    <c:if test="${not canAssignTemplateToStudy}">
                                        assign
                                    </c:if>
                                        the template to a site. A <b>Study Administrator</b> can do this.
                                    <tags:restrictedItem cssClass="control" url="/pages/cal/copyStudy"
                                                                      queryString="study=${template.id}"> Copy </tags:restrictedItem>
                                </div>
                             </c:if>
                             <c:if test="${not empty template.study.studySites}">
                                 <c:forEach items="${template.study.studySites}" var="studySite" varStatus="studySiteStatus">
                                     <c:if test="${not empty studySite.unapprovedAmendments}">
                                  <div style="width:80%;float:right;">      Waiting for approval at site "${studySite.site.name}" - a <b>Site Coordinator</b> can do that.
                                  <tags:restrictedItem cssClass="control" url="/pages/cal/copyStudy"
                                                                      queryString="study=${template.id}"> Copy </tags:restrictedItem>
                                  </div>
                                     </c:if>
                                     <c:if test="${empty studySite.unapprovedAmendments}">
                                         <c:set var="isSubjectCoordinatorAssigned" value="false"/>
                                         <c:forEach items="${studySite.userRoles}" var="userRole" varStatus="userRoleStatus">
                                             <c:if test="${userRole.role == 'SUBJECT_COORDINATOR'}">
                                                <c:set var="isSubjectCoordinatorAssigned" value="true"/>
                                             </c:if>
                                         </c:forEach>
                                         <c:if test="${isSubjectCoordinatorAssigned == false}">
                                             <div style="width:80%;float:right;"> Subject Coordinator has to be assigned to the study at the site "${studySite.site.name}" - a <b>Site Coordinator</b> can do this.
                                             <tags:restrictedItem cssClass="control" url="/pages/cal/copyStudy"
                                                                      queryString="study=${template.id}"> Copy </tags:restrictedItem>
                                             </div>
                                         </c:if>
                                     </c:if>
                                 </c:forEach>

                             </c:if>
                            --%>
                         </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>

        <c:if test="${not empty availableTemplates}">
            <h3>Released templates</h3>
            <laf:division>
                <label for="add-template">Search for study: </label>
                <input id="studies-autocompleter-input" type="text" autocomplete="off"
                       hint="Search for studies" class="autocomplete"/>
                <div id="studies-autocompleter-div" class="autocomplete"></div>

                <label id="selected-study" style="display:none;"> Selected study: </label>
                <a class="primary" id="selected-study-itself"></a>

                <div class="mainDiv">
                    <div id="columnLeft">
                        <c:set var="myCount" value="0"/>
                        <c:set var="divId" value="0"/>
                        <ul class="menu">
                            <c:forEach items="${availableTemplates}" var="template" varStatus="status">
                                <c:if test="${status.count-1 == 0 || myCount == 10}">
                                    <c:set var="myCount" value="0"/>
                                    <c:if test="${status.count > 1}">
                                        </div>
                                    </c:if>
                                    <div id="L${divId +1}">
                                    <c:set var="divId" value="${divId+1}"/>
                                </c:if>

                                <li class="autoclear ${commons:parity(status.count)}">
                                    <c:set var="myCount" value="${myCount +1}"/>
                                    <a href="<c:url value="/pages/cal/template?study=${template.study.id}"/>" class="primary">
                                        ${template.study.releasedDisplayName}
                                    </a>
                              <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/copyStudy"
                                                                 queryString="study=${template.study.id}"> Copy </tags:restrictedListItem>
                            </ul>
                                </li>
                                <c:if test="${status.last}">
                                    </div>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </div>

                    <div id="columnRight">
                        <c:set var="myCount" value="0"/>
                        <c:set var="divId" value="0"/>
                        <ul class="menu">
                            <c:forEach items="${availableTemplates}" var="template" varStatus="status">
                                <c:if test="${status.count-1 == 0 || myCount == 10}">
                                    <c:set var="myCount" value="0"/>
                                    <c:if test="${status.count > 1}">
                                        </div>
                                    </c:if>
                                    <div id="R${divId +1}">
                                    <c:set var="divId" value="${divId+1}"/>
                                </c:if>

                                <li class="autoclear ${commons:parity(status.count)}">
                                    <c:set var="myCount" value="${myCount +1}"/>
                                    <a href="<c:url value="/pages/cal/template?study=${template.study.id}"/>" class="primary">
                                        ${template.study.releasedDisplayName}
                                    </a>
                                    <ul class="controls">
                                        <tags:restrictedListItem cssClass="control" url="/pages/cal/copyStudy"
                                                                 queryString="study=${template.study.id}"> Copy </tags:restrictedListItem>
                                    </ul>
                                </li>
                                <c:if test="${status.last}">
                                    </div>
                                </c:if>
                             </c:forEach>
                        </ul>
                    </div>
                </div>

                <ul class="menu" >
                    <li style="float:right;">
                        <a id="nextTenStudies" class="control" style="cursor:pointer;" onclick="nextTenStudies()"> Next 10 studies -> </a>
                    </li>
                </ul>
                <br style="clear:both;">
            </laf:division>
        </c:if>
    </laf:box>
</body>
</html>