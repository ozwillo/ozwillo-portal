$(document).ready(function () {

	initBindings();

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
                error: function(jqXHR) {
                    // TODO check jqXHR
                    var error = $("<div class='error-message'>Error</div>");
                    $("body").append(error);
                    error.show().delay(1000).fadeOut();
                    showViewMode($form);
                }
            });

		});
		
		// Remove relationship
		$('.lnk-remove', $el).click(function() {
			$form = $(this).parents('.form-table-row');
			var agentName = $('.data-agent-name', $form).html();
			if (confirm('Remove agent ' + agentName + '?')) {
				$.ajax({
					url: '/my/network/relationships/delete/' + $form.attr('id'),
					method: 'POST',
					success: refreshRelationshipsTable
				})
			}
		});
		
		// Add relationship
		$('#form-add-relationship', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshRelationshipsTable
			});
			$('#modal-add-relationship').modal('hide');
			return false;
		});
	}
	
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

});