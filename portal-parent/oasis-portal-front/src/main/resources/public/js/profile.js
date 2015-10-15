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
		//$('.personal-data-form', $el).submit(function(e) {
		//	e.preventDefault();
		//	$form = $(this);
		//	$.ajax({
		//		url: $form.attr('action'),
		//		method: 'POST',
		//		data: $form.serialize(),
		//		success: function(accountFragment) {
		//			var layoutSelector = '#' + $form.attr('data');
		//			$(layoutSelector).replaceWith(accountFragment);
		//			initBindings($(layoutSelector));
		//			// if switching language, the returned account layout fragment from xhr will contain
		//			// the new localized labels. We however also want to update other layouts fields's labels
		//			// (only if these are in view mode as to prevent reseting in-editing state forms).
		//			if($form.attr('data')==='account') {
		//
		//				if($('#identity').find('button').attr('class').indexOf('action-toggle-view active')>0) {
		//					$.ajax({
		//						url: '/my/profile/fragment/layout/identity',
		//						method: 'GET',
		//						data: $form.serialize(),
		//						success: function(identityFragment) {
		//
		//							$('#identity').replaceWith(identityFragment);
		//							initBindings($('#identity'));
		//						}
		//					});
		//				}
		//				if($('#address').find('button').attr('class').indexOf('action-toggle-view active')>0) {
		//					$.ajax({
		//						url: '/my/profile/fragment/layout/address',
		//						method: 'GET',
		//						data: $form.serialize(),
		//						success: function(identityFragment) {
		//
		//							$('#address').replaceWith(identityFragment);
		//							initBindings($('#address'));
		//						}
		//					});
		//				}
		//			}
		//		}
		//	})
		//});
		
		// Edit avatar
    	$('#btn-edit-avatar', $el).click(function(e) {
    		$('#modal-edit-avatar').modal('show');
    		return false;
    	})
    	$('#btn-remove-avatar', $el).click(function(e) {
    		$('#selected-avatar').val('');
    		$('#selected-avatar-image').hide();
    		
    		$('#empty-avatar-label').show();
    		$('#btn-remove-avatar').hide();
    	})
		$('.action-select-avatar', $el).click(function(e) {
			e.preventDefault();
			$('.action-select-avatar').removeClass('selected');
			$(this).addClass('selected');
    		$('#selected-avatar').val($(this).attr('src'));
    		$('#selected-avatar-image').attr('src', $(this).attr('src'));
    		$('#selected-avatar-image').show();
    		
    		$('#btn-remove-avatar').show();
    		$('#empty-avatar-label').hide();
    		
    		$('#modal-edit-avatar').modal('hide');
    		return false;
		});

    	$('#btn-upload-avatar', $el).click(function(e) {
    		$('#upload-avatar').click();
    	});
        $('#upload-avatar', $el).change(function(e) {
        	console.log(this.files[0], this.form.action);
    		var formData = new FormData();
	        formData.append('iconFile', this.files[0]);
	        $.ajax({
	            url: this.form.action,
	            cache: false,
	            contentType: false,
	            processData: false,
	            type: "POST",
	            data: formData,
	            success: function(servedImageUrlData) {
	        		$('#selected-avatar').val(servedImageUrlData);
	        		$('#selected-avatar-image').attr('src', servedImageUrlData);
	        		
	        		$('#btn-remove-avatar').show();
	        		$('#empty-avatar-label').hide();
	            },
	            error: function (xhr, status, err) {
	                console.error(status, err.toString());
	        		$('#modal-avatar-upload-failure').modal('show');
	            }
	        });
    	});
    	
    	$('.action-select-option', $el).click(function(e) {
    		var $this = $(this);
    		var optionId = e.target.id;
    		// $(this) isn't the enclosing widget, then can't use the following code used by .widget-dropdown
    		// var $widget = $(this);
    		// var $valueHolder = $('input', $widget);
    		// we could use $(this).closest('ul').parent() or $(this).closest('ul').closest('input') 
    		var widgetId = optionId.substring(optionId.indexOf('option-')+7, optionId.lastIndexOf('-'));
    		$('#selected-option-'+widgetId).val($this.attr('data'));
    		$('#selected-option-'+widgetId+'-label').html($this.html());
    	});
    	
    	// Special layout widgets
    	$('.widget-dropdown', $el).each(function() {
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
    	
    	$('.widget-date-view', $el).each(function() {
    		localizeDate($(this));
    	});
    	$('.widget-date-edit', $el).each(function() {
    		var $widget = $(this);
    		var $valueHolder = $('.value-holder', $widget);
    		var $datePicker = $('.form-control', $widget);
    		var locale = $('#layouts').attr('data');

    		localizeDate($datePicker);
    		$datePicker.datepicker($.datepicker.regional[locale.replace('en', 'en-GB')]);
    		$datePicker.datepicker("option", "altFormat", "yy-mm-dd");
    		$datePicker.datepicker("option", "altField", '#' + $valueHolder.attr('id'));
		});

		$('#address\\.country', $el).select2({
			minimumInputLength: 0,
			ajax: {
				url: store_service + "/dc-countries",
				dataType: 'json',
				quietMillis: 250,
				data: function (term) {
					return {
						q: term
					};
				},
				results: function (data) {
					var areas = data.areas;
					areas = areas.filter(function(n){return n !== null; });
					return { results: areas };
				},
				cache: true
			},
			formatResult: function(area) {
				return '<option className="action-select-option" value=' + area.uri + '>' + area.name + '</option>';
			},
			formatSelection: function(area) {
				return area.name;
			},
			dropdownCssClass: "bigdrop",
			escapeMarkup: function (m) { return m; }
		});
	}
	
	function localizeDate($el) {
		var locale = $('#layouts').attr('data');
		var dateFormat = $.datepicker.regional[locale.replace('en', 'en-GB')].dateFormat;
		
    	if ($el[0].tagName == 'INPUT') {
    		if ($el.val() != '') {
    			$el.val($.datepicker.formatDate(dateFormat, new Date($el.val())));
    		}
    	}
    	else {
    		if ($el.html() != '') {
    			$el.html($.datepicker.formatDate(dateFormat, new Date($el.html())));
    		}
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
            },
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }
		})
	}

});
