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
    controlBox.appendChild(renameControl);
    SC.inPlaceEdit(armA, renameControl.href, {
        externalControl: renameControl.id,
        externalControlOnly: true,
        highlight: false,
        clickToEditText: armA.title
    })

    var deleteControl = createDeleteControl('arm', armId)
    controlBox.appendChild(deleteControl)
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the arm '" + armA.textContent +
                "'?  This will permanently remove it, all its periods, and its events.  " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ))
}

function createStudyControls() {
    var h1 = $$("h1")[0];
    var studyId = ${param.study}
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    h1.appendChild(controlBox)

    var renameControl = createRenameControl('study', studyId)
    controlBox.appendChild(renameControl)
    SC.inPlaceEdit("study-name", renameControl.href, {
        externalControl: renameControl.id, 
        clickToEditText: "Click to rename"
    })

    var addEpochControl = createAddControl("Add epoch", 'study', studyId)
    SC.asyncLink(addEpochControl, {}, "epochs-indicator")
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
    controlBox.appendChild(addArmControl)

    var renameControl = createRenameControl('epoch', epochId)
    controlBox.appendChild(renameControl)
    SC.inPlaceEdit(epochName, renameControl.href, {
        externalControl: renameControl.id,
        clickToEditText: "Click to rename"
    })

    var deleteControl = createDeleteControl('epoch', epochId)
    controlBox.appendChild(deleteControl)
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the epoch '" + epochName.textContent +
                "'?  This will permanently remove it, all its arms, its periods, and its events. " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ));
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

function createControlAnchor(controlName, text, baseHref, objectType, objectId) {
    return Builder.node("a", {
        className: objectType + '-control',
        id: objectType + "-" + objectId + "-" + controlName,
        href: baseHref + '?' + objectType + '=' + objectId
    }, text)
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