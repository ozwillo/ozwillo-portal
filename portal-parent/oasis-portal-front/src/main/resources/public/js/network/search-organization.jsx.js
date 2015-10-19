/** @jsx React.DOM */

// Modal containing the organization search form
var SearchOrganizationModal = React.createClass({
    componentDidMount: function () {
        $(this.getDOMNode()).modal({show: false});
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).off('hidden');
    },
    close: function (event) {
        $(this.getDOMNode()).modal('hide');
    },
    open: function () {
        $(this.getDOMNode()).modal('show');
        this.refs.form.resetSearchVals();
    },
    openCreateOrModify: function(organization) {
        this.close();
        this.props.successHandler(organization);
    },
    render: function () {
        return (
            <div className="modal fade">
                <div className='modal-dialog modal-lg'>
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" onClick={this.close}>&times;</button>
                            <h3>{t('my.network.find-or-create-organization')}</h3>
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
            searching: false
        };
    },
    onCountryChange: function(country, country_uri) {
        this.setState({ country: country, country_uri: country_uri });
    },
    resetSearchVals: function() {
        this.refs.legal_name.getDOMNode().value = '';
        this.refs.tax_reg_num.getDOMNode().value = '';
        this.refs.sector_type_PUBLIC_BODY.getDOMNode().checked = false;
        this.refs.sector_type_COMPANY.getDOMNode().checked = false;
        this.refs.country.getUserCountry();
    },
    searchOrganization: function (event) {
        if (event) { event.preventDefault(); }
        if (this.state.searching) { return; } /* do nothing if we're already searching... */
        var legal_name = this.refs.legal_name.getDOMNode().value.trim();
        var tax_reg_num = this.refs.tax_reg_num.getDOMNode().value.replace(/\s/g, '');
        var sector_type = '';
        if (this.refs.sector_type_PUBLIC_BODY.getDOMNode().checked)
            sector_type = 'PUBLIC_BODY';
        else if (this.refs.sector_type_COMPANY.getDOMNode().checked)
            sector_type = 'COMPANY';
        var errors = [];
        if (!this.state.country) { errors.push("country"); }
        if (!legal_name)  { errors.push("legal_name"); }
        if (!tax_reg_num) { errors.push("tax_reg_num"); }
        if (sector_type === '') { errors.push("sector_type"); }

        if (errors.length == 0) {
            this.setState({ searching: true });

            $.ajax({
                url: network_service + '/search-organization',
                type: 'get',
                contentType: 'json',
                data: { country: this.state.country, country_uri: this.state.country_uri, legal_name: legal_name,
                    tax_reg_num: tax_reg_num, sector_type: sector_type},
                success: function (data) {
                    if (data) {
                        // organization does not exist in Ozwillo, going to next step
                        // data is the organization retrieved from DC or newly created
                        this.setState({searching: false });
                        this.props.successHandler(data);
                    } else {
                        // organization exists in Ozwillo, display an error message in the modal
                        this.setState({ searching: false, errors: ['general'] });
                    }
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                    this.setState({ searching: false, errors: ['general'] });
                }.bind(this)
            });
        } else {
            this.setState({ errors : errors });
        }
    },
    renderLabel: function(htmlFor, class_name, label){
        var cn = ($.inArray(class_name, this.state.errors) != -1 ? 'col-sm-3 control-label error' : 'col-sm-3 control-label');
        return (
            <label htmlFor={htmlFor} className={cn}>{label}
                <label className="error">&nbsp;*</label>
            </label>
        );
    },
    renderSectorType: function () {
        if (this.state.country) {
            var sectorTypeClassName = 'col-sm-3 control-label';
            sectorTypeClassName = ($.inArray('sector_type', this.state.errors) != -1 ? sectorTypeClassName + ' error' : sectorTypeClassName);

            return (
                <div className="form-group">
                    <label htmlFor="organization-sector-type"
                           className={sectorTypeClassName}>{t('search.organization.sector-type')}
                        <label className="error">&nbsp;*</label>
                    </label>
                    <label className="radio-inline col-sm-3">
                        <input type="radio" value="PUBLIC_BODY" ref="sector_type_PUBLIC_BODY">
                            {t('search.organization.sector-type.PUBLIC_BODY')}
                        </input>
                    </label>
                    <label className="radio-inline col-sm-3">
                        <input type="radio" value="COMPANY" ref="sector_type_COMPANY">
                            {t('search.organization.sector-type.COMPANY')}
                        </input>
                    </label>
                </div>
            );
        }
    },
    renderLegalName: function() {
        if (this.state.country) {
            return (
                <div className="form-group">
                    {this.renderLabel("organization-name", 'legal_name', t('search.organization.legal-name'))}
                    <div className="col-sm-8">
                        <input type="text" className="form-control" maxLength="100" ref="legal_name" />
                    </div>
                </div>
            )
        }
    },
    renderTaxRegNum: function() {
        if (this.state.country) {
            var taxRegNumLabel = '';
            switch(this.state.country){
                case 'България' : taxRegNumLabel = t('search.organization.business-id.bg'); break;
                case 'Italia'   : taxRegNumLabel = t('search.organization.business-id.it'); break;
                case 'France'   : taxRegNumLabel = t('search.organization.business-id.fr'); break;
                case 'España'   : taxRegNumLabel = t('search.organization.business-id.es'); break;
                case 'Türkiye'  : taxRegNumLabel = t('search.organization.business-id.tr'); break;
                default         : taxRegNumLabel = t('search.organization.business-id.en'); break;
            }
            return (
                <div className="form-group">
                    {this.renderLabel("organization-business-id", 'tax_reg_num', taxRegNumLabel)}
                    <div className="col-sm-8">
                        <input type="text" className="form-control" maxLength="20" ref="tax_reg_num"/>
                    </div>
                </div>
            )
        }
    },
    renderGeneralErrorMessage: function() {
        if ($.inArray('general', this.state.errors) != -1) {
            return (
                <label className="error">{t('search.organization.cannot-be-used')}</label>
            )
        }
    },
    render: function () {
        return (
            <div>
                <div className="modal-body">
                    <form onSubmit={this.searchOrganization} className="form-horizontal">
                        <div className="form-group">
                            <div className="form-group">
                                {this.renderLabel("organization-country-name", 'country', t('search.organization.country'))}
                                <div className="col-sm-5">
                                    <CountrySelect className="form-control"
                                                   ref="country"
                                                   url={store_service + "/dc-countries"}
                                                   onCountryChange={this.onCountryChange} />
                                </div>
                            </div>
                            {this.renderLegalName()}
                            {this.renderTaxRegNum()}
                            {this.renderSectorType()}
                        </div>
                    </form>
                    {this.renderGeneralErrorMessage()}
                </div>
                <div className="modal-footer">
                    <button key="cancel" className="btn btn-default" onClick={this.props.cancelHandler}>{t('ui.cancel')}</button>,
                    <button key="success" className="btn btn-primary" onClick={this.searchOrganization}>{t('ui.search')}</button>
                </div>
            </div>
        );
    }
});

//http://stackoverflow.com/questions/25793918/creating-select-elements-in-react-js
/** PROPS: onChange(), url */
var CountrySelect = React.createClass({
    propTypes: {
        url: React.PropTypes.string.isRequired
    },
    getInitialState: function() {
        return { countries: [], country_uri: '' }
    },
    handleCountryChange: function(event) {
        var country_uri = event.target.value;
        var country = event.target.selectedOptions[0].label;
        this.setState({ country_uri : country_uri });
        this.props.onCountryChange(country, country_uri);
    },
    componentWillMount: function() {
        if (this.props.url) {
            // get countries from DC
            $.ajax({
                url: this.props.url,
                type: 'get',
                dataType: 'json',
                data: {q:' '},
                success: function (data) {
                    var areas = data.areas.filter(function(n){return n !== null; });
                    var options = [{ value: '', label: '' }];
                    for (var i = 0; i < areas.length; i++) {
                        options.push({ value: areas[i].uri, label: areas[i].name })
                    }
                    this.setState({ countries : options });
                    this.getUserCountry();
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }.bind(this)
            });
        }
    },
    getUserCountry: function() {
        $.ajax({
            url: network_service + '/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                // For now country is a free field, we gonna try to match it with countries loaded from the DC
                // When profile is integrated with geodata from DC, migrate this part
                this.setState({ country_uri: this.getValue(data.address.country) });
                this.props.onCountryChange(data.address.country, this.getValue(data.address.country));
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
            return <option className="action-select-option" key={index} value={country.value}>{country.label}</option>;
        });

        // the parameter "value=" is selected option. Default selected option can either be set here. Using browser-based function decodeURIComponent()
        return (
            <select className="btn btn-default dropdown-toggle" ref="country"
                    value={this.state.country_uri} onChange={this.handleCountryChange} >
                {options}
            </select>
        );
    }
});
