require_spec("spec_helper.js");
require_main("psc-tools/range.js");
require_main("subject/subject_centric_schedule.js");

Screw.Unit(function() {
  describe("SegmentRow", function() {
    var row;
    
    var createSegment = function(name, start, length) {
      return {
        name: name,
        dateRange: new psc.tools.Range(
          start, new Date(start.getTime() + length * 24 * 60 * 60 * 1000))
      }
    }
    
    before(function() {
      row = new psc.subject.SegmentRow(
        new Date(2009, 2, 9), new Date(2009, 2, 17))
    })
    
    it("will accomodate anything when empty", function() {
      expect(row.willFit(createSegment("F", new Date(2009, 2, 11), 4))).to(be_true)
    })
    
    it("will accomodate a non-overlapping other segment", function() {
      row.add(createSegment("A", new Date(2009, 2, 10), 4))
      expect(row.willFit(createSegment("F", new Date(2009, 2, 16), 1))).to(be_true)
    })
    
    it("will not accomodate an overlapping other segment", function() {
      row.add(createSegment("A", new Date(2009, 2, 10), 6))
      expect(row.willFit(createSegment("F", new Date(2009, 2, 12), 1))).to(be_false)
    })
    
    it("will not accomodate a segment that is out of range high", function() {
      expect(row.willFit(createSegment("A", new Date(2009, 2, 1), 3))).to(be_false)
    })
    
    it("will not accomodate a segment that is out of range low", function() {
      expect(row.willFit(createSegment("A", new Date(2009, 2, 19), 3))).to(be_false)
    })
    
    it("will add a segment that fits", function() {
      row.add(createSegment("C", new Date(2009, 2, 10), 4))
      expect(row.segments.length).to(equal, 1)
      expect(row.segments[0].name).to(equal, "C")
    })
    
    it("will not add a segment which does not fit", function() {
      row.add(createSegment("C", new Date(2009, 2, 10), 4))
      expect(function() { row.add(createSegment("T", new Date(2009, 2, 11), 2)) }).
        to(raise, "T will not fit in this row.  Use willFit to check first.")
    })
  })
})