<%@page contentType="text/javascript" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
function createAllStudySegmentControls() {
    $$('#epochs li.studySegment').each(function(li) {
        createStudySegmentControls(li)
    })
    $$('#epochs ul').each(function(ul) {
        _updateAllStudySegmentsControlVisibility(ul.id)
    })
}

//have to call this function separately in selectStudySegment.jsp        
function epochControls() {
    $$('.deletePeriod').each(function(td) {
        deletePeriodControls(td);
    })

    $$('.editPeriod').each(function(td){
        editPeriodControls(td)
    })

    addPeriodControl()
}

function createStudySegmentControls(studySegmentItem) {
    var studySegmentA = studySegmentItem.getElementsByTagName("A")[0];
    var studySegmentId = studySegmentA.id.substring('studySegment'.length+1)
    var controlBox = Builder.node("div", {className: 'studySegment-controls controls'});
    studySegmentItem.appendChild(controlBox)

    var renameControl = createRenameControl('studySegment', studySegmentId, null, 'study segment')
    SC.inPlaceEdit(studySegmentA, renameControl.href, {
        externalControl: renameControl,
        externalControlOnly: true,
        highlight: false,
        clickToEditText: studySegmentA.title
    })

    var deleteControl = createDeleteControl('studySegment', studySegmentId, 'study segment')
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the study segment '" + studySegmentA.innerHTML +
                "'?  This will permanently remove it, all its periods, and its events.  " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ))

    var moveUpControl   = createMoveControl(-1, '&#9650;', 'studySegment', studySegmentId, 'study segment')
    SC.asyncLink(moveUpControl, {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9660;', 'studySegment', studySegmentId, 'study segment')
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateStudySegmentControlVisibility('studySegment-' + studySegmentId + '-item')
}

function createDeletePeriodControls(periodItem) {
    var periodId = $(periodItem).readAttribute('periodId')
    var periodName = periodItem.readAttribute('periodName')
    var studySegmentId = periodItem.readAttribute('studySegmentId')
    periodItem.show();
    var href = '<c:url value="/pages/cal/deletePeriod"/>?period=' + periodId + "&studySegment=" + studySegmentId
     Event.observe(periodItem, "click", deletePeriod(function() {
            return "Are you sure you want to delete the period '" + periodName +
                "'?  This will permanently remove it and its events. " +
                "\n\nThis action cannot be undone."
        }, periodId, studySegmentId, href
    ))
}

function deletePeriod(confirmMessageFn, periodId, studySegmentId, link) {
    return function(e) {
       Event.stop(e);
        if (window.confirm(confirmMessageFn())) {
            var formdata = "";
            formdata = formdata + "period=" + periodId + "&";
            formdata = formdata+ "studySegment=" + studySegmentId + "&";
            $('epochs-indicator').reveal()
            new Ajax.Request(link, {
                postBody: formdata,
                onComplete: function() {
                    $('epochs-indicator').conceal()
                }
            })
            return true;
        }
    }
}

function populationAddButtonCreate() {
    var studyId = ${param.study};
    var populationLI = $$('.addPopulationButton')[0];
    var addPopulationHref = '<c:url value="/pages/cal/template/population?study="/>' + studyId;
    var addPopulationLink = Builder.node("a", {className: 'control', href: addPopulationHref});
    addPopulationLink.innerHTML='Add';
    populationLI.appendChild(addPopulationLink);
}

function deletePeriodControls(tdItem) {
    var aLinkPeriodId = tdItem.readAttribute('periodId');
    var aLinkPeriodName = tdItem.readAttribute('periodName');
    var aLinkPeriodstudySegmentId = tdItem.readAttribute('studySegmentId');
    var aLink = Builder.node("a", {className: 'control', periodName: aLinkPeriodName, periodId: aLinkPeriodId, studySegmentId: aLinkPeriodstudySegmentId, href: '#'});
    aLink.innerHTML='Delete';
    tdItem.appendChild(aLink);
    createDeletePeriodControls(aLink);
}

function editPeriodControls(tdItem) {
    var aLinkPeriodId = tdItem.readAttribute('periodId');
    var href ='<c:url value="/pages/cal/editPeriod?period="/>' + aLinkPeriodId;
    var aLink = Builder.node("a", {className: 'control', href: href});
    aLink.innerHTML='Edit';
    tdItem.appendChild(aLink);
}

function addPeriodControl() {
    var pItem= $$('.addPeriod')[0];
    var studySegmentId = pItem.readAttribute('studySegmentId');
    var href = '<c:url value="/pages/cal/newPeriod?studySegment="/>' + studySegmentId;
    var aLink = Builder.node("a", {className: 'control', href: href});
    aLink.innerHTML="Add period";
    pItem.insertBefore(aLink, pItem.firstChild);
}      

function updateStudySegmentControlVisibility(studySegmentItem) {
    var thisStudySegment = $(studySegmentItem)
    var siblings = $A(thisStudySegment.parentNode.getElementsByTagName("LI"))

    updateMoveControlVisibility('studySegment', thisStudySegment.id.split('-')[1], thisStudySegment, siblings)
}

function updateAllStudySegmentsControlVisibility(epochId) {
    _updateAllStudySegmentsControlVisibility('epoch-' + epochId + '-studySegments')
}

function _updateAllStudySegmentsControlVisibility(eltId) {
    var studySegmentItems = $$('#' + eltId + ' li');
    studySegmentItems.each(updateStudySegmentControlVisibility)
    updateDeleteControlVisibility('studySegment', studySegmentItems, eltId);
    if (studySegmentItems.length <= 1) {
        $$('#' + eltId + ' li .studySegment-rename-control').each(function(e) { e.hide() })
    } else {
        $$('#' + eltId + ' li .studySegment-rename-control').each(function(e) { e.show() })
    }
}

function createAddEpochControl() {
    var studyId = ${param.study}
    var addEpochControl = createAddControl("Add epoch", 'study', studyId, 'study')
    SC.asyncLink(addEpochControl, {}, "epochs-indicator")
    $('addEpoch').appendChild(addEpochControl)
}

function hideShowReleaseTemplateButton() {
    if ($('errorMessages').empty()) {
        $('errorMessages').hide()
    } else {
        $('errorMessages').show()
    }
}

function createStudyControls(anyProvidersAvailable, canEdit) {
    var h1 = $$("h1")[0];
    var studyId = ${param.study}
    var controlBox = Builder.node("span", {className: 'study-controls controls'})
    h1.appendChild(controlBox)

    var renameControl = createRenameControl('study', studyId, "Set protocol identifier", 'study')

    if (anyProvidersAvailable) {
        var externalStudyControl = createExternalStudyControl('study', studyId, "Associate with external study")
    }
    
    SC.inPlaceEdit("study-name", renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename", onComplete:function() {
            hideShowReleaseTemplateButton()
        }
    })

    controlBox.appendChild(renameControl)

    if (anyProvidersAvailable) {
        controlBox.appendChild(externalStudyControl)
    }
}

function createAllEpochControls() {
    $$("#epochs h4").each(createEpochControls)
}

function createEpochControls(epochH4) {
    var controlBox = Builder.node("div", {className: 'epoch-controls controls'});
    epochH4.appendChild(controlBox);
    var epochId = epochH4.id.split('-')[1]
    var epochName = $('epoch-' + epochId + '-name')

    var addStudySegmentControl = createAddControl("Add segment", 'epoch', epochId, 'epoch')
    SC.asyncLink(addStudySegmentControl, {}, "epochs-indicator")

    var renameControl = createRenameControl('epoch', epochId, null, 'epoch')
    SC.inPlaceEdit(epochName, renameControl.href, {
        externalControl: renameControl,
        clickToEditText: "Click to rename"
    })

    var deleteControl = createDeleteControl('epoch', epochId, 'epoch')
    Event.observe(deleteControl, "click", deleteHandler(function() {
            return "Are you sure you want to delete the epoch '" + epochName.innerHTML +
                "'?  This will permanently remove it, all its study segments, its periods, and its events. " +
                "\n\nThis action cannot be undone."
        }, deleteControl.href
    ));

    var moveUpControl   = createMoveControl(-1, '&#9668;', 'epoch', epochId, 'epoch')
    SC.asyncLink(moveUpControl,   {}, "epochs-indicator")
    var moveDownControl = createMoveControl( 1, '&#9658;', 'epoch', epochId, 'epoch')
    SC.asyncLink(moveDownControl, {}, "epochs-indicator")

    controlBox.appendChild(moveUpControl)
    controlBox.appendChild(addStudySegmentControl)
    controlBox.appendChild(renameControl)
    controlBox.appendChild(deleteControl)
    controlBox.appendChild(moveDownControl)

    updateEpochControlVisibility('epoch-' + epochId)
}

function updateEpochControlVisibility(epochElt) {
    var thisEpoch = $(epochElt)
    var siblings = $$('div.epoch')

    updateMoveControlVisibility('epoch', thisEpoch.id.split('-')[1], thisEpoch, siblings)
    updateDeleteControlVisibility('epoch', siblings, 'epochs')
}

function updateAllEpochsControlVisibility() {
    $$('div.epoch').each(updateEpochControlVisibility)
}


function createRenameControl(objectType, objectId, name, nameOfTheElement) {
    if(name == null) {
        name ="Set name"
    }
    return createControlAnchor("rename", name, "Change the name of this " + nameOfTheElement, '<c:url value="/pages/cal/template/rename"/>', objectType, objectId)
}

function createExternalStudyControl(objectType, objectId, name) {
    return createControlAnchor("external", name, "Associate with external study ", '<c:url value="/pages/cal/template/externalStudy"/>', objectType, objectId)
}

function createDeleteControl(objectType, objectId, nameOfTheElement) {
    return createControlAnchor("delete", "Delete", "Delete this " + nameOfTheElement, '<c:url value="/pages/cal/template/delete"/>', objectType, objectId)
}

function createAddControl(text, objectType, objectId, nameOfTheElement) {
    return createControlAnchor("add", text, "Add to this " + nameOfTheElement, '<c:url value="/pages/cal/template/addTo"/>', objectType, objectId)
}

function createMoveControl(offset, text, objectType, objectId, nameOfTheElement) {
    return createControlAnchor("move" + offset, text, "Reorder the " + nameOfTheElement + "s", '<c:url value="/pages/cal/template/move?offset="/>' + offset, objectType, objectId)
}

function createControlAnchor(controlName, text, title, baseHref, objectType, objectId) {
    var href = baseHref;
    if (href.indexOf('?') >= 0) {
        href += '&'
    } else {
        href += '?'
    }
    href += objectType + '=' + objectId
    if (objectType != 'study') href += '&study=${param.study}'
    var a = Builder.node("a", {
        className: objectType + '-' + controlName + '-control ' + objectType + '-control control',
        id: objectType + "-" + objectId + "-" + controlName,
        href: href,
        title: title
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

function updateDeleteControlVisibility(objectType, elts, containerId) {
    var controls = $$('#' + containerId + ' .' + objectType + '-delete-control')
    if (elts.length <= 1) {
        controls.each(function(c) { c.hide() })
    } else {
        controls.each(function(c) { c.show() })
    }
}