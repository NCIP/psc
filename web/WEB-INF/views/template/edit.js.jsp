<%@page contentType="text/javascript" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
function createAllArmControls() {
    $$('#epochs li.arm').each(createArmControls)
}

function createArmControls(armItem) {
    var armA = armItem.getElementsByTagName("A")[0];
    var armId = armA.id.substring(4)
    var controlBox = Builder.node("div", {className: 'arm-controls controls'});
    armItem.appendChild(controlBox)

    var renameControl = createRenameControl('arm', armId)
    SC.inPlaceEdit(armA, renameControl.href, {
        externalControl: renameControl,
        externalControlOnly: true,
        highlight: false,
        clickToEditText: armA.title
    })

    var deleteControl = createDeleteControl('arm', armId)
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the arm '" + armA.innerHTML +
                "'?  This will permanently remove it, all its periods, and its events.  " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ))

    var moveUpControl   = createMoveControl(-1, '&#9650;', 'arm', armId)
    SC.asyncLink(moveUpControl, {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9660;', 'arm', armId)
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateArmControlVisibility('arm-' + armId + '-item')
}

function updateArmControlVisibility(armItem) {
    var thisArm = $(armItem)
    var siblings = $A(thisArm.parentNode.getElementsByTagName("LI"))

    updateMoveControlVisibility('arm', thisArm.id.split('-')[1], thisArm, siblings)
    updateDeleteControlVisibility('arm', siblings);
}

function updateAllArmsControlVisibility(epochId) {
    var armItems = $$('#epoch-' + epochId + '-arms li');
    armItems.each(updateArmControlVisibility)
}

function createStudyControls() {
    var h1 = $$("h1")[0];
    var studyId = ${param.study}
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    h1.appendChild(controlBox)

    var renameControl = createRenameControl('study', studyId)
    SC.inPlaceEdit("study-name", renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename"
    })

    var addEpochControl = createAddControl("Add epoch", 'study', studyId)
    SC.asyncLink(addEpochControl, {}, "epochs-indicator")

    controlBox.appendChild(renameControl)
    controlBox.appendChild(addEpochControl)
}

function createAllEpochControls() {
    $$("#epochs h4").each(createEpochControls)
}

function createEpochControls(epochH4) {
    var controlBox = Builder.node("div", {className: 'epoch-controls controls'});
    epochH4.appendChild(controlBox);
    var epochId = epochH4.id.split('-')[1]
    var epochName = $('epoch-' + epochId + '-name')

    var addArmControl = createAddControl("Add arm", 'epoch', epochId)
    SC.asyncLink(addArmControl, {}, "epochs-indicator")

    var renameControl = createRenameControl('epoch', epochId)
    SC.inPlaceEdit(epochName, renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename"
    })

    var deleteControl = createDeleteControl('epoch', epochId)
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the epoch '" + epochName.innerHTML +
                "'?  This will permanently remove it, all its arms, its periods, and its events. " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ));

    var moveUpControl   = createMoveControl(-1, '&#9668;', 'epoch', epochId)
    SC.asyncLink(moveUpControl,   {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9658;', 'epoch', epochId)
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(addArmControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateEpochControlVisibility('epoch-' + epochId)
}

function updateEpochControlVisibility(epochElt) {
    var thisEpoch = $(epochElt)
    var siblings = $$('div.epoch')

    updateMoveControlVisibility('epoch', thisEpoch.id.split('-')[1], thisEpoch, siblings)
    updateDeleteControlVisibility('epoch', siblings)
}

function updateAllEpochsControlVisibility() {
    $$('div.epoch').each(updateEpochControlVisibility)
}

function createRenameControl(objectType, objectId) {
    return createControlAnchor("rename", "Rename", '<c:url value="/pages/template/rename"/>', objectType, objectId)
}

function createDeleteControl(objectType, objectId) {
    return createControlAnchor("delete", "Delete", '<c:url value="/pages/template/delete"/>', objectType, objectId)
}

function createAddControl(text, objectType, objectId) {
    return createControlAnchor("add", text, '<c:url value="/pages/template/addTo"/>', objectType, objectId)
}

function createMoveControl(offset, text, objectType, objectId) {
    return createControlAnchor("move" + offset, text, '<c:url value="/pages/template/move?offset="/>' + offset, objectType, objectId)
}

function createControlAnchor(controlName, text, baseHref, objectType, objectId) {
    var href = baseHref;
    if (href.indexOf('?') >= 0) {
        href += '&'
    } else {
        href += '?'
    }
    href += objectType + '=' + objectId
    var a = Builder.node("a", {
        className: objectType + '-' + controlName + '-control ' + objectType + '-control control',
        id: objectType + "-" + objectId + "-" + controlName,
        href: href
    })
    a.innerHTML = text
    return a
}

function deleteHandler(confirmMessageFn, link) {
    return function(e) {
        Event.stop(e);
        if (window.confirm(confirmMessageFn())) {
            $('epochs-indicator').reveal()
            new Ajax.Request(link, {
                onComplete: function() {
                    $('epochs-indicator').conceal()
                }
            })
        }
    }
}

function updateMoveControlVisibility(objectType, objectId, thisElement, siblings) {
    var isFirst = thisElement == siblings[0]
    var isLast = thisElement == siblings.last()

    var downControl = $(objectType + '-' + objectId + '-move1')
    if (isLast) {
        downControl.conceal();
    } else {
        downControl.reveal();
    }

    var upControl = $(objectType + '-' + objectId + '-move-1' )
    if (isFirst) {
        upControl.conceal();
    } else {
        upControl.reveal();
    }

    if (isFirst && isLast) {
        upControl.hide(); downControl.hide();
    } else {
        upControl.show(); downControl.show();
    }
}

function updateDeleteControlVisibility(objectType, elts) {
    var controls = $$('.' + objectType + '-delete-control')
    if (elts.length <= 1) {
        controls.each(function(c) { c.hide() })
    } else {
        controls.each(function(c) { c.show() })
    }
}