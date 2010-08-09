<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<jsp:useBean id="teamJSON" scope="request" type="java.lang.String"/>
<jsp:useBean id="studiesJSON" scope="request" type="java.lang.String"/>

<html>
<head>
    <title>Administer Study Teams</title>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <script type="text/javascript">
        var TEAM = ${teamJSON};
        var STUDIES =  ${studiesJSON};

        function initTeamTable() {
            var teamDS = new YAHOO.util.LocalDataSource(TEAM);
            teamDS.responseSchema = {
                fields: ['username', 'last_first', 'is_sscm', 'managed_calendar_count']
            };
            new YAHOO.widget.DataTable("team", [
                { key: "username",   label: "Username",        sortable: true },
                { key: "last_first", label: "Name",            sortable: true },
                { key: "memberships", label: "Select studies", sortable: false,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        elCell.innerHTML =
                            "<a href='<c:url value="/pages/admin/team/member?user="/>" +
                                oRecord.getData('username') + "' class='control'>Select</a>";
                    }
                },
                { key: "managed_calendar_count", label: "Reassign subjects", sortable: true,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        if (oRecord.getData('is_sscm')) {
                            var count = oRecord.getData('managed_calendar_count');
                            if (count) {
                                elCell.innerHTML =
                                    "<a href='<c:url value="/pages/admin/team/subjects?user="/>" +
                                        oRecord.getData('username') + "' class='control'>Reassign " +
                                        count + " subject" + (count == 1 ? '' : 's') + "</a>";
                            } else {
                                elCell.innerHTML =
                                    "<acronym title=\"Not applicable: no subjects\">N/A</acronym>";
                                jQuery(elCell).addClass("na");
                            }
                        } else {
                            elCell.innerHTML =
                                "<acronym title=\"Not applicable: can't manage calendars\">N/A</acronym>";
                            jQuery(elCell).addClass("na");
                        }
                    }
                }
            ], teamDS);
        }

        function initStudiesTable() {
            var studiesDS = new YAHOO.util.LocalDataSource(STUDIES);
            studiesDS.responseSchema = {
                fields: ['identifier', 'id' ]
            };
            new YAHOO.widget.DataTable("studies", [
                { key: "identifier", label: "Study", sortable: true },
                { key: "studies", label: "Assign Team Members", sortable: false,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        elCell.innerHTML =
                            "<a href='<c:url value="/pages/admin/team/study?study="/>" +
                                oRecord.getData('id') + "' class='control'>Assign</a>";
                    }
                }
            ], studiesDS);
        }

        jQuery(initTeamTable);
        jQuery(initStudiesTable);
    </script>
</head>
<body>
<laf:box title="Manage workload" cssClass="yui-skin-sam">
    <laf:division>
        <p>
            You can manage which Study Subject Calendar Managers and Data Readers have access to which
            studies.  You can also pick which Study Subject Calendar Manager is primarily responsible
            for a particular study subject.
        </p>
    </laf:division>
    <a name="team"></a>
    <h3>By team member</h3>
    <laf:division>
        <div id="team">
            <tags:activityIndicator/> Team loading...
        </div>
    </laf:division>
    <a name="studies"></a>
    <h3>By study</h3>
    <laf:division>
        <div id="studies">
            <tags:activityIndicator/> Study loading...
        </div>
    </laf:division>
</laf:box>
</body>
</html>