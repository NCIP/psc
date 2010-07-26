<%@ page import="edu.northwestern.bioinformatics.studycalendar.domain.Period" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<jsp:useBean scope="request" id="study" type="edu.northwestern.bioinformatics.studycalendar.domain.Study"/>
<jsp:useBean scope="request" id="epoch" type="edu.northwestern.bioinformatics.studycalendar.domain.Epoch"/>
<jsp:useBean scope="request" id="studySegment" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySegment"/>
<jsp:useBean scope="request" id="period" type="edu.northwestern.bioinformatics.studycalendar.domain.Period"/>
<jsp:useBean scope="request" id="grid" type="edu.northwestern.bioinformatics.studycalendar.web.template.period.PeriodActivitiesGrid"/>

<jsp:useBean scope="request" id="activityTypes" type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.domain.ActivityType>"/>
<jsp:useBean scope="request" id="activitySources" type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.domain.Source>"/>
<c:if test="${requestScope['selectedActivity']}">
    <jsp:useBean scope="request" id="selectedActivity" type="edu.northwestern.bioinformatics.studycalendar.domain.Activity"/>
</c:if>

<jsp:useBean scope="request" id="canEdit" type="java.lang.Boolean"/>

<tags:escapedUrl var="collectionResource"
    value="api~v1~studies~${study.name}~template~development~epochs~${epoch.name}~study-segments~${studySegment.name}~periods~${period.gridId}~planned-activities"/>

<html>
<head>
    <title>Manage ${period.displayName} activities</title>
    <tags:javascriptLink name="jquery/jquery.enumerable"/>
    
    <tags:javascriptLink name="manage-period/model" />
    <tags:javascriptLink name="manage-period/actions" />
    <tags:javascriptLink name="manage-period/grid-controls" />
    <tags:javascriptLink name="manage-period/rows" />
    <tags:javascriptLink name="manage-period/activity-notes" />
    <tags:javascriptLink name="manage-period/pa-labels" />
    <tags:javascriptLink name="manage-period/presentation" />
    <tags:javascriptLink name="resig-templates" />

    <tags:resigTemplate id="new_activity_row_template">
        <tr class="new-row unused activity" activity-code="[#= code #]" activity-source="[#= source #]">
            <td title="[#= name #]">
                <span class="activity-name">[#= name #]</span>
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_days_row_template">
        <tr class="new-row unused activity">
            [# for (var i = parseInt($('days').getAttribute('day-count')) ; i > 0 ; i--) { #]
            <td class="cell">&nbsp;</td>
            [# } #]
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_notes_row_template">
        <tr class="new-row unused activity">
            <td>
                <a class="notes-edit" href="#notes-edit">
                    View<c:if test="${canEdit}">/Edit</c:if>
                </a>
                <div class="notes-content">
                    <span class="details note" style="display: none"></span>
                    <span class="condition note" style="display: none"></span>
                    <span class="labels note" style="display: none"></span>
                    <span class="weight note"  style="display: none"></span>
                    &nbsp;
                </div>
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_activity_tbody_template">
        <tbody class="activity-type [#= selector #]" activity-type="[#= name #]">
            <tr class="activity-type">
                <th>
                    <span class="text">[#= name #]</span>
                </th>
            </tr>
        </tbody>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_days_tbody_template">
        <tbody class="activity-type [#= selector #]" activity-type="[#= name #]">
            <tr class="activity-type">
                [# for (var i = parseInt($('days').getAttribute('day-count')) ; i > 0 ; i--) { #]
                <td>
                    <span class="text">&nbsp;</span>
                </td>
                [# } #]
            </tr>
        </tbody>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_notes_tbody_template">
        <tbody class="activity-type [#= selector #]" activity-type="[#= name #]">
            <tr class="activity-type">
                <td>
                    <span class="text">&nbsp;</span>
                </td>
            </tr>
        </tbody>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_activity_autocompleter_row">
        <li activity-type-selector="[#= type.selector #]" activity-type-name="[#= type.name #]">
            <span class="activity-name">[#= name #]</span>
            <span class="informal">
                (<span class="activity-code">[#= code #]</span>)
                <span class="activity-source">[#= source #]</span>
            </span>
        </li>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_palabel_autocompleter">
        <li>
            <span class="label-name">[#= label #]</span>
        </li>
    </tags:resigTemplate>

    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="manage-period-activities"/>
    <style type="text/css">
        .days table { width: <%= Math.min(40, ((Period) request.getAttribute("period")).getDuration().getQuantity() * 4) %>em; }
    </style>

    <script type="text/javascript">
        psc.template.mpa.Actions.collectionUri = '${collectionResource}';
        psc.template.mpa.canEdit = ${canEdit};
    </script>
    <tags:javascriptLink name="manage-period/wiring" />
</head>
<body>

<laf:box title="${canEdit ? 'Manage' : 'View'} ${period.displayName} activities">
<laf:division>

<!--
    TOOL SECTION
 -->

<c:if test="${canEdit}">
<div class='section' id='tools-section'>
    <div id='tool-palette'>
        <h2>Tools</h2>
        <ul>
            <li class='tool-selector selected' id='add-tool' title='Add'>
                <img alt='Add' src='<c:url value="/images/add.png"/>' />
            </li>
            <li class='tool-selector' id='move-tool' title='Move'>
                <img alt='Move' src='<c:url value="/images/move.png"/>' />
            </li>
            <li class='tool-selector' id='delete-tool' title='Delete'>
                <img alt='Delete' src='<c:url value="/images/delete.png"/>' />
            </li>
            <li id='tool-details'>
                <div class='tool-detail' id='add-tool-detail'><label>
                    Add a new activity for
                    <select id='population-selector'>
                        <option value=''>all subjects</option>
                        <c:forEach items="${study.populations}" var="pop">
                            <option value="${pop.naturalKey}">${pop.name}</option>
                        </c:forEach>
                    </select>
                    by clicking in the grid.
                </label></div>
                <div class='tool-detail' id='move-tool-detail'>
                    <span class='step-0'>
                        First, click on the one you want to move.
                    </span>

                    <span class='step-1'>
                        Then, click the target cell (in the same row).  Click elsewhere to cancel.
                    </span>
                </div>
                <div class='tool-detail' id='delete-tool-detail'>
                    Click on a cell to clear the activity from that day.
                </div>
            </li>
        </ul>
    </div>
</div>
</c:if>

<!--
    HEADING SECTION
 -->

<div class='section' id='heading-section'>
    <div class='activities heading column' id='activities-heading'></div>
    <div class='days heading column' id='days-heading'>
        <table>
            <tr title="Relative to study segment">
                <c:forEach items="${grid.dayHeadings}" var="oneDay" varStatus="cell">
                    <td class="day" day="${grid.columnDayNumbers[cell.index]}">
                        <div class="period-duration-reference" title="Relative to period">
                            ${grid.period.duration.unit}&nbsp;${cell.count}
                        </div>
                        <c:forEach items="${oneDay}" var="dayNumber">
                            <c:choose>
                                <c:when test="${empty dayNumber}">
                                    <div>&hellip;</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="cycle-number ${dayNumber.cycleEvenOrOdd}">
                                        ${dayNumber}
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </td>
                </c:forEach>
                <!-- hidden cell so we can scroll past and keep the columns aligned at the end -->
                <td class='trailer' id='days-heading-trailer'>
                  &nbsp;
                </td>
            </tr>
        </table>
    </div>
    <div class='notes heading column' id='notes-heading'>
        <ul>
            <li class='details'>
                <a href='#details'>Details</a>
            </li>
            <li class='condition'>
                <a href='#condition'>Condition</a>
            </li>
            <li class='labels'>
                <a href='#labels'>Labels</a>
            </li>
            <li class='weight'>
                <a href='#weight'>Weight</a>
            </li>
        </ul>
    </div>
</div>

<!--
    DATA SECTION
 -->

<div class="section" id="data-section">
    <div class="activities column" id="activities">
        <table>
            <c:forEach items="${grid.rowGroups}" var="typeAndRows">
                <tbody class="activity-type ${typeAndRows.key.selector}" activity-type="${typeAndRows.key.name}">
                    <tr class="activity-type">
                        <th>
                            <span class="text">${typeAndRows.key.name}</span>
                        </th>
                    </tr>
                    <c:forEach items="${typeAndRows.value}" var="row" varStatus="rowStatus">
                        <tr class="activity" activity-code="${row.activity.code}" activity-source="${row.activity.source.naturalKey}">
                            <td title="${row.activity.name}">
                                <span class="activity-name">${row.activity.name}</span>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </c:forEach>
            <tr class='trailer' id='activities-trailer'>
                <!-- hidden cell so we can scroll past and keep the columns aligned at the end -->
                <td>
                    &nbsp;
                </td>
            </tr>
        </table>
    </div>

    <div class="days column" id="days" day-count="${grid.columnCount}">
        <table>
            <c:forEach items="${grid.rowGroups}" var="typeAndRows">
                <tbody class="activity-type ${typeAndRows.key.selector}" activity-type="${typeAndRows.key.name}">
                    <!-- stripe for activity type -->
                    <tr class="activity-type">
                        <c:forEach begin="1" end="${grid.columnCount}">
                            <td>
                                <span class="text">&nbsp;</span>
                            </td>
                        </c:forEach>
                    </tr>
                    <c:forEach items="${typeAndRows.value}" var="row">
                        <tr class="activity">
                            <c:forEach items="${row.plannedActivities}" var="pa" varStatus="cell">
                                <td class="cell">
                                    <c:choose>
                                        <c:when test="${empty pa}">
                                            &nbsp;
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${not study.inAmendmentDevelopment}">
                                                <div class="marker <c:if test="${not empty pa.population}">population-${pa.population.abbreviation}</c:if>" resource-href="${collectionResource}/${pa.gridId}">
                                                    ${empty pa.population ? '&times;' : pa.population.abbreviation}
                                                </div>
                                            </c:if>
                                            <c:if test="${study.inAmendmentDevelopment}">
                                                <%-- TODO: this should be addressed in the controller, not in the view --%>
                                                <c:if test="${not empty pa.population}">
                                                    <c:set var="populationId" value="${pa.population.gridId}"/>
                                                    <c:set var="popName" value=""/>
                                                    <c:set var="popAbbr" value=""/>

                                                    <c:forEach items="${study.populations}" var="pop">
                                                        <c:if test="${pop.gridId == populationId}">
                                                           <c:set var="popName" value="${pop.name}"/>
                                                           <c:set var="popAbbr" value="${pop.abbreviation}"/>
                                                        </c:if>

                                                    </c:forEach>
                                                </c:if>


                                                <div class="marker <c:if test="${not empty pa.population}">population-${popAbbr}</c:if>" resource-href="${collectionResource}/${pa.gridId}">
                                                    ${empty pa.population ? '&times;' : popAbbr}
                                                </div>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                </tbody>
            </c:forEach>
        </table>
    </div>

    <div class="notes column" id="notes">
        <div id="notes-preview">
            <h2>[Activity name]</h2>
            <dl>
                <dt>Details</dt>
                <dd class='none' id='details-preview'>None</dd>
                <dt>Condition</dt>
                <dd class='none' id='condition-preview'>None</dd>
                <dt>Labels</dt>
                <dd class='none' id='labels-preview'>None</dd>
                <dt>Weight</dt>
                <dd class='none' id='weight-preview'>None</dd>
            </dl>
            <c:if test="${canEdit}">
                <p id="notes-preview-edit">Click to edit</p>
            </c:if>
        </div>
        <table>
            <c:forEach items="${grid.rowGroups}" var="typeAndRows">
                <tbody class="activity-type ${typeAndRows.key.selector}" activity-type="${typeAndRows.key.name}">
                    <!-- stripe for activity type -->
                    <tr class="activity-type">
                        <td>
                            <span class='text'>&nbsp;</span>
                        </td>
                    </tr>
                    <c:forEach items="${typeAndRows.value}" var="row">
                        <tr class="activity">
                            <td>
                                <a id="notes-edit" class='notes-edit' href='#notes-edit'>
                                    View<c:if test="${canEdit}">/Edit</c:if><c:if test="${not canEdit}"> all notes</c:if>
                                </a>
                                <div class='notes-content'>
                                    <span class='note details' style='display: none'>
                                        ${row.details}
                                    </span>
                                    <span class='note condition' style='display: none'>
                                        ${row.condition}
                                    </span>
                                    <span class='note weight'>
                                        ${row.weight}
                                    </span>                                    
                                    <span class='note labels' style='display: none'>
                                        <c:forEach items="${row.labels}" var="label">
                                            ${label}
                                        </c:forEach>
                                    </span>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </c:forEach>
            <tr class='trailer' id='notes-trailer'>
                <!-- hidden cell so we can scroll past and keep the columns aligned at the end -->
                <td>
                    &nbsp;
                </td>
            </tr>
        </table>
    </div>
</div>

<!--
    FOOTER SECTION
 -->

<div class='section' id='footer-section'>
    <div class='activities footer column' id='activities-footer'></div>
    <div class='days footer column' id='days-footer'>
        <div id='message'>
            <c:if test="${empty grid.rowGroups}">
                There are no activities in this period.  Use the tools below to find one and add it.
            </c:if>
        </div>
    </div>
    <div class='notes footer column' id='notes-footer'></div>
</div>

</laf:division>

<!--
    NEW ACTIVITY
 -->

<c:if test="${canEdit}">
    <laf:division title="Add another activity">
        <div id="activities-input">
            <label for="activities-autocompleter-input">Pick an activity from:</label>
            <select id="activity-source-filter">
                <option value="">Any source</option>
                <c:forEach items="${activitySources}" var="source">
                    <option>${source.name}</option>
                </c:forEach>
            </select>
            <select id="activity-type-filter">
                <option value="">Any type</option>
                <c:forEach items="${activityTypes}" var="activityType">
                    <option>${activityType.name}</option>
                </c:forEach>
            </select>
            <input id="activities-autocompleter-input" type="text" autocomplete="off" class="autocomplete"
                   hint="With this name or code"/>

            <tags:restrictedItem url="/pages/newActivity" queryString="returnToPeriod=${period.id}" cssClass="control">
              Create new activity
            </tags:restrictedItem>


            <div style="position: relative">
                <div id="activities-autocompleter-div" class="autocomplete" style="display: none"></div>
            </div>
        </div>
    </laf:division>
</c:if>

<laf:division>
    <a class="control" href="<c:url value="/pages/cal/template?studySegment=${studySegment.id}&study=${study.id}&amendment=${study.developmentAmendment.id}"/>">
        Return to template
    </a>
</laf:division>

</laf:box>

<script type="text/javascript">
    // Doing this in dom:loaded does not work consistently in Safari
    Event.observe(window, "load", function() {
        // hide the trailer for the horizontal scroll if there's no horizontal scroll happening
        if ($('days').getWidth() >= $$("#days table").first().getWidth()) {
            $$("#heading-section .trailer").invoke("hide")
        }
        <c:if test="${canEdit && not empty selectedActivity}">
        psc.template.mpa.ActivityRows.addSelectedActivityRow({
            name: '${selectedActivity.name}',
            code: '${selectedActivity.code}',
            source: '${selectedActivity.source.name}'
        }, {
            selector: "activity-type-" + '${selectedActivity.type.name}'.toLowerCase().replace(" ","_"),
            name: '${selectedActivity.type.name}'
        });
        </c:if>
    })
</script>

<div id="lightbox">
    <div id="edit-notes-lightbox">
        <h1>Editing planned activity notes for <span class="activity-name">[Not filled in]</span></h1>
        <div class="row">
            <div class="label">
                <label for="edit-notes-details">Details</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-notes-details" hint="No details" />
            </div>
        </div>
        <div class="row">
            <div class="label">
                <label for="edit-notes-condition">Condition</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-notes-condition" hint="No condition" />
            </div>
        </div>

        <div class="row">
            <div class="label">
                <label for="edit-notes-labels">Labels</label>
            </div>
            <div class="value">
                <!--<input type="text" class="text" id="edit-notes-labels" hint="No labels" />-->
                <input id="edit-notes-labels" class="autocomplete" type="text" hint="No labels" autocomplete="off" />
                <div style="position: relative">
                    <div id="edit-notes-labels-div" class="autocomplete" style="display: none;"></div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="label">
                <label for="edit-notes-weight">Weight</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-notes-weight" hint="Default is 0" />
            </div>
        </div>
        <h5 id="error" class="error" style="display:none">Error: Weight value must be an integer. </h5>
        <div class="row">
            <div class="submit">
                <input type="button" value="Done" id="edit-notes-done"/>
            </div>
        </div>
    </div>
</div>

</body>
</html>