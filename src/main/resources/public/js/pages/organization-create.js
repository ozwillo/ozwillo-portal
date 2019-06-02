import React from 'react';
import OrganizationForm from '../components/forms/organization-form';
import UpdateTitle from '../components/update-title';
import OrganizationService from "../util/organization-service";
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import customFetch from "../util/custom-fetch";

class OrganizationCreate extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            countries: [],
            isLoading: false,
            error: ''
        };

        this._organizationService = new OrganizationService();
    }

    componentDidMount() {
        customFetch('/api/geo/countries')
            .then((countries) => this.setState({ countries: countries.areas }));
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
                                  countries={this.state.countries}
                                  label={i18n._(t`organization.form.create`)}/>
            </div>
        </section>;
    }

}

export default OrganizationCreate;
