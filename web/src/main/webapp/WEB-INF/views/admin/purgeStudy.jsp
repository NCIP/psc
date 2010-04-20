<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<html>
<head>
<title>Purge Study</title>

    <title>Site Coordinator Dashboard</title>


    <tags:stylesheetLink name="main"/>
    <style type="text/css">
        #studies-autocompleter-input {
        width: 40%
        }

        /*div.label {*/
        /*width: 50%;*/
        /*}*/

        form {
            width: 40em;
        }
    </style>

    <tags:includeScriptaculous/>
    <tags:javascriptLink name="admin/purge-study/purge-study"/>
    <script type="text/javascript">
        jQuery(document).ready(function() {
              psc.admin.PurgeStudy.init();
          })
    </script>

</head>

  <body>
    <laf:box title="Calendars">
        <laf:division>
            <div class="row">
                <div class="label">
                        Study:
                </div>
                <div class="value">
                    <input id="studies-autocompleter-input" type="text" 
                           hint="Search for studies" class="autocomplete" autocomplete="off"/>
                    <div id="studies-autocompleter-div" class="autocomplete"></div>
                </div>
            </div>
        </laf:division>
    </laf:box>
  </body>
</html>