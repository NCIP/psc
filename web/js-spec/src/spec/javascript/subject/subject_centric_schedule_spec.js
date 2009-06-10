require_spec("spec_helper.js");
require_main("psc-tools/range.js");
require_main("subject/subject_centric_schedule.js");

Screw.Unit(function() {
  describe("ScheduleDay", function() {
    var day;
    
    before(function() {
      day = new psc.subject.ScheduleDay(new Date(2009, 3, 8))
    })
    
    it("exposes the date", function() {
      expect(day.date).to(equal, new Date(2009, 3, 8))
    })
    
    it("reports that it is not today when it isn't", function() {
      expect(day.today()).to(be_false)
    })
    
    it("reports that it is today when it is", function() {
      expect((new psc.subject.ScheduleDay(new Date())).today()).to(be_true)
    })
    
    it("reports that it is empty when it is", function() {
      expect(day.empty()).to(be_true)
    })
    
    it("reports that it isn't empty when it isn't", function() {
      day.addActivity({ activity: 'foo', state: 'huh?' })
      expect(day.empty()).to(be_false)
    })
    
    it("returns an empty list of activities when empty", function() {
      expect(day.activities.length).to(equal, 0)
    })
    
    it("returns the added activities when there are some", function() {
      day.addActivity({ activity: { name: 'foo', type: 'Procedure' } })
      expect(day.activities[0].activity.name).to(equal, 'foo')
    })
    
    describe("detail timeline class list", function() {
      var jan1, oct1, april8;
      
      before(function() {
        jan1   = new psc.subject.ScheduleDay(new Date(2009, 0, 1))
        oct1   = new psc.subject.ScheduleDay(new Date(2009, 9, 1))
        april8 = new psc.subject.ScheduleDay(new Date(2009, 3, 8))
      })
      
      it("includes day", function() {
        expect(april8.detailTimelineClasses()).to(include, "day")
      })
      
      it("includes the date class", function() {
        expect(april8.detailTimelineClasses()).to(include, "date-2009-04-08")
      })
      
      it("includes month-start when at the start", function() {
        expect(oct1.detailTimelineClasses()).to(include, "month-start")
      })
      
      it("does not include month-start when not at the start", function() {
        expect(april8.detailTimelineClasses()).to_not(include, "month-start")
      })
      
      it("includes year-start when at the start", function() {
        expect(jan1.detailTimelineClasses()).to(include, "year-start")
      })
      
      it("does not include year-start when not at the start", function() {
        expect(oct1.detailTimelineClasses()).to_not(include, "year-start")
      })
      
      it("includes today when it is today", function() {
        var today = new psc.subject.ScheduleDay(new Date())
        expect(today.detailTimelineClasses()).to(include, "today")
      })
      
      it("does not include today when not today", function() {
        expect(jan1.detailTimelineClasses()).to_not(include, "today")
      })
      
      it("includes has-activities when it has some", function() {
        oct1.addActivity({ activity: 'dc' })
        expect(oct1.detailTimelineClasses()).to(include, "has-activities")
      })
      
      it("does not include has-activities when empty", function() {
        expect(jan1.detailTimelineClasses()).to_not(include, "has-activities")
      })
    })
  })
  
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