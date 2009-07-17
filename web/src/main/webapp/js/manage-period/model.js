psc.namespace('template.mpa');

/** TODO: this module needs tests */

psc.template.mpa.Model = (function ($) {
  function findCell(row, col) {
    if (row === row + 0) {
      row = findRow('days', row);
    }
    return row.find('td.cell:eq(' + col + ')');
  }

  return {
    activity: function(row) {
      var activityDefRow = this.findRow('activities', row);
      return {
        code:   $.trim(activityDefRow.attr('activity-code')),
        name:   $.trim(activityDefRow.find('.activity-name').text()),
        type:   $.trim(activityDefRow.parent('tbody').find('tr.activity-type .text').text()),
        source: $.trim(activityDefRow.attr('activity-source'))
      };
    },

    findRow: function(columnName, row) {
      return $('#' + columnName + ' tr.activity:eq(' + row + ')');
    },

    cellData: function(cell) {
      var rowNumber = this.rowNumberFor(cell);
      var colNumber = cell.closest('tr.activity').children('td.cell').index(cell);

      var notesRow = this.findRow('notes', rowNumber);

      var pop = cell.find('.marker').text().trim();
      if (pop && !pop.match(/\w/)) { pop = null; }

      return {
        activity: this.activity(rowNumber),
        day: $('#days-heading td').eq(colNumber).attr('day'),
        population: pop,
        details:   $.trim(notesRow.find('.details').text()),
        condition: $.trim(notesRow.find('.condition').text()),
        labels:    $.trim(notesRow.find('.labels').text()).replace(/\s+/g, ' '),
        weight:    $.trim(notesRow.find('.weight').text()),
        row:    rowNumber,
        column: colNumber,
        cell:   cell,
        occupied: function () {
          return cell.find('.marker').length != 0;
        },
        href: cell.find('.marker').attr('resource-href')
      }
    },

    rowNumberFor: function(element) {
      var row = $(element).closest('tr.activity');
      return $(element).closest('table').find('tr.activity').index(row);
    }
  };
}(jQuery));