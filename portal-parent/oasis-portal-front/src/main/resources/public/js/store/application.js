function setupAppPage() {


    $(".install-application * .btn-indicator").popover({
        content: $("div#install-app-popover").html(),
        html:true,
        placement:'bottom',
        trigger:'click'
    });

    if ($(".install-application * .btn-indicator").attr("data-open-popover") == "true") {
        $(".install-application * .btn-indicator:first").popover("show");
    }

    $(".install-application * .btn-indicator").on("shown.bs.popover", function() {
        $("a.orgselector").click(function(event) {
            event.preventDefault();

            var form = $("#buyform");
            var orgid = $(this).attr("data-orgid");
            form.find("#organizationId").attr("value", orgid);
            form.submit();
        });

        $("#ownbuy").click(function() {
            var form = $("#buyform");
            form.find("#organizationId").attr("value", null);
            form.submit();
        });
    });


}
