$(document).ready(function() {

    $("a.authority-link").click(function(event) {
        event.preventDefault();

        var authId = $(this).attr("data-authid");

        $("#" + authId).collapse("toggle");
        var child = $(this).children("b.caret");
        if (child.hasClass("inverse")) {
            child.removeClass("inverse");
        } else {
            child.addClass("inverse");
        }
    });


    $("a.authority-link[data-default='true']").click();


});
