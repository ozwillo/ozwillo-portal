import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import MemberDropdownHeader from './member-dropdown-header';
import MemberDropdownFooter from './member-dropdown-footer'
import CustomTooltip from '../../custom-tooltip';

//Actions
import {fetchUpdateRoleMember} from '../../../actions/member';
import InstanceService from "../../../util/instance-service";
import OrganizationService from "../../../util/organization-service";

import { i18n } from "../../../config/i18n-config"

class MemberDropdown extends React.Component {

    static propTypes = {
        member: PropTypes.object.isRequired,
        organization: PropTypes.object.isRequired,
        memberRemoved: PropTypes.func
    };

    state = {
        errors: {},
        instances: [],
        memberInstances: [],
        memberInstancesWithoutAccess: []
    };

    constructor(){
        super();
        this._instanceService = new InstanceService();
        this._organizationService = new OrganizationService();
    }

    componentDidMount(){
        const {instances} = this.props.organization;
        if(instances) {
            const memberInstances = this.memberInstances(instances);
            const memberInstancesWithoutAccess = this.getInstancesWithoutAccess(instances);
            this.setState({memberInstances, memberInstancesWithoutAccess, instances});
        }
    }

    addAccessToInstance = async (instance) => {
        let {instances} = this.state;

        if (!instance) {
            return;
        }

        const acl = await this._instanceService.fetchCreateAcl(this.props.member, instance);
        let memberInstances = this.memberInstances(instances);

        //add the new user without fetching the API
        instances.find(i => i.id === instance.id).users.push(this.props.member);
        memberInstances.push(instance);
        const memberInstancesWithoutAccess = this.getInstancesWithoutAccess(instances);

        this.setState({memberInstances, instances, memberInstancesWithoutAccess});
        return acl;
    };

    removeAccessToInstance = async (e) => {
        let {instances} = this.state;
        const instanceId = e.currentTarget.dataset.instance;
        let instance = instances.find((instance) => {
            return instance.id === instanceId;
        });

        try {
            await this._instanceService.fetchDeleteAcl(this.props.member, instance);
            const errors = Object.assign({}, this.state.errors, {[instanceId]: ''});

            //remove the user without fetching the API
            let memberInstances = this.memberInstances(instances);
            memberInstances.splice(memberInstances.indexOf(instance.id),1);
            instance.users.splice(instance.users.indexOf(this.props.member.id),1);
            const memberInstancesWithoutAccess = this.getInstancesWithoutAccess(instances);

            this.setState({memberInstances, instances, memberInstancesWithoutAccess, errors});
        }catch(err){
            const errors = Object.assign({}, this.state.errors, {[instanceId]: err.error});
            this.setState({errors});
        }
    };

    removeMemberInOrganization = async(member) => {
        await this._organizationService.removeUser(this.props.organization.id, member);
        this.props.memberRemoved(member);
    };

    removeInvitationToJoinAnOrg = async (member) => {
        await this._organizationService.removeUserInvitation(this.props.organization.id, member);
        this.props.memberRemoved(member);
    };

    memberInstances = (instances) => {
        const memberInstances = [];

        instances.forEach(instance => {
            if (!instance.users) {
                return;
            }

            const u = instance.users.find((user) => {
                return user.id === this.props.member.id;
            });

            if (u) {
                memberInstances.push(instance);
            }
        });
        return memberInstances;
    };

     getInstancesWithoutAccess = (instances) => {
         return instances.filter( instance => {
             if (!instance.users) {
                 return true;
             }

             return !instance.users.find((user) => {
                 return user.id === this.props.member.id;
             })
         });
    };

    onUpdateRoleMember = (isAdmin) => {
        return this.props.fetchUpdateRoleMember(this.props.organization.id, this.props.member.id, isAdmin);
    };

    handleDropDown = (dropDownState) => {
        if(dropDownState){
            //TODO fetch instances of the specific user, currently we need to fetch all the users of all the instances, then check in which instance is our user
        }
    };

    render() {
        const {memberInstances, memberInstancesWithoutAccess} = this.state;
        const member = this.props.member;
        const isPending = !member.name;
        const org = this.props.organization;

        const Header = <MemberDropdownHeader member={member}
                                             organization={org}
                                             onRemoveMemberInOrganization={this.removeMemberInOrganization}
                                             onUpdateRoleMember={this.onUpdateRoleMember}
                                             onRemoveInvitationToJoinAnOrg={this.removeInvitationToJoinAnOrg}/>;
        const dropDownicon = <i className="fa fa-list-alt option-icon"/>;
        return <DropDownMenu header={Header} dropDownIcon={dropDownicon} isAvailable={!isPending} dropDownChange={this.handleDropDown}>
            { org.admin &&
                <section className="dropdown-content">
                <table className='table table-striped'>
                    <tbody>
                        {
                            memberInstances.map((instance, i) => {
                                return <tr key={instance.id}>
                                        <td className="fill-content">{instance.name}</td>
                                        <td className="fill-content">{this.state.errors[instance.id]}</td>
                                        <td className="fill-content col-md-1">
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
                                      instances={memberInstancesWithoutAccess}
                                      onAddAccessToInstance={this.addAccessToInstance}/>
                </section>
            }
        </DropDownMenu>
    }
}

const mapDispatchToProps = dispatch => {
    return {
        fetchUpdateRoleMember(organizationId, memberId, isAdmin) {
            return dispatch(fetchUpdateRoleMember(organizationId, memberId, isAdmin));
        },
    };
};

export default connect(null, mapDispatchToProps)(MemberDropdown);
