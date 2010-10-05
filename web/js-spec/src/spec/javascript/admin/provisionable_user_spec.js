require_spec('spec_helper.js');
require_main('common.js');
require_main('underscore-min.js');
require_main('admin/provisionable_user.js');

Screw.Unit(function () {
  (function ($) {
    describe('psc.admin.ProvisionableUser', function () {
      it("is constructable", function () {
        var user = new psc.admin.ProvisionableUser('jo');
        expect(user.username).to(equal, "jo");
      });

      it("makes the construction-time memberships available", function () {
        var user = new psc.admin.ProvisionableUser('jo', {
          registrar: { sites: ['IL036'] }
        });

        expect(user.memberships.registrar.sites[0]).to(equal, 'IL036')
      });

      it("defaults memberships to an empty object", function () {
        expect((new psc.admin.ProvisionableUser('jo')).memberships).to(equal, { });
      });

      describe('has_membership', function () {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            registrar: { sites: ['IL036'] },
            business_administrator: {}
          });
        });

        it("is true for a matching unscoped membership", function () {
          expect(user.hasMembership('business_administrator')).to(be_true);
        });

        it("is true for the unscoped version of a matching scoped membership", function () {
          expect(user.hasMembership('registrar')).to(be_true);
        });

        it("is false for a non-matching unscoped membership", function () {
          expect(user.hasMembership('system_administrator')).to(be_false);
        });

        it("is true for an exactly-matching scoped membership", function () {
          expect(user.hasMembership('registrar', { site: 'IL036' })).to(be_true);
        });

        it("is false for non-matching scoped membership", function () {
          expect(user.hasMembership('registrar', { site: 'TN008' })).to(be_false);
        });

        it("is false for a scoped membership with a different role but the same scope", function () {
          expect(user.hasMembership('data_reader', { site: 'IL036' })).to(be_false);
        });
      });

      describe('adding memberships', function () {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            registrar: { sites: ['IL036'] },
            business_administrator: {}
          });
        });

        it('can add a site for an existing membership', function () {
          user.add('registrar', { sites: ['NJ032'] });
          expect(user.memberships.registrar.sites.length).to(equal, 2);
          expect(user.memberships.registrar.sites[0]).to(equal, 'IL036');
          expect(user.memberships.registrar.sites[1]).to(equal, 'NJ032');
        });

        it('can add a site for a new membership', function () {
          user.add('subject_manager', { sites: ['TN008'] });
          expect(user.memberships.subject_manager.sites.length).to(equal, 1);
          expect(user.memberships.subject_manager.sites[0]).to(equal, 'TN008');
        });

        it('can add a new membership without a site', function () {
          user.add('system_administrator');
          expect(user.memberships.system_administrator).to(be_true);
        });

        it('ignores sites that already exist', function () {
          user.add('registrar', { sites: ['IL036'] });
          expect(user.memberships.registrar.sites.length).to(equal, 1);
        });

        describe("and recording the changes", function () {
          it("records new unscoped memberships", function () {
            user.add('system_administrator');
            expect(user.changes[0].kind).to(equal, 'add');
            expect(user.changes[0].role).to(equal, 'system_administrator');
          });

          it("does not record repeat unscoped adds", function () {
            user.add('business_administrator');
            expect(user.changes.length).to(equal, 0);
          });

          it("records new roles with scopes", function () {
            user.add('subject_manager', { sites: ['TN008'] });
            expect(user.changes.length).to(equal, 2);
            expect(user.changes[0].kind).to(equal, 'add');
            expect(user.changes[0].role).to(equal, 'subject_manager');
            expect(user.changes[0].scopeType).to(equal, 'site');
            expect(user.changes[0].scopeIdentifier).to(equal, 'TN008');
            expect(user.changes[1].kind).to(equal, 'add');
            expect(user.changes[1].role).to(equal, 'subject_manager');
            expect(user.changes[1].scopeType).to(be_false);
          });

          it("records a new scope for an existing role", function () {
            user.add('registrar', { sites: ['NJ032'] });
            expect(user.changes.length).to(equal, 1);
            expect(user.changes[0].kind).to(equal, 'add');
            expect(user.changes[0].role).to(equal, 'registrar');
            expect(user.changes[0].scopeType).to(equal, 'site');
            expect(user.changes[0].scopeIdentifier).to(equal, 'NJ032');
          });

          it("ignores an existing scope for an existing role", function () {
            user.add('registrar', { sites: ['IL036'] });
            expect(user.changes.length).to(equal, 0);
          });
        });
      });

      describe('removing memberships', function () {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            registrar: { sites: ['IL036', 'NJ032'] },
            business_administrator: {}
          });
        });

        it('can remove a site from an existing membership', function () {
          user.remove('registrar', { sites: ['NJ032'] });
          expect(user.memberships.registrar.sites.length).to(equal, 1);
          expect(user.memberships.registrar.sites[0]).to(equal, 'IL036');
        });

        it('can remove an entire role', function () {
          user.remove('business_administrator');
          expect(user.memberships.business_administrator).to(be_false);
        });

        it("ignores sites that don't already exist", function () {
          user.remove('registrar', { sites: ['TN008'] });
          expect(user.memberships.registrar.sites.length).to(equal, 2);
        });

        describe("and recording the changes", function () {
          it("records removed unscoped memberships", function () {
            user.remove('business_administrator');
            expect(user.changes[0].kind).to(equal, 'remove');
            expect(user.changes[0].role).to(equal, 'business_administrator');
          });

          it("does not record removes for non-present roles", function () {
            user.remove('system_administrator');
            expect(user.changes.length).to(equal, 0);
          });

          it("records removal of individual scopes", function () {
            user.remove('registrar', { sites: ['IL036'] });
            expect(user.changes[0].kind).to(equal, 'remove');
            expect(user.changes[0].role).to(equal, 'registrar');
            expect(user.changes[0].scopeType).to(equal, 'site');
            expect(user.changes[0].scopeIdentifier).to(equal, 'IL036');
          });

          it("ignores an non-existent scope for an existing role", function () {
            user.remove('registrar', { sites: ['TN008'] });
            expect(user.changes.length).to(equal, 0);
          });

          it("records removes for individual scopes when removing a whole role", function () {
            user.remove('registrar');
            expect(user.changes.length).to(equal, 3);
            expect(user.changes[0].kind).to(equal, 'remove');
            expect(user.changes[0].role).to(equal, 'registrar');
            expect(user.changes[0].scopeType).to(equal, 'site');
            expect(user.changes[0].scopeIdentifier).to(equal, 'IL036');
            expect(user.changes[1].kind).to(equal, 'remove');
            expect(user.changes[1].role).to(equal, 'registrar');
            expect(user.changes[1].scopeType).to(equal, 'site');
            expect(user.changes[1].scopeIdentifier).to(equal, 'NJ032');
            expect(user.changes[2].kind).to(equal, 'remove');
            expect(user.changes[2].role).to(equal, 'registrar');
          });
        });
      });

      // This is not implemented as well as it might be, but it is
      // good enough for the common cases.  Server-side processing
      // will take care of the rest.
      describe("merging changes", function () {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            registrar: { sites: ['IL036', 'NJ032'] },
            business_administrator: {}
          });
        });

        it("leaves the user in the role when adding and removing the same scope in the same role", function () {
          user.add("data_reader", { sites: ["TN008"] });
          user.remove("data_reader", { sites: ["TN008"] });
          expect(user.changes.length).to(equal, 1);
          expect(user.changes[0].kind).to(equal, "add");
          expect(user.changes[0].scopeType).to(be_false);
        });

        it("neutralizes a remove and add for the same scope in the same role", function () {
          user.remove("registrar", { sites: ["IL036"] });
          user.add("registrar", { sites: ["IL036"] });
          expect(user.changes.length).to(equal, 0);
        });

        it("neutralizes a scoped add with an unscoped remove", function () {
          user.add("data_reader", { sites: ["TN008"] });
          user.remove("data_reader");
          expect(user.changes.length).to(equal, 0);
        });

        it("does not neutralize a scoped remove with an unscoped add", function () {
          user.remove("registrar", { sites: ["NJ032"] });
          user.add("registrar");
          expect(user.changes.length).to(equal, 1);
        });

        it("neutralizes an unscoped remove with a scoped re-add", function () {
          user.remove("registrar");
          user.add("registrar", { sites: ["TN008"] });
          // expect remove of IL036, NJ032, add of TN008, but no global remove
          expect(user.changes.length).to(equal, 3);
          expect(user.changes[0].kind).to(equal, "remove");
          expect(user.changes[0].scopeIdentifier).to(equal, "IL036");
          expect(user.changes[1].kind).to(equal, "remove");
          expect(user.changes[1].scopeIdentifier).to(equal, "NJ032");
          expect(user.changes[2].kind).to(equal, "add");
          expect(user.changes[2].scopeIdentifier).to(equal, "TN008");
        });

        it("does not neutralize an unscoped add with a scoped remove", function () {
          user.add("data_reader");
          user.remove("data_reader", { sites: ["TN008"] });
          expect(user.changes.length).to(equal, 1);
          expect(user.changes[0].kind).to(equal, "add");
          expect(user.changes[0].scopeType).to(be_false);
        });

        it("neutralizes an unscoped add with an unscoped remove", function () {
          user.add("system_administrator");
          user.remove("system_administrator");
          expect(user.changes.length).to(equal, 0);
        });

        it("neutralizes an unscoped remove with an unscoped add", function () {
          user.remove("business_administrator");
          user.add("business_administrator");
          expect(user.changes.length).to(equal, 0);
        });
      });

      describe("firing events", function () {
        var user;
        var receivedData;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            data_reader: { sites: ['IL036'] },
            system_administrator: { }
          });
          receivedData = [];
          $(user).bind("membership-change", function (evt, data) {
            receivedData.push(data);
          });
        });

        it("fires membership change for an add", function () {
          user.add('data_importer');
          expect(receivedData.length).to(equal, 1);
          expect(receivedData[0].kind).to(equal, 'add');
          expect(receivedData[0].role).to(equal, 'data_importer');
        });

        it("fires membership change for a remove", function () {
          user.remove('system_administrator');
          expect(receivedData.length).to(equal, 1);
        });
      });

      describe("matching memberships", function() {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {
            data_reader: { sites: ['IL036'] }
          });
        });

        it("returns data_reader memberships", function() {
          expect(user.matchingMemberships(['data_reader'], null)).to(equal, ['data_reader']);
        });

        it("returns data_reader memberships", function() {
          expect(user.matchingMemberships(['data_reader'], {site: 'IL036'})).to(equal, ['data_reader']);
        });

        it("returns no memberships", function() {
          expect(user.matchingMemberships(['system_administrator'], null)).to(equal, []);
        })
      });

      describe("restricting to specific provisionable roles and scopes", function() {
        var user;

        before(function () {
          user = new psc.admin.ProvisionableUser('jo', {}, [
            {key: 'registrar', scopes: ['study', 'site']},
            {key: 'business_administrator'}
          ]);
        });


        it("should not add an unsupported role", function() {
          user.add('made_up_role');
          expect(user.changes.length).to(equal, 0);
        });

        it("should not add an unsupported scope", function() {
          user.add('business_administrator', { sites: ['IL036'] });
          expect(user.changes.length).to(equal, 1);
          expect(user.changes[0].kind).to(equal, "add");
          expect(user.changes[0].scopeType).to(be_false);
        });

        it("should add a supported role", function() {
          user.add('registrar');
          expect(user.changes.length).to(equal, 1);
        });

        it("should add an unsupported scope", function() {
          user.add('registrar', { sites: ['IL036'] });
          expect(user.changes.length).to(equal, 2);
          expect(user.changes[0].role).to(equal, "registrar");
          expect(user.changes[0].kind).to(equal, "add");
          expect(user.changes[0].scopeIdentifier).to(equal, 'IL036');
          expect(user.changes[1].role).to(equal, "registrar");
          expect(user.changes[1].kind).to(equal, "add");
          expect(user.changes[1].scopeIdentifier).to(be_false);
        });

      });
    });
  }(jQuery));
});
