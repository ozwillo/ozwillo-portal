import React from 'react';
import { connect } from 'react-redux';

//Components
import Tabs from '../components/tabs';
import { ServicesTabHeader, ServicesTab } from '../components/tabs/services-tab';
import { MembersTabHeader, MembersTab } from '../components/tabs/members-tab';
import { AdminTabHeader, AdminTab } from '../components/tabs/admin-tab';

//actions
import { fetchOrganizationWithId } from "../actions/organization";
import PropTypes from "prop-types";
import { fetchUsersOfService } from "../actions/service";

const tabsHeaders = {
    services: ServicesTabHeader,
    members: MembersTabHeader,
    admin: AdminTabHeader
};

const tabs = {
    services: ServicesTab,
    members: MembersTab,
    admin: AdminTab
};


class OrganizationDesc extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.fetchOrganizationWithId()
            .then(() => {
                if(this.props.organization.admin) {
                    this.props.organization.services.forEach((service) => {
                        this.props.fetchUsersOfService(service);
                    });
                }
            });
    }

    render() {
        const tabToDisplay = this.props.match.params.tab || 'services';

        return <section className="organization-desc oz-body wrapper flex-col">
            <header className="header flex-row">
                <h1 className="title">{this.props.organization.name}</h1>
            </header>

            <Tabs className="content" headers={tabsHeaders} tabs={tabs} tabToDisplay={tabToDisplay}/>

        </section>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current
    };
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        fetchOrganizationWithId() {
            return dispatch(fetchOrganizationWithId(ownProps.match.params.id));
        },
        fetchUsersOfService(service) {
            return dispatch(fetchUsersOfService(service));
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationDesc);