if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  selectDisplayedNotes: function(evt) {
    var srcHref;
    if (evt && evt.type == 'click') {
      srcHref = Event.element(evt).href
    } else {
      srcHref = location.href
    }
    
    var anchorStart = srcHref.indexOf('#')
    var selectedNoteType
    if (anchorStart >= 0) {
      selectedNoteType = srcHref.substring(anchorStart + 1)
    } else {
      selectedNoteType = "details" 
    }
    
    $w("details conditions labels").
      reject(function(t) { return t == selectedNoteType }).
      each(SC.MP.hideNoteType)
    SC.MP.showNoteType(selectedNoteType)
  },
  
  flipNoteType: function(type, show) {
    console.log(show + " " + type)
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
    $('notes-preview').hide()
  },
  
  updateNotePreview: function(evt) {
    var editButton = Event.element(evt)
    var notesRow = Event.findElement(evt, "tr")
    var rowClass = $w(notesRow.className).detect(function(clz) { return clz.substring(0, 4) == "row-" })
    
    var box = $('notes-preview')
    
    // update contents
    box.select('h2').first().innerHTML = 
      $$("#activities ." + rowClass + " td").first().title
    $w('details conditions labels').each(function(noteKind) {
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
  }
})

$(document).observe("dom:loaded", function() {
  SC.MP.selectDisplayedNotes();
  $$("#notes-heading ul a").each(function(a) {
    $(a).observe("click", SC.MP.selectDisplayedNotes)
  })
  $$(".notes-edit").each(SC.MP.registerNotePreviewHandler)
})