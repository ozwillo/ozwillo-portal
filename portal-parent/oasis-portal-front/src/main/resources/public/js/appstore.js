$(document).ready(function() {

    /* init csrf prevention */
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });



    $("#confirmation").on("hide.bs.modal", function(e) {
        $(this).removeData("bs.modal");
    });

    $("#confirmation").on("shown.bs.modal", function(e) {

        $("#confirmation * .btn-buy").click(function (e) {
            var appId = $(this).attr("data-appId");
            var appType = $(this).attr("data-appType");

            $("#subscribe_app").attr("value", appId);
            $("#subscribe_apptype").attr("value", appType);
            $("#subscribe").submit();

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

});

