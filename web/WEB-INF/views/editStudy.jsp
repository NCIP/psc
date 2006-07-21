<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>${action} Study</title>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
    <script type="text/javascript">
        /*
        <div class="row arm-name added-arm">
            <div class="label"><label for="arm-name-2">Arm 3 name</label></div>
            <div class="value"><input type="text" id="arm-name-2" name="armName[2]"/></div>
        </div>
        */
        function addArmName() {
            var group = $('arm-names-group');
            var added_arms = $$('.added-arm');
            var newIdx = added_arms.length + 2;
            var newNum = newIdx + 1
            var newInputId = "arm-name-" + newIdx;
            var newRow = Builder.node('div', {className:'row arm-name added-arm'}, [
                Builder.node('div', {className:'label'}, [
                    Builder.node('label', {'for': newInputId}, "Arm " + newNum + " name")
                ]),
                Builder.node('div', {className:'value'}, [
                    Builder.node('input', {type:'text', id:newInputId, name:"armName[" + newIdx + "]"})
                ])
            ]);
            newRow.style.display = "none";
            group.insertBefore(newRow, $('arm-names-group-buttons'));
            slideAndShow(newRow);
        }

        function delLastArmName() {
            var group = $('arm-names-group');
            var added_arms = $$('.added-arm');
            if (added_arms.length < 1) return;
            var last_arm = added_arms[added_arms.length - 1];
            slideAndHide(last_arm, {
                afterFinish: function() {
                    last_arm.remove();
                    controlLastDelButton();
                }
            });
        }

        function controlLastDelButton() {
            var added_arms = $$('.added-arm');
            if (added_arms.length < 1) {
                $('del-arm-button').disabled = true;
            } else {
                $('del-arm-button').disabled = false;
            }
        }

        function slideAndHide(element, options) {
            var e = $(element);
            new Effect.Parallel(
                [
                    new Effect.BlindUp(e, {sync:true}),
                    new Effect.Fade(e, {sync:true})
                ], $H(options).merge({
                    duration:1.0
                })
            );
        }

        function slideAndShow(element, options) {
            var e = $(element);
            new Effect.Parallel(
                [
                    new Effect.BlindDown(e, {sync:true}),
                    new Effect.Appear(e, {sync:true})
                ], $H(options).merge({
                    duration:1.0
                })
            );
        }

        function registerHandlers() {
            Event.observe('arms-yes', 'click', function() { slideAndShow('arm-names-group') });
            Event.observe('arms-no',  'click', function() { slideAndHide('arm-names-group') });
            Event.observe('add-arm-button', 'click', addArmName);
            Event.observe('add-arm-button', 'click', controlLastDelButton);
            Event.observe('del-arm-button', 'click', delLastArmName);
        }

        Event.observe(window, 'load', registerHandlers);
        Event.observe(window, 'load', controlLastDelButton);
    </script>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 20em;
        }
    </style>
</head>
<body>
<h1>${action} Study</h1>

<form action="<c:url value=""/>" method="post">
    <div class="row">
        <div class="label">
            <label for="study-name">Study name</label>
        </div>
        <div class="value">
            <input type="text" id="study-name" name="studyName"/>
        </div>
    </div>
    <div class="row">
        <div class="label">
            Multiple arms?
        </div>
        <div class="value">
            <label><input type="radio" id="arms-yes" name="arms" value="yes"/> Yes</label>
            <label><input type="radio" id="arms-no"  name="arms" value="no" checked="checked"/> No</label>
        </div>
    </div>
    <!-- Scriptaculous requires the display:none to be on the element directly. -->
    <div id="arm-names-group" style="display: none">
        <div class="row arm-name">
            <div class="label"><label for="arm-name-0">Arm 1 name</label></div>
            <div class="value"><input type="text" id="arm-name-0" name="armName[0]"/></div>
        </div>
        <div class="row arm-name">
            <div class="label"><label for="arm-name-1">Arm 2 name</label></div>
            <div class="value"><input type="text" id="arm-name-1" name="armName[1]"/></div>
        </div>
        <div class="row" id="arm-names-group-buttons">
            <div class="value submit">
                <input type="button" id="del-arm-button" value="Remove last arm"/>
                <input type="button" id="add-arm-button" value="Add arm"/>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="value submit">
            <input type="submit" value="Create"/>
        </div>
    </div>
</form>

</body>
</html>