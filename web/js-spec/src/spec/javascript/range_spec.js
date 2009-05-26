require("spec_helper.js");
require("../../../../src/main/webapp/js/psc-tools/range.js");

Screw.Unit(function() {
  describe("Range", function() {
    var q4, fourthWeekOfNov, firstWeekOfDec, startingInNov, thanksgivingWeekend
    before(function() {
      q4 = new psc.tools.Range(new Date(2008, 9, 1), new Date(2008, 11, 31))
      fourthWeekOfNov = new psc.tools.Range(new Date(2008, 10, 23), new Date(2008, 10, 29))
      firstWeekOfDec = new psc.tools.Range(new Date(2008, 10, 30), new Date(2008, 11, 6))
      startingInNov = new psc.tools.Range(new Date(2008, 10, 1))
      thanksgivingWeekend = new psc.tools.Range(new Date(2008, 10, 27), new Date(2008, 10, 30))
    })
    
    describe("properties", function() {
      it("exposes the start point", function() {
        expect(fourthWeekOfNov.start).to(equal, new Date(2008, 10, 23))
      })
      
      it("exposes the stop point", function() {
        expect(fourthWeekOfNov.stop).to(equal, new Date(2008, 10, 29))
      })
      
      it("has a null stop point if indefinite", function() {
        expect(startingInNov.stop).to(equal, null)
      })
    })
    
    describe("#intersects", function() {
      describe("with a concrete range", function() {
        it("intersects when a superset", function() {
          expect(q4.intersects(fourthWeekOfNov)).to(equal, true)
        })

        it("intersects when a subset", function() {
          expect(fourthWeekOfNov.intersects(q4)).to(equal, true)
        })

        it("does not intersect when adjacent", function() {
          expect(fourthWeekOfNov.intersects(firstWeekOfDec)).to(equal, false)
        })

        it("intersects when it overlaps on the left", function() {
          expect(fourthWeekOfNov.intersects(thanksgivingWeekend)).to(equal, true)
        })

        it("intersects when it overlaps on the right", function() {
          expect(thanksgivingWeekend.intersects(fourthWeekOfNov)).to(equal, true)
        })

        it("intersects when tangent", function() {
          expect(thanksgivingWeekend.intersects(firstWeekOfDec)).to(equal, true)
        })
      })
    })
    
    describe("#includes", function() {
      describe("with a single value", function() {
        it("includes a point inside", function() {
          expect(q4.includes(new Date(2008, 10, 4))).to(equal, true)
        })
        
        it("does not include a point outside", function() {
          expect(q4.includes(new Date(2008, 3, 9))).to(equal, false)
        })
        
        it("includes start point", function() {
          expect(q4.includes(new Date(2008, 9, 1))).to(equal, true)
        })
        
        it("includes end point", function() {
          expect(q4.includes(new Date(2008, 11, 31))).to(equal, true)
        })
      })
      
      describe("with another range", function() {
        it("includes a pure subset", function() {
          expect(q4.includes(thanksgivingWeekend)).to(equal, true)
        })
        
        it("includes itself", function() {
          expect(fourthWeekOfNov.includes(fourthWeekOfNov)).to(equal, true)
        })
        
        it("does not include disjoint ranges", function() {
          expect(fourthWeekOfNov.includes(firstWeekOfDec)).to(equal, false)
        })
        
        it("does not include overlapping ranges", function() {
          expect(fourthWeekOfNov.includes(thanksgivingWeekend)).to(equal, false)
        })
      })
    })
  })
})