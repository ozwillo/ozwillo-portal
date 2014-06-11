$(window).scroll(function() {
    if ($(window).scrollTop() > 0) {
        $(".navbar").addClass("opaque", 500);
        $("#logo").attr("src", "/img/ozwillo-logo-small.png");
    } else {
        $(".navbar").removeClass("opaque", 500);
        $("#logo").attr("src", "/img/ozwillo-logo-white-small.png");
    }
});

$('a.after-the-break').click(function () {
    $('html, body').animate({
        scrollTop: $($.attr(this, 'href')).offset().top - 50
    }, 500);
    return false;
});