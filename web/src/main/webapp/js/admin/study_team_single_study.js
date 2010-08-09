psc.namespace('admin.team');

psc.admin.team.SingleStudy = (function ($) {
  var users, study;

  function updateStudyRole(evt) {
    var username = evt.target.getAttribute("username");
    var user = findUser(username);
    if (!user) {
      alert("No user " + username + " registered for provisioning");
      return;
    }
    var role = evt.target.getAttribute("role");
    if ($(evt.target).attr('checked')) {
      user.add(role, { studies: [ study ] });
    } else {
      user.remove(role, { studies: [ study ] });
    }
  }

  function findUser(username) {
    return _(users).detect(function (user) { return user.username == username });
  }

  return {
    init: function (s, u) {
      users = u;
      study = s;

      $('input.study-role-control').click(updateStudyRole);
    },

    serializeRoleChanges: function () {
      return YAHOO.lang.JSON.stringify(
        _(users).reduce({}, function (ch, user) {
          ch[user.username] = user.changes;
          return ch;
        }));
    }
  };
}(jQuery));