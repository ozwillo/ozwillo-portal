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


    var updateNotifications = function () {

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
                    setTimeout(updateNotifications, 2000);
                }
        );

    }

    if ($(".my-oasis").attr("data")) {
        updateNotifications();
    }
  	

})
.ajaxError(
	function(e, x, settings, exception) {
		// blacklist containing error codes to check for page reload
		var statusErrorMap = [401];
		if ($.inArray(x.status, statusErrorMap) > (-1) ){
			setTimeout(function () { location.reload(1); }, 200);
		}

		if (x.status && exception){
			var err_message = x.status + exception;

			var error = jQuery("<div id='dialog-error' />" );
			error.attr('title', messages['ui.something_went_wrong_title']);
			error.text(err_message);
			//openErrorDialog(error, 2500); // RC1 hotfix (better : see #220 NullPointerException)
        }
            
	}
)
.ajaxSuccess(function( event, xhr, settings ) {
	if (xhr.getResponseHeader("X-Oasis-Portal-Kernel-SomethingWentWrong")){
		var error = jQuery("<div id='dialog-error' />" );
		error.attr('title', messages['ui.something_went_wrong_title']);
		error.text(messages['ui.something_went_wrong_msg']);

		//openErrorDialog(error, 2500); // RC1 hotfix (better : see #219 feedback on explicit user actions)
	}
});


// Functions

function openErrorDialog(error, timeOut){
		$("body").append(error);
		error.show();

		var dlg_width = 150;
		var dlg_height = 100;

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