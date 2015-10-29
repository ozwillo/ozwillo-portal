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

    	/****** Country SELECT2 *******/

    	var countryName = $('#address\\.country').val();

    	$('#address\\.country', $el).prepend('<option/>').val(function(){return $('[selected]',this).val() ;}) //to show the placeholder it is required to have at least one option
    	$('#address\\.country', $el).select2({
			minimumInputLength: 0,
			multiple: false,
			placeholder: countryName, // saved preset country name
			ajax: {
				url: store_service + "/dc-countries",
				dataType: 'json',
				quietMillis: 250,
				data: function (term) {
					return {
						q: term
					};
				},
				results: function (data, page) {
					var areas = data.areas;
					areas = areas.filter(function(n){return n !== null; });
					return { results: areas };
				},
				cache: true
			},
			formatResult: function(area) {
				return '<option className="action-select-option" value=' + area.name + '>' + area.name + '</option>';
			},
			id: function(area) { // This option must ALWAYS be set, otherwise it will take the Id from the object return (area.id)
				return area.name;
			},
			formatSelection: function(data) { // (data, container, escapeMarkup) {
				var area = data;
				if (area.uri) { setCountryUri(area.uri); } //set Country uri in order to filter the localization by it
				if(countryName !== area.name){// clean other fields if the country has changed
					$('#address\\.locality').attr("placeholder", " ");
					$('#address\\.locality').val("");
					$('#address\\.locality').data("select2").setPlaceholder();
					// empty the postal code
					$('#address\\.postalCode').val("");
				}
				return area.name;
			},
			dropdownCssClass: "bigdrop"
		});
		var countryUri ; // = null;
		function setCountryUri(country_uri) { countryUri = country_uri; }
		function getCountryUri() { return countryUri; }

		/* If country is present (contains a preset country name) it should fetch the list of countries and filter it by its
		   name in order to set the countryURI */
		if(countryName !== undefined && countryName !== null){
			$.ajax({
				url: store_service + "/dc-countries",
				type: 'get',
				dataType: 'json',
				data: {q: countryName}, // fetch only those that matche the preset country name, otherwise it will require to update the country field
				success: function (data) {
					if(data && data.areas ){
						var areas = data.areas.filter(function(n){return n.name === countryName; });
						if(areas !== null && areas.constructor === Array && areas.length>0){
							$('#address\\.country').val(areas[0].name);
							countryUri =  areas[0].uri;
						}
					}
				},
				error: function (xhr, status, err) {
					console.error(status, err.toString());
				}
			});
		}

		/****** City SELECT2 *******/

		var localityName = $('#address\\.locality').val();

		$('#address\\.locality', $el).prepend('<option/>').val(function(){return $('[selected]',this).val() ;})
		$('#address\\.locality', $el).select2({
			placeholder: localityName,
			minimumInputLength: 3,
			multiple: false,
			ajax: {
				url: store_service + "/dc-cities",
				dataType: 'json',
				quietMillis: 250,
				data: function (term) {
					return {
						country_uri: ''+getCountryUri(), // to filter by country
						q: term  // to avoid getting any results when the country is not selected
					};
				},
				results: function (data) {
					var areas = data.areas;
					areas = areas.filter(function(n){return n !== null; });
					return { results: areas };
				},
				/*transport: function (params, success, failure) {
				    var $request = $.ajax(params);
				    $request.then(success);
				    $request.fail(failure); // in case error should be shown as a displayed message?
				    return $request;
				},*/
				cache: true
			},
			formatResult: function(area) {
				return '<option className="action-select-option" value=' + area.uri + '>' + area.name + '</option>';
			},
			formatNoMatches: function () { // displayed error message in select2 if not match any fetch value
				return t("my.profile.errormsg.formatNoMatches")
			},
			formatAjaxError: function (jqXHR, textStatus, errorThrown) { // displayed error message in select2 if error in request/response
				return t("my.profile.errormsg.formatAjaxError")
			},
			id: function(data) { // This option must ALWAYS be set, otherwise it will take the Id from the object return (area.id)
				return data.name;
			},
			formatSelection: function(area) {
				if(area.postalCode) { $('#address\\.postalCode').val(area.postalCode); } // get and update the zip code from selected ville/zone
				localityName = area.name;
				return area.name;
			},
			dropdownCssClass: "bigdrop",
			escapeMarkup: function (m) { return m; }
		});
		$('#address\\.locality').val(localityName);

	} // end of initBindings() function

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

	// Change to EDIT mode
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