import React from 'react';
import { connect } from 'react-redux';

//Components
import Tabs from '../components/tabs';
import { ServicesTabHeader, ServicesTab } from '../components/tabs/services-tab';
import { MembersTabHeader, MembersTab } from '../components/tabs/members-tab';

//actions
import { fetchOrganizationWithId } from "../actions/organization";
import PropTypes from "prop-types";

const tabsHeaders = {
    services: ServicesTabHeader,
    members: MembersTabHeader,
    admin: () => 'Admin'
};

const tabs = {
    services: ServicesTab,
    members: MembersTab,
    admin: () => {
        return <h1>Admin</h1>
    }
};


class OrganizationDesc extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.fetchOrganizationWithId();
    }

    render() {
        return <section className="organization-desc oz-body wrapper flex-col">

            <header className="header flex-row">
                {/*<img className="organization-logo" src="/img/appli-catalogue.png" />*/}
                <i className="fa fa-picture-o organization-logo" />
                <h1 className="title">Name of the organization</h1>
            </header>

            <Tabs className="content" headers={tabsHeaders} tabs={tabs}/>

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
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationDesc);