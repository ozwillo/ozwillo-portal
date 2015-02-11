var setupLinks = function(element) {

    var settings = $("#settings");



    $("a.servicesettings-link").click(function(event) {
        event.preventDefault();
        var href = $(this).attr("href");
        console.log("Bringing up service settings for " + href);

        $.get(href, function(fragment) {
            settings.html(fragment);
            settings.find("#modal-window").modal({});
            settings.find("#saveService").click(function(event) {
                event.preventDefault();

                console.log("Saving service " + href);
                settings.find("#service-form").submit();
            });
        });
    });

    $("a.usersettings-link").click(function(event) {
        event.preventDefault();
        var href = $(this).attr("href");
        $.get(href, function(fragment) {
            settings.html(fragment);
            settings.find("#modal-window").modal({});

            init_users();
        });
    });
};

var setupCollapsible = function(element) {

    element.find("a.instance-link").click(function(event) {
        event.preventDefault();
        var href = $(this).attr("href");
        $(href).collapse("toggle");
        var child = $(this).children("b.caret");
        if (child.hasClass("inverse")) {
            child.removeClass("inverse");
        } else {
            child.addClass("inverse");
        }
    });

};

$(document).ready(function() {



    $("a.authority-link").click(function(event) {
        event.preventDefault();

        var authId = $(this).attr("data-authid");
        var authType = $(this).attr("data-authtype");

        var target = $("#" + authId);
        target.collapse("toggle");
        var child = $(this).children("b.caret");
        if (child.hasClass("inverse")) {
            child.removeClass("inverse");

            $.get($(this).attr("href") + authType + "/" + authId,
                function(fragment) {
                    target.html(fragment);
                    setupCollapsible(target);
                    setupLinks(target);
                }
            );


        } else {
            child.addClass("inverse");
        }
    });


    $("a.authority-link[data-default='true']").click();
    $("a.authority-link[data-authtype='INDIVIDUAL']").click();


});
