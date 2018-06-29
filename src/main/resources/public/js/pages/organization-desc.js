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
import {fetchOrganizationWithId, fetchOrganizationInfo, fetchUserOrganizationsLazyMode} from "../actions/organization";
import {fetchUsersOfInstance} from "../actions/instance";
import {fetchApplications} from "../actions/app-store";

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

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            orgSelected: null
        };

        this.onChangeOrganization = this.onChangeOrganization.bind(this);
        this.initialize = this.initialize.bind(this);
    }

    initialize(id) {
        this.setState({ isLoading: true });

        Promise.all([
            this.props.fetchUserOrganizationsLazyMode(),
            this.props.fetchOrganizationWithId(id)
                .then(() => {
                    const org = this.props.organization;
                    const requests = [];

                    // Update selector
                    this.setState({ orgSelected: org });

                    // Fetch users for each instance
                    if (org.admin) {
                        org.instances.forEach((instance) => {
                            this.props.fetchUsersOfInstance(instance);
                        });
                    }

                    // Fetch information of current organization
                    if (!this.isPersonal) {
                        requests.push(this.props.fetchOrganizationInfo(org.dc_id));
                    }

                    return Promise.all(requests);
                }),
            this.props.fetchApplications()
        ])
        .catch((err) => { console.error(err); })
        .then(() => { this.setState({ isLoading: false }); });
    }

    onChangeOrganization(organization) {
        this.setState({ orgSelected: organization });

        // Update url
        this.props.history.replace(`/my/organization/${organization.id}/`);

        // Update page
        this.initialize(organization.id);
    }

    componentDidMount() {
        this.initialize(this.props.match.params.id);
    }

    get isPersonal() {
        return this.props.organization.id === this.props.userInfo.sub;
    }

    render() {
        const tabToDisplay = this.props.match.params.tab || defaultTabToDisplay;
        const isOrgAdmin = this.props.organization.admin

        return <section className="organization-desc oz-body wrapper flex-col">

            <Select
                className="select"
                value={this.state.orgSelected}
                labelKey="name"
                valueKey="id"
                onChange={this.onChangeOrganization}
                clearable={false}
                options={this.props.organizations}/>

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
                        !this.isPersonal && isOrgAdmin && <React.Fragment>
                            <header className="title">
                                <span>{this.props.organization.name}</span>
                            </header>
                            <Tabs className="content" headers={tabsHeaders} tabs={tabs} tabToDisplay={tabToDisplay}/>
                        </React.Fragment>

                    }

                    {
                        !this.isPersonal && !isOrgAdmin && <React.Fragment>
                            <header className="title">
                                <span>{this.props.organization.name}</span>
                            </header>
                            <section className="box">
                                <tabs.members />
                            </section>
                        </React.Fragment>

                    }

                    {
                        this.isPersonal && <React.Fragment>
                            <header className="title">
                                <span>{this.context.t('organization.desc.applications')}</span>
                            </header>

                            <section className="box">
                                <tabs.instances />
                            </section>
                        </React.Fragment>
                    }
                </React.Fragment>
            }

        </section>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
        organizations: state.organization.organizations,
        userInfo: state.userInfo,
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchOrganizationWithId(id) {
            return dispatch(fetchOrganizationWithId(id));
        },
        fetchUsersOfInstance(instance) {
            return dispatch(fetchUsersOfInstance(instance));
        },
        fetchApplications() {
            return dispatch(fetchApplications());
        },
        fetchOrganizationInfo(dcId) {
            return dispatch(fetchOrganizationInfo(dcId));
        },
        fetchUserOrganizationsLazyMode() {
            return dispatch(fetchUserOrganizationsLazyMode());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationDesc);