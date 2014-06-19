$(document).ready(function () {



    $("a.nav-link").hover(
        function () {
            var image_purple = $(this).find("img.purple");
            image_purple.fadeIn(250);
        },
        function () {
            var image_purple = $(this).find("img.purple");
            image_purple.fadeOut(250);
        }
    );

    var updateNotifications = function () {

        $.get($(".my-oasis").attr("data").toString(),
                function (notifData) {
                    var element = $(".my-oasis").find("span.badge.badge-notifications");
                    if (notifData.notificationsCount > 0) {
                        if (element.size() == 0) {
                            $(".my-oasis").append($("<span class='badge badge-notifications'>" + notifData.notificationsCount + "</span>"));
                        } else {
                            element.html(notifData.notificationsCount);
                        }
                    } else if (element.size() != 0) {
                        element.remove();
                    }
                }
        );
        setTimeout(updateNotifications, 2000);
    }

    updateNotifications();

});