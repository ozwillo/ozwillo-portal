import React from 'react';
import PropTypes from "prop-types";
import {connect} from 'react-redux';

//Components
import Select from 'react-select';
import Tabs from '../components/tabs';
import {InstancesTabHeader, InstancesTab} from '../components/tabs/instances-tab';
import {MembersTabHeader, MembersTab} from '../components/tabs/members-tab';
import {AdminTabHeader, AdminTab} from '../components/tabs/admin-tab';
import UpdateTitle from '../components/update-title';

//actions
import {fetchOrganizationWithId, fetchOrganizationInfo} from "../actions/organization";
import customFetch from "../util/custom-fetch";

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

const tabsHeaders = {
    instances: InstancesTabHeader,
    members: MembersTabHeader,
    admin: AdminTabHeader
};

const tabs = {
    instances: InstancesTab,
    members: MembersTab,
    admin: AdminTab
};

const defaultTabToDisplay = 'instances';


class OrganizationDesc extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            orgSelected: null,
            organizations: []
        };

        this.onChangeOrganization = this.onChangeOrganization.bind(this);
        this.initialize = this.initialize.bind(this);
    }

    initialize(id) {
        this.setState({isLoading: true});
        if (!this.isPersonal()) {
            customFetch(`/my/api/organizationHistory/visit/${id}`,
                {
                    method: "POST",
                });
        }
        this.props.fetchOrganizationWithId(id)
            .then(() => {
                // Update selector

                this.setState({orgSelected: this.props.organization});
                this.setState({isLoading: false});
            });
        customFetch('/my/api/organization')
            .then((organizations) => {
                this.setState({organizations: organizations})
            });
    }


    onChangeOrganization(organization) {
        this.setState({orgSelected: organization});
        // Update url
        this.props.history.replace(`/my/organization/${organization.id}/`);
        // Update page
        this.initialize(organization.id);


    }

    componentDidMount() {
        this.initialize(this.props.match.params.id);
    }

    isPersonal = () => {
        return this.props.organization.id === this.props.userInfo.sub;
    };

    render() {
        const tabToDisplay = this.props.match.params.tab || defaultTabToDisplay;
        const isOrgAdmin = this.props.organization.admin;

        let {orgSelected, organizations} = this.state;

        return <section className="organization-desc oz-body wrapper flex-col">

            <Select
                className="select organization-switcher"
                value={orgSelected}
                labelKey="name"
                valueKey="id"
                onChange={this.onChangeOrganization}
                clearable={false}
                options={organizations}/>

            {
                this.state.isLoading &&
                <div className="container-loading text-center">
                    <i className="fa fa-spinner fa-spin loading"/>
                </div>
            }

            {
                !this.state.isLoading &&
                <React.Fragment>

                    <UpdateTitle title={this.props.organization.name}/>

                    {
                        !this.isPersonal() && isOrgAdmin && <React.Fragment>
                            <header className="title">
                                <span>{this.props.organization.name}</span>
                            </header>
                            <Tabs className="content" headers={tabsHeaders} tabs={tabs}
                                  tabToDisplay={tabToDisplay}/>
                        </React.Fragment>

                    }

                    {
                        !this.isPersonal() && !isOrgAdmin && <React.Fragment>
                            <header className="title">
                                <span>{this.props.organization.name}</span>
                            </header>
                            <section className="box">
                                <tabs.members/>
                            </section>
                        </React.Fragment>

                    }

                    {
                        this.isPersonal() && <React.Fragment>
                            <header className="title">
                                <span>{i18n._(t`organization.desc.applications`)}</span>
                            </header>

                            <section className="box">
                                <tabs.instances/>
                            </section>
                        </React.Fragment>
                    }
                </React.Fragment>
            }

        </section>;
    }
}

const
    mapStateToProps = state => {
        return {
            organization: state.organization.current,
            userInfo: state.userInfo,
        };
    };

const
    mapDispatchToProps = dispatch => {
        return {
            fetchOrganizationWithId(id) {
                return dispatch(fetchOrganizationWithId(id));
            },
            fetchOrganizationInfo(dcId) {
                return dispatch(fetchOrganizationInfo(dcId));
            },
        };
    };

export default connect(mapStateToProps, mapDispatchToProps)

(
    OrganizationDesc
)
;
