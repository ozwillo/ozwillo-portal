import React from 'react';
import PropTypes from "prop-types";
import {connect} from 'react-redux';

//Components
import Tabs from '../components/tabs';
import {InstancesTabHeader, InstancesTab} from '../components/tabs/instances-tab';
import {MembersTabHeader, MembersTab} from '../components/tabs/members-tab';
import {AdminTabHeader, AdminTab} from '../components/tabs/admin-tab';
import UpdateTitle from '../components/update-title';

//actions
import {fetchOrganizationWithId, fetchOrganizationInfo} from "../actions/organization";
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
    }

    componentDidMount() {
        Promise.all([
            this.props.fetchOrganizationWithId()
                .then(() => {
                    const org = this.props.organization;
                    const requests = [];
                    if (org.admin) {
                        org.instances.forEach((instance) => {
                            this.props.fetchUsersOfInstance(instance);
                        });
                    }

                    if (!this.isPersonal) {
                        requests.push(this.props.fetchOrganizationInfo(org.dc_id));
                    }

                    return Promise.all(requests);
                }),
            this.props.fetchApplications()
        ]).catch((err) => {
            console.error(err);
        });
    }

    get isPersonal() {
        return this.props.organization.id === this.props.userInfo.sub;
    }

    render() {
        const tabToDisplay = this.props.match.params.tab || defaultTabToDisplay;

        return <section className="organization-desc oz-body wrapper flex-col">

            <UpdateTitle title={this.props.organization.name}/>



            {
                !this.isPersonal && <React.Fragment>
                    <header className="title">
                        <span>{this.props.organization.name}</span>
                    </header>
                    <Tabs className="content" headers={tabsHeaders} tabs={tabs} tabToDisplay={tabToDisplay}/>
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

        </section>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
        userInfo: state.userInfo
    };
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        fetchOrganizationWithId() {
            return dispatch(fetchOrganizationWithId(ownProps.match.params.id));
        },
        fetchUsersOfInstance(instance) {
            return dispatch(fetchUsersOfInstance(instance));
        },
        fetchApplications() {
            return dispatch(fetchApplications());
        },
        fetchOrganizationInfo(dcId) {
            return dispatch(fetchOrganizationInfo(dcId));
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationDesc);