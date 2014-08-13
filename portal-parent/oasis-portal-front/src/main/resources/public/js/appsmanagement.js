var setupLinks = function(element) {

    var appSettings = $("#appsettings");
    var serviceSettings = $("#servicesettings");

    $("a.appsettings-link").click(function(event) {
        event.preventDefault();

        var href = $(this).attr("href");
        console.log("Bringing up app settings for " + href);

        $.get(href, function(fragment) {
            appSettings.html(fragment);
            appSettings.find("#modal-window").modal({});
        });
    });

    $("a.servicesettings-link").click(function(event) {
        event.preventDefault();
        var href = $(this).attr("href");
        console.log("Bringing up service settings for " + href);

        $.get(href, function(fragment) {
            serviceSettings.html(fragment);
            serviceSettings.find("#modal-window").modal({});
            serviceSettings.find("#saveService").click(function(event) {
                event.preventDefault();

                console.log("Saving service " + href);
                serviceSettings.find("#service-form").submit(); // TODO replace with something more AJAXy
            });
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


    $("a.authority-link[data-authtype='INDIVIDUAL']").click();


});
