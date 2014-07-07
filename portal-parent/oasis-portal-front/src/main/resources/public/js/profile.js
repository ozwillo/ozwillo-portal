$(document).ready(function () {
    
	initBindings();

	function initBindings($el) {
		
		$('button.btn-edit', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'EDIT');
		});
	
		$('button.btn-view', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'VIEW');
		});

		$('form', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			saveLayout($form.attr('data'), $form.serialize());
		});
		
	}
	
	function toggleProfileLayout(id, mode) {
		$.ajax({
			url: '/my/profile/mode',
			method: 'POST',
			data: {
				id: id,
				mode: mode
			},
			success: function(data) {
				$('#' + id).replaceWith(data);
				initBindings($('#' + id));
			}
		})
	}
	
	function saveLayout(id, data) {
		$.ajax({
			url: '/my/profile/save',
			method: 'POST',
			data: data,
			success: function(data) {
				$('#' + id).replaceWith(data);
				initBindings($('#' + id));
			}
		})
		
	}

});