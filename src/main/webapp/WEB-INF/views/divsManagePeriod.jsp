<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<tags:escapedUrl var="collectionResource"
    value="api~v1~studies~${study.name}~template~development~epochs~${epoch.name}~study-segments~${studySegment.name}~periods~${period.gridId}~planned-activities"/>
<html>
<head>
    <title>Manage ${period.displayName} activities</title>
    <tags:javascriptLink name="manage-period/plan-activities" />
    <tags:javascriptLink name="manage-period/manage-activity-rows" />
    <tags:javascriptLink name="manage-period/activity-notes" />
    <tags:javascriptLink name="manage-period/reindex" />
    <tags:javascriptLink name="manage-period/server" />
    <tags:javascriptLink name="resig-templates" />

    <tags:resigTemplate id="new_activity_row_template">
        <tr class="new-row unused activity">
            <td title="[#= name #]" activity-code="[#= code #]" activity-source="[#= source #]">
                <span class="row-number">-1</span>
                [#= name #]
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
                    View/Edit
                </a>
                <div class="notes-content">
                    <span class="details" style="display: none"></span>
                    <span class="condition" style="display: none"></span>
                    <span class="labels" style="display: none"></span>
                    &nbsp;
                </div>
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_activity_tbody_template">
        <tbody class="activity-type [#= selector #]">
            <tr class="activity-type">
                <th>
                    <span class="text">[#= name #]</span>
                </th>
            </tr>
        </tbody>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_days_tbody_template">
        <tbody class="activity-type [#= selector #]">
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
        <tbody class="activity-type [#= selector #]">
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

    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="manage-period"/>
    <style type="text/css">
        .days table { width: 40em; }
    </style>

    <script type="text/javascript">
        SC.MP.collectionResource = '${collectionResource}'
    </script>
</head>
<body>

<laf:box title="Manage ${period.displayName} activities">
<laf:division>

<!--
    SOURCE SECTION
 -->

<div class="section" id="source-section">
    <div id="populations">
        <table>
            <tr>
                <td>
                    <div class='population' id='populations-all'>
                        <h2>All subjects</h2>
                        <div class='marker'>
                            &times;
                        </div>
                    </div>
                </td>
                <c:forEach items="${study.populations}" var="pop">
                    <td title="${pop.name}">
                        <div class='population' id='population-${pop.abbreviation}'>
                            <h2>${pop.name}</h2>
                            <div class='marker population-${pop.abbreviation}'>
                                ${pop.abbreviation}
                            </div>
                        </div>
                    </td>
                </c:forEach>
            </tr>
        </table>
    </div>
    <div class='population' id='remove-target'>
        <h2>Remove</h2>
        <div class='cell'></div>
    </div>
</div>

<!--
    HEADING SECTION
 -->

<div class='section' id='heading-section'>
    <div class='activities heading column' id='activities-heading'></div>
    <div class='days heading column' id='days-heading'>
        <table>
            <tr title="Relative to study segment">
                <c:forEach items="${grid.dayHeadings}" var="oneDay" varStatus="cell">
                    <td class="day" day-number="${grid.columnDayNumbers[cell.index]}">
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
            <!-- Labels will be enabled in 2.2.1 -->
            <%--<li class='labels' style="display: none">--%>
            <li class='labels'>
                <a href='#labels'>Labels</a>
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
                <tbody class="activity-type ${typeAndRows.key.selector}">
                    <tr class="activity-type">
                        <th>
                            <span class="text">${typeAndRows.key.name}</span>
                        </th>
                    </tr>
                    <c:forEach items="${typeAndRows.value}" var="row" varStatus="rowStatus">
                        <tr class="activity">
                            <td title="${row.activity.name}" activity-code="${row.activity.code}" activity-source="${row.activity.source.naturalKey}">
                                <span class="row-number">${rowStatus.count}</span>
                                ${row.activity.name}
                                <input type="hidden" value="${row.activity.id}" name="activityId"/>
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

    <div class="days column" id="days">
        <table>
            <c:forEach items="${grid.rowGroups}" var="typeAndRows">
                <tbody class="activity-type ${typeAndRows.key.selector}">
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
                                            <div class="marker <c:if test="${not empty pa.population}">population-${pa.population.abbreviation}</c:if>" resource-href="${collectionResource}/${pa.gridId}">
                                                ${empty pa.population ? '&times;' : pa.population.abbreviation}
                                            </div>
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
        <div id="notes-preview" style="display: none">
            <h2>[Activity name]</h2>
            <dl>
                <dt>Details</dt>
                <dd class='none' id='details-preview'>None</dd>
                <dt>Condition</dt>
                <dd class='none' id='condition-preview'>None</dd>
                <!-- Labels will be enabled in 2.2.1 -->
                <dt>Labels</dt>
                <dd class='none' id='labels-preview'>None</dd>
            </dl>
            <p id="notes-preview-edit">Click to edit</p>
        </div>
        <table>
            <c:forEach items="${grid.rowGroups}" var="typeAndRows">
                <tbody class="activity-type ${typeAndRows.key.selector}">
                    <!-- stripe for activity type -->
                    <tr class="activity-type">
                        <td>
                            <span class='text'>&nbsp;</span>
                        </td>
                    </tr>
                    <c:forEach items="${typeAndRows.value}" var="row">
                        <tr class="activity">
                            <td>
                                <a class='notes-edit' href='#notes-edit'>
                                    View/Edit
                                </a>
                                <div class='notes-content'>
                                    <span class='details' style='display: none'>
                                        ${row.details}
                                    </span>
                                    <span class='condition' style='display: none'>
                                        ${row.condition}
                                    </span>
                                    <span class='labels' style='display: none'>
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

        <a id="newActivityLink" class="control" href="<c:url value="/pages/newActivity?returnToPeriod=${period.id}"/>">Create new activity</a>

        <div style="position: relative">
            <div id="activities-autocompleter-div" class="autocomplete"></div>
        </div>
    </div>

</laf:division>

<laf:division>
    <a class="control" href="<c:url value="/pages/cal/template?studySegment=${studySegment.id}&study=${study.id}&amendment=${study.developmentAmendment.id}"/>">
        Return to template
    </a>
</laf:division>

</laf:box>

<script type="text/javascript">
    // Sync scrolling
    $('days').observe('scroll', function() {
        $('days-heading').scrollLeft = $('days').scrollLeft
        $('activities').scrollTop = $('days').scrollTop
        $('notes').scrollTop = $('days').scrollTop
    })

    // Doing this in dom:loaded does not work consistently in Safari
    Event.observe(window, "load", function() {
        // hide the trailer for the horizontal scroll if there's no horizontal scroll happening
        if ($('days').getWidth() >= $$("#days table").first().getWidth()) {
            $$("#heading-section .trailer").invoke("hide")
        }
        <c:if test="${not empty selectedActivity}">
        SC.MP.addActivityRow({
            name: '${selectedActivity.name}',
            code: '${selectedActivity.code}',
            source: '${selectedActivity.source.name}'
        }, {
            id: ${selectedActivity.type.id},
            name: '${selectedActivity.type.name}'
        })
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
                <input id="edit-notes-labels" class="text" type="text" hint="No labels" autocomplete="off" />
                <div id="edit-notes-labels-div" class="autocomplete" style="display: none;"/>
            </div>
        </div>
        <div class="row">
            <div class="submit">
                <input type="button" value="Done" id="edit-notes-done"/>
            </div>
        </div>
    </div>
</div>

</body>
</html>