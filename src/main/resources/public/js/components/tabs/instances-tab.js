import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

//Components
import AddInstanceDropdown from '../dropdown_menus/instance/add-instance-dropdown';
import InstanceDropdown from '../dropdown_menus/instance/instance-dropdown';
import customFetch from '../../util/custom-fetch';

import { i18n } from "../../config/i18n-config"
import { t } from "@lingui/macro"

class InstancesTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/instances`}>
            <header className="tab-header">
                <span>{i18n._(t`organization.desc.applications`)}</span>
            </header>
        </Link>;
    }
}

const InstancesTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(InstancesTabHeader);


class InstancesTab extends React.Component {

    state = {
        organizationMembers: null,
        instances: null
    };

    componentDidMount(){
        const instances = [...this.props.organization.instances];
        this.setState({instances})
        this.fetchOrganizationMembers();
    }

    componentWillReceiveProps(nextProps, nextContext) {
        this.setState({instances: nextProps.organization.instances})
    }

    fetchOrganizationMembers = () => {
        const {id} = this.props.organization;
        customFetch(`/my/api/organization/${id}/accepted-members`).then(res => {
            this.setState({organizationMembers: res});
        })
    };

    render() {
        const org = this.props.organization;
        const {organizationMembers, instances} = this.state;

        if(!instances){
            return null
        }

        return <article className="instances-tab">

            {
                org.admin &&
                <section className="add-instance">
                    <AddInstanceDropdown/>
                </section>
            }


            <section>
                <ul className="instances-list undecorated-list flex-col">
                    {
                        instances.map(instance => {
                            return <li key={instance.id + instance.applicationInstance.status} className="instance">
                                <InstanceDropdown instance={instance} organizationMembers={organizationMembers} isAdmin={org.admin}/>
                            </li>
                        })
                    }
                </ul>
            </section>

        </article>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
    };
};

const InstanceTabWithRedux = connect(mapStateToProps)(InstancesTab);


export {
    InstanceTabWithRedux as InstancesTab,
    InstancesTabHeaderWithRedux as InstancesTabHeader
};
