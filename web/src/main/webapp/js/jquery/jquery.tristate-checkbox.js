/**
 * This is a tri-state checkbox which switches between
 * the states unchecked, checked, intermediate.
 *
 * This code is based of the code written at:
 * http://github.com/bcollins/jquery.tristate/blob/master
 */
(function($) {
    $.fn.tristate = function(settings) {
      var config = {
          initialState: 'unchecked'
      };

      if (settings) $.extend(config, settings);

      var getNextState = function(state) {
          switch (state) {
          case 'intermediate':
              return 'checked';
          case 'unchecked':
              return 'checked';
          case 'checked':
              return 'unchecked';
          }
      }

      var isStateChecked = function(state) {
        switch (state) {
          case 'intermediate' :
            return true;
          case 'checked':
            return true;
          default:
            return false;
        }
      }

      var getState = function(checkbox) {
        return $(checkbox).attr('state');
      }

      var update = function(checkbox, state) {
        var c = $(checkbox);
        c.attr('state', state);
        c.attr('checked', isStateChecked(state));
        c.trigger('tristate-state-change');
      }

      var isValidState = function(testing) {
        if (testing === 'unchecked' || testing === 'checked' || testing === 'intermediate') {
          return true;
        } else {
          return false;
        }
      }

      this.each(function() {
        var state = isValidState(config.initialState) ? config.initialState : 'unchecked';
        update(this, state);
        $(this).click(function() {
          var current = getState(this);
          update(this, getNextState(current));
        });
      });
    };
})(jQuery);