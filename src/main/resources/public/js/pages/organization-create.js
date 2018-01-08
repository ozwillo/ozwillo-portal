import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Select from 'react-select';

//Component
import LegalNameAutosuggest from '../components/legal-name-autosuggest';

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
            legalName: '',
            sectorType: ''
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
        this.handleCountriesChange = this.handleCountriesChange.bind(this);
        this.handleLegalNameChange = this.handleLegalNameChange.bind(this);
        this.handleSectorTypeChange = this.handleSectorTypeChange.bind(this);
        this.onOrganizationSelected = this.onOrganizationSelected.bind(this);
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

    handleLegalNameChange(legalName) {
        this.setState({ legalName })
    }

    handleSectorTypeChange(e) {
        this.setState({
            sectorType: e.currentTarget.value
        });
    }

    onOrganizationSelected(organization) {
        this.setState({ organization });
    }

    // TODO: change that !!!!
    get taxRegNumLabel() {
        if(!this.state.countrySelected){
            return '';
        }

        let taxRegNumLabel = '';
        switch(this.state.countrySelected.name){
            case 'България' : taxRegNumLabel = this.context.t('search.organization.business-id.bg'); break;
            case 'Italia'   : taxRegNumLabel = this.context.t('search.organization.business-id.it'); break;
            case 'France'   : taxRegNumLabel = this.context.t('search.organization.business-id.fr'); break;
            case 'España'   : taxRegNumLabel = this.context.t('search.organization.business-id.es'); break;
            case 'Türkiye'  : taxRegNumLabel = this.context.t('search.organization.business-id.tr'); break;
            default         : taxRegNumLabel = this.context.t('search.organization.business-id.en'); break;
        }

        return taxRegNumLabel;
    }

    render() {
        const countrySelected = this.state.countrySelected;
        const legalName = this.state.legalName;
        const sectorType = this.state.sectorType;

        return <section className="organization-create oz-body wrapper flex-col">
            <header className="header flex-row">
                <h1 className="title">Create an organization</h1>
            </header>
            <section>

                <form className="oz-form">
                    <fieldset className="oz-fieldset">
                        <legend className="oz-legend">Search an organization</legend>
                        <div className="flex-row">
                            <label htmlFor="country" className="label">Country</label>
                            <Select className="select field" value={countrySelected} onChange={this.handleCountriesChange}
                                    options={this.props.countries} clearable={false} valueKey="uri" labelKey="name"
                                    placeholder="Country" />
                        </div>

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="legal-name" className="label">Legal name</label>
                                <LegalNameAutosuggest onChange={this.handleLegalNameChange} className="field"
                                                      countryUri={countrySelected.uri}
                                                      onOrganizationSelected={this.onOrganizationSelected}
                                                      value={legalName} />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="tax_reg_num" className="label">{this.taxRegNumLabel}</label>
                                <input id="tax_reg_num" name="tax_reg_num" type="number" className="form-control field" />
                            </div>
                        }

                        {
                            countrySelected &&
                            <div className="flex-row">
                                <label htmlFor="sector_type" className="label">{this.context.t('search.organization.sector-type')} * </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="PUBLIC_BODY"
                                           disabled={this.props.static} onChange={this.handleSectorTypeChange}
                                           checked={sectorType === 'Public' || sectorType === 'PUBLIC_BODY'} />
                                    {this.context.t('search.organization.sector-type.PUBLIC_BODY')}
                                </label>

                                <label className="radio-inline field">
                                    <input type="radio" name="sector_type" value="COMPANY"
                                           disabled={this.props.static} onChange={this.handleSectorTypeChange}
                                           checked={sectorType === 'Private' || sectorType === 'COMPANY'} />
                                    {this.context.t('search.organization.sector-type.COMPANY')}
                                </label>
                            </div>
                        }

                    </fieldset>

                    <fieldset>
                        <legend>Organization</legend>

                    </fieldset>

                    <fieldset>
                        <legend>Contact Information</legend>


                    </fieldset>

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