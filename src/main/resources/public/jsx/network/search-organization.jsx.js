'use strict';

import React from 'react';
import ReactDOM from 'react-dom';

import Autosuggest from 'react-autosuggest';
var debounce = require('debounce');

import t from '../util/message';

var SearchOrganizationModal = React.createClass({
    componentDidMount: function () {
        $(ReactDOM.findDOMNode(this)).on("shown.bs.modal", function() {
            $("input#legal_name", this).focus();
        }.bind(this));
    },
    close: function (event) {
        $(ReactDOM.findDOMNode(this)).modal('hide');
    },
    open: function () {
        $(ReactDOM.findDOMNode(this)).modal('show');
        this.refs.form.resetSearchVals();
    },
    openCreateOrModify: function(organization) {
        $(ReactDOM.findDOMNode(this)).on("hidden.bs.modal", function() {
            this.props.successHandler(organization);
        }.bind(this));
        this.close();
    },
    render: function () {
        return (
            <div className="modal fade" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className='modal-dialog modal-lg' role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"} /></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{t('search.organization.search-organization')}</h4>
                        </div>
                        <SearchOrganizationForm ref="form"
                                                successHandler={this.openCreateOrModify}
                                                cancelHandler={this.close} />
                    </div>
                </div>
            </div>
        );
    }
});

var SearchOrganizationForm = React.createClass({
    getInitialState: function () {
        return {
            country: '',
            country_uri : '',
            errors: [],
            searching: false,
            exist_in_dc: false,
            exist_in_kernel: false,
            selected_organization: null,
            legal_name: '',
            tax_reg_num: null,
            sector_type: null
        };
    },
    resetSearchVals: function() {
        this.setState(this.getInitialState());
        this.refs.country.initUserCountry();
        this.refs.legal_name.reinitState();
    },
    onCountryChange: function(country, country_uri) {
        this.setState({ country: country, country_uri: country_uri, errors: [] });
    },
    onOrganizationSelected: function(organization) {
        $.ajax({
            url: network_service + "/kernel-organization",
            dataType: 'json',
            data: { dc_id: organization.id },
            type: 'head',
            global: false
        }).fail(function(xhr, status, err) {
            console.log("Returned status for kernel organization search " + organization.id, xhr.status, err.toString());
            if (xhr.status === 302)
                this.setState({ exist_in_dc: true, selected_organization: organization, exist_in_kernel: true, errors: ['general'] });
            else if (xhr.status === 404)
                this.setState({ exist_in_dc: true, selected_organization: organization, exist_in_kernel: false, errors: [] });
            else
                this.setState({ errors: ['technical'] })
        }.bind(this));
    },
    onOrganizationChange: function(value) {
        this.setState({
            exist_in_dc: false, exist_in_kernel: false,
            selected_organization: null, legal_name: value,
            errors: []
        });
    },
    onTaxRegNumChange: function(event) {
        this.setState({ tax_reg_num: event.target.value });
    },
    onSectorTypeChange: function(event) {
        if (this.state.selected_organization) {
            var selected_organization = this.state.selected_organization;
            selected_organization.sector_type = event.target.value;
            selected_organization.is_new_sector_type = true;
            this.setState({ selected_organization: selected_organization });
        } else {
            this.setState({ sector_type: event.target.value });
        }
    },
    searchOrganization: function (event) {
        if (event) { event.preventDefault(); }
        if (this.state.searching) { return; } /* do nothing if we're already searching... */

        var errors = [];
        if (!this.state.country)     { errors.push("country"); }
        if (!this.state.legal_name || this.state.legal_name.trim() === '')  { errors.push("legal_name"); }
        if (!this.state.tax_reg_num || this.state.tax_reg_num.trim() === '') { errors.push("tax_reg_num"); }
        if (!this.state.sector_type) { errors.push("sector_type"); }

        if (errors.length == 0) {
            this.setState({ searching: true });

            $.ajax({
                url: network_service + '/search-organization',
                type: 'get',
                contentType: 'json',
                data: { country: this.state.country, country_uri: this.state.country_uri, legal_name: this.state.legal_name,
                    tax_reg_num: this.state.tax_reg_num.replace(/\s/g, ''), sector_type: this.state.sector_type },
                success: function (data) {
                    if (data) {
                        // organization really does not exist in Ozwillo, going to next step
                        // data is the organization retrieved from DC or newly created
                        this.props.successHandler(data);
                    } else {
                        // organization already exists in Ozwillo, display an error message in the modal
                        this.setState({ searching: false, errors: ['general'] });
                    }
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                    this.setState({ searching: false, errors: ['technical'] });
                }.bind(this)
            });
        } else {
            this.setState({ errors: errors });
        }
    },
    onNextStep: function() {
        // called when organization has been found in DC and not in kernel (so we allow going to next step of creation process)
        // BUT some organizations do not have a sector type
        if (this.state.selected_organization.sector_type)
            this.props.successHandler(this.state.selected_organization);
        else
            this.setState({ errors: ['sector_type'] });
    },
    renderGeneralErrorMessage: function() {
        if (this.state.errors.indexOf('general') != -1) {
            return (
                <div className="alert alert-danger">{t('search.organization.cannot-be-used')}</div>
            )
        } else if (this.state.errors.indexOf('technical') != -1) {
            return (
                <div className="alert alert-danger">{t('search.organization.technical-problem')}</div>
            )
        }
    },
    renderSearchButton: function() {
        if (!this.state.exist_in_dc && this.state.country) {
            return (
                <button type="submit" className="btn btn-default-inverse"
                        onClick={this.searchOrganization}>{t('ui.search')}</button>
            )
        } else if (this.state.exist_in_dc && !this.state.exist_in_kernel) {
            return (
                <button type="submit" className="btn btn-default-inverse"
                        onClick={this.onNextStep}>{t('ui.next')}</button>
            )
        }
    },
    render: function () {
        return (
            <div>
                <div className="modal-body">
                    <form onSubmit={this.searchOrganization} className="form-horizontal">
                        <div className="form-group">
                            <CountrySelect ref="country"
                                           url={store_service + "/dc-countries"}
                                           countryUri={this.state.country_uri}
                                           onCountryChange={this.onCountryChange} />
                            <LegalName ref="legal_name"
                                       countryUri={this.state.country_uri}
                                       inError={$.inArray('legal_name', this.state.errors) != -1}
                                       onOrganizationSelected={this.onOrganizationSelected}
                                       onChange={this.onOrganizationChange}
                                       value={this.state.legal_name} />
                            <TaxRegNum country={this.state.country}
                                       inError={$.inArray('tax_reg_num', this.state.errors) != -1}
                                       display={!this.state.exist_in_kernel && this.state.country}
                                       static={this.state.exist_in_dc}
                                       value={this.state.selected_organization ? this.state.selected_organization.tax_reg_num : this.state.tax_reg_num}
                                       onChange={this.onTaxRegNumChange} />
                            <SectorType inError={$.inArray('sector_type', this.state.errors) != -1}
                                        display={!this.state.exist_in_kernel && this.state.country}
                                        static={this.state.exist_in_dc && this.state.selected_organization.sector_type !== null && !this.state.selected_organization.is_new_sector_type}
                                        value={this.state.selected_organization ? this.state.selected_organization.sector_type : this.state.sector_type}
                                        onChange={this.onSectorTypeChange}/>
                        </div>
                    </form>
                    {this.renderGeneralErrorMessage()}
                </div>
                <div className="modal-footer">
                    <button type="button" key="cancel" className="btn oz-btn-cancel"
                            onClick={this.props.cancelHandler}>{t('ui.cancel')}</button>
                    {this.renderSearchButton()}
                </div>
            </div>
        );
    }
});

// http://stackoverflow.com/questions/25793918/creating-select-elements-in-react-js
var CountrySelect = React.createClass({
    propTypes: {
        url: React.PropTypes.string.isRequired,
        onCountryChange: React.PropTypes.func.isRequired,
        countryUri: React.PropTypes.string
    },
    getInitialState: function() {
        return { countries: [] }
    },
    handleCountryChange: function(event) {
        var country_uri = event.target.value;
        var country = event.target.selectedOptions[0].label;
        this.props.onCountryChange(country, country_uri);
    },
    componentWillMount: function() {
        this.initUserCountry();
    },
    // TODO port the next two functions with promises
    initUserCountry: function() {
        // get countries from DC
        $.ajax({
            url: this.props.url,
            type: 'get',
            dataType: 'json',
            data: {q: ' '},
            success: function (data) {
                var areas = data.areas.filter(function (n) {
                    return n !== null;
                });
                var options = [{value: '', label: ''}];
                for (var i = 0; i < areas.length; i++) {
                    options.push({value: areas[i].uri, label: areas[i].name})
                }
                this.setState({ countries: options });
                this.getUserCountry();
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getUserCountry: function() {
        $.ajax({
            url: network_service + '/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                // Address is an optional field in user's profile
                if (data.address) {
                    // Try to match country with countries loaded from the DC
                    this.props.onCountryChange(data.address.country, this.getValue(data.address.country));
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getValue: function(label) {
        if (!label || label !== "") {
            for (var i = 0; i < this.state.countries.length; i++) {
                if (this.state.countries[i].label === label) {
                    return this.state.countries[i].value;
                }
            }
        }
        return null;
    },
    render: function() {
        var options = this.state.countries.map(function(country, index) {
            return <option key={index} value={country.value}>{country.label}</option>;
        });

        // the parameter "value=" is selected option. Default selected option can either be set here. Using browser-based function decodeURIComponent()
        return (
            <div className="form-group">
                <label htmlFor="country" className="col-sm-3 control-label required">
                    {t('search.organization.country')} *
                </label>
                <div className="col-sm-8">
                    <select className="form-control" id="country"
                            value={this.props.countryUri} onChange={this.handleCountryChange}>
                        {options}
                    </select>
                </div>
            </div>
        );
    }
});

var LegalName = React.createClass({
    propTypes: {
        countryUri: React.PropTypes.string.isRequired,
        value: React.PropTypes.string.isRequired,
        inError: React.PropTypes.bool.isRequired,
        onOrganizationSelected: React.PropTypes.func.isRequired,
        onChange: React.PropTypes.func.isRequired
    },
    getInitialState: function() {
        return {
            suggestions: []
        };
    },
    reinitState: function() {
        this.setState({ suggestions: [] });
    },
    searchOrganizations: function(query) {
        if (query.trim().length < 3) return;

        $.ajax({
            url: network_service + "/search-organizations",
            dataType: 'json',
            data: { country_uri: this.props.countryUri, query: query },
            type: 'get',
            success: function(data) {
                this.setState({ suggestions : data });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error("Error while searching for organizations with query " + query, status, err.toString())
            }
        })
    },
    renderSuggestion: function(data) {
        return (
            <div>
                <p className="main-info">{data.legal_name}</p>
                <p className="complementary-info">{data.city}</p>
            </div>
        )
    },
    onSuggestionsUpdateRequested: function({ value, reason }) {
        this.props.onChange(value);
        if (reason !== 'enter' && reason !== 'click')
            debounce(this.searchOrganizations(value), 500);
    },
    onSuggestionSelected: function(event, { suggestion, suggestionValue, method }) {
        this.props.onOrganizationSelected(suggestion);
    },
    render: function() {
        if (this.props.countryUri) {
            const inputProps = {
                value: this.props.value,
                onChange: (event, { newValue, method }) => this.props.onChange(newValue),
                type: 'search',
                placeholder: '',
                className: 'form-control'
            };

            var formGroupClass = this.props.inError ? 'form-group has-error' : 'form-group';
            return (
                <div className={formGroupClass}>
                    <label htmlFor="legal_name" className="col-sm-3 control-label required">
                        {t('search.organization.legal-name')} *
                    </label>

                    <div className="col-sm-8">
                        <div className="input-group">
                            <Autosuggest suggestions={this.state.suggestions}
                                         onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                                         onSuggestionSelected={this.onSuggestionSelected}
                                         getSuggestionValue={suggestion => suggestion.legal_name}
                                         renderSuggestion={this.renderSuggestion}
                                         inputProps={inputProps}
                                         shouldRenderSuggestions={input => input.trim().length > 2}/>
                            <span className="input-group-addon"><i className="fa fa-search"></i></span>
                        </div>
                    </div>
                </div>
            )
        } else {
            return null;
        }
    }
});

var TaxRegNum = React.createClass({
   render: function() {
       if (this.props.display) {
           var taxRegNumLabel = '';
           switch(this.props.country){
               case 'България' : taxRegNumLabel = t('search.organization.business-id.bg'); break;
               case 'Italia'   : taxRegNumLabel = t('search.organization.business-id.it'); break;
               case 'France'   : taxRegNumLabel = t('search.organization.business-id.fr'); break;
               case 'España'   : taxRegNumLabel = t('search.organization.business-id.es'); break;
               case 'Türkiye'  : taxRegNumLabel = t('search.organization.business-id.tr'); break;
               default         : taxRegNumLabel = t('search.organization.business-id.en'); break;
           }
           var formGroupClass = this.props.inError ? 'form-group has-error' : 'form-group';
           return (
               <div className={formGroupClass}>
                   <label htmlFor="tax_reg_num" className="col-sm-3 control-label required">
                       {taxRegNumLabel} *
                   </label>
                   <div className="col-sm-8">
                       <input type="text" id="tax_reg_num" className="form-control" maxLength="20"
                           disabled={this.props.static} value={this.props.value} onChange={this.props.onChange} />
                   </div>
               </div>
           )
       } else {
           return null;
       }

   }
});

var SectorType = React.createClass({
   render: function() {
       if (this.props.display) {
           var formGroupClass = this.props.inError ? 'form-group has-error' : 'form-group';

           return (
               <div className={formGroupClass}>
                   <label className="col-sm-3 control-label required">{t('search.organization.sector-type')} * </label>
                   <div className="col-sm-8">
                       <label className="radio-inline col-sm-3">
                           <input type="radio" name="sector_type" value="PUBLIC_BODY"
                                  disabled={this.props.static} onChange={this.props.onChange}
                                  checked={this.props.value === 'Public' || this.props.value === 'PUBLIC_BODY'} />
                           {t('search.organization.sector-type.PUBLIC_BODY')}
                       </label>
                       <label className="radio-inline col-sm-3">
                           <input type="radio" name="sector_type" value="COMPANY"
                                  disabled={this.props.static} onChange={this.props.onChange}
                                  checked={this.props.value === 'Private' || this.props.value === 'COMPANY'} />
                           {t('search.organization.sector-type.COMPANY')}
                       </label>
                   </div>
               </div>
           );
       } else {
           return null;
       }
   }
});

module.exports = { SearchOrganizationModal };
