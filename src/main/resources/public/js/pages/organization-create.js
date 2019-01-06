import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';

//Component
import OrganizationForm from '../components/forms/organization-form';
import UpdateTitle from '../components/update-title';

//Action
import {fetchCountries} from '../actions/config';
import OrganizationService from "../util/organization-service";
import {resetOrganizationFormAction} from '../actions/components/organization-form';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

class OrganizationCreate extends React.Component {



    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            error: ''
        };

        //bind methods
        this._organizationService = new OrganizationService();
    }

    componentDidMount() {
        this.props.fetchCountries();
    }

    onSubmit = async (organization) => {
        this.setState({isLoading: true});
        try {
            const organizationCreated = await this._organizationService.createOrganization(organization);
            this.props.history.push(`/my/organization/${organizationCreated.id}/admin`);
        }catch(err){
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });

            this.setState({
                isLoading: false,
                error: err.error || 'An error has occurred.\nYour request has not been sent.'
            });
        }
    };

    render() {
        return <section className="organization-create oz-body wrapper flex-col">
            <UpdateTitle title={i18n._(t`organization.form.create`)}/>

            <header className="title">
                <span>{i18n._(t`organization.form.create`)}</span>
            </header>

            <div className="box">
                <div className="error">
                    <span>{this.state.error}</span>
                </div>
                <OrganizationForm onSubmit={this.onSubmit} isLoading={this.state.isLoading}
                                  countries={this.props.countries}
                                  label={i18n._(t`organization.form.create`)}/>
            </div>
        </section>;
    }

}

const mapStateToProps = state => {
    return {
        countries: state.config.countries,
        userInfo: state.userInfo,
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
