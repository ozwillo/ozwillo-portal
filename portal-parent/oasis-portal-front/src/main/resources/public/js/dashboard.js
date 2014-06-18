$(document).ready(function () {

    var updateAppNotifications = function () {

        $.get("/my/api/app-notifications/" + $("#applications").attr("data"),
            function (notifData) {
                for (var i = 0 ; i < notifData.length ; i++) {
                    var notif = notifData[i];

                    var appLink = $("#app-" + notif.applicationId);

                    if (appLink.length != 0) {

                        var element = appLink.find("span.badge.badge-notifications");
                        if (notif.count > 0) {
                            if (element.length == 0) {
                                appLink.append($("<span class='badge badge-notifications'>" + notif.count + "</span>"));
                            } else {
                                element.html(notif.count);
                            }
                        } else if (element.length != 0) {
                            element.remove();
                        }
                    }

                }
            }
        );
        setTimeout(updateAppNotifications, 2000);
    };

    updateAppNotifications();
});