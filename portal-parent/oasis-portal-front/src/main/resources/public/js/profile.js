$(document).ready(function () {
    
	initBindings();

	function initBindings($el) {
		
		// Data layouts
		$('.action-toggle-edit', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'EDIT');
		});
		$('.action-toggle-view', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'VIEW');
		});
		$('.personal-data-form', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: function(data) {
					var layoutSelector = '#' + $form.attr('data');
					$(layoutSelector).replaceWith(data);
					initBindings($(layoutSelector));
				}
			})
		});
		
		// Edit avatar
    	$('#btn-edit-avatar', $el).click(function(e) {
    		$('#modal-edit-avatar').modal('show');
    		return false;
    	})
		$('.action-select-avatar', $el).click(function(e) {
			e.preventDefault();
			$('.action-select-avatar').removeClass('selected');
			$(this).addClass('selected');
    		$('#selected-avatar').val($(this).attr('src'));
    		return false;
		});
		$('#form-edit-avatar', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshAccountData
			});
		});
		
		// Edit language
    	$('#btn-edit-language', $el).click(function(e) {
    		$('#modal-edit-language').modal('show');
    		return false;
    	})
    	$('.action-select-language', $el).click(function(e) {
    		var $this = $(this);
    		$('#selected-language').val($this.attr('data'));
    		$('#selected-language-label').html($this.html());
    	});
		$('#form-edit-language', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: function() {
					// TODO (see #7) Rely on specific resolver instead of the interceptor
					window.location = '/my/profile?lang=' + $('#selected-language').val();
				}
			});
		});

		// Edit email
    	$('#btn-edit-email', $el).click(function(e) {
    		$('#modal-edit-email').modal('show');
    		return false;
    	})
		$('#form-edit-email', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshAccountData
			});
		});
    	
    	// Special layout widgets
    	$('.widget-dropdown').each(function() {
    		var $widget = $(this);
    		var $valueHolder = $('input', $widget);
    		var $labelHolder = $('.value-label', $widget);
    		$('a', $widget).click(function() {
    			$valueHolder.val($(this).attr('data'));
    			$labelHolder.html($(this).html());
    			$('button', $widget).dropdown('toggle');
    			return false;
    		});
    	});
    	
    	$('.widget-date-view').each(function() {
    		localizeDate($(this));
    	});
    	$('.widget-date-edit').each(function() {
    		var $widget = $(this);
    		var $valueHolder = $('.value-holder', $widget);
    		var $datePicker = $('.form-control', $widget);
    		var locale = $('#layouts').attr('data');

    		localizeDate($datePicker);
    		$datePicker.datepicker($.datepicker.regional[locale.replace('en', '')]);
    		$datePicker.datepicker("option", "altFormat", "yy-mm-dd");
    		$datePicker.datepicker("option", "altField", '#' + $valueHolder.attr('id'));
		});
	}
	
	function localizeDate($el) {
		var locale = $('#layouts').attr('data');
		var dateFormat = $.datepicker.regional[locale.replace('en', '')].dateFormat;
    	if ($el[0].tagName == 'INPUT') {
    		$el.val($.datepicker.formatDate(dateFormat, new Date($el.val())));
    	}
    	else {
    		$el.html($.datepicker.formatDate(dateFormat, new Date($el.html())));
    	}
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
	
	function refreshAccountData(html) {
		$('.modal.in').one('hidden.bs.modal', function() {
			$('#account-data').replaceWith(html);
			initBindings($('#account-data'));
		});
		$('.modal.in').modal('hide');
	}

});