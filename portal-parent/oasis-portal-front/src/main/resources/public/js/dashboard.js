$(document).ready(function () {



    /* init csrf prevention */
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });

    /* switch dashboard */
    $("a.dash-switch-link").click(function(e) {
        e.preventDefault();
        var link = $(this);
        $.get(link.attr("href") + "/fragment",
            function(fragment) {
                $("#applications").html(fragment);
                $("#applications").attr("data", link.attr("data"));


                $("div.dash-switcher * li").removeClass("active").addClass("inactive");
                link.parent().addClass("active").removeClass("inactive")
                init_drag();

                $("div.dash-switcher * li").droppable("option", "disabled", false);
                link.parent().droppable("option", "disabled", true);

                if (typeof history.pushState == 'function') { // avoid doing it in IE!
                    history.pushState({}, "dashboard", link.attr("href"));
                }
            }
        );
    });

    /* Create new dashboard */
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


    /* Notifications */

    var updateAppNotifications = function () {

        $.get("/my/api/app-notifications/" + $("#applications").attr("data"),
            function (notifData) {
                for (var i = 0 ; i < notifData.length ; i++) {
                    var notif = notifData[i];

                    var appLink = $("#app-" + notif.applicationId);

                    if (appLink.length != 0) {

                        var element = appLink.find("span.badge.badge-notifications");
                        if (notif.count > 0) {
                            element.html(notif.count);
                            element.show();
                        } else {
                            element.hide();
                        }
                    }

                }
            }
        );
        setTimeout(updateAppNotifications, 2000);
    };

    updateAppNotifications();

    var init_drag = function() {
        /* application drag and drop */
        $(".app-icon").draggable({

                cursor:"move",
                revert: true,
                stack: "#applications div"
            }
        );

        $(".app-icon").droppable({
            accept: ".app-icon",
            hoverClass: "hovered",
            drop: function(e, ui) {
                var draggable = ui.draggable;

                var draggedId = draggable.find("a.app-link").attr("id");
                var thisId = $(this).find("a.app-link").attr("id");

                draggable.draggable('option', 'revert', false);

                var url = $("#create-dash").attr("action") + "/reorder";

                $.ajax({url: url,
                        type: "POST",
                        data: JSON.stringify({
                            "contextId": $("#applications").attr("data"),
                            "draggedId": draggedId,
                            "destId": thisId,
                            "before": false
                        }),
                        contentType: "application/json; charset=utf-8",
                        dataType: "html",
                        success: function (fragment) {
                            $("#applications").html(fragment);
                            init_drag();
                        }
                    }
                );
            }
        });


        $("div.dash-switcher * li.inactive").droppable({
            accept: ".app-icon",
            hoverClass: "hovered",
            drop: function(e, ui) {
                var draggable = ui.draggable;

                var draggedId = draggable.find("a.app-link").attr("id").substr(4);
                var thisId = $(this).find("a").attr("data");

                draggable.draggable('option', 'revert', false);

                var url = $("#create-dash").attr("action") + "/move_context";

                $.ajax({url: url,
                        type: "POST",
                        data: JSON.stringify({
                            "contextId": $("#applications").attr("data"),
                            "draggedId": draggedId,
                            "destId": thisId
                        }),
                        contentType: "application/json; charset=utf-8",
                        dataType: "html",
                        success: function (fragment) {
                            $("#applications").html(fragment);
                            init_drag();
                        }
                    }
                );
            }
        });
    };

    init_drag();



});

