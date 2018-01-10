import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

//Component
import LegalNameAutosuggest from '../autosuggests/legal-name-autosuggest';
import TaxRegActivityAutosuggest from '../autosuggests/tax-reg-activity-autosuggest';
import GeoAreaAutosuggest from '../autosuggests/geoarea-autosuggest';

class OrganizationForm extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        countries: PropTypes.array,
        organization: PropTypes.object,
        onSubmit: PropTypes.func.isRequired,
        isLoading: PropTypes.bool
    };

    static defaultProps = {
        countries: [],
        isLoading: false,
        organization: {
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
            phone_number: '',
            po_box: '',
            sector_type: '',
            street_and_number: '',
            tax_reg_activity: '',
            tax_reg_activity_uri: '',
            tax_reg_num: '',
            web_site: '',
            zip: '',
        }
    }

    constructor(props){
        super(props);

        this.state = {
            countrySelected: null,
            organization: this.props.organization,
            contact_lastname: '',
            contact_name: ''
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
        this.onOrganizationSelected = this.onOrganizationSelected.bind(this);
        this.handleCountriesChange = this.handleCountriesChange.bind(this);
        this.handleTaxRegActivityChange = this.handleTaxRegActivityChange.bind(this);
        this.handleJurisdictionChange = this.handleJurisdictionChange.bind(this);
        this.handleCityChange = this.handleCityChange.bind(this);
        this.handleOrganizationChange = this.handleOrganizationChange.bind(this);
    }

    // TODO: change that !!!!
    get taxLabels() {
        if(!this.state.countrySelected){
            return '';
        }

        let lang = '';
        switch(this.state.countrySelected.name){
            case 'България' : lang = 'bg'; break;
            case 'Italia'   : lang = 'it'; break;
            case 'France'   : lang = 'fr'; break;
            case 'España'   : lang = 'es'; break;
            case 'Türkiye'  : lang = 'tr'; break;
            default         : lang = 'en'; break;
        }

        return  {
            taxRegNum: this.context.t(`search.organization.business-id.${lang}`),
            taxRegOfficialId: this.context.t(`my.network.organization.tax_reg_official_id.${lang}`),
            taxRegActivity: this.context.t(`my.network.organization.tax_reg_activity.${lang}`)
        };
    }

    componentWillReceiveProps(nextProps) {
        this.setState({ organization: nextProps.organization});
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onSubmit(this.state.organization);
    }

    onOrganizationSelected(organization) {
        this.setState({ organization });
    }

    handleCountriesChange(country) {
        this.setState({
            countrySelected: country,
            organization: Object.assign({}, this.state.organization,
                {
                    country_uri: country.uri,
                    country: country.name
                })
        });
    }

    handleTaxRegActivityChange(taxRegActivity) {
        this.setState({
            organization: Object.assign({}, this.state.organization,
                {
                    tax_reg_activity_uri: taxRegActivity.uri,
                    tax_reg_activity: taxRegActivity.name
                })
        });
    }

    handleJurisdictionChange(jurisdiction) {
        this.setState({
            organization: Object.assign({}, this.state.organization,
                {
                    jurisdiction_uri: jurisdiction.uri,
                    jurisdiction: jurisdiction.name
                })
        });
    }

    handleCityChange(city) {
        this.setState({
            organization: Object.assign({}, this.state.organization,
                {
                    city_uri: city.uri,
                    city: city.name,
                    zip: city.postalCode
                })
        });
    }


    handleOrganizationChange (e) {
        const el = e.currentTarget;
        const field = el.name;
        const value = ( el.type === 'checkbox') ?  el.checked : el.value;

        this.setState({
            organization: Object.assign({}, this.state.organization, { [field]: value })
        });
    }

    render() {
        const countrySelected = this.state.countrySelected;
        const organization = this.state.organization;
        const taxLabels = this.taxLabels;

        return <form className="oz-form" onSubmit={this.onSubmit}>
            {/*     Organization     */}
            <fieldset className="oz-fieldset">
                <legend className="oz-legend">Organization</legend>
                <div className="flex-row">
                    <label htmlFor="country" className="label">{this.context.t('my.network.organization.country')} *</label>
                    <Select className="select field" value={countrySelected} onChange={this.handleCountriesChange}
                            options={this.props.countries} clearable={false} valueKey="uri" labelKey="name"
                            placeholder="Country" required={true} />
                </div>

                {
                    countrySelected &&
                    <div>
                        <div className="flex-row">
                            <label htmlFor="legal_name" className="label">
                                {this.context.t('my.network.organization.legal_name')} *
                            </label>
                            <LegalNameAutosuggest name="legal_name" required={true}
                                                  onChange={this.handleOrganizationChange}
                                                  countryUri={countrySelected.uri}
                                                  onOrganizationSelected={this.onOrganizationSelected}
                                                  value={organization.legal_name}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="tax_reg_num" className="label">{taxLabels.taxRegNum} toto *</label>
                            <input id="tax_reg_num" name="tax_reg_num" type="number" required={true}
                                   className="form-control field" onChange={this.handleOrganizationChange}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="sector_type" className="label">
                                {this.context.t('search.organization.sector-type')} *
                            </label>

                            <label className="radio-inline field">
                                <input type="radio" name="sector_type" value="PUBLIC_BODY" required={true}
                                       disabled={this.props.static} onChange={this.handleOrganizationChange}
                                       checked={organization.sector_type === 'Public' ||
                                       organization.sector_type === 'PUBLIC_BODY'}/>
                                {this.context.t('search.organization.sector-type.PUBLIC_BODY')}
                            </label>

                            <label className="radio-inline field">
                                <input type="radio" name="sector_type" value="COMPANY"
                                       disabled={this.props.static} onChange={this.handleOrganizationChange}
                                       checked={organization.sector_type === 'Private' ||
                                       organization.sector_type === 'COMPANY'}/>
                                {this.context.t('search.organization.sector-type.COMPANY')}
                            </label>
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
                                   value={organization.alt_name} onChange={this.handleInputChange}/>
                        </div>

                        <div className="flex-row">
                            <label htmlFor="org_type" className="label">
                                {this.context.t('my.network.organization.org_type')}
                            </label>
                            <input name="org_type" className="form-control field" id="org_type" type="text"
                                   value={organization.alt_name} onChange={this.handleInputChange}
                                   placeholder={this.context.t('my.network.organization.org_type.placeholder')}/>
                        </div>

                        <div className="flex-row">
                            <label className="label">{taxLabels.taxRegActivity}</label>
                            <TaxRegActivityAutosuggest name="tax_reg_activity"
                                                       onChange={this.handleOrganizationChange}
                                                       onTaxRegActivitySelected={this.handleTaxRegActivityChange}
                                                       countryUri={countrySelected.uri}
                                                       value={organization.tax_reg_activity}/>
                        </div>

                        {
                            organization.sector_type === 'PUBLIC_BODY' &&
                            <div className="flex-row">
                                <label htmlFor="tax_reg_official_id" className="label">
                                    {taxLabels.taxRegOfficialId}
                                </label>
                                <input name="tax_reg_official_id" className="form-control field"
                                       id="tax_reg_official_id" type="text"
                                       value={organization.tax_reg_official_id} onChange={this.handleInputChange}/>
                            </div>
                        }

                        <div className="flex-row">
                            <label name="jurisdiction" className="label">
                                {this.context.t('my.network.organization.jurisdiction')} *
                            </label>
                            <GeoAreaAutosuggest name="jurisdiction" required={true}
                                                value={organization.jurisdiction}
                                                onChange={this.handleOrganizationChange}
                                                onGeoAreaSelected={this.handleJurisdictionChange}
                                                endpoint="/geographicalAreas"
                                                countryUri={countrySelected.uri}/>
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
                countrySelected &&
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">Contact Information</legend>
                    <div className="flex-row">
                        <label htmlFor="street_and_number" className="label">
                            {this.context.t('my.network.organization.street_and_number')} *
                        </label>
                        <input id="street_and_number" name="street_and_number" type="text" required={true}
                               value={organization.street_and_number} className="form-control field"
                               onChange={this.handleOrganizationChange} />
                    </div>

                    <div className="flex-row">
                        <label htmlFor="po_box" className="label">{this.context.t('my.network.organization.po_box')}</label>
                        <input id="po_box" name="po_box" type="text"
                               value={organization.po_box} className="form-control field"
                               onChange={this.handleOrganizationChange} />
                    </div>

                    <div className="flex-row">
                        <label htmlFor="city" className="label">{this.context.t('my.network.organization.city')} *</label>
                        <GeoAreaAutosuggest name="city" required={true} countryUri={countrySelected.uri}
                                            endpoint="/dc-cities" onChange={this.handleOrganizationChange}
                                            onGeoAreaSelected={this.handleCityChange} value={organization.city} />
                    </div>

                    <div className="flex-row">
                        <label htmlFor="zip" className="label">{this.context.t('my.network.organization.zip')} *</label>
                        <input id="zip" name="zip" type="text" maxLength={6} required={true} className="form-control field"
                               value={organization.zip} onChange={this.handleOrganizationChange} />
                    </div>

                    <div className="flex-row">
                        <label htmlFor="cedex" className="label">{this.context.t('my.network.organization.cedex')}</label>
                        <input id="cedex" name="cedex" type="text" maxLength={3} value={organization.cedex}
                               className="form-control field" onChange={this.handleOrganizationChange} />
                    </div>

                </fieldset>
            }

            {/*     Submit button     */}
            {
                countrySelected &&
                (
                    ( !this.props.isLoading && <input type="submit" value="Send" className="submit btn"/> ) ||
                    <button type="button" className="submit btn icon">
                        <i className="fa fa-spinner fa-spin" />
                    </button>
                )
            }
        </form>;
    }
}

export default OrganizationForm;