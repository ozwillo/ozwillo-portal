$(document).ready(function () {
	
	initBindings();

	function initBindings($el) {
		showViewMode();
		
		$('.btn', $el).button();
		
		// Show relationship form
		$('.btn-edit', $el).click(function() {
			$form = $(this).parents('.form-table-row');
			showViewMode();
			showEditMode($form);
		});
		
		// Save relationship changes
		$('.btn-accept', $el).click(function() {
			$form = $(this).parents('.form-table-row');
			var isAdmin = $('label.active input', $form).val() == 'yes';

			//  TODO
			$.ajax({
				url: '/my/network/fragment/relationships',
				method: 'GET',
				success: refreshRelationshipsTable
			})
		});
		
		// Remove relationship
		$('.btn-remove', $el).click(function() {
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
		$('#relationships').replaceWith(data);
		initBindings($('#relationships'));
	}

});