<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>


<%@taglib prefix="commons1" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>

<html>
  <head>
      <tags:includeScriptaculous/>
      <tags:stylesheetLink name="main"/>
      <tags:stylesheetLink name="report"/>

      <%--<tags:stylesheetLink name="report" dynamic="true"/>--%>
      <style type="text/css">
          table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>) }
          table.query-results th.order1 a { background-image: url(<c:url value="/images/arrow_down.png"/>) }
          table.query-results th.order2 a { background-image: url(<c:url value="/images/arrow_up.png"/>) }
      </style>

      <script type="text/javascript">


        function selectDeselect() {
            var data='activityIds='
            var lengthOfCheckboxes = $$('input[name*=defaultToCheckbox]').length
            var isChecked = false
            for (var i =0; i< lengthOfCheckboxes; i++) {
                if ($$('input[name*=defaultToCheckbox]')[i].checked) {
                    var id = $$('input[name*=defaultToCheckbox]')[i].id
                    isChecked = true;
                    break;
                }
            }
            if(isChecked) {
                for (var i =0; i< lengthOfCheckboxes; i++) {
                    if (! $$('input[name*=defaultToCheckbox]')[i].checked) {
                    
                        $$('input[name*=defaultToCheckbox]')[i].disabled = true
                    }
                }                
            } else {
                //means nothing is selected, we should enable the default to checkboxes
              for (var i =0; i< lengthOfCheckboxes; i++) {
                    if ($$('input[name*=defaultToCheckbox]')[i].disabled) {
                        $$('input[name*=defaultToCheckbox]')[i].disabled = false
                    }
                }
            }
             <%--action="<c:url value="/pages/activity/reconcileActivity"/>"--%>
            var href = '<c:url value="/pages/activity/reconcile"/>'
            href = href+"?"+data
        }


        function checkErrorsAndPerformReconcile() {
            var errorsElt = $('errors')
            var errorMessage
            if (checkCorrectSelection()) {
                console.log("==== here? ");
                if (errorsElt.hasChildNodes()) {
                    errorsElt.remove($('errorMessage'))
                    console.log("==== no messages")
                }
                performReconcile()
            } else {
                if (! errorsElt.hasChildNodes()) {
                    errorMessage = Builder.node('h2', {id:'errorMessage'}, 'Please verify the selection. At least two activities should be selected in Reconcile column, and one from Default To column')
                    errorsElt.appendChild(errorMessage)
                }
            }

        }

        function checkCorrectSelection() {
            var result = false;
            var lengthOfDefaultToCheckboxes = $$('input[name*=defaultToCheckbox]').length
            var isDefaultToCheckboxChecked = false
            for (var i =0; i< lengthOfDefaultToCheckboxes; i++) {
                if ($$('input[name*=defaultToCheckbox]')[i].checked) {
                    var id = $$('input[name*=defaultToCheckbox]')[i].id
                    isDefaultToCheckboxChecked = true;
                    break;
                }
            }

            var howManyOfReconcileCheckboxesChecked = 0;
            var lengthOfReconcileCheckboxes = $$('input[name*=reconcileCheckbox]').length
            for (var i =0; i< lengthOfReconcileCheckboxes; i++) {
                if ($$('input[name*=reconcileCheckbox]')[i].checked) {
                    var id = $$('input[name*=reconcileCheckbox]')[i].id
                    howManyOfReconcileCheckboxesChecked = howManyOfReconcileCheckboxesChecked+1;
                }
            }
            if (isDefaultToCheckboxChecked && howManyOfReconcileCheckboxesChecked>2){
                result = true;
            }
            console.log("===== result = " + result);
            return result;
        }


        function performReconcile() {
            console.log("===== are we here?")
            var data='activityIds='
            var lengthOfCheckboxes = $$('input[name*=reconcileCheckbox]').length
            for (var i =0; i< lengthOfCheckboxes; i++) {
                if ($$('input[name*=reconcileCheckbox]')[i].checked) {
                    var id = $$('input[name*=reconcileCheckbox]')[i].id
                    console.log("===== id " + id)
                    data=data+id+","
                }
            }
            data = data +"&"
            var defaultToId;
            var lengthOfReconcileCheckboxes = $$('input[name*=reconcileCheckbox]').length
            for (var i =0; i< lengthOfReconcileCheckboxes; i++) {
                if ($$('input[name*=reconcileCheckbox]')[i].checked) {
                    defaultToId = $$('input[name*=reconcileCheckbox]')[i].id
                    data = data + "defaultTo="+defaultToId+"&"
                    break;
                }
            }

            console.log("====== data " + data)
            var href = '<c:url value="/pages/activity/reconcile"/>'
            href = href+"?"+data
            var request = new Ajax.Request(href,
            {
                method: 'post'

            })
            console.log("=== here?")

        }

      </script>
  </head>
  <body>
      <laf:box title="Activity Report">
          <laf:division>
              <div id="errors" style="margin-right:10px; margin-left:0.5em;"></div>
              <br style="clear:both"/>
              <div id="myTable">
                  <h1>inside reconicile activity</h1>
                <display:table name="activitiesPerSource" class="query-results" id="row" requestURI="activitiesPerSource" export="false" >
                    <display:column title="Reconcile">
                       <input id="${row.id}" class="checkbox${row.id}" checked="checked" type="checkbox" value="false" name="reconcileCheckbox"/>
                    </display:column>
                    <display:column title="Default to">
                       <input id="${row.id}" class="checkbox${row.id}" selected="false" type="checkbox" value="false" name="defaultToCheckbox" onclick="selectDeselect()"/>
                    </display:column>
                    <display:column title="Activity Name">
                        <label id="Name${row.id}">${row.name}</label>
                    </display:column>

                    <display:column title="Type">
                        <label id="Type${row.id}">${row.type}</label>
                    </display:column>

                    <display:column title="Code">
                        <label id="Code${row.id}">${row.code}</label>
                    </display:column>

                </display:table>
              </div>
              <input type="button" id="reconcileButton" name="reconcileButton" value="Reconcile" onclick="checkErrorsAndPerformReconcile()"/>
      </laf:division>
      </laf:box>
  </body>

</html>