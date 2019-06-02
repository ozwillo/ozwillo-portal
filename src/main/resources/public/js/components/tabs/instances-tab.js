import React from 'react';
import {Link} from 'react-router-dom';

import AddInstanceDropdown from '../dropdown_menus/instance/add-instance-dropdown';
import InstanceDropdown from '../dropdown_menus/instance/instance-dropdown';
import customFetch from '../../util/custom-fetch';
import InstanceService from "../../util/instance-service";

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

class InstancesTab extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            organizationMembers: null,
            instances: null
        };

        this._instanceService = new InstanceService();
    }

    componentDidMount() {
        this._instanceService.fetchInstancesOfOrganization(this.props.organization.id)
            .then(instances => this.setState({ instances }));
        this.fetchOrganizationMembers();
    }

    fetchOrganizationMembers = () => {
        const {id} = this.props.organization;
        customFetch(`/my/api/organization/${id}/accepted-members`).then(res => {
            this.setState({organizationMembers: res});
        })
    };

    onChangeInstanceStatus = (instanceId, status) => {
        const instances = this.state.instances;
        const instance = instances.find(instance => instance.id === instanceId);
        instance.status = status;
        instance.applicationInstance.status = status;
        this.setState({ instances });
    }

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
                    <AddInstanceDropdown organization={this.props.organization}/>
                </section>
            }


            <section>
                <ul className="instances-list undecorated-list flex-col">
                    {
                        instances.map(instance => {
                            return <li key={instance.id + instance.applicationInstance.status} className="instance">
                                <InstanceDropdown organization={this.props.organization}
                                                  instance={instance}
                                                  organizationMembers={organizationMembers}
                                                  isAdmin={org.admin}
                                                  onChangeInstanceStatus={this.onChangeInstanceStatus}/>
                            </li>
                        })
                    }
                </ul>
            </section>

        </article>;
    }
}

export { InstancesTab, InstancesTabHeader };
