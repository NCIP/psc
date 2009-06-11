require_spec('spec_helper.js');

Screw.Unit(function () {
  describe("psc.namespace", function () {
    it("creates a single level namespace", function () {
      expect(psc.foo).to(be_undefined);
      
      psc.namespace("foo");
      expect(psc.foo).to(equal, {});
    });
    
    it("does not wipe out an existing namespace", function () {
      psc.bar = { I_THINK: "yam" };
      psc.namespace("bar");
      expect(psc.bar.I_THINK).to(equal, "yam");
    });
    
    it("creates nested namespaces", function () {
      psc.bar = { };
      psc.namespace("bar.a.d.t");
      expect(psc.bar.a.d.t).to(equal, {});
    });
  });
});
