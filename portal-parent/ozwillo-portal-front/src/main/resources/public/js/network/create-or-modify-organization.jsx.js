/** @jsx React.DOM */

var CreateOrModifyOrganizationModal = React.createClass({
    getInitialState: function () {
        return {
            DCOrganization: {},
            step: 1
        };
    },
    componentDidMount: function() {
        // TODO : only effective at the first opening
        $(this.getDOMNode()).on("shown.bs.modal", function() {
            $("input:first", this).focus();
        });
    },
    open: function(DCOrganization) {
        this.setState({ DCOrganization: DCOrganization, step: 1 });
        $(this.getDOMNode()).modal('show');
    },
    onStepChange: function(stepId) {
        this.setState({ step: stepId });
    },
    close: function() {
        $(this.getDOMNode()).modal('hide');
    },
    closeAfterSuccess: function() {
        $(this.getDOMNode()).modal('hide');
        this.props.successHandler();
    },
    render: function () {

        var modalTitle = (this.state.DCOrganization.exist ? t('my.network.modify-org') : t('my.network.create-org'))
            + " " + this.state.DCOrganization.legal_name;
        var modalSubTitle = t('my.network.organization.step') + " " + this.state.step + " / 2";
        return (
            <div className="modal fade" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className='modal-dialog modal-lg' role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"} /></span>
                            </button>
                            <h3 className="modal-title" id="modalLabel">{modalTitle}</h3>
                            <h4>{modalSubTitle}</h4>
                        </div>
                        <CreateOrModifyOrganizationForm ref="form"
                                                        successHandler={this.closeAfterSuccess}
                                                        cancelHandler={this.close}
                                                        DCOrganization={this.state.DCOrganization}
                                                        step={this.state.step}
                                                        onStepChange={this.onStepChange}
                                                        fromStore={false}
                                                        org={this.props.org}
                        />
                    </div>
                </div>
            </div>
        );
    }
});

var CreateOrModifyOrganizationForm = React.createClass({
    getInitialState: function () {
        return {
            createOrUpdateError: { code: '', message: ''}
        }
    },
    onNextTab: function() {
        if (this.refs.tab1.validateFields()) {
            this.props.onStepChange(2);
        }
    },
    onPrevTab: function() {
        if (this.refs.tab2.validateFields()) {
            this.props.onStepChange(1);
        }
    },
    onCreate: function() {
        if (this.refs.tab1.validateFields() && this.refs.tab2.validateFields()) {
            var finalOrganization = $.extend({}, this.props.DCOrganization, this.refs.tab1.getFields());
            if (finalOrganization.exist) {
                this.updateOrganization(finalOrganization);
            } else {
                this.createOrganization(finalOrganization);
            }
        }
    },
    createOrganization: function(DCOrganization) {
        $.ajax({
            url: network_service + '/create-dc-organization',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(DCOrganization),
            success: function (data) {
                if (data) {
                    this.props.successHandler(data);
                } else {
                    console.error('Organization was not created : ' + DCOrganization);
                    this.setState({ createOrUpdateError: { code: 'Invalid response', message: 'The received response was empty' } });
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                this.setState({ createOrUpdateError: { code: err, message: xhr.responseText } });
            }.bind(this)
        });
    },
    updateOrganization: function(DCOrganization){
        $.ajax({
            url: network_service + '/update-dc-organization',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(DCOrganization),
            success: function (data) {
                if (data) {
                    this.props.successHandler(data);
                } else {
                    console.error('Organization was not modified : ' + DCOrganization);
                    this.setState({ createOrUpdateError: { code: 'Invalid response', message: 'The received response was empty' } });
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                this.setState({ createOrUpdateError: { code: err, message: xhr.responseText } });
            }.bind(this)
        });
    },
    onCancel: function(){
        this.props.cancelHandler();
        this.setState({ createOrUpdateError: {code: '', message: ''}});
    },
    renderCreateOrUpdateError: function () {
        if (this.state.createOrUpdateError.code !== '') {
            return (
                <div className="alert alert-danger">{this.state.createOrUpdateError.message} ({this.state.createOrUpdateError.code})</div>
            )
        }
    },
    render: function () {
        var DCOrganization = this.props.DCOrganization;
        return (
            <div>
                <div className="modal-body">
                    <form id="add-organization" onSubmit={this.onCreate} className="form-horizontal">
                        <div className="form-horizontal">
                            <Tab1 id="1" ref="tab1" DCOrganization={DCOrganization} currentTab={this.props.step} fromStore={this.props.fromStore} />
                            <Tab2 id="2" ref="tab2" DCOrganization={DCOrganization} currentTab={this.props.step} org={this.props.org}/>
                        </div>
                        {this.renderCreateOrUpdateError()}
                    </form>
                </div>
                <div className="modal-footer">
                    <Button activeTab={this.props.step}
                            onNext={this.onNextTab}
                            onPrev={this.onPrevTab}
                            onCancel={this.onCancel}
                            onCreate={this.onCreate}
                            inModification={DCOrganization.inModification} />
                </div>
            </div>
        );
    }
});

var Button = React.createClass({
    renderStepSwitcherButton: function () {
        if (this.props.activeTab === 1) {
            return (
                <button type="button" key="next" className="control btn btn-default-inverse" onClick={this.props.onNext}>
                    {t('ui.next')}
                </button>
            )
        } else {
            return (
                <button type="button" key="prev" className="control btn btn-default-inverse" onClick={this.props.onPrev}>
                    {t('ui.previous')}
                </button>
            )
        }
    },
    renderCreateButton: function () {
        if (this.props.activeTab === 2) {
            if (this.props.inModification) {
                return (
                    <button type="submit" key="success" className="btn oz-btn-save" onClick={this.props.onCreate}>
                        {t('ui.edit')}
                    </button>
                )
            } else {
                return (
                    <button type="submit" key="success" className="btn oz-btn-save" onClick={this.props.onCreate}>
                        {t('ui.create')}
                    </button>
                )
            }
        }
    },
    render: function() {
        return (
            <div>
                <button type="button" key="cancel" className="btn oz-btn-cancel" onClick={this.props.onCancel}>
                    {t('ui.cancel')}
                </button>
                {this.renderStepSwitcherButton()}
                {this.renderCreateButton()}
            </div>
        );
    }
});

var Field = React.createClass({
    propTypes: {
        name: React.PropTypes.string.isRequired,
        labelClassName: React.PropTypes.string,
        divClassName: React.PropTypes.string,
        error: React.PropTypes.bool,
        isRequired: React.PropTypes.bool
    },
    renderLabel: function(htmlFor, class_name, label, isRequired) {
        return (
            <label htmlFor={htmlFor} className={class_name}>{label} {isRequired ? '*' : ''} </label>
        );
    },
    render: function() {
        var labelClassName = this.props.labelClassName ? this.props.labelClassName : "control-label col-sm-3";
        if (this.props.isRequired) {
            labelClassName = labelClassName + " required";
        }
        var divClassName = this.props.divClassName ? this.props.divClassName : "col-sm-7";
        var formDivClassName = this.props.error ? "form-group has-error" : "form-group";

        return (
            <div className={formDivClassName}>
                {this.renderLabel(this.props.name, labelClassName, t('my.network.organization.' + this.props.name), this.props.isRequired)}
                <div className={divClassName}>
                    {this.props.children}
                </div>
            </div>
        );
    }
});

var Tab1 = React.createClass({
    getInitialState: function() {
        return {
            DCOrganization: {},
            contact_lastname: '',
            contact_name: '',
            errors: []
        };
    },
    componentWillMount: function() {
        $.ajax({
            url: network_service + '/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                this.setState({
                    contact_lastname: data.user_lastname,
                    contact_name: data.user_name
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({ DCOrganization: nextProps.DCOrganization });
    },
    handleInputChange: function(event) {
        var DCOrganization = this.state.DCOrganization;
        if (event.target.id === 'contact_lastname')
            this.setState({ contact_lastname : event.target.value });
        else if (event.target.id === 'contact_name')
            this.setState({ contact_name : event.target.value });
        else if (event.target.name === 'city') {
            DCOrganization.city_uri = event.target.value;
            if (event.added)
                DCOrganization.city = event.added.name;
        } else {
            DCOrganization[event.target.id] = event.target.value;
        }

        this.setState({ DCOrganization: DCOrganization });
    },
    validateFields: function() {
        // FIXME : as input changes are store in state, better check errors in state rather than refs
        var errors = [];
        if (!this.props.DCOrganization.inModification && !this.props.fromStore) {
            if (this.refs.contact_lastname.getDOMNode().value.trim() === '')
                errors.push("contact_lastname");
            if (this.refs.contact_name.getDOMNode().value.trim() === '')
                errors.push("contact_name");
        }
        if (this.refs.street_and_number.getDOMNode().value.trim() === '')
            errors.push("street_and_number");
        if (!this.state.DCOrganization.city || this.state.DCOrganization.city === '')
            errors.push("city");
        if (this.refs.zip.getDOMNode().value.trim() === ''
            || this.refs.zip.getDOMNode().value.trim().search(/^\d+$/) === -1)
            errors.push("zip");
        if (this.refs.cedex.getDOMNode().value.trim() !== ''
            && this.refs.cedex.getDOMNode().value.trim().search(/^\d+$/) === -1)
            errors.push("cedex");

        if (errors.length > 0)
            errors.push("validation");
        this.setState({ errors: errors });

        return (errors.length == 0);
    },
    getFields: function() {
        if (!this.props.DCOrganization.inModification && !this.props.fromStore) {
            return {
                contact_lastName: this.refs.contact_lastname.getDOMNode().value.trim(),
                contact_name: this.refs.contact_name.getDOMNode().value.trim()
            }
        } else {
            return {}
        }
    },
    renderGeneralErrorMessage: function() {
        if ($.inArray('validation', this.state.errors) != -1) {
            return (
                <div className="alert alert-danger">{t('my.network.organization.invalid_fields')}</div>
            )
        }
    },
    renderProfileInformation: function() {
        if (!this.props.DCOrganization.inModification && !this.props.fromStore)
            return (
                <fieldset>
                    <legend>{t('my.network.organization.profile_information')}</legend>
                    <Field name="contact_lastname" error={$.inArray("contact_lastname", this.state.errors) != -1} isRequired={true}>
                        <input className="form-control" ref="contact_lastname" id="contact_lastname" type="text"
                               value={this.state.contact_lastname} onChange={this.handleInputChange} />
                    </Field>
                    <Field name="contact_name" error={$.inArray("contact_name", this.state.errors) != -1} isRequired={true}>
                        <input className="form-control" ref="contact_name" id="contact_name" type="text"
                               value={this.state.contact_name} onChange={this.handleInputChange} />
                    </Field>
                </fieldset>
            )
    },
    render: function() {
        var className = this.props.currentTab === 1 ? "" : "hidden";
        var country_uri = this.props.DCOrganization ? this.props.DCOrganization.country_uri : '';
        // FIXME : quite ugly and non-Reacty, find a better way to do this
        //         when called from the store, componentWillReceiveProps does not get called b/c there is no change in props
        //         find the right lifecycle hook to handle this properly
        this.state.DCOrganization = this.props.DCOrganization;

        return (
            <div id="tab1" className={className}>
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-sm-15">
                            {this.renderProfileInformation()}
                            <fieldset>
                                <legend>{t('my.network.organization.contact_information')}</legend>
                                <Field name="street_and_number" error={$.inArray("street_and_number", this.state.errors) != -1} isRequired={true}>
                                    <input className="form-control" ref="street_and_number" id="street_and_number" type="text"
                                           value={this.state.DCOrganization.street_and_number} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="additional_address_field" error={$.inArray("additional_address_field", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="additional_address_field" id="additional_address_field" type="text"
                                           value={this.state.DCOrganization.additional_address_field} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="po_box" error={$.inArray("po_box", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="po_box" id="po_box" type="text"
                                           value={this.state.DCOrganization.po_box} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="city" error={$.inArray("city", this.state.errors) != -1} isRequired={true}>
                                    <GeoSingleSelect2Component ref="city" className="form-control" name="city"
                                                               key={this.state.DCOrganization.city}
                                                               urlResources={store_service + "/dc-cities"}
                                                               countryFilter={ {country_uri: country_uri} }
                                                               onChange={this.handleInputChange}
                                                               placeholder={this.state.DCOrganization.city} />
                                </Field>
                                <Field name="zip" divClassName='col-sm-2' error={$.inArray("zip", this.state.errors) != -1} isRequired={true}>
                                    <input className="form-control" ref="zip" id="zip" type="text" maxLength={6}
                                           value={this.state.DCOrganization.zip} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="cedex" divClassName='col-sm-2' error={$.inArray("cedex", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="cedex" id="cedex" type="text" maxLength={3}
                                           value={this.state.DCOrganization.cedex} onChange={this.handleInputChange} />
                                </Field>
                            </fieldset>
                        </div>
                    </div>
                </div>
                {this.renderGeneralErrorMessage()}
            </div>
        );
    }
});

var Tab2 = React.createClass({
    getInitialState: function() {
        return {
            DCOrganization: {},
            errors: []
        };
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({ DCOrganization: nextProps.DCOrganization });
    },
    handleInputChange: function(event) {
        var DCOrganization = this.state.DCOrganization;

        if (event.target.id === 'in_activity')
            DCOrganization.in_activity = event.target.checked;
        else if (event.target.name === 'tax_reg_activity') {
            DCOrganization.tax_reg_activity_uri = event.target.value;
            if (event.added)
                DCOrganization.tax_reg_activity = event.added.name;
        } else if (event.target.name === 'jurisdiction') {
            DCOrganization.jurisdiction_uri = event.target.value;
            if (event.added)
                DCOrganization.jurisdiction = event.added.name;
        } else
            DCOrganization[event.target.id] = event.target.value;

        this.setState({ DCOrganization: DCOrganization });
    },
    validateFields: function() {
        var errors = [];
        if (this.refs.legal_name.getDOMNode().value.trim() === '')
            errors.push("legal_name");
        if (this.state.DCOrganization.sector_type === 'PUBLIC_BODY' &&
            (!this.state.DCOrganization.jurisdiction || this.state.DCOrganization.jurisdiction === ''))
            errors.push("jurisdiction");

        if (errors.length > 0)
            errors.push("validation");
        this.setState({ errors: errors });

        return (errors.length == 0);
    },
    getFields: function() {
        return {}
    },
    // Get tax reg related labels according to organization's country
    // returns { tax_reg_num_label : string, tax_reg_official_id_label : string (optional), tax_reg_activity_uri_label : string (optional)
    getTaxRegLabels: function() {
        var tax_reg_num_label,
            tax_reg_official_id_label = '',
            tax_reg_activity_label = '';
        var n = this.state.DCOrganization.country_uri ? this.state.DCOrganization.country_uri.lastIndexOf('/') : -1;
        var acronymCountry = n > 0 ? this.state.DCOrganization.country_uri.substring(n + 1) : '';
        switch (acronymCountry) {
            case 'BG' : tax_reg_num_label = 'tax_reg_num.bg'; tax_reg_official_id_label = 'tax_reg_activity.bg'; break;
            case 'IT' : tax_reg_num_label = 'tax_reg_num.it'; tax_reg_official_id_label = 'tax_reg_activity.it'; break;
            case 'FR' : tax_reg_num_label = 'tax_reg_num.fr'; tax_reg_official_id_label = 'tax_reg_official_id.fr';
                        tax_reg_activity_label = 'tax_reg_activity.fr'; break;
            case 'ES' : tax_reg_num_label = 'tax_reg_num.es'; tax_reg_official_id_label = 'tax_reg_activity.es'; break;
            case 'TR' : tax_reg_num_label = 'tax_reg_num.tr'; tax_reg_official_id_label = 'tax_reg_official_id.tr';
                        tax_reg_activity_label = 'tax_reg_activity.tr'; break;
            default   : tax_reg_num_label = 'tax_reg_num.en'; break;
        }

        return {
            tax_reg_num_label: tax_reg_num_label,
            tax_reg_official_id_label: tax_reg_official_id_label,
            tax_reg_activity_label: tax_reg_activity_label
        };
    },
    renderTaxRegOfficialId: function(tax_reg_official_id_label) {
        if (tax_reg_official_id_label !== '' && this.state.DCOrganization.sector_type === 'PUBLIC_BODY') {
            return (
                <Field name={tax_reg_official_id_label} error={$.inArray("tax_reg_official_id", this.props.errors) != -1} isRequired={false}>
                    <input className="form-control" ref="tax_reg_official_id" id="tax_reg_official_id" type="text"
                           value={this.state.DCOrganization.tax_reg_official_id} onChange={this.handleInputChange} />
                </Field>
            )
        }
    },
    renderJurisdiction: function() {
        if (this.state.DCOrganization.sector_type === 'PUBLIC_BODY') {
            return (
                <Field name="jurisdiction" error={$.inArray("jurisdiction", this.props.errors) != -1} isRequired={true}>
                    <GeoSingleSelect2Component ref="jurisdiction" className="form-control" name="jurisdiction"
                                               key="jurisdiction"
                                               urlResources={store_service + "/geographicalAreas"}
                                               countryFilter={ {country_uri: this.props.DCOrganization.country_uri} }
                                               onChange={this.handleInputChange}
                                               placeholder={this.state.DCOrganization.jurisdiction} />
                </Field>

            )
        }
    },
    renderGeneralErrorMessage: function() {
        if ($.inArray('validation', this.state.errors) != -1) {
            return (
                <div className="alert alert-danger">{t('my.network.organization.invalid_fields')}</div>
            )
        }
    },
    render: function() {
        var className = this.props.currentTab === 2 ? "" : "hidden";
        var taxRegNumLabels = this.getTaxRegLabels();
        var tax_reg_activity_uri_placeholder = !this.state.DCOrganization.tax_reg_activity_uri ? ' '
            : this.state.DCOrganization.tax_reg_activity_uri.substring(this.state.DCOrganization.tax_reg_activity_uri.lastIndexOf("/") + 1);
        var not_admin = this.props.org ? !this.props.org.admin : false;

        return (
            <div id="tab2" className={className}>
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-sm-15">
                            <fieldset>
                                <legend>{t('my.network.organization.additional_information')}</legend>
                                <Field name="legal_name" error={$.inArray("legal_name", this.state.errors) != -1} isRequired={true}>
                                    <input className="form-control" ref="legal_name" id="legal_name" type="text"
                                           value={this.state.DCOrganization.legal_name} onChange={this.handleInputChange}
                                           disabled={not_admin} />
                                </Field>
                                <Field name={taxRegNumLabels.tax_reg_num_label} error={$.inArray("tax_reg_num", this.state.errors) != -1} isRequired={true}>
                                    <input className="form-control" ref="tax_reg_num" id="tax_reg_num" type="text"
                                           value={this.state.DCOrganization.tax_reg_num} disabled={true} />
                                </Field>
                                <Field name="in_activity" error={$.inArray("in_activity", this.state.errors) != -1} isRequired={false}>
                                    <input id="in_activity" type="checkbox" checked={this.state.DCOrganization.in_activity}
                                           onChange={this.handleInputChange} />
                                </Field>
                                <Field name="alt_name" error={$.inArray("alt_name", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="alt_name" id="alt_name" type="text"
                                           value={this.state.DCOrganization.alt_name} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="org_type" error={$.inArray("org_type", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="org_type" id="org_type" type="text"
                                           value={this.state.DCOrganization.org_type} onChange={this.handleInputChange}
                                           placeholder={t('my.network.organization.org_type.placeholder')} />
                                </Field>
                                <Field name={taxRegNumLabels.tax_reg_activity_label} class_name_div='col-sm-3' isRequired={false}>
                                    <GeoSingleSelect2Component ref="tax_reg_activity" className="form-control" name="tax_reg_activity"
                                                               key="tax_reg_activity_uri"
                                                               urlResources={store_service + "/dc-taxRegActivity"}
                                                               countryFilter={ {country_uri: this.props.DCOrganization.country_uri} }
                                                               onChange={this.handleInputChange}
                                                               minimumInputLength={2}
                                                               placeholder={tax_reg_activity_uri_placeholder} />
                                </Field>
                                {this.renderTaxRegOfficialId(taxRegNumLabels.tax_reg_official_id_label)}
                                {this.renderJurisdiction()}
                                <Field name="phone_number" error={$.inArray("phone_number", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="phone_number" id="phone_number" type="text"
                                           value={this.state.DCOrganization.phone_number} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="web_site" error={$.inArray("web_site", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="web_site" id="web_site" type="text"
                                           value={this.state.DCOrganization.web_site} onChange={this.handleInputChange} />
                                </Field>
                                <Field name="email" error={$.inArray("email", this.state.errors) != -1} isRequired={false}>
                                    <input className="form-control" ref="email" id="email" type="text"
                                           value={this.state.DCOrganization.email} onChange={this.handleInputChange} />
                                </Field>
                            </fieldset>
                        </div>
                    </div>
                </div>
                {this.renderGeneralErrorMessage()}
            </div>
        );
    }
});
