'use strict';

$(document).ready(function () {

    var nextNotificationTimeoutId;

    var getNotificationsCount = function () {

        $.ajax({
            url: $(".my-oasis").attr("data").toString(),
            type: 'get',
            global: false,
            success: function (notifData) {
                var element = $(".my-oasis span.badge.badge-notifications");
                element.html(notifData.notificationsCount);
                element.attr("data-content", notifData.notificationsMessage);

                $('.my-oasis p.welcome').attr('title', notifData.notificationsMessage);
                $('.my-oasis p.welcome').tooltip({
                    placement: "bottom",
                    container: "body",
                    content: notifData.notificationsMessage
                });
                nextNotificationTimeoutId = setTimeout(getNotificationsCount , 60000);
            },
            error: function(xhr, status, err) {
                if ($.inArray(xhr.status, [401]) > -1) {
                    window.clearTimeout(nextNotificationTimeoutId);
                    nextNotificationTimeoutId = undefined;
                }
            }
        });

    };

    if ($(".my-oasis").attr("data")) {
        if(notificationsEnabled){
            getNotificationsCount();
        }
    }
})
.ajaxError(
	function(e, xhr, settings, exception) {
		// blacklist containing error codes to check for page reload
		var statusErrorMap = [401];
		var xstat = xhr.status;
		if ($.inArray(xstat, statusErrorMap) > (-1) ){
            setTimeout(function () { location.reload(1); }, 200);
        }

		if (xstat && exception && messages){

            var title = messages['ui.something_went_wrong_title'];
            var err_message = "" ; //xstat  + " " + exception ;
            var err_detail = messages['ui.error_detail_title'] + xhr.responseText;
            if (xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong"))
                err_detail = err_detail + " " +xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong");

            if ( devmode || ( (xstat / 100) !== 5 )){
                openErrorDialog(title, err_message, err_detail);
            }
        }

    }
)
.ajaxSuccess(function( event, xhr, settings ) {
    if ( devmode && xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong")){

        var title = messages['ui.something_went_wrong_title'];
        var err_msg = messages['ui.something_went_wrong_msg'];
        var err_detail = messages['ui.error_detail_title']
                + xhr.responseText + " "
                + xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong");

        openErrorDialog(title, err_msg, err_detail);
	}
});


// Functions

function openErrorDialog(title, err_msg, err_detail) {
    var errorContainer = $('#error-container');
    errorContainer.removeClass('hidden');
    var errorMsg = $('#error-message');
    errorMsg.text(err_msg + '\n' + err_detail);
    errorMsg.attr('title', title);
}
