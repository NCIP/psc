psc.namespace('admin');

psc.admin.UserAdmin = (function ($) {
  var user;

  var provisionableSites = new Array();
  PROVISIONABLE_SITES.each(function(site) {
    provisionableSites.push(site.identifier)
  });

  function selectRole(roleKey) {
    $('a.role').removeClass('selected');
    $('input.roles-to-edit').attr('checked', false);
    $('#role-' + roleKey).addClass('selected');
    $('input.roles-to-edit[value=' + roleKey + ']').attr('checked', true);
    startEditing(roleKey);
  }

  function selectMultipleRoles(roleKeys) {
    _(roleKeys).each(function(key) {
      $('#role-' + key).addClass('selected');
    });
  }

  function deselectMultipleRoles(roleKeys) {
    _(roleKeys).each(function(key) {
      $('#role-' + key).removeClass('selected');
    });
  }

  function selectRoleTab(evt) {
    var roleKey = evt.target.id.substring("role-".length)
    selectRole(roleKey);
    syncAllVsOne();
    return false;
  }

  function selectRoleCheckbox(evt) {
    var checked = _($('input.roles-to-edit:checked')).map(function(elt) {
      return $(elt).val();
    });

    var unchecked = _($('input.roles-to-edit:not(:checked)')).map(function(elt) {
      return $(elt).val();
    });

    selectMultipleRoles(checked);
    deselectMultipleRoles(unchecked);

    startEditingMultiple(checked);
    syncAllVsOne();
    return false;
  }

  function determineProvisionableStudies(role) {
    var isTM = _(role.uses || []).indexOf("template_management") >= 0;
    var isSP = _(role.uses || []).indexOf("site_participation") >= 0;
    if (isTM && isSP) {
      return PROVISIONABLE_STUDIES['template_management+site_participation'];
    } else if (isTM) {
      return PROVISIONABLE_STUDIES['template_management'];
    } else {
      // use participation even for roles that don't have uses
      return PROVISIONABLE_STUDIES['site_participation'];
    }
  }

  function startEditing(roleKey) {
    var role = _.detect(PROVISIONABLE_ROLES, function (role) { return role.key === roleKey });
    if (role) {
      var enableRoleControl = isControlEnabled('role-control', role);
      var enableSitesControl = isControlEnabled('sites-control', role);

      $('#role-editor-pane').html(resigTemplate('role_editor_template', {
          role: role, sites: PROVISIONABLE_SITES, studies: determineProvisionableStudies(role),
          enableRoleControl: enableRoleControl, enableSitesControl: enableSitesControl
        })).
        find('input.role-group-membership').attr('checked', !!user.memberships[role.key]).
        click(updateGroupMembership).end().
        parent().attr('role', roleKey);
      registerScopeControls('#role-editor-pane', role, 'site', 'sites');
      registerScopeControls('#role-editor-pane', role, 'study', 'studies');
    } else {
      alert("No such role " + roleKey);
    }
  }

  function getMembershipList(kind, userMembershipsRole) {
     return userMembershipsRole[kind].toString().split(",");
  }
  
  function startEditingMultiple(roleKeys) {
    var roles = _.select(PROVISIONABLE_ROLES, function (role) { return _.include(roleKeys, role.key)});

    if (_.isEmpty(roles)) {
      $('#role-editor-pane').empty();
    } else if(_(roles).size() == 1) {
      var roleKey = _(roles).first().key;
      startEditing(roleKey)
    } else if (_(roles).size() > 1) {
      var enableRoleControl = _(roles).any(function(r) {return isControlEnabled('role-control', r)});
      var enableSitesControl = _(roles).any(function(r) {return isControlEnabled('sites-control', r)});
      $('#role-editor-pane').html(resigTemplate('multiple_role_editor_template', {
        roles: roles, sites: PROVISIONABLE_SITES,
        studies: uniqueStudies(_(roles).map(function(r) {return determineProvisionableStudies(r)}).flatten()),
        enableRoleControl: enableRoleControl, enableSitesControl: enableSitesControl,
        utils: {mapRoleKeys: mapRoleKeys, mapRoleNames: mapRoleNames, escapeIdSpaces: escapeIdSpaces}
      }))

      registerMultipleGroupControl('#role-editor-pane', roles);
      registerMultipleScopeControls('#role-editor-pane', roles, 'site', 'sites')
      registerMultipleScopeControls('#role-editor-pane', roles, 'study', 'studies')
    }
  }

  function uniqueStudies(studies) {
    var unique = [];
    _(studies).each(function(s) {
      if (!_(unique).pluck('identifier').include(s.identifier)) {
        unique.push(s)
      }
    });
    return unique;
  }

  function isControlEnabled(controlKey, role) {
      var enableRoleControl = true;
      var enableSitesControl = true;
      var provisionableStudies = new Array();
      determineProvisionableStudies(role).each(function(study) {
          provisionableStudies.push(study.identifier)
      });
      var mem = user.memberships[role.key]
      if (mem != null) {

        if (mem['sites'] != null) {
          var isSiteSubset = isSubsetOfProvision(provisionableSites, mem['sites']);
        }

        // Disable role membership control when the user we are provisioning is a
        // member of sites or studies that are not in the list of provisionable studies.
        if (mem['studies'] != null) {
          var isStudySubset = isSubsetOfProvision(provisionableStudies, mem['studies']);
          if (!isSiteSubset || !isStudySubset) {
            enableRoleControl = false;
          }
        } else {
          if (!isSiteSubset) {
            enableRoleControl = false;
          }
        }

        // Disable sites membership control when the user we are provisioning is a
        // member of all-sites and the all-sites membership is not in the list of provisionable sites.
        if (!_.isEmpty(provisionableSites) && !_.isEmpty(mem['sites'])) {
          if (!_.include(provisionableSites,'__ALL__') && _.include(mem['sites'],'__ALL__')) {
            enableSitesControl = false;
          }
        }
      }

    switch(controlKey) {
      case 'role-control': return enableRoleControl;
      case 'sites-control': return enableSitesControl;
      default: return null;
    }
  }

  function isSubsetOfProvision(provisionableList, membershipList) {
    if(!_.isEmpty(provisionableList) && !_.isEmpty(membershipList)) {
      return _.all(membershipList, function(ele){
        return _.include(provisionableList,ele)
      })
    }
  }

  function updateGroupMembership(evt) {
    var r = evt.target.value;
    if ($(evt.target).attr('checked')) {
      user.add(r);
    } else {
      user.remove(r);
    }
  }

  function registerMultipleGroupControl(pane, roles) {
    var state = determineTristateCheckboxState(roles);

    var input = $(pane).find('#multiple-group-membership:first');

    $(input).tristate({initialState: state});

    updateIntermediateStateLabel(state, pane, '#partial-multiple-group-membership-info', roles);

    $(input).bind('tristate-state-change', _(updateMultipleGroupMemberships).bind(this, mapRoleKeys(roles)));
    $(input).bind('tristate-state-change', _(function (pane, roles, evt) {
      var state = $(evt.target).attr('state');
      updateIntermediateStateLabel(state, pane, '#partial-multiple-group-membership-info', roles);
    }).bind(this, pane, roles));
  }

  function updateMultipleGroupMemberships(roleKeys, evt) {
    console.log("Updating user obj", roleKeys, evt)

    var state = $(evt.target).attr('state');

    _(roleKeys).each(function(k) {
      switch(state) {
      case 'checked':
        user.add(k); break;
      case 'unchecked':
        user.remove(k); break;
      default:
        console.log("State does not exist", state);
      }
    });
  }

  function updateIntermediateStateLabel(state, pane, label, roles, scopeType, scopeValue) {
    var label = $(pane).find(label + ':first');

    if (state == 'intermediate') {
      var roleKeys = mapRoleKeys(roles);
      var scope = buildScopeObject(scopeType, scopeValue);
      var matchingRoleKeys = user.matchingMemberships(roleKeys, scope);
      var matchingRoles = _(PROVISIONABLE_ROLES).select(function (role) {return _(matchingRoleKeys).include(role.key)});
      $(label).html('(Checked for ' +  mapRoleNames(matchingRoles).join(', ') + ')').show();
    } else {
      $(label).hide().empty();
    }
  }

  function determineTristateCheckboxState(roles, scopeType, scopeValue) {
    var roleKeys = mapRoleKeys(roles);
    var scope = buildScopeObject(scopeType, scopeValue);

    var status = user.membershipsStatus(roleKeys, scope);
    console.log("determineTristateCheckboxState", scopeType, scopeValue, status);
    switch(status) {
      case 'FULL':    return 'checked';
      case 'PARTIAL': return 'intermediate';
      case 'NONE':    return 'unchecked';
      default:
        console.log("Memberships status unknown: ", status);
        return 'unchecked';
    }
  }

  function registerScopeControls(pane, role, scopeType, scopeTypePlural) {
    $(pane).find('input.scope-' + scopeType).each(function (i, input) {
      var qMembership = {}; qMembership[scopeType] = $(input).attr(scopeType + '-identifier');
      $(input).attr(
          'checked',
          user.hasMembership(role.key, qMembership));
    }).click(_(updateMembershipScope).bind(this, role.key, scopeType, scopeTypePlural));
  }

  function registerMultipleScopeControls(pane, roles, scopeType, scopeTypePlural) {
    $(pane).find('input.scope-' + scopeType).each(function (i, input) {
      var scopeValue = $(input).attr(scopeType + '-identifier');
      var state = determineTristateCheckboxState(roles, scopeType, scopeValue);
      var label = '#partial-scope-' + scopeType + '-' + escapeIdSpaces(scopeValue) + '-info';
      $(input).tristate({initialState: state});
      updateIntermediateStateLabel(state, pane, label, roles, scopeType, scopeValue);
    }).bind('tristate-state-change', _(function(roles, scopeType, scopeTypePlural, evt) {
      updateMultipleMembershipScope(_(roles).map(function(r){return r.key}), scopeType, scopeTypePlural, evt);
      var scopeValue = $(evt.target).attr(scopeType + '-identifier');
      var state = determineTristateCheckboxState(roles, scopeType, scopeValue);
      var label = '#partial-scope-' + scopeType + '-' + escapeIdSpaces(scopeValue) + '-info';
      updateIntermediateStateLabel(state, pane, label, roles, scopeType, scopeValue);
    }).bind(this, roles, scopeType, scopeTypePlural));
  }

  /*
    "data_reader": {
      "sites": ["__ALL__"],
      "studies": ["__ALL__"]

  */
  function buildScopeObject(scopeType, scopeValue) {
    var scope = null;
    if (scopeType) {
      scope = {}; scope[scopeType] = scopeValue;
    }
    return scope;
  }

  function mapRoleKeys(roles) {
    return roles.map(function(r){return r.key});
  }

  function mapRoleNames(roles) {
    return _(roles).map(function(r){return r.name});
  }

  function escapeIdSpaces(id) {
    return id.replace(' ', '--space--');
  }
  
  function updateMembershipScope(roleKey, scopeType, scopeListName, evt) {
    console.log("Updating user obj", roleKey, scopeType, scopeListName, evt);
    var scope = {}; scope[scopeListName] = [ evt.target.getAttribute(scopeType + '-identifier') ];
    if ($(evt.target).attr('checked')) {
      user.add(roleKey, scope);
    } else {
      user.remove(roleKey, scope);
    }
  }

  function updateMultipleMembershipScope(roleKeys, scopeType, scopeListName, evt) {
    var scope = {}; scope[scopeListName] = [ evt.target.getAttribute(scopeType + '-identifier') ];
    var state = $(evt.target).attr('state')

    console.log("Updating user obj", roleKeys, scopeType, scopeListName, scope, state, evt);

    _(roleKeys).each(function(roleKey) {
      switch(state) {
      case 'checked':
        user.add(roleKey, scope); break;
      case 'unchecked':
        user.remove(roleKey, scope); break;
      default:
        console.log("State does not exist", state);
      }
    });
  }

  function syncRoleTabOnChange(evt, data) {
    if (user.hasMembership(data.role)) {
      $('a#role-' + data.role).addClass('member');
    } else {
      $('a#role-' + data.role).removeClass('member');
    }
  }

  function syncRoleEditorOnChange(evt, data) {
    var role = data.role;
    var editorRole = $('#role-editor').attr('role');
    if (role !== editorRole && editorRole.indexOf(',') !== -1) { return; }
    var input;
    if (data.scopeType) {
      input = $('#role-editor input#scope-' + data.scopeType + '-' + data.scopeIdentifier);
      setTimeout(syncAllVsOne, 0);
    } else {
      input = $('#role-editor input#group-' + data.role);
    }
    input.attr('checked', data.kind === 'add');
  }

  function syncAllVsOne() {
    _(['scope-study', 'scope-site']).each(function (scopeClass) {
      var isAll = $('#role-editor input.all.' + scopeClass + ":checked").length > 0;
      if (isAll) {
        var oneSelector = '#role-editor input.one.' + scopeClass;
        var allSelector = '#role-editor input.all.' + scopeClass;
        if ($(allSelector + "[state='checked']").length > 0) {
          $(oneSelector + "[state='intermediate']").click();
          $(oneSelector).
            filter(':checked').click().end().
            attr("disabled", true).
            closest('div.row').addClass('disabled');
        } else if ($(allSelector + "[state='intermediate']").length > 0) {
          $(oneSelector).
            filter(':checked').end().
            attr("disabled", true).
            closest('div.row').addClass('disabled');
        } else {
          $(oneSelector).
            filter(':checked').click().end().
            attr("disabled", true).
            closest('div.row').addClass('disabled');
        }
      } else {
        $('#role-editor input.one.' + scopeClass).
          attr("disabled", false).
          closest('div.row').removeClass('disabled');
      }
    });
  }

  function syncUsername() {
    user.username = $('input#username').val();
  }

  return {
    init: function (u) {
      user = u;
      _(user.memberships).each(function (scopes, roleKey) {
        $('#role-' + roleKey).addClass('member');
      }, this);
      var firstRole;
      if ($('a.role.member').length > 0) {
        firstRole = $('a.role.member')[0].id.substring("role-".length);
      } else {
        firstRole = 'study_subject_calendar_manager';
      }
      selectRole(firstRole);
      $('a.role').click(selectRoleTab);
      $('input.roles-to-edit').change(selectRoleCheckbox);
      $(user).bind('membership-change', syncRoleTabOnChange).
        bind('membership-change', syncRoleEditorOnChange);
      $('input#username').keyup(syncUsername);
      syncAllVsOne();
    },

    serializeRoleChanges: function () {
      if (user.username) {
        var changes = {}; changes[user.username] = user.changes;
        return YAHOO.lang.JSON.stringify(changes);
      } else {
        return "{}";
      }
    }
  };
})(jQuery);
