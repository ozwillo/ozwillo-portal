import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

import LegalNameAutosuggest from '../autosuggests/legal-name-autosuggest';
import TaxRegActivityAutosuggest from '../autosuggests/tax-reg-activity-autosuggest';
import GeoAreaAutosuggest from '../autosuggests/geoarea-autosuggest';


import { i18n } from "../../config/i18n-config"
import { t } from "@lingui/macro"

class OrganizationForm extends React.Component {

    static propTypes = {
        countries: PropTypes.array,
        onSubmit: PropTypes.func.isRequired,
        isLoading: PropTypes.bool,
        label: PropTypes.string.isRequired,
        alreadyRegistered: PropTypes.bool,
        initialTaxRegNum: PropTypes.string
    };

    static defaultProps = {
        countries: [],
        isLoading: false,
        organization: {},
        alreadyRegistered: false
    };

    constructor(props) {
        super(props);

        this.state = {
            isRegistrable: props.alreadyRegistered,
            organization: props.organization || {
                cedex: '',
                city: '',
                city_uri: '',
                country: '',
                country_uri: '',
                email: '',
                in_activity: false,
                jurisdiction: '',
                jurisdiction_uri: '',
                legal_name: '',
                org_type: '',
                phone_number: '',
                po_box: '',
                sector_type: '',
                street_and_number: '',
                tax_reg_official_id: '',
                tax_reg_activity: '',
                tax_reg_activity_uri: '',
                tax_reg_num: '',
                web_site: '',
                zip: '',
                alt_name: '',
                version: '0'
            }
        };

        this.onSubmit = this.onSubmit.bind(this);
        this.onOrganizationSelected = this.onOrganizationSelected.bind(this);
        this.handleCountryChange = this.handleCountryChange.bind(this);
        this.handleTaxRegActivityChange = this.handleTaxRegActivityChange.bind(this);
        this.checkKernelAvailability = this.checkKernelAvailability.bind(this);
        this.handleJurisdictionChange = this.handleJurisdictionChange.bind(this);
        this.handleCityChange = this.handleCityChange.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.verifyTaxRegNum = this.verifyTaxRegNum.bind(this);
        OrganizationForm.createOptions = OrganizationForm.createOptions.bind(this);
    }

    // TODO: change that !!!!
    get taxLabels() {
        if (!this.state.organization.country || !this.state.organization.country_uri) {
            return '';
        }

        let lang = '';
        switch (this.state.organization.country) {
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
            taxRegNum: i18n._(`search.organization.business-id.${lang}`),
            taxRegOfficialId: i18n._(`my.network.organization.tax_reg_official_id.${lang}`),
            taxRegActivity: i18n._(`my.network.organization.tax_reg_activity.${lang}`)
        };
    }

    static createOptions(countries) {
        return countries && countries.map(country => {
            return {label: country.name, value: country.uri};
        }) || [];
    }

    checkKernelAvailability(country_uri, tax_reg_num) {
        $.ajax({
            url: `/my/api/organization/kernel?countryUri=${country_uri}&taxRegNumber=${tax_reg_num}`,
            dataType: 'json',
            type: 'GET'
        }).done(data => {
            const isRegistrable = (data === false);
            this.setState({ isRegistrable: isRegistrable });
        }).fail((jqXHR, errorThrown) => {
            if (jqXHR.status === 404) {
                this.setState({ isRegistrable: true });
            } else {
                console.error("Error while searching for existence of " + tax_reg_num, jqXHR.status, errorThrown.toString())
            }
        });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onSubmit(this.state.organization, e);
    }

    onOrganizationSelected(organization) {
        this.setState({ organization: organization });
    }

    handleCountryChange(country) {
        this.setState({ organization: Object.assign({}, { country_uri: country.value, country: country.label } )});
    }

    handleTaxRegActivityChange(taxRegActivity) {
        this.setState({ organization: Object.assign(this.state.organization,
                { tax_reg_activity_uri: taxRegActivity.uri, tax_reg_activity: taxRegActivity.name } )});
    }

    handleJurisdictionChange(e,jurisdiction) {
        this.setState({ organization: Object.assign(this.state.organization,
                { jurisdiction_uri: jurisdiction.uri, jurisdiction: jurisdiction.name } )});
    }

    handleCityChange(e,city) {
        this.setState({ organization: Object.assign(this.state.organization,
                { city_uri: city.uri, city: city.name, zip: city.postalCode } )});
    }

    handleFieldChange(e) {
        const el = e.currentTarget;
        const field = el.name;
        const value = (el.type === 'checkbox') ? el.checked : el.value;

        this.setState({ organization: Object.assign(this.state.organization,
                { [field]: value } )});
    }

    // Verify fields
    verifyTaxRegNum(e) {
        const el = e.currentTarget;
        const msg = (/^[A-Za-z\d]*$/.test(this.state.organization.tax_reg_num)) ? '' :
            'The field must contain only letters or numbers without spaces';
        el.setCustomValidity(msg);

        if ((!this.props.alreadyRegistered && this.state.organization.tax_reg_num !== '')
            || (this.props.alreadyRegistered && this.props.initialTaxRegNum !== this.state.organization.tax_reg_num)) {
            this.checkKernelAvailability(this.state.organization.country_uri, this.state.organization.tax_reg_num);
        }
    }

    render() {
        const organization = this.state.organization;
        const countryIsSelected = !!(organization.country && organization.country_uri);
        const taxLabels = this.taxLabels;
        const isPublic = (organization.sector_type === 'Public' || organization.sector_type === 'PUBLIC_BODY');
        const isPrivate = (organization.sector_type === 'Private' || organization.sector_type === 'COMPANY');

        return <form className="oz-form" onSubmit={this.onSubmit}>
            {/*     Organization     */}
            <fieldset className="oz-fieldset">
                <legend className="oz-legend">{i18n._(t`organization.form.title`)}</legend>
                <div className="flex-row">
                    <label htmlFor="country"
                           className="label">{i18n._(t`my.network.organization.country`)} *</label>
                    <Select className="select field" value={organization.country_uri}
                            onChange={this.handleCountryChange}
                            options={OrganizationForm.createOptions(this.props.countries)} clearable={false}
                            required={true} disabled={this.state.organization.id !== undefined}/>
                </div>

                {
                    countryIsSelected &&
                    <div>
                        <div className="flex-row">
                            <label htmlFor="legal_name" className="label">
                                {i18n._(t`my.network.organization.legal_name`)} *
                            </label>
                            <LegalNameAutosuggest name="legal_name" required={true} value={organization.legal_name}
                                                  onChange={this.handleFieldChange}
                                                  countryUri={organization.country_uri}
                                                  onOrganizationSelected={this.onOrganizationSelected}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="tax_reg_num" className="label">{taxLabels.taxRegNum} *</label>
                            <input id="tax_reg_num" name="tax_reg_num" type="text" required={true}
                                   className="form-control field" onChange={this.handleFieldChange}
                                   onBlur={this.verifyTaxRegNum} value={organization.tax_reg_num}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="sector_type" className="label">
                                {i18n._(t`search.organization.sector-type`)} *
                            </label>

                            <div className="align-radio">
                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="PUBLIC_BODY" required={true}
                                           disabled={this.props.static} onChange={this.handleFieldChange}
                                           checked={isPublic}/>
                                    {i18n._(t`search.organization.sector-type.PUBLIC_BODY`)}
                                </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="COMPANY"
                                           disabled={this.props.static} onChange={this.handleFieldChange}
                                           checked={isPrivate}/>
                                    {i18n._(t`search.organization.sector-type.COMPANY`)}
                                </label>
                            </div>
                        </div>
                    </div>
                }

                {
                    this.state.isRegistrable &&

                    <div>
                        <div className="flex-row">
                            <label htmlFor="in_activity" className="label">
                                {i18n._(t`my.network.organization.in_activity`)}
                            </label>
                            <input id="in_activity" name="in_activity" type="checkbox"
                                   checked={organization.in_activity}
                                   onChange={this.handleFieldChange} className="field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="alt_name" className="label">
                                {i18n._(t`my.network.organization.alt_name`)}
                            </label>
                            <input name="alt_name" className="form-control field" id="alt_name" type="text"
                                   value={organization.alt_name} onChange={this.handleFieldChange}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="org_type" className="label">
                                {i18n._(t`my.network.organization.org_type`)}
                            </label>
                            <input name="org_type" className="form-control field" id="org_type" type="text"
                                   value={organization.org_type} onChange={this.handleFieldChange}
                                   placeholder={i18n._(t`my.network.organization.org_type.placeholder`)}/>
                        </div>

                        <div className="flex-row">
                            <label className="label">{taxLabels.taxRegActivity}</label>
                            <TaxRegActivityAutosuggest name="tax_reg_activity"
                                                       onChange={this.handleFieldChange}
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
                                       onChange={this.handleFieldChange}/>
                            </div>
                        }

                        <div className="flex-row">
                            <label htmlFor="jurisdiction" className="label">
                                {i18n._(t`my.network.organization.jurisdiction`)} {(isPublic && '*') || ''}
                            </label>
                            <GeoAreaAutosuggest name="jurisdiction" required={isPublic}
                                                value={organization.jurisdiction}
                                                onChange={this.handleFieldChange}
                                                onGeoAreaSelected={this.handleJurisdictionChange}
                                                endpoint="areas"
                                                countryUri={organization.country_uri}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="phone_number" className="label">
                                {i18n._(t`my.network.organization.phone_number`)}
                            </label>
                            <input id="phone_number" name="phone_number" type="number" value={organization.phone_number}
                                   onChange={this.handleFieldChange} className="form-control field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="web_site" className="label">
                                {i18n._(t`my.network.organization.web_site`)}
                            </label>
                            <input id="web_site" name="web_site" type="text" value={organization.web_site}
                                   onChange={this.handleFieldChange} className="form-control field"/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="email" className="label">
                                {i18n._(t`my.network.organization.email`)}
                            </label>
                            <input id="email" name="email" type="email" value={organization.email}
                                   onChange={this.handleFieldChange} className="form-control field"/>
                        </div>
                    </div>

                }
            </fieldset>

            {/*     Contact information     */}
            {
                this.state.isRegistrable &&
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">{i18n._(t`organization.form.contact-information`)}</legend>
                    <div className="flex-row">
                        <label htmlFor="street_and_number" className="label">
                            {i18n._(t`my.network.organization.street_and_number`)} *
                        </label>
                        <input id="street_and_number" name="street_and_number" type="text" required={true}
                               value={organization.street_and_number} className="form-control field"
                               onChange={this.handleFieldChange}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="po_box"
                               className="label">{i18n._(t`my.network.organization.po_box`)}</label>
                        <input id="po_box" name="po_box" type="text"
                               value={organization.po_box} className="form-control field"
                               onChange={this.handleFieldChange}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="city"
                               className="label">{i18n._(t`my.network.organization.city`)} *</label>
                        <GeoAreaAutosuggest name="city" required={true}
                                            countryUri={organization.country_uri}
                                            endpoint="cities"
                                            onChange={this.handleFieldChange}
                                            onGeoAreaSelected={this.handleCityChange}
                                            value={organization.city} />
                    </div>

                    <div className="flex-row">
                        <label htmlFor="zip" className="label">{i18n._(t`my.network.organization.zip`)} *</label>
                        <input id="zip" name="zip" type="text" maxLength={6} required={true}
                               className="form-control field"
                               value={organization.zip} onChange={this.handleFieldChange} readOnly={true}/>
                    </div>

                    <div className="flex-row">
                        <label htmlFor="cedex"
                               className="label">{i18n._(t`my.network.organization.cedex`)}</label>
                        <input id="cedex" name="cedex" type="text" maxLength={3} value={organization.cedex}
                               className="form-control field" onChange={this.handleFieldChange}/>
                    </div>

                </fieldset>
            }

            {/*     Submit button     */}
            <div className="flex-row">
                {
                    this.state.isRegistrable &&
                    (
                        (!this.props.isLoading &&
                            <input type="submit" value={this.props.label} className="submit btn btn-submit"/>) ||
                        <button type="button" className="submit btn icon">
                            <i className="fa fa-spinner fa-spin loading"/>
                        </button>
                    )
                }

                {
                    !this.state.isRegistrable && organization.tax_reg_num &&
                        <div className="alert alert-danger center-block">
                            {i18n._(t`my.network.organization.not-registrable`)}
                        </div>
                }
            </div>
        </form>;
    }
}

export default OrganizationForm;
