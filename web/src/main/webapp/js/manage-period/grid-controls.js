/*global psc jQuery */
psc.namespace("template.mpa");

psc.template.mpa.GridControls = (function ($) {
  var Model = psc.template.mpa.Model;
  var currentAction, moveOrigin;
  
  function actionChanged(evt, data) {
    currentAction = data;
  }
  
  function row(blockId, number) {
    return $('#' + blockId + ' tr.activity').eq(number);
  }
  
  function cellClick(evt) {
    var cell = $(this).closest('.cell');
    
    if (cell.is('.in-progress')) {
      return false;
    }
    
    var clickData = Model.cellData(cell);
    clickData.action = currentAction;
    if (currentAction.name === 'add') {
      var pop = $('#population-selector').val();
      clickData.population = pop === '' ? null : pop;
      addClick(clickData);
    } else if (currentAction.name === 'delete') {
      deleteClick(clickData);
    } else if (currentAction.name == 'move') {
      if (!clickData.href && moveOrigin) {
        clickData.href = moveOrigin.href;
      }
      if (!clickData.population && moveOrigin) {
        clickData.population = moveOrigin.population;
      }
      moveClick(clickData);
    } else {
      throw "unimplemented action " + currentAction.name + ", " + currentAction.step;
    }
    return false;
  }
  
  function addClick(clickData) {
    if (clickData.occupied()) {
      clickData.message = "There is already an activity in that row and column.";
      $('#days').trigger('action-error', clickData);
    } else {
      $('#days').trigger('action-started', clickData);
    }
  }
  
  function deleteClick(clickData) {
    if (clickData.occupied()) {
      $('#days').trigger('action-started', clickData);
    } else {
      clickData.message = "There is no activity in that row and column.";
      $('#days').trigger('action-error', clickData);
    }
  }
  
  function moveClick(clickData) {
    if (currentAction.step === 0) {
      startMove(clickData);
    } else if (currentAction.step === 1) {
      completeMove(clickData);
    } else {
      throw "unimplemented action " + currentAction.name + ", " + currentAction.step;
    }
  }
  
  function startMove(clickData) {
    if (clickData.occupied()) {
      moveOrigin = clickData;
      $('#days').trigger('action-started', clickData);
      $('#days').trigger('action-changed', { name: 'move', step: 1 });
    } else {
      clickData.message = "Click on the activity you'd like to move, first.";
      $('#days').trigger('action-error', clickData);
    }
  }
  
  function completeMove(clickData) {
    clickData.startColumn = moveOrigin.column;
    if (clickData.row !== moveOrigin.row) {
      clickData.message = "You can only move activities within the same row.";
      $('#days').trigger('action-error', clickData);
      resetMove();
    } else if (clickData.occupied() || clickData.column === moveOrigin.column) {
      clickData.message = 
        "To move, click in an empty cell elsewhere in the row.  To cancel, click outside the grid.";
      $('#days').trigger('action-error', clickData);
    } else {
      $('#days').trigger('action-started', clickData);
      resetMove();
    }
  }
  
  function resetMove() {
    if (moveOrigin) {
      $('#days').trigger('action-changed', { name: 'move', step: 0 });
      moveOrigin = null;
    }
  }
  
  return {
    init: function () {
      $('#days').bind('action-changed', actionChanged).bind('row-added', function (evt, data) {
        Model.findRow('days', data.row).find('td.cell').click(cellClick);
      });
      $('#days td.cell').click(cellClick);
      $('body').click(resetMove);
    },
    
    reset: function () {
      currentAction = null;
      moveOrigin = null;
    }
  };
}(jQuery));
