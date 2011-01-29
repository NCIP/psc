/*global window jQuery */
psc.namespace("tools");

/**
 * Encapsulates the pattern where you need to respond to a change in a value,
 * but the response takes longer than the interval between updates.  This 
 * object coalesces updates and invokes the response periodically (by default
 * every [20ms + response execution time]).
 */

psc.tools.AsyncUpdater = function(updateFn, comparableValueFn) {
  var self = this;
  var requestedValue = null, currentValue = null;
  var timeoutHandle;

  if (comparableValueFn) {
    self.comparableValueFn = comparableValueFn;
  } 

  function needsUpdate() {
    return (requestedValue && self.comparableValueFn(requestedValue) !== self.comparableValueFn(currentValue))
        || (requestedValue == null && currentValue != null);
  }

  function updater() {
    if (needsUpdate()) {
      updateFn(requestedValue);
      currentValue = requestedValue;
    }
    timeoutHandle = setTimeout(updater, self.refreshTime);
  }
  timeoutHandle = setTimeout(updater, this.refreshTime);

  this.update = function (newValue) {
    requestedValue = newValue;
  };
  
  this.stop = function () {
    clearTimeout(timeoutHandle);
  }

  // Accessor for current value for debugging.  Don't modify the object returned here.
  this.current = function() {
    return currentValue;
  }
}

psc.tools.AsyncUpdater.prototype.refreshTime = 20; // ~50 fps

psc.tools.AsyncUpdater.prototype.comparableValueFn = function (v) {
  return v;
};