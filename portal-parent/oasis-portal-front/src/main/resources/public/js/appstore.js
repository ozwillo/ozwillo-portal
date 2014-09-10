$(document).ready(function() {

    /* init csrf prevention */
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });



    $("input[name='audience']").change(function(e) {
        if(! $(this).is(":checked")) {
            if (! $("input[name='audience']:checked").length) {
                $(this).prop("checked", true); // recheck it
            }

        }
        $("#searchForm").submit();

    });

    $("#searchForm").submit(function(e){
        e.preventDefault();
        $.post($(this).attr("action"), $(this).serialize(), function(result) {
            $(".app-store-result").html(result);
        });
    });



    $("button.btn-indicator").click(function() {
        $("div#veil").show();

        var href = $(this).attr("href");

        if (typeof history.pushState == "function") {
            history.pushState({}, "application", href);
        }

        var appPage = $("div#apppage");

        $.get(href + "/inner", function(fragment) {
            appPage.html(fragment);
            appPage.show();
        });

    });

    var resetAppStore = function() {
        $("div#veil").fadeOut();
        $("div#apppage").fadeOut();

    };


    $("div#veil").click(function() {
        resetAppStore();
        window.history.back();
    });
    $(window).on("popstate", function(event) {
        resetAppStore(event);
    });
});

