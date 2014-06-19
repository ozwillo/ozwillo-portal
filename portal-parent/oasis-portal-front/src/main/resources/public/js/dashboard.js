$(document).ready(function () {

    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
    $("a.dash-switch-link").click(function(e) {
        e.preventDefault();
        var link = $(this);
        $.get(link.attr("href") + "/fragment",
            function(fragment) {
                $("#applications").html(fragment);
                $("#applications").attr("data", link.attr("data"));

                $("div.dash-switcher * li").removeClass("active");
                link.parent().addClass("active");

                history.pushState({}, "dashboard", link.attr("href"));
            }
        );
    });

    var dashboard_template = $("#dashboard_template");
    dashboard_template.detach();

    $("#create-dash").submit(function (e) {
        e.preventDefault();
        var input = $("#dashboardname");
        var name = input.val();
        if (name) {

            $.post($("#create-dash").attr("action") + "/fragment",
                {"dashboardname": name},
                function (result) {
                    var dash = dashboard_template.clone(true);
                    dash.removeAttr("id");
                    var link = dash.find("a.dash-switch-link");
                    link.attr("data", result);
                    link.attr("href", link.attr("href") + result);
                    link.html(name);
                    $("#dashboard_list").append(dash);
                    link.click();
                    input.val("");
                    input.trigger("blur");
                });
        }
    });

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