<%@page contentType="text/javascript" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
function createAllArmControls() {
    $$('#epochs li.arm').each(createArmEditControls)
}

function createArmEditControls(armItem) {
    var armA = armItem.getElementsByTagName("A")[0];
    var armId = armA.id.substring(4)
    var controlBox = Builder.node("div", {className: 'arm-controls controls'});
    armItem.appendChild(controlBox)

    var renameControl = createRenameControl('arm', armId)
    controlBox.appendChild(renameControl);
    SC.inPlaceEdit(armA, renameControl.href, {
        externalControl: renameControl.id,
        externalControlOnly: true,
        highlight: false,
        clickToEditText: armA.title
    })
}

function createRenameControl(objectType, objectId) {
    return Builder.node("a", {
        className: objectType + '-control',
        id: objectType + "-" + objectId + "-rename",
        href: '<c:url value="/pages/template/rename"/>' + '?' + objectType + '=' + objectId
    }, "Rename")
}

function createStudyControls() {
    var h1 = $$("h1")[0];
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    h1.appendChild(controlBox)

    var renameControl = createRenameControl('study', ${param.study})
    controlBox.appendChild(renameControl)
    SC.inPlaceEdit("study-name", renameControl.href, { externalControl: renameControl.id })
}

function createAllEpochControls() {
    $$("#epochs h4").each(createEpochControls)
}

function createEpochControls(epochH4) {
    var controlBox = Builder.node("div", {className: 'epoch-controls controls'});
    epochH4.appendChild(controlBox);

    var epochId = epochH4.id.split('-')[1]
    var renameControl = createRenameControl('epoch', epochId)
    controlBox.appendChild(renameControl)
    SC.inPlaceEdit('epoch-' + epochId + '-name', renameControl.href, {externalControl: renameControl.id})
}

