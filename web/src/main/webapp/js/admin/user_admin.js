psc.namespace('admin');

psc.admin.UserAdmin = (function ($) {
  var user;

  var provisionableSites = new Array();
  PROVISIONABLE_SITES.each(function(site) {
    provisionableSites.push(site.identifier)
  });

  function selectRole(roleKey) {
    $('a.role').removeClass('selected');
    $('#role-' + roleKey).addClass('selected');
    
    if (roleKey === 'multiple-roles') {
      startEditingMultiple();
      $('input.roles-to-edit').change(selectRoleCheckbox);

    } else {
      startEditing(roleKey);
    }
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

    addRoleAndScopesToMultipleTemplate(checked);
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

  function startEditingMultiple(roleKeys) {
    var roles = findRoles(roleKeys);

    $('#role-editor-pane').html(resigTemplate('multiple_role_editor_template', {roles: roles}));
  }

  function addRoleAndScopesToMultipleTemplate(roleKeys) {
    var roles = findRoles(roleKeys);

    var enableRoleControl = _(roles).any(function(r) {return isControlEnabled('role-control', r)});
    var enableSitesControl = _(roles).any(function(r) {return isControlEnabled('sites-control', r)});
    $('#scope-container').html(resigTemplate('role_and_scope_assignment_template', {
      roles: roles, sites: PROVISIONABLE_SITES,
      studies: uniqueStudies(_(roles).map(function(r) {return determineProvisionableStudies(r)}).flatten()),
      enableRoleControl: enableRoleControl, enableSitesControl: enableSitesControl,
      utils: {mapRoleKeys: mapRoleKeys, mapRoleNames: mapRoleNames, escapeIdSpaces: escapeIdSpaces}
    }))

    registerMultipleGroupControl('#role-editor-pane', roles);
    registerMultipleScopeControls('#role-editor-pane', roles, 'site', 'sites');
    registerMultipleScopeControls('#role-editor-pane', roles, 'study', 'studies');

    registerGroupControlIntermediateStateLabel('#role-editor-pane', roles);
    registerScopeControlIntermediateStateLabel('#role-editor-pane', roles, 'site', 'sites');
    registerScopeControlIntermediateStateLabel('#role-editor-pane', roles, 'study', 'studies');
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
        // Disable role membership control when the user we are provisioning is a
        // member of sites and/or studies that are not in the list of provisionable studies.
        if (mem['sites'] != null && mem['studies'] != null) {
          if (!(isSubsetOfProvision(provisionableSites, mem['sites'])
              && isSubsetOfProvision(provisionableStudies, mem['studies']))) {
            enableRoleControl = false;
          }
        } else if (mem['studies'] != null) {
          if (!isSubsetOfProvision(provisionableStudies, mem['studies'])) {
            enableRoleControl = false;
          }
        } else if (mem['sites'] != null) {
          if (!isSubsetOfProvision(provisionableSites, mem['sites'])) {
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
    var roleKeys = evt.target.value ? evt.target.value.split(',') : [];

    _(roleKeys).each(function(k) {
      if (isChecked(evt.target)) {
        user.add(k);
      } else {
        user.remove(k);
      }
    });
  }

  function isChecked(input) {
    if (isTristateCheckbox(input)) {
      return $(input).tristate('state') == 'checked';
    } else {
      return $(input).attr('checked');
    }
  }

  function isTristateCheckbox(input) {
    return !!$(input).tristate('state');
  }


  function registerMultipleGroupControl(pane, roles) {
    var input = $(pane).find('#multiple-group-membership');

    $(input).
        tristate({initialState: determineTristateCheckboxState(roles)}).
        bind('tristate-state-change', updateGroupMembership);
  }

  function registerGroupControlIntermediateStateLabel(pane, roles) {
    var input = '#multiple-group-membership', label = '#partial-multiple-group-membership-info';
    if (!$(input).tristate) {return;}

    updateIntermediateStateLabel(pane, input, label, roles);

    $(input).bind('tristate-state-change', _(function(pane, label, roles, evt) {
      updateIntermediateStateLabel(pane, evt.target, label, roles);
    }).bind(this, pane, label, roles));
  }

  function registerScopeControlIntermediateStateLabel(pane, roles, scopeType, scopeTypePlural) {
    var findScopeValue = function(input, scopeType) {return $(input).attr(scopeType + '-identifier');}
    var findLabel = function(scopeType, scopeValue) {return '#partial-scope-' + scopeType + '-' + escapeIdSpaces(scopeValue) + '-info';};

    $(pane).find('input.scope-' + scopeType).each(function (i, input) {
      var scopeValue = findScopeValue(input, scopeType);
      var label = findLabel(scopeType, scopeValue);
      updateIntermediateStateLabel(pane, input, label, roles, scopeType, scopeValue);
    }).bind('tristate-state-change', _(function(pane, roles, scopeType, scopeTypePlural, evt) {
      var input = evt.target;
      var scopeValue = findScopeValue(input, scopeType);
      var label = findLabel(scopeType, scopeValue);
      updateIntermediateStateLabel(pane, input, label, roles, scopeType, scopeValue);
    }).bind(this, pane, roles, scopeType, scopeTypePlural));
  }

  function updateIntermediateStateLabel(pane, input, label, roles, scopeType, scopeValue) {
    if (!$(input).tristate || $(input).tristate('state') != 'intermediate') {$(label).hide().empty(); return;}

    var c = userRoleClassifications(mapRoleKeys(roles), scopeType, scopeValue);

    var text = 'Applies to {1}, but not {2}.'.
        replace('{1}', displayableRoleNames(findRoles(c.applies))).
        replace('{2}', displayableRoleNames(findRoles(c.doesNotApply)));

    if (c.scopeNotAvailable.length > 0) {
      text += ' {1} assignment not available for {2}.'.
          replace('{1}', scopeType.charAt(0).toUpperCase() + scopeType.slice(1)).
          replace('{2}', displayableRoleNames(findRoles(c.scopeNotAvailable)));
    }

    $(label).text('(' + text + ')').show();
  }

  function userRoleClassifications(roleKeys, scopeType, scopeValue) {
    var c = {};
    var scope = buildScopeObject(scopeType, scopeValue);

    c.applies =
        psc.tools.Arrays.minus(user.selectProvisionableRolesKeys(roleKeys, scope), user.matchingMemberships(roleKeys, scope));
    c.doesNotApply =
        psc.tools.Arrays.minus(user.selectProvisionableRolesKeys(roleKeys, scope), c.applies);
    c.scopeNotAvailable =
        psc.tools.Arrays.minus(psc.tools.Arrays.minus(roleKeys, c.applies), c.doesNotApply);

    return c;
  }

  function findRoles(roleKeys) {
    roleKeys = roleKeys || [];
    return _(PROVISIONABLE_ROLES).select(function (role) { return _.include(roleKeys, role.key)});
  }

  function determineTristateCheckboxState(roles, scopeType, scopeValue) {
    var roleKeys = mapRoleKeys(roles);
    var scope = buildScopeObject(scopeType, scopeValue);

    var status = user.membershipsStatus(roleKeys, scope);
    console.log("Tri-state checkbox status", status, scopeType, scopeValue);
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
      $(input).tristate({initialState: state});
    }).bind('tristate-state-change', _(function(roles, scopeType, scopeTypePlural, evt) {
      _(roles).each(function(r) {
        updateMembershipScope(r.key, scopeType, scopeTypePlural, evt);
      })
    }).bind(this, roles, scopeType, scopeTypePlural));
  }

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

  function displayableRoleNames(roles) {
    return _(roles).pluck('name').join(', ');
  }

  function escapeIdSpaces(id) {
    return id.replace(' ', '--space--');
  }
  
  function updateMembershipScope(roleKey, scopeType, scopeListName, evt) {
    console.log("Updating user obj", roleKey, scopeType, scopeListName, evt);
    var scope = {}; scope[scopeListName] = [ evt.target.getAttribute(scopeType + '-identifier') ];
    if (isChecked(evt.target)) {
      user.add(roleKey, scope);
    } else {
      user.remove(roleKey, scope);
    }
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
      if ($(input).length <= 0) {
        input = $('#role-editor input#multiple-group-membership');
      }
    }
    input.attr('checked', data.kind === 'add');
    if (input.attr('state')) {
      input.attr('state', data.kind === 'add' ? 'checked' : 'unchecked');
      $(input).parents('.row').find('.partial-membership').empty();
    }
  }

  function syncAllVsOne() {
    _(['scope-study', 'scope-site']).each(function (scopeClass) {
      var isAll = $('#role-editor input.all.' + scopeClass + ":checked").length > 0;
      if (isAll) {
        var oneSelector = '#role-editor input.one.' + scopeClass;

        var regular = $(oneSelector).filter(function(){return !isTristateCheckbox(this)}).
          filter(':checked').click().end();

        var tristate = $(oneSelector).filter(function(){return isTristateCheckbox(this)}).
          tristate('state', 'unchecked');
        
        regular.add(tristate).
          attr("disabled", true).
          closest('div.row').addClass('disabled');
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

  function toggleRoleView(evt, action) {
    if(!action) {
      action = $('#show-all-toggle:contains(Show)').length > 0 ? 'hide' : 'show';
    }

    if (action === 'show') {
      $('#show-all-toggle').text('Show Suite Roles');
      $("div.role-tab[role-type='suite']").hide();
    } else {
      $('#show-all-toggle').text('Hide Suite Roles')
      $("div.role-tab[role-type='suite']").show();
    }
    return false;
  }

  return {
    init: function (u) {
      user = u;
      _(user.memberships).each(function (scopes, roleKey) {
        $('#role-' + roleKey).addClass('member');
      }, this);
      var firstRole;
      if ($("div.role-tab[role-type='psc'] a.role.member").length > 0) {
        firstRole = $('a.role.member')[0].id.substring("role-".length);
      } else {
        firstRole = 'study_subject_calendar_manager';
      }
      selectRole(firstRole);
      $('a.role').click(selectRoleTab);
      $(user).bind('membership-change', syncRoleTabOnChange).
        bind('membership-change', syncRoleEditorOnChange);
      $('input#username').keyup(syncUsername);
      syncAllVsOne();
      toggleRoleView(null, 'show');
      $('#show-all-toggle').click(toggleRoleView);
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
