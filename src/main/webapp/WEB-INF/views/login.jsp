<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <!--<title>Account login</title>-->
    <style type="text/css">
        div#body {
            width: 24em;
            border: 2px outset #000;
            background-color: #fff;
            padding: 0;
            margin: 3em auto;
        }
        h1 {
            text-align: center;
        }
        form#login {
            margin: 1em 2em;
        }
        /* need for fomatting the login box */
        .box {
            width:40%;
            margin-left:30%;
            margin-right:30%;
            
        }
    </style>
</head>
<body>
<laf:box title="Account login">
    <laf:division>

        <h1>Please log in</h1>
        <tags:loginForm/>

    </laf:division>
</laf:box>
</body>
</html>
