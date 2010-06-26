psc.namespace('admin');

psc.admin.UserAdmin = (function ($) {
  var user;

  function selectRole(roleKey) {
    $('a.role').removeClass('selected');
    $('#role-' + roleKey).addClass('selected');
    startEditing(roleKey);
  };

  function selectRoleTab(evt) {
    var roleKey = evt.target.id.substring("role-".length)
    selectRole(roleKey);
    return false;
  };

  function startEditing(roleKey) {
    var role = _.detect(PROVISIONABLE_ROLES, function (role) { return role.key === roleKey });
    if (role) {
      $('#role-editor-pane').html(resigTemplate('role_editor_template', { role: role })).
        find('input.role-group-membership').attr('checked', !!user.memberships[role.key]).
        click(updateGroupMembership).end().
        find('input.scope-site').each(function (i, siteInput) {
          $(siteInput).attr(
            'checked',
            user.hasMembership(role.key, { site: $(siteInput).attr('site-identifier') }));
        }).click(_(updateMembershipScope).bind(this, role.key)).end().
        parent().attr('role', roleKey);
    } else {
      alert("No such role " + roleKey);
    }
  }

  function updateGroupMembership(evt) {
    var role = evt.target.id.substring("group-".length);
    if ($(evt.target).attr('checked')) {
      user.add(role);
    } else {
      user.remove(role);
    }
  }

  function updateMembershipScope(roleKey, evt) {
    var scope = evt.target.getAttribute('site-identifier');
    if ($(evt.target).attr('checked')) {
      user.add(roleKey, { sites: [ scope ] });
    } else {
      user.remove(roleKey, { sites: [ scope ] });
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
      input = $('#role-editor input#scope-site-' + data.scopeIdentifier);
    } else {
      input = $('#role-editor input#group-' + data.role);
    }
    input.attr('checked', data.kind === 'add');
  }

  return {
    init: function (u) {
      user = u;
      _(user.memberships).each(function (scopes, roleKey) {
        $('#role-' + roleKey).addClass('member');
      }, this);
      selectRole('system_administrator');
      $('a.role').click(selectRoleTab);
      $(user).bind('membership-change', syncRoleTabOnChange).
        bind('membership-change', syncRoleEditorOnChange);
    },

    serializeRoleChanges: function () {
      return YAHOO.lang.JSON.stringify(user.changes);
    }
  };
})(jQuery);
