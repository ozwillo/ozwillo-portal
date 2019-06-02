import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';
import OrganizationForm from '../forms/organization-form';
import { i18n } from "../../config/i18n-config"
import { t } from "@lingui/macro"
import NotificationMessageBlock from '../notification-message-block';
import customFetch, {urlBuilder} from "../../util/custom-fetch";

class AdminTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/admin`}>
            <header className="tab-header">
                <span>{i18n._(t`organization.desc.admin`)}</span>
            </header>
        </Link>;
    }

}

class AdminTab extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            organization: {},
            isLoading: true,
            success: '',
            error: ''
        };

        this.onSubmit = this.onSubmit.bind(this);
    }

    componentDidMount() {
        // Fetch information of current organization
        if (!this.isPersonal) {
            customFetch(urlBuilder('/my/api/organization/info', {dcId: this.props.organization.dc_id}))
                .then((organization) => this.setState({ organization: organization, isLoading: false }));
        }
        customFetch('/api/geo/countries')
            .then((res) => this.setState({ countries: res.areas }));
    }

    onSubmit(org) {
        this.setState({isLoading: true});

        //Transform empty fields to null
        Object.keys(org).forEach(key => {
            org[key] = org[key] || null;
        });

        return customFetch('/my/api/organization', {
            method: 'PUT',
            json: org
        }).then((org) => {
            this.setState({
                isLoading: false,
                success: i18n._(t`organization.desc.form.success`),
                organization: org
            })
        })
        .catch(err => {
            this.setState({
                isLoading: false,
                success: '',
                error: err.error
            })
        });
    }

    render() {
        if (!this.state.organization.id) {
            return (
                <div className="container-loading text-center">
                    <i className="fa fa-spinner fa-spin loading"/>
                </div>
            );
        } else {
            return (
                <article className="admin-tab">

                    <OrganizationForm onSubmit={this.onSubmit}
                                      organization={this.state.organization}
                                      countries={this.state.countries}
                                      isLoading={this.state.isLoading}
                                      label={i18n._(t`ui.save`)}
                                      alreadyRegistered={true}
                                      initialTaxRegNum={this.state.organization.tax_reg_num}/>

                    <div className="text-center">
                        <NotificationMessageBlock type={this.state.error ? 'danger' : 'success'}
                                                  display={this.state.error !== '' || this.state.success !== ''}
                                                  close={() => this.setState({error: '', success: ''})}
                                                  message={this.state.error ? this.state.error : this.state.success}/>
                    </div>
                </article>
            );
        }
    }
}

export { AdminTabHeader, AdminTab };
