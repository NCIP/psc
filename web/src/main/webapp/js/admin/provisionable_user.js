psc.namespace('admin');

psc.admin.ProvisionableUser = function (username, memberships, provisionableRoles) {
  this.username = username;
  this.memberships = memberships;
  if (!this.memberships) {
    this.memberships = {}
  }
  this.provisionableRoles = provisionableRoles;

  this.changes = [];

  function scopeTypeFromCollectionName(collectionName) {
    return collectionName == 'studies' ? 'study' :
      collectionName.substring(0, collectionName.length - 1);
  }

  function collectionNameFromScopeType(scopeType) {
    return scopeType == 'study' ? 'studies' : scopeType + 's';
  }

  var registerChange = _(function(newChange) {
    if (window['console'] && console.log) {
      console.log("Registering", newChange.kind, "of", newChange.role, newChange);
    }

    var otherKind = newChange.kind == 'add' ? 'remove' : 'add';
    var reverses = _(this.changes).select(function (change) {
      var candidate = change.kind == otherKind &&
        change.role == newChange.role;
      var exact;
      if (newChange.scopeType) {
        exact = newChange.scopeType == change.scopeType &&
          newChange.scopeIdentifier == change.scopeIdentifier;
      } else {
        exact = !change.scopeType;
      }
      return candidate && exact;
    });
    if (reverses.length != 0) {
      this.changes = _(this.changes).reject(function (change) {
        return _(reverses).include(change);
      });
    } else {
      this.changes.push(newChange);
    }

    jQuery(this).trigger('membership-change', newChange);
  }).bind(this)

  this.isProvisionableRole = function(roleKey) {
    if (!provisionableRoles) return true;
    return _(this.provisionableRoles).pluck('key').include(roleKey);
  }

  this.isProvisionableScope = function(roleKey, scopeType) {
    if (!provisionableRoles) return true;
    var role = _(this.provisionableRoles).detect(function(r){return r.key == roleKey});
    if (!role) return false;
    return role['scopes'] && _(role['scopes']).include(scopeType);
  }

  this.add = function (roleKey, scope) {
    if (!this.isProvisionableRole(roleKey)) {return;}
    var newMembership = false;
    if (this.memberships[roleKey] === undefined) {
      this.memberships[roleKey] = {};
      newMembership = true;
    }
    var m = this.memberships[roleKey];
    if (typeof scope === 'object') {
      _(scope).each(function (value, key) {
        var scopeType = scopeTypeFromCollectionName(key)
        if (this.isProvisionableScope(roleKey, scopeType)) {
          if (!m[key]) { m[key] = []; }
          _(value).each(function (ident) {
            if (_(m[key]).indexOf(ident) == -1) {
              m[key].push(ident);
              registerChange({
                role: roleKey,
                kind: "add",
                scopeType: scopeTypeFromCollectionName(key),
                scopeIdentifier: ident
              });
            }
          }, this);
        }
      }, this);
    }
    if (newMembership) {
      registerChange({
        role: roleKey,
        kind: 'add'
      });
    }
  };

  this.remove = function (roleKey, scope) {
    if (this.memberships[roleKey] === undefined) { return; }
    var m = this.memberships[roleKey];
    if (typeof scope == 'object') {
      _(scope).each(function (identList, name) {
        if (!m[name]) { _.breakLoop(); }
        _(identList).each(function (ident) {
          var loc = _(m[name]).indexOf(ident);
          if (loc == -1) return;
          m[name].splice(loc, 1);
          registerChange({
            role: roleKey,
            kind: "remove",
            scopeType: scopeTypeFromCollectionName(name),
            scopeIdentifier: ident
          });
        }, this);
      }, this);
    } else {
      _(this.memberships[roleKey]).each(function (identList, name) {
        _(identList).each(function (ident) {
          registerChange({
            role: roleKey,
            kind: "remove",
            scopeType: scopeTypeFromCollectionName(name),
            scopeIdentifier: ident
          });
        });
      });
      delete this.memberships[roleKey]
      registerChange({
        role: roleKey,
        kind: "remove"
      });
    }
  };

  this.hasMembership = function (roleKey, scope) {
    if (!this.memberships[roleKey]) { return false; }
    if (!scope) { return true; }
    var scopeType = _(scope).chain().keys().first().value();
    var collectionName = collectionNameFromScopeType(scopeType);
    if (!this.memberships[roleKey][collectionName]) { return false; }
    return _(this.memberships[roleKey][collectionName]).include(scope[scopeType]);
  };

  this.hasAnyMemberships = function (roleKeys, scope) {
    return _(roleKeys).any(function(roleKey) {
      return this.hasMembership(roleKey, scope);
    }, this);
  };

  this.hasAllMemberships = function(roleKeys, scope) {
    return _(roleKeys).all(function(roleKey) {
      return this.hasMembership(roleKey, scope);
    }, this);
  };

  this.matchingMemberships = function(roleKeys, scope) {
    return _(roleKeys).select(function(roleKey) {
      return this.hasMembership(roleKey, scope);
    }, this);
  };

  this.membershipsStatus = function(roleKeys, scope) {
    var pKeys = selectProvisionableRolesKeys(roleKeys, scope);
    if (this.hasAllMemberships(pKeys, scope)) {
      return 'FULL';
    } else if (this.hasAnyMemberships(pKeys, scope)) {
      return 'PARTIAL';
    } else {
      return 'NONE';
    }
  };

  var selectProvisionableRolesKeys = _(function(roleKeys, scope) {
    return _(roleKeys).select(function(k) {
      var scopeType = null;
      if (scope) {
        var scopeType = _(scope).chain().keys().first().value();
      }
      return this.isProvisionableRole(k, scopeType);
    }, this)
  }).bind(this);
}
