<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>

<html>
<head>
    <title>Assign subject</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.row div.label{
            /*width: 35%;*/
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 30em;
        }
        ul#population-checkboxes {
            margin: 0; padding: 0;
        }
        ul#population-checkboxes li {
            list-style-type: none
        }

        td.existingSubjectContent  {
            vertical-align:top;
        }

        .bundle-list {
            padding-top:5em;
            padding-left:1em;
            width:50%
        }

        .radioButton{
            padding-left:0.5em;
            padding-top:0.5em;
        }

        .mainTable{
            width:100%;
            border:1em;
        }
        .mainForm {
            width:100%;
        }

        .newSubjectContent{
            width:50%;
            border-right:1px solid black;
        }

        .existingSubjectContent{
            width:50%;
        }
        .commonForBothColumns {
            border-top: 1px solid black;
        }

        .commonDivToDisableForBoth {
            padding-left:33%;
            padding-right:33%;
            float:left;
        }

    </style>

   <script type="text/javascript">

       var subjectAutocompleter;

       function createSubjectsAutocompleter() {
            subjectAutocompleter = new SC.FunctionalAutocompleter(
                'subjects-autocompleter-input', 'subjects-autocompleter-div', subjectAutocompleterChoices, {
                    select: "subjects-name",
                    afterUpdateElement: function(input, selected) {
                        input.value = ""
                        input.focus()
                    }
                }
            );
        }



        var bundleList;

        function subjectAutocompleterChoices(str, callback) {
            var searchString = $F("subjects-autocompleter-input")
            if (searchString == "Search for subjects") {
                searchString = ""
            }

            var uri = SC.relativeUri("/api/v1/subjects")
            if (searchString.blank()) {
                return;
            }

            var params = { };
            if (!searchString.blank()) params.q = searchString;

            SC.asyncRequest(uri, {
                method: "GET", parameters: params,
                onSuccess: function(response) {

                     var bundleListColumns = [

                       {key: "check", label: "",
                            formatter:  YAHOO.widget.DataTable.formatCheckbox
                       },
                       { key: "first_name", label: "First Name", sortable: true },
                       { key: "last_name", label: "Last Name", sortable: true },
                       { key: "date_of_birth", label: "Date of Birth", sortable: true },
                       { key: "gender", label: "Gender", sortable: true },
                       { key: "person_id", label: "Person ID", sortable: true },
                       { key: "assignments", label: "Assignments", formatter: myFormatAssignment }

                    ];

                    var myDataSource = new YAHOO.util.DataSource(response.responseJSON);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    myDataSource.responseSchema = {
                        resultsList : "results",
                        fields : [
                            { key: "first_name" },
                            { key: "last_name" },
                            { key: "date_of_birth"},
                            { key: "gender"},
                            { key: "person_id" },
                            { key: "assignments", formatter: myFormatAssignment }
                        ]
                    };

                    bundleList = new YAHOO.widget.DataTable("bundle-list", bundleListColumns, myDataSource, {scrollable:true});
                }
            })
        }

        // Define a custom format function
        var myFormatAssignment = function(elCell, oRecord, oColumn, oData) {
            var text = undefined;
            var assignments = oRecord.getData('assignments'),
                assignment = undefined,
                i = 0;

            for(i = 0; i < assignments.length; ++i) {
                assignment = assignments[i];
                var site = assignment.site;
                var study = assignment.study;
                var start_date = assignment.start_date;
                var end_date = assignment.end_date;

                text = "- Subject is already enrolled to site '" + site + "' and study '" + study + "' as of start date '" + start_date +"'"
                if (end_date != null && end_date.length>0) {
                       text = text + " and till the end date '" + end_date + "'"
                }
                elCell.innerHTML = elCell.innerHTML  + text + '</br>'
            }
        };

        function toggleAlert(buttonName) {
            if(buttonName.className == "radioButton1"){
                disableEnableElementsOfDiv1(true)
                disableEnableElementsOfDiv2(false)
                disableEnableElementsForCommonDiv(false)
            } else {
                disableEnableElementsOfDiv2(true)
                disableEnableElementsOfDiv1(false)
                disableEnableElementsForCommonDiv(false)
            }

        }

        function disableEnableElementsOfDiv1(flag) {
            $('subjects-autocompleter-input').disabled = flag;
            if (flag) {
                $('bundle-list').hide()
            } else {
                $('bundle-list').show()
            }
        }

        function disableEnableElementsOfDiv2(flag) {
            $('firstName').disabled = flag;
            $('lastName').disabled = flag;
            $('dateOfBirth').disabled = flag;
            $('gender').disabled = flag;
            $('personId').disabled = flag;

        }

        function disableEnableElementsForCommonDiv(flag) {
            $('studySubjectId').disabled = flag;
            $('studySegment').disabled = flag;
            $('startDate').disabled = flag;
            $('population-checkboxes').disabled = flag;
        }

        $(document).observe('dom:loaded', function() {
            createSubjectsAutocompleter()
            disableEnableElementsForCommonDiv(true)
            disableEnableElementsOfDiv1(true)
            disableEnableElementsOfDiv2(true)
        })



    </script>
</head>
<body>
<laf:box title="Assign Subject to Study ${study.assignedIdentifier}" cssClass="yui-skin-sam" autopad="true">

<c:url value="/pages/cal/assignSubject" var="action"/>
<form:form method="post" action="${action}" cssClass="mainForm">
    <table class="mainTable">
        <tr class="subjectContent">
            <td class="newSubjectContent">
                <Input type = radio class="radioButton1" Name = r1 Value = "New Subject" onclick="toggleAlert(this)" > New Subject
                <c:if test="${not empty sites}">

                    <laf:division cssClass="divisionClass">
                            <form:errors path="*"/>

                            <div class="row">
                                <div class="label">
                                    <form:label path="firstName">First Name</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="firstName"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="lastName">Last Name</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="lastName"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="dateOfBirth">Date of Birth (mm/dd/yyyy)</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="dateOfBirth"/>
                                </div>
                                <!--for IE7 -> need to set the break, to get a new row be aligned along with others,
                                since the date is split into two rows and cause the wrong format for the next row-->
                                <br>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="gender">Gender</form:label>
                                </div>
                                <div class="value">
                                    <form:select path="gender">
                                        <form:options items="${genders}"/>
                                    </form:select>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="personId">Person Id</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="personId"/>
                                </div>
                            </div>
                            <br />

                    </laf:division>
                </c:if>
            </td>
            <td class="existingSubjectContent">
                <Input type = radio class="radioButton2" Name = r1 Value = "NW" onclick="toggleAlert(this)" > Existing Subject
                <div class="commonDivToDisable2">
                    <div class="row">
                        <div class="label"> Name: </div>
                        <div class="value">
                            <input id="subjects-autocompleter-input" class="autocomplete input-hint" type="text" autocomplete="off" hint="Search for subjects" value=""/>
                            <div id="subjects-autocompleter-div" class="autocomplete" style="display: none;"/>
                        </div>
                    </div>

                    <div id="bundle-list" class="bundle-list">
                    </div>
                </div>
            </td>
        </tr>
        <tr>
            <td class="commonForBothColumns" colspan="2">
                <div class="commonDivToDisableForBoth">
                    <input type="hidden" name="study" value="${study.id}"/>
                    <div class="row">
                        <div class="label">
                            <form:label path="site">Site</form:label>
                        </div>
                        <div class="value">
                            <c:if test="${fn:length(sites) gt 1}">
                                <form:select path="site">
                                   <c:choose>
                                    <c:when test="${defaultSite != null}">
                                        <c:forEach items="${sites}" var="pair">
                                           <c:if test="${defaultSite eq pair.key.id}">
                                              <form:option value="${pair.key.id}" label="${pair.value}"/>
                                           </c:if>
                                        </c:forEach>
                                        <c:forEach items="${sites}" var="pair">
                                           <c:if test="${defaultSite != pair.key.id}">
                                              <form:option value="${pair.key.id}" label="${pair.value}"/>
                                           </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${sites}" var="pair">
                                            <form:option value="${pair.key.id}" label="${pair.value}"/>
                                        </c:forEach>
                                    </c:otherwise>
                                    </c:choose>
                                </form:select>
                            </c:if>
                            <c:if test="${fn:length(sites) eq 1}">
                                <c:forEach items="${sites}" var="pair">
                                    ${pair.value}
                                    <input type="hidden" name="site" value="${pair.key.id}"/>
                                </c:forEach>
                            </c:if>
                        </div>
                    </div>
                    <div class="row">
                        <div class="label">
                            <form:label path="studySubjectId">StudySubject Id</form:label>
                        </div>
                        <div class="value">
                            <form:input path="studySubjectId"/>
                        </div>
                    </div>

                    <c:if test="${not empty studySegments}">
                        <div class="row">
                            <div class="label">
                                <form:label path="studySegment">Select studySegment for first epoch</form:label>
                            </div>
                            <div class="value">
                                <form:select path="studySegment">
                                    <c:forEach items="${studySegments}" var="sSegment">
                                            <form:option value="${sSegment.id}" label="${sSegment.qualifiedName}"/>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                    </c:if>

                    <div class="row">
                        <div class="label">
                            <form:label path="startDate">Start date of first epoch</form:label>
                        </div>
                        <div class="value">
                            <laf:dateInput path="startDate"/>
                        </div>
                    </div>

                    <c:if test="${not empty populations}">
                        <div class="row">
                            <div class="label">
                                Populations
                            </div>
                            <div class="value">
                                <ul id="population-checkboxes">
                                <c:forEach items="${populations}" var="pop">
                                    <li><label>
                                        <form:checkbox path="populations" value="${pop.id}"/>
                                        ${pop.name}
                                    </label></li>
                                </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </c:if>

                    <div class="row">
                        <div class="submit">
                            <input type="submit" value="Assign"/>
                        </div>
                    </div>
                </div>
            </td>
        </tr>

    </table>
 </form:form>
    <c:if test="${not empty unapprovedSites}">
        <h3>Unapproved sites</h3>
        <laf:division>
            <p>
                Please note:  the following sites are participating in the study, but the template
                has not yet been approved for them.  No subjects may be assigned until this happens.
                Please speak to a site coordinator if you have any questions.
            </p>
            <ul>
                <c:forEach items="${unapprovedSites}" var="site">
                    <li>${site.name}</li>
                </c:forEach>
            </ul>
        </laf:division>
    </c:if>
</laf:box>
</body>
</html>