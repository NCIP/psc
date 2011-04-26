<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons1" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions"%>

<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://displaytag.sf.net" prefix="display" %>

<html>
  <head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:stylesheetLink name="report"/>

    <style type="text/css">
        table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>) }
        table.query-results th.order1 a { background-image: url(<c:url value="/images/arrow_down.png"/>) }
        table.query-results th.order2 a { background-image: url(<c:url value="/images/arrow_up.png"/>) }


        a#newActivityLink {
            padding-left: 2em
        }

        #activities-input label {
            font-weight: bold;
        }

        #activities-input {
            margin: 1em 0;
            background-color: #ddd;
            padding: 0.5em;
        }

        #newActivity label {
            font-weight: bold;
        }

        #newActivity {
            /*margin: 1em 0;*/
            background-color: #ddd;
            /*padding: 0.5em;*/
        }

        #activityName label {
            margin-left:10em;
        }

        #createNewActivity label {
            margin-bottom:1em;
        }

        #exportOptions {
            padding: 0 1em 0 1em;
        }

        .underlined {
            text-decoration:underline;
            display:none
        }

    </style>

   <script type="text/javascript">

        function editActivityType(activityTypeId) {
            var inputTypeName = 'TypeName'+activityTypeId
            var labelTypeName = 'Type'+activityTypeId
            $(inputTypeName).style.display = 'inline'
            $(labelTypeName).hide();

            var editButton = 'Edit'+activityTypeId
            $(editButton).style.display = 'none'

            var saveButton = 'Save'+activityTypeId
            $(saveButton).style.display = "inline"
        }

        function saveActivityType(activityTypeId) {
            var inputTypeName = 'TypeName'+activityTypeId
            var labelTypeName = 'Type'+activityTypeId
            var data = ''
            data = data+"action=save&";
            data = data+"activityTypeId="+activityTypeId+"&";
            data = data+"activityTypeName="+$(inputTypeName).value+"&";

            var href = '<c:url value="/pages/activities/saveActivityType"/>'
            href= href+"?"+data
            var saveRequest = new Ajax.Request(href,
            {
                method: 'post',
                onComplete: function(t) {
                    $(labelTypeName).innerHTML = $(inputTypeName).value;
                    $(inputTypeName).style.display ='none'
                    $(labelTypeName).show()


                    var editButton = 'Edit'+activityTypeId
                    $(editButton).style.display="inline"

                    var saveButton = 'Save'+activityTypeId
                    $(saveButton).style.display="none"
                }
            });

        }


        function addNewActivityType() {
            var activityTypeName = $('addActivityTypeName').value
            //TODO: this page will be re-written to use yui data table and resource --
            // make sure to do check on empty activity type in the resource.
            if (activityTypeName.trim().length == 0){
                $('errors').innerHTML ="Activity Type can not be empty";
            } else {
                $('errors').innerHTML =""

                var href = '<c:url value="/pages/activities/addNewActivityType"/>'
                var data="";

                data = data+"action=add&";
                data= data+"activityTypeName="+activityTypeName+"&"
                href=href+"?"+data
                var newActivityReques = new Ajax.Request(href,
                {
                    method: 'post'

                });
            }
            return true;
        }

        function deleteActivityType(activityTypeId) {
            var inputTypeName = 'TypeName'+activityTypeId
            var labelTypeName = 'Type'+activityTypeId
            var data = ''
            data = data+"action=delete&";
            data = data+"activityTypeId="+activityTypeId+"&";
            data = data+"activityTypeName="+$(inputTypeName).value+"&";

            var href = '<c:url value="/pages/activities/deleteActivityType"/>'
            href= href+"?"+data
            var deleteRequest = new Ajax.Request(href,
            {
                method: 'post'
            });

            return true;
        }       
   </script>
  </head>
  <body>
      <laf:box title="Activity types">
        <laf:division>

            <div id="errors" style="margin-right:10px; margin-left:0.5em;">
                <form:errors path="*"/>
            </div>
            <div id="newActivity" style="margin-bottom:10px;"></div>

            <div id="myTable">
                <tags:activityTypesTable/>
                <script><tags:addNewActivityTypeRow/></script>
            </div>
        </laf:division>
    </laf:box>
  </body>
</html>