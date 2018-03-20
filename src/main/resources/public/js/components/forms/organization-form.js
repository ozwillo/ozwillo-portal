import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import {connect} from 'react-redux';

//Component
import LegalNameAutosuggest from '../autosuggests/legal-name-autosuggest';
import TaxRegActivityAutosuggest from '../autosuggests/tax-reg-activity-autosuggest';
import GeoAreaAutosuggest from '../autosuggests/geoarea-autosuggest';

//Actions
import {updateOrganizationFormAction, resetOrganizationFormAction} from '../../actions/components/organization-form';

class OrganizationForm extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        countries: PropTypes.array,
        onSubmit: PropTypes.func.isRequired,
        isLoading: PropTypes.bool,
        label: PropTypes.string.isRequired,
        countryFieldIsDisabled: PropTypes.bool
    };

    static defaultProps = {
        countries: [],
        isLoading: false,
        organization: {},
        countryFieldIsDisabled: false
    };

    constructor(props) {
        super(props);

        this.state = {
            options: this.createOptions(this.props.countries)
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
        this.onOrganizationSelected = this.onOrganizationSelected.bind(this);
        this.handleCountryChange = this.handleCountryChange.bind(this);
        this.handleTaxRegActivityChange = this.handleTaxRegActivityChange.bind(this);
        this.handleJurisdictionChange = this.handleJurisdictionChange.bind(this);
        this.handleCityChange = this.handleCityChange.bind(this);
        this.handleOrganizationChange = this.handleOrganizationChange.bind(this);
        this.verifyTaxRegNum = this.verifyTaxRegNum.bind(this);
        this.verifyCountry = this.verifyCountry.bind(this);
        this.createOptions = this.createOptions.bind(this);
    }

    // TODO: change that !!!!
    get taxLabels() {
        if (!this.props.organization.country || !this.props.organization.country_uri) {
            return '';
        }

        let lang = '';
        switch (this.props.country) {
            case 'България' :
                lang = 'bg';
                break;
            case 'Italia'   :
                lang = 'it';
                break;
            case 'France'   :
                lang = 'fr';
                break;
            case 'España'   :
                lang = 'es';
                break;
            case 'Türkiye'  :
                lang = 'tr';
                break;
            default         :
                lang = 'en';
                break;
        }

        return {
            taxRegNum: this.context.t(`search.organization.business-id.${lang}`),
            taxRegOfficialId: this.context.t(`my.network.organization.tax_reg_official_id.${lang}`),
            taxRegActivity: this.context.t(`my.network.organization.tax_reg_activity.${lang}`)
        };
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            options: this.createOptions(nextProps.countries)
        });
    }

    createOptions(countries) {
        return countries && countries.map(country => {
            return {label: country.name, value: country.uri};
        }) || [];
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onSubmit(this.props.organization, e);
    }

    onOrganizationSelected(organization) {
        this.props.updateOrganizationForm(organization);
    }

    handleCountryChange(country) {
        this.props.resetOrganizationForm();
        this.props.updateOrganizationForm({
            country_uri: country.value,
            country: country.label
        });
    }

    handleTaxRegActivityChange(taxRegActivity) {
        this.props.updateOrganizationForm({
            tax_reg_activity_uri: taxRegActivity.uri,
            tax_reg_activity: taxRegActivity.name
        });
    }

    handleJurisdictionChange(jurisdiction) {
        this.props.updateOrganizationForm({
            jurisdiction_uri: jurisdiction.uri,
            jurisdiction: jurisdiction.name
        });
    }

    handleCityChange(city) {
        this.props.updateOrganizationForm({
            city_uri: city.uri,
            city: city.name,
            zip: city.postalCode
        });
    }

    handleOrganizationChange(e) {
        const el = e.currentTarget;
        const field = el.name;
        const value = (el.type === 'checkbox') ? el.checked : el.value;

        this.props.updateOrganizationForm({[field]: value});
    }

    // Verify fields
    verifyTaxRegNum(e) {
        const el = e.currentTarget;
        const msg = (/^[A-Za-z\d]*$/.test(this.props.organization.tax_reg_num)) ? '' :
            'The field must contain only letters or numbers without spaces';
        el.setCustomValidity(msg);
    }

    verifyCountry(e) {
        const el = e.currentTarget;
        const msg = (!this.props.organization.country_uri) ? 'You must select a country.' : '';
        el.setCustomValidity(msg);
    }


    render() {
        const organization = this.props.organization;
        const countryIsSelected = !!(organization.country && organization.country_uri);
        const taxLabels = this.taxLabels;
        const isPublic = (organization.sector_type === 'Public' || organization.sector_type === 'PUBLIC_BODY');
        const isPrivate = (organization.sector_type === 'Private' || organization.sector_type === 'COMPANY');

        return <form className="oz-form" onSubmit={this.onSubmit}>
            {/*     Organization     */}
            <fieldset className="oz-fieldset">
                <legend className="oz-legend">{this.context.t('organization.form.title')}</legend>
                <div className="flex-row">
                    <label htmlFor="country"
                           className="label">{this.context.t('my.network.organization.country')} *</label>
                    <Select className="select field" value={organization.country_uri}
                            onChange={this.handleCountryChange}
                            options={this.state.options} clearable={false}
                            placeholder="Country" required={true} disabled={this.props.countryFieldIsDisabled}/>
                </div>

                {
                    countryIsSelected &&
                    <div>
                        <div className="flex-row">
                            <label htmlFor="legal_name" className="label">
                                {this.context.t('my.network.organization.legal_name')} *
                            </label>
                            <LegalNameAutosuggest name="legal_name" required={true} value={organization.legal_name}
                                                  onChange={this.handleOrganizationChange}
                                                  countryUri={organization.country_uri}
                                                  onOrganizationSelected={this.onOrganizationSelected}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="tax_reg_num" className="label">{taxLabels.taxRegNum} *</label>
                            <input id="tax_reg_num" name="tax_reg_num" type="text" required={true}
                                   className="form-control field" onChange={this.handleOrganizationChange}
                                   onBlur={this.verifyTaxRegNum} value={organization.tax_reg_num}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="sector_type" className="label">
                                {this.context.t('search.organization.sector-type')} *
                            </label>

                            <div className="align-radio">
                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="PUBLIC_BODY" required={true}
                                           disabled={this.props.static} onChange={this.handleOrganizationChange}
                                           checked={isPublic}/>
                                    {this.context.t('search.organization.sector-type.PUBLIC_BODY')}
                                </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="COMPANY"
                                           disabled={this.props.static} onChange={this.handleOrganizationChange}
                                           checked={isPrivate}/>
                                    {this.context.t('search.organization.sector-type.COMPANY')}
                                </label>
                            </div>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="in_activity" className="label">
                                {this.context.t('my.network.organization.in_activity')}
                            </label>
                            <input id="in_activity" name="in_activity" type="checkbox"
                                   checked={organization.in_activity}
                                   onChange={this.handleOrganizationChange} className="field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="alt_name" className="label">
                                {this.context.t('my.network.organization.alt_name')}
                            </label>
                            <input name="alt_name" className="form-control field" id="alt_name" type="text"
                                   value={organization.alt_name} onChange={this.handleOrganizationChange}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="org_type" className="label">
                                {this.context.t('my.network.organization.org_type')}
                            </label>
                            <input name="org_type" className="form-control field" id="org_type" type="text"
                                   value={organization.org_type} onChange={this.handleOrganizationChange}
                                   placeholder={this.context.t('my.network.organization.org_type.placeholder')}/>
                        </div>

                        <div className="flex-row">
                            <label className="label">{taxLabels.taxRegActivity}</label>
                            <TaxRegActivityAutosuggest name="tax_reg_activity"
                                                       onChange={this.handleOrganizationChange}
                                                       onTaxRegActivitySelected={this.handleTaxRegActivityChange}
                                                       countryUri={organization.country_uri}
                                                       value={organization.tax_reg_activity}/>
                        </div>

                        {
                            isPublic &&
                            <div className="flex-row">
                                <label htmlFor="tax_reg_official_id" className="label">
                                    {taxLabels.taxRegOfficialId}
                                </label>
                                <input name="tax_reg_official_id" className="form-control field"
                                       id="tax_reg_official_id" type="text"
                                       value={organization.tax_reg_official_id}
                                       onChange={this.handleOrganizationChange}/>
                            </div>
                        }

                        <div className="flex-row">
                            <label name="jurisdiction" className="label">
                                {this.context.t('my.network.organization.jurisdiction')} {(isPublic && '*') || ''}
                            </label>
                            <GeoAreaAutosuggest name="jurisdiction" required={isPublic}
                                                value={organization.jurisdiction}
                                                onChange={this.handleOrganizationChange}
                                                onGeoAreaSelected={this.handleJurisdictionChange}
                                                endpoint="/geographicalAreas"
                                                countryUri={organization.country_uri}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="phone_number" className="label">
                                {this.context.t('my.network.organization.phone_number')}
                            </label>
                            <input id="phone_number" name="phone_number" type="number" value={organization.phone_number}
                                   onChange={this.handleOrganizationChange} className="form-control field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="web_site" className="label">
                                {this.context.t('my.network.organization.web_site')}
                            </label>
                            <input id="web_site" name="web_site" type="text" value={organization.web_site}
                                   onChange={this.handleOrganizationChange} className="form-control field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="email" className="label">
                                {this.context.t('my.network.organization.email')}
                            </label>
                            <input id="email" name="email" type="email" value={organization.email}
                                   onChange={this.handleOrganizationChange} className="form-control field"/>
                        </div>
                    </div>

                }
            </fieldset>

            {/*     Contact information     */}
            {
                countryIsSelected &&
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">{this.context.t('organization.form.contact-information')}</legend>
                    <div className="flex-row">
                        <label htmlFor="street_and_number" className="label">
                            {this.context.t('my.network.organization.street_and_number')} *
                        </label>
                        <input id="street_and_number" name="street_and_number" type="text" required={true}
                               value={organization.street_and_number} className="form-control field"
                               onChange={this.handleOrganizationChange}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="po_box"
                               className="label">{this.context.t('my.network.organization.po_box')}</label>
                        <input id="po_box" name="po_box" type="text"
                               value={organization.po_box} className="form-control field"
                               onChange={this.handleOrganizationChange}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="city"
                               className="label">{this.context.t('my.network.organization.city')} *</label>
                        <GeoAreaAutosuggest name="city" required={true} countryUri={organization.country_uri}
                                            endpoint="/dc-cities" onChange={this.handleOrganizationChange}
                                            onGeoAreaSelected={this.handleCityChange} value={organization.city}
                                            onBlur={this.verifyCountry}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="zip" className="label">{this.context.t('my.network.organization.zip')} *</label>
                        <input id="zip" name="zip" type="text" maxLength={6} required={true}
                               className="form-control field"
                               value={organization.zip} onChange={this.handleOrganizationChange} readOnly={true}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="cedex"
                               className="label">{this.context.t('my.network.organization.cedex')}</label>
                        <input id="cedex" name="cedex" type="text" maxLength={3} value={organization.cedex}
                               className="form-control field" onChange={this.handleOrganizationChange}/>
                    </div>

                </fieldset>
            }

            {/*     Submit button     */}
            <div className="flex-row">
                {
                    countryIsSelected &&
                    (
                        (!this.props.isLoading &&
                            <input type="submit" value={this.props.label} className="submit btn-submit"/>) ||
                        <button type="button" className="submit btn icon">
                            <i className="fa fa-spinner fa-spin loading"/>
                        </button>
                    )
                }
            </div>
        </form>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organizationForm
    }
};

const mapDispatchToProps = dispatch => {
    return {
        updateOrganizationForm(organization) {
            return dispatch(updateOrganizationFormAction(organization));
        },
        resetOrganizationForm() {
            return dispatch(resetOrganizationFormAction());
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationForm);