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

/*    $.ajax({
      success: function(output, status, xhr) { 
              if(xhr.getResponseHeader('X-Oasis-Portal-Kernel-SomethingWentWrong'))
		alert("Something Wrong");
      },
      error: function(output) {
              $('.sysMsg').html(output);
            }
      }); 
*/

})
.ajaxError(
        function(e, x, settings, exception) {
            var message;
            var statusErrorMap = {
                '400' : "Server understood the request, but request content was invalid.",
                '401' : "Unauthorized access.",
                '403' : "Forbidden resource can't be accessed.",
                '500' : "Internal server error.",
                '503' : "Service unavailable."
            };
            if (x.status) {
		// check for a known error message
                message =statusErrorMap[x.status];
		if(x.status =='401'){
		  location.reload(true);		
		}

            }

	    if(!message){
                      message="Unknown Error \n.";
	    }else if(exception){
                message = exception;
            }

	    var error = $("<div class='error-message'>"+message+"</div>");
	    $("body").append(error);
	    error.show();//.delay(1000).fadeOut();
            
	}
);
