import React from 'react';
import PropTypes from 'prop-types';
import DropDownMenu from '../../dropdown-menu';
import MemberDropdownHeader from './member-dropdown-header';
import MemberDropdownFooter from './member-dropdown-footer'
import CustomTooltip from '../../custom-tooltip';
import InstanceService from "../../../util/instance-service";
import OrganizationService from "../../../util/organization-service";

import { i18n } from "../../../config/i18n-config"

class MemberDropdown extends React.Component {

    static propTypes = {
        member: PropTypes.object.isRequired,
        organization: PropTypes.object.isRequired,
        organizationInstances: PropTypes.array.isRequired,
        memberRemoved: PropTypes.func,
        memberRoleUpdated: PropTypes.func
    };

    state = {
        errors: {},
        instances: [],
        instancesWithoutAccess: []
    };

    constructor(props){
        super(props);
        this._instanceService = new InstanceService();
        this._organizationService = new OrganizationService();
    }

    addAccessToInstance = async (instance) => {
        let {instances, instancesWithoutAccess} = this.state;

        this._instanceService.fetchCreateAcl(this.props.member, instance)
            .then(() => this.setState({ instances: instances.concat([instance]),
                instancesWithoutAccess: instancesWithoutAccess.filter(currentInstance => currentInstance.id !== instance.id) }))
            .catch(err => {
                const errors = Object.assign({}, this.state.errors, {[instance.id]: err.error});
                this.setState({errors});
            });
    };

    removeAccessToInstance = async (e) => {
        let {instances, instancesWithoutAccess} = this.state;
        const instanceId = e.currentTarget.dataset.instance;
        let instance = instances.find((instance) => {
            return instance.id === instanceId;
        });

        this._instanceService.fetchDeleteAcl(this.props.member, instance)
            .then(() => this.setState({ instances: instances.filter(currentInstance => currentInstance.id !== instance.id),
                instancesWithoutAccess: instancesWithoutAccess.concat([instance]) }))
            .catch(err => {
                const errors = Object.assign({}, this.state.errors, {[instance.id]: err.error});
                this.setState({errors});
            });
    };

    removeMemberInOrganization = async(member) => {
        await this._organizationService.removeUser(this.props.organization.id, member);
        this.props.memberRemoved(member);
    };

    removeInvitationToJoinAnOrg = async (member) => {
        await this._organizationService.removeUserInvitation(this.props.organization.id, member);
        this.props.memberRemoved(member);
    };

    getInstancesWithoutAccess = (memberInstances) => {
         return this.props.organizationInstances.filter(instance => {
             return !memberInstances.find((memberInstance) => {
                 return memberInstance.id === instance.id;
             })
         });
    };

    onUpdateRoleMember = async (isAdmin) => {
        await this._organizationService.updateUserRole(this.props.organization.id, this.props.member.id, isAdmin);
        this.props.memberRoleUpdated(this.props.member.id, isAdmin);
    };

    handleDropDown = (dropDownState) => {
        if (dropDownState) {
            this._instanceService.fetchInstancesOfUserForOrganization(this.props.organization.id, this.props.member.id)
                .then(instances => this.setState({ instances: instances,
                    instancesWithoutAccess: this.getInstancesWithoutAccess(instances) }));
        }
    };

    render() {
        const {instances, instancesWithoutAccess} = this.state;
        const member = this.props.member;
        const isPending = !member.name;
        const org = this.props.organization;

        const Header = <MemberDropdownHeader member={member}
                                             organization={org}
                                             onRemoveMemberInOrganization={this.removeMemberInOrganization}
                                             onUpdateRoleMember={this.onUpdateRoleMember}
                                             onRemoveInvitationToJoinAnOrg={this.removeInvitationToJoinAnOrg}/>;
        const dropDownicon = <CustomTooltip title={i18n._(`tooltip.see.app.member`)}><i className="fa fa-list-alt option-icon"/></CustomTooltip>;
        return <DropDownMenu header={Header} dropDownIcon={dropDownicon} isAvailable={!isPending} dropDownChange={this.handleDropDown}>
            { org.admin &&
                <section className="dropdown-content">
                <table className='table table-striped'>
                    <tbody>
                        {
                            instances.map((instance, i) => {
                                return <tr key={instance.id}>
                                        <td className="fill-content">{instance.name}</td>
                                        <td className="fill-content">{this.state.errors[instance.id]}</td>
                                        <td className="fill-content col-sm-1">
                                            <CustomTooltip title={i18n._(`tooltip.remove.instance`)}>
                                                <button className="btn icon delete"
                                                        onClick={this.removeAccessToInstance} data-instance={instance.id}>
                                                    <i className="fa fa-trash option-icon delete"/>
                                                </button>
                                            </CustomTooltip>
                                        </td>
                                </tr>;
                            })
                        }
                    </tbody>
                </table>
                <MemberDropdownFooter member={member}
                                      instances={instancesWithoutAccess}
                                      onAddAccessToInstance={this.addAccessToInstance}/>
                </section>
            }
        </DropDownMenu>
    }
}

export default MemberDropdown;
