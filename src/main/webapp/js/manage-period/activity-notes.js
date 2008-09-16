if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  NOTE_TYPES: $w("details condition labels"),
  DEFAULT_NOTE_TYPE: "details",

  selectDisplayedNotes: function(evt) {
    var srcHref;
    if (evt && evt.type == 'click') {
      srcHref = Event.element(evt).href
    } else {
      srcHref = location.href
    }
    
    var anchorStart = srcHref.indexOf('#')
    var selectedNoteType = SC.MP.DEFAULT_NOTE_TYPE
    if (anchorStart >= 0) {
      var anchorName = srcHref.substring(anchorStart + 1)
      if (SC.MP.NOTE_TYPES.include(anchorName)) {
        selectedNoteType = anchorName
      }
    }
    
    SC.MP.NOTE_TYPES.
      reject(function(t) { return t == selectedNoteType }).
      each(SC.MP.hideNoteType)
    SC.MP.showNoteType(selectedNoteType)
  },
  
  flipNoteType: function(type, show) {
    $$("#notes span." + type).invoke(show)
  },
  
  showNoteType: function(type) {
    SC.MP.flipNoteType(type, "show")
    $$("#notes-heading li." + type).first().addClassName("selected")
  },
  
  hideNoteType: function(type) {
    SC.MP.flipNoteType(type, "hide")
    $$("#notes-heading li." + type).first().removeClassName("selected")
  },
  
  registerNotePreviewHandler: function(editButton) {
    $(editButton).observe('mouseout', SC.MP.hideNotePreview)
    $(editButton).observe('mouseover', SC.MP.updateNotePreview)
    $('notes-preview').observe('mouseover', function() { $('notes-preview').show() })
    $('notes-preview').observe('mouseout', SC.MP.hideNotePreview)
  },
  
  hideNotePreview: function(evt) {
    var box = $('notes-preview')
    box.hide()
  },
  
  updateNotePreview: function(evt) {
    var editButton = Event.element(evt)
    var notesRow = Event.findElement(evt, "tr")
    var rowClass = SC.MP.findRowIndexClass(notesRow)

    var box = $('notes-preview')
    $w(box.className).each(function(clz) {
      if (clz.substring(0, 4) == "row-") {
        box.removeClassName(clz)
      }
    })
    box.addClassName(rowClass)

    // update contents
    box.select('h2').first().innerHTML = 
      $$("#activities ." + rowClass + " td").first().title
    SC.MP.NOTE_TYPES.each(function(noteKind) {
      var content = notesRow.select("." + noteKind).first().innerHTML.strip()
      var elt = $(noteKind + "-preview")
      if (content.length == 0) {
        elt.innerHTML = "None"
        elt.addClassName("none")
      } else {
        elt.innerHTML = content
        elt.removeClassName("none")
      }
    })
    
    // reposition box
    var boxHeight = box.getDimensions().height
    box.style.top = ($('notes').scrollTop + 18) + "px"
    box.style.height = boxHeight + "px"
    
    box.show()
  },

  clickEditButton: function(evt) {
    var editButton = Event.element(evt)
    SC.MP.editNotes(editButton.up("tr"))
  },

  clickNotesPreview: function(evt) {
    var rowClass = SC.MP.findRowIndexClass($('notes-preview'))
    SC.MP.editNotes($$("#notes tr." + rowClass).first())
  },

  editNotes: function(notesRow) {
    var rowClass = SC.MP.findRowIndexClass(notesRow)
    var activity = SC.MP.findActivity(rowClass.substring(4))
    $$(".column ." + rowClass).invoke("addClassName", "emphasized")
    $$("#edit-notes-lightbox .activity-name").invoke("update", activity.name)
    SC.MP.NOTES_OBSERVERS = { }
    $w("details condition labels").each(function(noteKind) {
      var noteSpan = notesRow.select(".notes-content ." + noteKind).first();
      var noteInput = $('edit-notes-' + noteKind);
      // copy in current values from spans
      noteInput.value = noteSpan.innerHTML.unescapeHTML().strip()
      SC.applyInputHint(noteInput)
      // observe the text field and update the span
      SC.MP.NOTES_OBSERVERS[noteKind] = new Form.Element.Observer(
        noteInput,
        0.4,
        function(e, value) {
          if (noteInput.hasClassName("input-hint")) {
            noteSpan.innerHTML = ""
          } else {
            noteSpan.innerHTML = value.strip()
          }
        }
      )
    })
    $('edit-notes-lightbox').addClassName(rowClass)
    $('edit-notes-lightbox').show()
    $$("#edit-notes-lightbox input").invoke("enable")
    LB.Lightbox.activate()
  },

  finishEditingNotes: function(evt) {
    $$("#edit-notes-lightbox input").invoke("disable")
    Object.values(SC.MP.NOTES_OBSERVERS).invoke("stop")

    var rowClass = SC.MP.findRowIndexClass($('edit-notes-lightbox'))
    $('edit-notes-lightbox').removeClassName(rowClass)
    
    var rowIndex = rowClass.substring(4)
    var cells = $$("#days ." + rowClass + " .cell")
    var success = function(r, c) {
      return function() {
        SC.MP.reportInfo("Successfully updated notes for " + r + ", " + c)
      }
    }

    for (var col = 0 ; col < cells.length ; col++) {
      var cell = cells[col]
      var marker = cell.select(".marker").first()
      if (marker) {
        cell.addClassName("pending")
        SC.MP.putPlannedActivity(marker.getAttribute("resource-href"), rowIndex, col,
          SC.MP.plannedActivityAjaxOptions(success(rowIndex, col), cell)
        )
      }
    }
    $$(".column ." + rowClass).invoke("removeClassName", "emphasized")
    LB.Lightbox.deactivate()
  }
})

$(document).observe("dom:loaded", function() {
  SC.MP.selectDisplayedNotes();
  $$("#notes-heading ul a").each(function(a) {
    $(a).observe("click", SC.MP.selectDisplayedNotes)
  })
  $$(".notes-edit").each(SC.MP.registerNotePreviewHandler)
  $$(".notes-edit").each(function(button) { button.observe("click", SC.MP.clickEditButton) })
  $('notes-preview').observe("click", SC.MP.clickNotesPreview)
  $('edit-notes-done').observe('click', SC.MP.finishEditingNotes)
  $$('#edit-notes-lightbox input').each(function(elt) {
    elt.observe('keyup', function(evt) {
      if (evt.keyCode == Event.KEY_RETURN) { SC.MP.finishEditingNotes(evt) }
    })
  })
})