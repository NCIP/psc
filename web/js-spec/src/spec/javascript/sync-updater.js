/**
 * Testing substitute for AsyncUpdater which behaves synchronously.  This
 * is necessary because rhino/envjs does not respond well to setTimeout being
 * called thousands of times.
 */

/*global window */
if (!window.psc) { var psc = { }; }
if (!psc.tools) { psc.tools = { }; }

psc.tools.AsyncUpdater = function(updateFn, comparableValueFn) {
  var self = this;
  var requestedValue = null, currentValue = null;
 
  if (comparableValueFn) {
    self.comparableValueFn = comparableValueFn;
  } 
 
  function needsUpdate() {
    return (requestedValue && self.comparableValueFn(requestedValue) !== self.comparableValueFn(currentValue))
        || (requestedValue === null && currentValue !== null);
  }
 
  function doUpdate() {
    if (needsUpdate()) {
      updateFn(requestedValue);
      currentValue = requestedValue;
    }
  }
 
  this.update = function (newValue) {
    requestedValue = newValue;
    doUpdate();
  };
 
  this.stop = function () {
    // No-op for sync operation
  }
}

psc.tools.AsyncUpdater.prototype.refreshTime = 20; // ~50 fps

psc.tools.AsyncUpdater.prototype.comparableValueFn = function (v) {
  return v;
};