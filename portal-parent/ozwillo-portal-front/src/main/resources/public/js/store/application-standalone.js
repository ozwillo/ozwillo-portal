$(document).ready(function() {

    $("div#veil").click(function() {
        window.location = $(this).find("a").attr("href");
    });


    setupAppPage();
});