if (!window.psc) { window.psc = { } }
if (!psc.subject) { psc.subject = { } }

psc.subject.SegmentRow = function(start, stop) {
  var range = new psc.tools.Range(start, stop);
  var segments = [];
  
  return {
    segments: segments,
    
    willFit: function(segment) {
      if (!range.includes(segment.dateRange)) return false;
      
      for (var i = 0 ; i < this.segments.length ; i++) {
        if (this.segments[i].dateRange.intersects(segment.dateRange)) {
          return false
        }
      }
      
      return true
    },
    
    add: function(segment) {
      if (this.willFit(segment)) {
        segments[segments.length] = segment
      } else {
        throw segment.name + " will not fit in this row.  Use willFit to check first."
      }
    }
  }
}