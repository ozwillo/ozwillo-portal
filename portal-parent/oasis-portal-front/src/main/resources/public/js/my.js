$(document).ready(function () {


    $("a.nav-link").hover(
        function () {
            var image_purple = $(this).find("img.purple");
            image_purple.fadeIn(250);
        },
        function () {
            var image_purple = $(this).find("img.purple");
            image_purple.fadeOut(250);
        }
    );


    var getNotificationsCount = function () {

        $.get($(".my-oasis").attr("data").toString(),
                function (notifData) {
                    var element = $(".my-oasis span.badge.badge-notifications");
                    if (notifData.notificationsCount > 0) {
                        element.html(notifData.notificationsCount);
                        element.show();

                        element.data("popover", null).popover({
                                placement:"bottom",
                                trigger:"hover",
                                container:"body",
                                content:notifData.notificationsMessage
                            });

                        element.attr("data-content", notifData.notificationsMessage);

                    } else {
                        element.hide();
                    }
                    setTimeout(getNotificationsCount , 2000);
                }
        );

    }

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

		if (xstat && exception){

            var title = messages['ui.something_went_wrong_title'];
            var err_message = "" ; //xstat  + " " + exception ;
            var err_detail = messages['ui.error_detail_title'] + xhr.responseText;
            if (xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong"))
                err_detail = err_detail + " " +xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong");

            var divError = createDivError(title, err_message, err_detail);

            if ( devmode || ( (xstat / 100) !== 5 )){
                openErrorDialog(divError, 3500);
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

        var divError = createDivError(title, err_msg, err_detail);

        openErrorDialog(divError, 3500);
	}
});


// Functions

function createDivError(title, err_msg, details){
    var error = jQuery("<div id='dialog-error' />" );
    error.attr('title', title);
    error.text(err_msg);
    error.append(jQuery("<br />" ));
    error.append(details);

    return error;
}

function openErrorDialog(divError, timeOut){
        $("body").append(divError);
        divError.show();

        var dlg_width = 250;
        var dlg_height = 200;

		var dlg_offset_x = $("#navbar-collapse").width() - dlg_width + 200;
		var dlg_margin_top = $("#navbar-collapse").outerHeight(true); // includeMargins=true
		var dlg_margin_bottom = $("footer").outerHeight(true); // includeMargins=true

		var $dlg = $('#dialog-error').dialog({
			autoOpen: false,
			width: dlg_width,
			height: dlg_height,
			resizable: false,
			show: 'fade',
			hide: 'fade',
			dialogClass: 'no-close',
			//modal: true,  // blocks the page
			open: function(event, ui) {
				setTimeout(function(){ $(".ui-dialog-content").dialog().dialog("close");}, timeOut);
			},
			position: [dlg_offset_x, dlg_margin_top]
		});

		$(window).bind('scroll', function(evt){
			var scrollTop = $(window).scrollTop();
			var bottom = $(document).height() - scrollTop;
			$dlg.dialog("option", {"position": [
				dlg_offset_x,
				((dlg_margin_top - scrollTop > 0)
					? dlg_margin_top - scrollTop
					: ( (bottom - dlg_height > dlg_margin_bottom)
						? 0
						: bottom - dlg_height - dlg_margin_bottom
					)
				)
			]});
		});

		$dlg.dialog('open');

}