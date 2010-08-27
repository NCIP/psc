psc.namespace('admin');

psc.admin.UserAdmin = (function ($) {
  var user;

  var provisionableSiteList = new Array();
  PROVISIONABLE_SITES.each(function(site) {
    provisionableSiteList.push(site.identifier)
  });

  function selectRole(roleKey) {
    $('a.role').removeClass('selected');
    $('#role-' + roleKey).addClass('selected');
    startEditing(roleKey);
  }

  function selectRoleTab(evt) {
    var roleKey = evt.target.id.substring("role-".length)
    selectRole(roleKey);
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
      var enabledGroupControl = true;
      var enabledSitesControl = true;
      var provisionableStudyList = new Array();
      determineProvisionableStudies(role).each(function(study) {
          provisionableStudyList.push(study.identifier)
      });
      var userMembershipsRole = user.memberships[role.key]
      if (userMembershipsRole != null) {

        if (userMembershipsRole['sites'] != null) {
          var membershipSiteList = userMembershipsRole['sites'].toString().split(",");
          var isSiteSubset = isSubsetOfProvision(provisionableSiteList, membershipSiteList);
        }

        if (userMembershipsRole['studies'] != null) {
          var membershipStudyList;
          membershipStudyList = userMembershipsRole['studies'].toString().split(",");
          var isStudySubset = isSubsetOfProvision(provisionableStudyList, membershipStudyList);
          if (!isSiteSubset || !isStudySubset) {
            enabledGroupControl = false;
          }
        } else {
          if (!isSiteSubset) {
            enabledGroupControl = false;
          }
        }

        if (!_.isEmpty(provisionableSiteList) && !_.isEmpty(membershipSiteList)) {
          if (!_.include(provisionableSiteList,'__ALL__') && _.include(membershipSiteList,'__ALL__')) {
            enabledSitesControl = false;
          }
        }
      }

      $('#role-editor-pane').html(resigTemplate('role_editor_template', {
          role: role, sites: PROVISIONABLE_SITES, studies: determineProvisionableStudies(role),
          enabledGroupControl: enabledGroupControl, enabledSitesControl: enabledSitesControl
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

  function isSubsetOfProvision(provisionableList, membershipList) {
    if(!_.isEmpty(provisionableList) && !_.isEmpty(membershipList)) {
      return _.all(membershipList, function(ele){
        return _.include(provisionableList,ele)
      })
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

  function updateGroupMembership(evt) {
    var role = evt.target.id.substring("group-".length);
    if ($(evt.target).attr('checked')) {
      user.add(role);
    } else {
      user.remove(role);
    }
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
    if (role !== editorRole) { return; }
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
        $('#role-editor input.one.' + scopeClass).
          filter(':checked').click().end().
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
