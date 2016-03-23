'use strict';

import 'select2/dist/js/select2.full';
import 'select2/dist/js/i18n/bg.js';
import 'select2/dist/js/i18n/ca.js';
import 'select2/dist/js/i18n/es.js';
import 'select2/dist/js/i18n/en.js';
import 'select2/dist/js/i18n/fr.js';
import 'select2/dist/js/i18n/it.js';
import 'select2/dist/js/i18n/tr.js';
import 'select2/dist/css/select2.min.css';

var moment = require('moment');

import 'bootstrap-datepicker';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.bg';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.ca';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.es';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.fr';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.it';
import 'bootstrap-datepicker/js/locales/bootstrap-datepicker.tr';

import './csrf';
import './my';

import '../css/specific.css';

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

		// Edit avatar
    	$('#btn-edit-avatar', $el).click(function(e) {
    		$('#modal-edit-avatar').modal('show');
    		return false;
    	});
    	$('#btn-remove-avatar', $el).click(function(e) {
    		$('#selected-avatar').val('');
    		$('#selected-avatar-image').hide();
    		
    		$('#empty-avatar-label').show();
    		$('#btn-remove-avatar').hide();
    	});
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

    	$('.widget-date-view', $el).each(function() {
    		localizeDate($(this));
    	});
    	$('.widget-date-edit', $el).each(function() {
    		var $widget = $(this);
    		var $valueHolder = $('.value-holder', $widget);
    		var $datePicker = $('.form-control', $widget);
    		var locale = $('#layouts').attr('data');

    		localizeDate($datePicker);
			$datePicker.datepicker({
				language: locale,
				autoclose: true,
				clearBtn: true,
				defaultViewDate: new Date($datePicker.val())
			}).on('changeDate', function(e) {
				var valueHolderId = '#' + $valueHolder.attr('id');
				$(valueHolderId).val(moment(e.date).format("YYYY-MM-DD"));
			});
		});

    	/****** Country SELECT2 *******/

    	var countryName = $('#address\\.country-placeholder').val();
    	$('#address\\.country', $el).select2({
			minimumInputLength: 0,
			multiple: false,
			placeholder: countryName, // saved preset country name
			allowClear: true,
			ajax: {
				url: store_service + "/dc-countries",
				dataType: 'json',
				delay: 250,
				data: function(params) {
					return {
						q: params.term || ''
					};
				},
				processResults: function(data) {
					var areas = data.areas;
					areas = areas.filter(function(n){return n !== null; })
						.map(function(area) { return { id: area.name, text: area.name, uri: area.uri } });
					return { results: areas };
				},
				cache: true
			},
			templateSelection: function(data) { // (data, container, escapeMarkup) {
				var area = data;
				if (area.uri) { setCountryUri(area.uri); } //set Country uri in order to filter the localization by it
				if (countryName !== area.id && area.id !== '') {
					// clean other fields if the country has changed
					$('#address\\.locality').attr("placeholder", " ");
					$('#address\\.locality').val("");
					$('#address\\.postalCode').val("");
				}
				return area.text;
			},
			templateResult: function(area) {
				return '<option className="action-select-option" value=' + area.text + '>' + area.text + '</option>';
			},
			escapeMarkup: function(markup) { return markup; },
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

		var localityName = $('#address\\.locality-placeholder').val();

		$('#address\\.locality', $el).select2({
			minimumInputLength: 3,
			multiple: false,
			placeholder: localityName,
			allowClear: true,
			ajax: {
				url: store_service + "/dc-cities",
				dataType: 'json',
				delay: 250,
				data: function (params) {
					return {
						country_uri: ''+getCountryUri(), // to filter by country
						q: params.term || '' // to avoid getting any results when the country is not selected
					};
				},
				processResults: function (data) {
					var areas = data.areas;
					areas = areas.filter(function(n){return n !== null; })
						.map(function(area) { return { id: area.name, text: area.name, postalCode: area.postalCode } });
					return { results: areas };
				},
				cache: true
			},
			templateSelection: function(area) {
				if(area.postalCode) { $('#address\\.postalCode').val(area.postalCode); } // get and update the zip code from selected ville/zone
				localityName = area.name;
				return area.text;
			},
			templateResult: function(area) {
				return '<option className="action-select-option" value=' + area.id + '>' + area.text + '</option>';
			},
			escapeMarkup: function(markup) { return markup; },
			dropdownCssClass: "bigdrop"
		});
	} // end of initBindings() function

	function localizeDate($el) {
		var locale = $('#layouts').attr('data');
		moment.locale(locale);

    	if ($el[0].tagName == 'INPUT') {
    		if ($el.val() != '') {
				$el.val(moment($el.val()).format("L"));
    		}
    	}
    	else {
    		if ($el.html() != '') {
				$el.html(moment($el.html()).format("L"));
    		}
    	}
	}

	// Switch a profile fragment to EDIT / VIEW mode
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
