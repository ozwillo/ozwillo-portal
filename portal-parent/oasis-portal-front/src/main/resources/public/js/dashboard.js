$(document).ready(function () {

    /* Init csrf prevention */
	
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });

    
    /* Dashboard switcher */

    
	var updateDashboardSwitcher = function(fragment) {
    	$('#dashboard-switcher').replaceWith(fragment);
    	bindDashboardSwitcher();
	}
	
    var bindDashboardSwitcher = function() {
    	
    	// Switch dashboard
	    $("a.dash-switch-link").click(function(e) {
	        e.preventDefault();
	        var link = $(this);
	
	        $.get(link.attr("href") + "/fragment/switcher",
	            function(fragment) {
	        		updateDashboardSwitcher(fragment);
		        	
		        	$.get(link.attr("href") + "/fragment",
	                    function(fragment) {
	                        $("#applications").html(fragment);
	                        $("#applications").attr("data", link.attr("data"));
	
	                        if (typeof history.pushState == 'function') { // avoid doing it in IE!
	                            history.pushState({}, "dashboard", link.attr("href"));
	                        }
	                    }
	                );
		        }
	        );
	    });

	    // Create new dashboard
	    var dashboard_template = $("#dashboard_template");
	    dashboard_template.detach();

	    $("#create-dash").submit(function (e) {
	        e.preventDefault();
	        
	        var input = $("#dashboardname");
	        var name = input.val();
	        if (name) {
	            $.ajax({
	            	url: $("#create-dash").attr("action"),
	            	method: 'POST',
	                data: {"dashboardname": name},
	                dataType: "html",
	                success: function(fragment) {
	                	updateDashboardSwitcher(fragment);
	                	refreshDashboard();
	                }
	            });
	        }
	    });
    	
    	// Open dashboard management
    	$('#manage-dashboard-btn').click(function(e) {
    		e.stopPropagation();
    		
    		$('#edit-dashboard-modal').modal('show');
    		return false;
    	});
	    
	    // Submit dashboard management
    	$('#dash-delete-btn').click(function() {
    		$('#dash-delete-value').val(true);
    	});
	    $("#manage-dash").submit(function (e) {
	        e.preventDefault();
    		
            $.ajax({
            	url: $(this).attr("action"),
            	method: 'POST',
                data: $(this).serialize(),
                dataType: 'html',
                success: function(fragment) {
                	updateDashboardSwitcher(fragment);
                	
                	// XXX Remove dangling Bootstrap backdrops after template reloading 
                	$('.modal-backdrop').remove();
                	
                	// Refresh dashboard to update title
                	if (!$('#dash-delete-value').val()) {
                		refreshDashboard();
                	}
                }
            });
	    });
	    
	};
	
	bindDashboardSwitcher();


    /* Notifications */

    var updateAppNotifications = function () {

        $.get("/my/api/app-notifications/" + $("#applications").attr("data"),
            function (notifData) {
                for (var i = 0 ; i < notifData.length ; i++) {
                    var notif = notifData[i];

                    var appLink = $("#app-" + notif.applicationId);

                    if (appLink.length != 0) {

                        var element = appLink.find("span.badge.badge-notifications");
                        if (notif.count > 0) {
                            element.html(notif.count);
                            element.show();
                        } else {
                            element.hide();
                        }
                    }

                }
            }
        );
        setTimeout(updateAppNotifications, 2000);
    };

    updateAppNotifications();
    
    
    /* Dashboard */

    var initDrag = function() {
        /* application drag and drop */
        $(".app-icon").draggable({
                cursor:"move",
                revert: true,
                stack: "#applications div"
        });

        $(".app-icon").droppable({
            accept: ".app-icon",
            hoverClass: "hovered",
            drop: function(e, ui) {
                var draggable = ui.draggable;

                var draggedId = draggable.find("a.app-link").attr("id");
                var thisId = $(this).find("a.app-link").attr("id");

                draggable.draggable('option', 'revert', false);

                var url = $("#create-dash").attr("action") + "/reorder";

                $.ajax({url: url,
                        type: "POST",
                        data: JSON.stringify({
                            "contextId": $("#applications").attr("data"),
                            "draggedId": draggedId,
                            "destId": thisId,
                            "before": false
                        }),
                        contentType: "application/json; charset=utf-8",
                        dataType: "html",
                        success: function (fragment) {
                            $("#applications").html(fragment);
                            init_drag();
                        }
                    }
                );
            }
        });


        $("div.dash-switcher * li.inactive").droppable({
            accept: ".app-icon",
            hoverClass: "hovered",
            drop: function(e, ui) {
                var draggable = ui.draggable;

                var draggedId = draggable.find("a.app-link").attr("id").substr(4);
                var thisId = $(this).find("a").attr("data");

                draggable.draggable('option', 'revert', false);

                var url = $("#create-dash").attr("action") + "/move_context";

                $.ajax({url: url,
                        type: "POST",
                        data: JSON.stringify({
                            "contextId": $("#applications").attr("data"),
                            "draggedId": draggedId,
                            "destId": thisId
                        }),
                        contentType: "application/json; charset=utf-8",
                        dataType: "html",
                        success: function (fragment) {
                            $("#applications").html(fragment);
                            initDrag();
                        }
                    }
                );
            }
        });
    };
    
    var refreshDashboard = function() {
    	// Load according to the active dashboard link
    	var $link = $(".active a.dash-switch-link");
    	$.get($link.attr('href') + "/fragment",
            function(fragment) {
                $("#applications").replaceWith(fragment);
                $("#applications").attr("data", $link.attr("data"));
                history.pushState({}, "dashboard", $link.attr("href"));
            }
        );
    	
    	initDrag();
    }
    
    initDrag();

});

