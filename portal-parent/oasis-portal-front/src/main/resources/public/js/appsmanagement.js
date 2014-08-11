var setupCollapsible = function(element) {
    console.log("Setting up collapsible from element: " + element);

    element.find("a.instance-link").click(function(event) {
        event.preventDefault();
        console.log("Clicked!!");
        var href = $(this).attr("href");
        $(href).collapse("toggle");
        var child = $(this).children("b.caret");
        if (child.hasClass("inverse")) {
            child.removeClass("inverse");
        } else {
            child.addClass("inverse");
        }
    });

}

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
                }
            );


        } else {
            child.addClass("inverse");
        }
    });


    $("a.authority-link[data-authtype='INDIVIDUAL']").click();


});
