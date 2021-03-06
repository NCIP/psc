<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="laf"   tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <title>Account login</title>
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
            margin-left: 5%;
            margin-right: 5%
        }
        /* need for fomatting the login box */
        .box {
            /*margin:auto sets a login box at the center of the screen for IE7*/
            margin:auto;
            width:400px;
        }

        form#login div.label {
            width:0%;
            margin-left:0;
        }
        .box .content {
            overflow:hidden;
        }
        form#login {
            width:90%;
        }

        form#login div.submit {
            text-align:center;
            width:100%;
        }
        div.row div.value {
            margin-left:7em;
        }
    </style>
    <script type="text/javascript">
         function isCorrectInput() {
            if ($F('username').length == 0 || $F('password').length == 0) {
               $('loginInputError').update("Username or Password can not be empty.");
               return false;
            }
            return true;
        }
        $(document).observe("dom:loaded", function() {
            $('login').observe("submit", function(fn) {
                if (!isCorrectInput()) {
                    Event.stop(fn);
                }
            })
        })
    </script>
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
