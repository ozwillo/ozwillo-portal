$(document).ready(function() {

    $("div#veil").click(function() {
        window.location = $(this).find("a").attr("href");
    });


    $(".btn-indicator-available").popover({
        content: $("div#install-app-popover").html(),
        html:true,
        placement:'bottom',
        trigger:'click'
    });


});