psc.namespace('admin.team');

psc.admin.team.SingleMember = (function ($) {
  var user;

  function updateStudyRole(evt) {
    var role = evt.target.getAttribute("role");
    var study = evt.target.getAttribute("study-identifier");
    console.log("updateStudyRole", study, role);
    if ($(evt.target).attr('checked')) {
      user.add(role, { studies: [ study ] });
    } else {
      user.remove(role, { studies: [ study ] });
    }
  }

  return {
    init: function (u) {
      user = u;

      $('input.study-role-control').click(updateStudyRole);
    },

    serializeRoleChanges: function () {
      return YAHOO.lang.JSON.stringify(user.changes);
    }
  };
}(jQuery));