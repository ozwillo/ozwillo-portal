$(document).ready(function () {

	initBindings();

    function reloadOrganizations() {
        $.ajax({
            url: '/my/network/fragment/organizations',
            method: 'GET',
            success: refreshRelationshipsTable
        });
    }

    function initBindings($el) {
        showViewMode();

        $("input[type='checkbox']").bootstrapSwitch();
		
//		$('.btn', $el).button();
		
		// Show relationship form
		$('.lnk-edit', $el).click(function() {
			$form = $(this).parents('.form-table-row');
			showViewMode();
			showEditMode($form);
		});
		
		// Save relationship changes
		$('.lnk-accept', $el).click(function() {
			$form = $(this).parents('.form-table-row');
            var isAdmin = $("input[name='admin']", $form).bootstrapSwitch('state');
            console.log("isAdmin > " + isAdmin);

            var request = {
                agentid: $form.attr("data-agentid"),
                orgid: $form.attr("data-orgid"),
                admin: isAdmin
            };

            $.ajax({
                url: '/my/network/api/agent',
                method: 'POST',
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function(data) {
                    $.ajax({
                        url: '/my/network/fragment/organizations',
                        method: 'GET',
                        success: refreshRelationshipsTable
                    });
                },
                error: showError
            });

		});

        $("#confirm-delete").on("show.bs.modal", function(e) {
            $form = $(e.relatedTarget).parents('.form-table-row');
            var agentid = $form.attr("data-agentid");
            var orgid = $form.attr("data-orgid");
            var modal = $(this);
            $.ajax({
                url: '/my/network/api/remove-message/' + agentid + "/" + orgid,
                method: 'GET',
                success: function(data) {
                    modal.find(".modal-body").html(data);
                },
                error: showError
            });

//            $(this).find(".danger").attr("href", deleteUrl);
            $(this).find(".danger").click(function(event) {
                $.ajax({
                    url: "/my/network/api/agent/" + agentid + "/" + orgid,
                    method: 'DELETE',
                    success: function() {
                        reloadOrganizations();
                        modal.modal("hide");

                    },
                    error: showError
                });

            });
        });


	}

    $(".invite-button").click(function(e) {
        $("#organization").val($(this).data("orgid"));
    });

    $("#invite-form").submit(function(e) {
        e.preventDefault();
        var request = {
            orgid: $("#organization").val(),
            email: $("#inviteEmail").val()
        };
        $.ajax({
            url: "/my/network/api/invite",
            method: "POST",
            contentType:"application/json",
            data: JSON.stringify(request),
            success: function(e) {
                reloadOrganizations();
                $("#invite").modal('hide');
                $("#organization").val('');
                $("#inviteEmail").val('');
            },
            error: showError
        });
    });

    $("#inviteSubmit").click(function() {
        $("#invite-form").submit();
    });

	function showViewMode($form) {
		$('.edit-mode', $form).hide();
		$('.view-mode', $form).show();
	}
	
	function showEditMode($form) {
		$('.view-mode', $form).hide();
		$('.edit-mode', $form).show();
	}
	
	function refreshRelationshipsTable(data) {
		$('#organizations').replaceWith(data);
		initBindings($('#organizations'));
	}


    function showError(jqXHR) {
        // TODO check jqXHR
        var error = $("<div class='error-message'>Error</div>");
        $("body").append(error);
        error.show().delay(1000).fadeOut();
        showViewMode($form);
    }
});