<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <title>${action} Study</title>
    <tags:includeScriptaculous/>
    <script type="text/javascript">
        function addArmName(epochIndex, original) {
            var groupId = 'arms-' + epochIndex + '-group';
            var group = $(groupId);
            var newIdx = $$('#' + groupId + ' .arm-name').length;
            var newRow = createArmNameRow(newIdx, epochIndex, false);
            newRow.style.display = "none";
            group.insertBefore(newRow, $('arms-' + epochIndex + '-group-buttons'));
            SC.slideAndShow(newRow);
        }

        function createArmNameRow(rowIdx, epochIndex, original) {
            var rowInputId = "arm-name-" + epochIndex + '-' + rowIdx;
            var rowNum = rowIdx + 1
            var rowClasses = 'row arm-name'
            if (!original) rowClasses += ' added-arm'

            return Builder.node('div', {className: rowClasses}, [
                Builder.node('div', {className:'label'}, [
                    Builder.node('label', {'for': rowInputId}, "Arm " + rowNum)
                ]),
                Builder.node('div', {className:'value'}, [
                    Builder.node('input', {type:'text', id:rowInputId, name:"armNames[" + epochIndex + "][" + rowIdx + "]"})
                ])
            ]);
        }

        function addEpoch() {
            var newIdx = $$('.epoch-group').length
            var newNum = newIdx + 1;
            var newId = 'epoch-' + newIdx + '-group'

            var newGroup = Builder.node('div', {className: 'epoch-group added-epoch'}, [
                Builder.node('div', {className: 'row' }, [
                    Builder.node('div', {className:'label'}, [
                        Builder.node('label', {'for':newId}, 'Epoch ' + newNum)
                    ]),
                    Builder.node('div', {className:'value'}, [
                        Builder.node('input', {type:'text', id:'epoch-name-' + newIdx, name:'epochNames[' + newIdx + ']'}),
                        "\n",
                        Builder.node('span', {}, [
                            'Multiple arms?',
                            "\n",
                            Builder.node('label', {}, [
                                Builder.node('input', {
                                    type:'radio', id:'multiple-arms-' + newIdx + '-yes',
                                    name:'arms[' + newIdx + ']', value:'true'}),
                                "Yes"
                            ]),
                            "\n",
                            Builder.node('label', {}, [
                                Builder.node('input', {
                                    type:'radio', id:'multiple-arms-' + newIdx + '-no',
                                    name:'arms[' + newIdx + ']', value:'false', checked:'checked'}),
                                "No"
                            ])
                        ])
                    ])
                ]),
                Builder.node('div', {className:'arms-group', id:'arms-' + newIdx + '-group', style:'display: none'}, [
                    createArmNameRow(0, newIdx, true),
                    createArmNameRow(1, newIdx, true),
                    Builder.node('div', {className:'row submit', id:'arms-' + newIdx + '-group-buttons'}, [
                        Builder.node('input', {type:'button', id:'add-arm-button-' + newIdx, value:'Add arm'}),
                        "\n",
                        Builder.node('input', {type:'button', id:'del-arm-button-' + newIdx, value:'Remove last arm'})
                    ])
                ])
            ]);
            newGroup.style.display = 'none';

            var parent = document.getElementsByTagName("FORM")[0]
            parent.insertBefore(newGroup, $('form-submit-row'))

            registerArmGroupHandlers(newIdx)
            controlLastArmDelButton(newIdx)

            SC.slideAndShow(newGroup)
        }

        function delLastItem(itemsSelector, buttonControlFn) {
            var items = $$(itemsSelector)
            if (items.length < 0) return
            var last = items[items.length - 1]
            SC.slideAndHide(last, {
                afterFinish: function() {
                    last.remove();
                    buttonControlFn();
                }
            })
        }

        function controlLastDelButton(itemsSelector, buttonId) {
            $(buttonId).disabled = $$(itemsSelector).length < 1
        }

        function controlLastArmDelButton(epochIndex) {
            controlLastDelButton('#arms-' + epochIndex +'-group .added-arm', 'del-arm-button-' + epochIndex)
        }

        function delLastArmName(epochIndex) {
            delLastItem('#arms-' + epochIndex +'-group .added-arm', function() { controlLastArmDelButton(epochIndex) })
        }

        function controlLastEpochDelButton() {
            controlLastDelButton('.added-epoch', 'del-epoch-button');
        }

        function delLastEpoch() {
            delLastItem('.added-epoch', controlLastEpochDelButton);
        }

        function registerArmGroupHandlers(epochIndex) {
            Event.observe('multiple-arms-' + epochIndex + '-yes', 'click', function() { SC.slideAndShow('arms-' + epochIndex + '-group') });
            Event.observe('multiple-arms-' + epochIndex + '-no',  'click', function() { SC.slideAndHide('arms-' + epochIndex + '-group') });
            Event.observe('add-arm-button-' + epochIndex, 'click', function() { addArmName(epochIndex) });
            Event.observe('add-arm-button-' + epochIndex, 'click', function() { controlLastArmDelButton(epochIndex) });
            Event.observe('del-arm-button-' + epochIndex, 'click', function() { delLastArmName(epochIndex) });
        }

        function registerInitialHandlers() {
            $$('.epoch-group').each(function(elt) {
                eId = elt.id.split('-')[1];
                registerArmGroupHandlers(eId);
            })
            Event.observe('add-epoch-button', 'click', addEpoch);
            Event.observe('add-epoch-button', 'click', controlLastEpochDelButton);
            Event.observe('del-epoch-button', 'click', delLastEpoch);
        }

        Event.observe(window, 'load', registerInitialHandlers);
        Event.observe(window, 'load', controlLastEpochDelButton);
        Event.observe(window, 'load', function() { controlLastArmDelButton(0) });
    </script>
    <style type="text/css">
        form {
            width: 34em;
        }
        div.label {
            width: 8em;
            text-align: right;
            padding-right: 1em;
            padding-top: 4px;
        }
        /*div.value {
            margin-left: 8em;
        }*/
        div.submit {
            text-align: right;
        }
        .epoch-group, #form-submit-row {
            margin-top: 3px;
            border-top: 1px solid black;
        }
        .arms-group {
            margin-left: 1em;
        }

        #form-submit-row input.submit {
            margin-left: 2em
        }
    </style>
</head>
<body>
<h1>${action} Study</h1>

<form:form method="post">
    <div class="row">
        <div class="label">
            <label for="study-name">Study name</label>
        </div>
        <div class="value">
            <input type="text" id="study-name" name="studyName"/>
        </div>
    </div>
    <div class="epoch-group" id="epoch-0-group">
        <div class="row">
            <div class="label"><label for="epoch-name-0">Epoch 1</label></div>
            <div class="value">
                <form:input path="epochNames[0]" id="epoch-name-0"/>
                <span>
                    Multiple arms?
                    <label><form:radiobutton path="arms[0]" id="multiple-arms-0-yes" value="true"/> Yes</label>
                    <label><form:radiobutton path="arms[0]" id="multiple-arms-0-no"  value="false"/> No</label>
                </span>
            </div>
        </div>
        <!-- Scriptaculous requires the display:none to be on the element directly. -->
        <div class="arms-group" id="arms-0-group" style="display: none">
            <div class="row arm-name">
                <div class="label"><label for="arm-name-0-0">Arm 1</label></div>
                <div class="value"><input type="text" id="arm-name-0-0" name="armNames[0][0]"/></div>
            </div>
            <div class="row arm-name">
                <div class="label"><label for="arm-name-0-1">Arm 2</label></div>
                <div class="value"><input type="text" id="arm-name-0-1" name="armNames[0][1]"/></div>
            </div>
            <div class="row submit" id="arms-0-group-buttons">
                <input type="button" id="add-arm-button-0" value="Add arm"/>
                <input type="button" id="del-arm-button-0" value="Remove last arm"/>
            </div>
        </div>
    </div>
    <%--div class="row">
        <div class="label">
            Multiple arms?
        </div>
        <div class="value">
            <label><input type="radio" id="arms-yes" name="arms" value="yes"/> Yes</label>
            <label><input type="radio" id="arms-no"  name="arms" value="no" checked="checked"/> No</label>
        </div>
    </div>
    <!-- Scriptaculous requires the display:none to be on the element directly. -->
    <div id="arms-group" style="display: none">
        <div class="row arm-name">
            <div class="label"><label for="arm-name-0">Arm 1 name</label></div>
            <div class="value"><input type="text" id="arm-name-0" name="armNames[0]"/></div>
        </div>
        <div class="row arm-name">
            <div class="label"><label for="arm-name-1">Arm 2 name</label></div>
            <div class="value"><input type="text" id="arm-name-1" name="armNames[1]"/></div>
        </div>
        <div class="row" id="arms-group-buttons">
            <div class="value submit">
                <input type="button" id="del-arm-button" value="Remove last arm"/>
                <input type="button" id="add-arm-button" value="Add arm"/>
            </div>
        </div>
    </div--%>
    <div class="row" id="form-submit-row">
        <div class="value submit">
            <input type="button" id="add-epoch-button" value="Add epoch"/>
            <input type="button" id="del-epoch-button" value="Remove last epoch"/>
            <input type="submit" class="submit" value="Create"/>
        </div>
    </div>
</form:form>

</body>
</html>