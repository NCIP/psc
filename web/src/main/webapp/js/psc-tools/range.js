if (!window.psc) { window.psc = { } }
if (!psc.tools) { psc.tools = { } }

psc.tools.Range = function(start, stop) {
  function includesRange(thisRange, otherRange) {
    return (thisRange.start <= otherRange.start) && (thisRange.stop >= otherRange.stop)
  }
  
  function includesPoint(thisRange, point) {
    return (thisRange.start <= point) && (point <= thisRange.stop)
  }
  
  return {
    start: start ? start : null,
    stop: stop ? stop : null,
    
    intersects: function(otherRange) {
      return otherRange.includes(this.start) || 
        otherRange.includes(this.stop) ||
        this.includes(otherRange)
    },
    
    includes: function(other) {
      if (other.start || other.stop) {
        return includesRange(this, other)
      } else {
        return includesPoint(this, other)
      }
    }
  }
}
