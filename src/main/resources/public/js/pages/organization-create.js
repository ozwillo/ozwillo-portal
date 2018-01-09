import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Select from 'react-select';

//Component
import LegalNameAutosuggest from '../components/autosuggests/legal-name-autosuggest';
import TaxRegActivityAutosuggest from '../components/autosuggests/tax-reg-activity-autosuggest';
import GeoAreaAutosuggest from '../components/autosuggests/geoarea-autosuggest';

//Action
import { fetchCountries } from '../actions/config';

class OrganizationCreate extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            countrySelected: null,
            organization: {}
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
        this.handleCountriesChange = this.handleCountriesChange.bind(this);
        this.handleTaxRegActivityChange = this.handleTaxRegActivityChange.bind(this);
        this.handleJurisdictionChange = this.handleJurisdictionChange.bind(this);
        this.onOrganizationSelected = this.onOrganizationSelected.bind(this);
        this.handleOrganizationChange = this.handleOrganizationChange.bind(this);
        this.handleCityChange = this.handleCityChange.bind(this);
    }

    componentDidMount() {
        this.props.fetchCountries();
    }

    onSubmit(e) {
        e.preventDefault();

        console.log('onSubmit');
    }

    handleCountriesChange(country) {
        console.log('country ', country)
        this.setState({ countrySelected: country})
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

    onOrganizationSelected(organization) {
        this.setState({ organization });
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
        const sectorType = this.state.sectorType;
        const organization = this.state.organization;
        const taxLabels = this.taxLabels;

        return <section className="organization-create oz-body wrapper flex-col">
            <header className="header flex-row">
                <h1 className="title">Create an organization</h1>
            </header>
            <section>
                <form className="oz-form">
                    <fieldset className="oz-fieldset">
                        <legend className="oz-legend">Organization</legend>
                        <div className="flex-row">
                            <label htmlFor="country" className="label">Country</label>
                            <Select className="select field" value={countrySelected} onChange={this.handleCountriesChange}
                                    options={this.props.countries} clearable={false} valueKey="uri" labelKey="name"
                                    placeholder="Country" />
                        </div>

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="legal_name" className="label">Legal name</label>
                                <LegalNameAutosuggest name="legal_name" className="field"
                                                      onChange={this.handleOrganizationChange}
                                                      countryUri={countrySelected.uri}
                                                      onOrganizationSelected={this.onOrganizationSelected}
                                                      value={organization.legal_name} />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="tax_reg_num" className="label">{taxLabels.taxRegNum}</label>
                                <input id="tax_reg_num" name="tax_reg_num" type="number" className="form-control field" />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="sector_type" className="label">{this.context.t('search.organization.sector-type')} * </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="PUBLIC_BODY"
                                           disabled={this.props.static} onChange={this.handleOrganizationChange}
                                           checked={ organization.sector_type === 'Public' ||
                                               organization.sector_type === 'PUBLIC_BODY' } />
                                    {this.context.t('search.organization.sector-type.PUBLIC_BODY')}
                                </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="COMPANY"
                                           disabled={this.props.static} onChange={this.handleOrganizationChange}
                                           checked={ organization.sector_type === 'Private' ||
                                                    organization.sector_type === 'COMPANY' } />
                                    {this.context.t('search.organization.sector-type.COMPANY')}
                                </label>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="in_activity" className="label">{this.context.t('my.network.organization.in_activity')}</label>

                                <input id="in_activity" name="in_activity" type="checkbox" checked={organization.in_activity}
                                       onChange={this.handleOrganizationChange} className="field"/>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="alt_name" className="label">{this.context.t('my.network.organization.alt_name')}</label>

                                <input name="alt_name" className="form-control field" id="alt_name" type="text"
                                       value={organization.alt_name} onChange={this.handleInputChange} />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="org_type" className="label">{this.context.t('my.network.organization.org_type')}</label>

                                <input name="org_type" className="form-control field" id="org_type" type="text"
                                       value={organization.alt_name} onChange={this.handleInputChange}
                                       placeholder={this.context.t('my.network.organization.org_type.placeholder')}/>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label className="label">{taxLabels.taxRegActivity}</label>

                                <TaxRegActivityAutosuggest name="tax_reg_activity" className="field"
                                                           onChange={this.handleOrganizationChange}
                                                           onTaxRegActivitySelected={this.handleTaxRegActivityChange}
                                                           countryUri={countrySelected.uri}
                                                           value={organization.tax_reg_activity} />
                            </div>
                        }

                        {
                            countrySelected && organization.sector_type === 'PUBLIC_BODY' &&
                            <div className="flex-row">
                                <label htmlFor="tax_reg_official_id" className="label">{taxLabels.taxRegOfficialId}</label>

                                <input name="tax_reg_official_id" className="form-control field" id="tax_reg_official_id" type="text"
                                       value={organization.tax_reg_official_id} onChange={this.handleInputChange} />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label name="jurisdiction" className="label">{this.context.t('my.network.organization.jurisdiction')}</label>

                                <GeoAreaAutosuggest name="jurisdiction" value={organization.jurisdiction} className="field"
                                                    onChange={this.handleOrganizationChange}
                                                    onGeoAreaSelected={this.handleJurisdictionChange}
                                                    endpoint="/geographicalAreas"
                                                    countryUri={countrySelected.uri}/>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="phone_number" className="label">{this.context.t('my.network.organization.phone_number')}</label>

                                <input id="phone_number" name="phone_number" type="number" value={organization.phone_number}
                                       onChange={this.handleOrganizationChange} className="form-control field"/>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="web_site" className="label">{this.context.t('my.network.organization.web_site')}</label>

                                <input id="web_site" name="web_site" type="text" value={organization.web_site}
                                       onChange={this.handleOrganizationChange} className="form-control field"/>
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="email" className="label">{this.context.t('my.network.organization.email')}</label>

                                <input id="email" name="email" type="email" value={organization.email}
                                       onChange={this.handleOrganizationChange} className="form-control field"/>
                            </div>
                        }
                    </fieldset>

                    {
                        countrySelected &&
                        <fieldset className="oz-fieldset">
                            <legend className="oz-legend">Contact Information</legend>
                            <div className="flex-row">
                                <label htmlFor="street_and_number" className="label">
                                    {this.context.t('my.network.organization.street_and_number')}
                                </label>

                                <input id="street_and_number" name="street_and_number" type="text"
                                       value={organization.street_and_number} className="form-control field"
                                       onChange={this.handleOrganizationChange} />
                            </div>

                            <div className="flex-row">
                                <label htmlFor="po_box" className="label">
                                    {this.context.t('my.network.organization.po_box')}
                                </label>

                                <input id="po_box" name="po_box" type="text"
                                       value={organization.po_box} className="form-control field"
                                       onChange={this.handleOrganizationChange} />
                            </div>

                            <div className="flex-row">
                                <label htmlFor="city" className="label">
                                    {this.context.t('my.network.organization.city')}
                                </label>

                                <GeoAreaAutosuggest name="city" className="field"
                                                    countryUri={countrySelected.uri}
                                                    endpoint="/dc-cities"
                                                    onChange={this.handleOrganizationChange}
                                                    onGeoAreaSelected={this.handleCityChange}
                                                    value={organization.city} />
                            </div>

                            <div className="flex-row">
                                <label htmlFor="zip" className="label">
                                    {this.context.t('my.network.organization.zip')}
                                </label>

                                <input id="zip" name="zip" type="text" maxLength={6}
                                       value={organization.zip} className="form-control field"
                                       onChange={this.handleOrganizationChange} />
                            </div>

                            <div className="flex-row">
                                <label htmlFor="cedex" className="label">
                                    {this.context.t('my.network.organization.cedex')}
                                </label>

                                <input id="cedex" name="cedex" type="text" maxLength={3}
                                       value={organization.cedex} className="form-control field"
                                       onChange={this.handleOrganizationChange} />
                            </div>

                        </fieldset>
                    }

                    <fieldset>
                        <legend>Update your profile</legend>


                    </fieldset>

                    <input type="submit" value="Send" />
                </form>

            </section>
        </section>;
    }

}

const mapStateToProps = state => {
    return {
        countries: state.config.countries
    }
};

const mapDispatchToProps = dispatch => {
    return {
        fetchCountries() {
            return dispatch(fetchCountries());
        }
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(OrganizationCreate);