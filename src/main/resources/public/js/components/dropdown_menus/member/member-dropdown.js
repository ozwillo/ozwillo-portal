import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import MemberDropdownHeader from './member-dropdown-header';
import MemberDropdownFooter from './member-dropdown-footer'
import CustomTooltip from '../../custom-tooltip';

//Actions
import {fetchCreateAcl, fetchDeleteAcl} from '../../../actions/acl';
import {fetchDeleteMember, fetchUpdateRoleMember} from '../../../actions/member';
import {fetchRemoveOrganizationInvitation} from "../../../actions/invitation";

class MemberDropdown extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    static propTypes = {
        member: PropTypes.object.isRequired,
        organization: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            errors: {}
        };

        //bind methods
        this.removeAccessToInstance = this.removeAccessToInstance.bind(this);
        this.addAccessToInstance = this.addAccessToInstance.bind(this);
        this.removeMemberInOrganization = this.removeMemberInOrganization.bind(this);
        this.removeInvitationToJoinAnOrg = this.removeInvitationToJoinAnOrg.bind(this);
        this.memberInstances = this.memberInstances.bind(this);
        this.filterInstanceWithoutAccess = this.filterInstanceWithoutAccess.bind(this);
        this.onUpdateRoleMember = this.onUpdateRoleMember.bind(this);
    }

    addAccessToInstance(instance) {
        if (!instance) {
            return;
        }

        return this.props.fetchCreateAcl(this.props.member, instance);
    }

    removeAccessToInstance(e) {
        const instanceId = e.currentTarget.dataset.instance;
        const instance = this.props.organization.instances.find((instance) => {
            return instance.id === instanceId;
        });

        this.props.fetchDeleteAcl(this.props.member, instance)
            .then(() => {
                const errors = Object.assign({}, this.state.errors, {[instanceId]: ''});
                this.setState({errors});
            })
            .catch((err) => {
                const errors = Object.assign({}, this.state.errors, {[instanceId]: err.error});
                this.setState({errors});
            });
    }

    removeMemberInOrganization(memberId) {
        return this.props.fetchDeleteMember(this.props.organization.id, memberId);
    }

    removeInvitationToJoinAnOrg(member) {
        return this.props.fetchRemoveOrganizationInvitation(this.props.organization.id, member);
    }

    memberInstances() {
        const instances = [];

        this.props.organization.instances.forEach(instance => {
            if (!instance.users) {
                return;
            }

            const u = instance.users.find((user) => {
                return user.id === this.props.member.id;
            });

            if (u) {
                instances.push(instance);
            }
        });

        return instances;
    }

    filterInstanceWithoutAccess(instance) {
        if (!instance.users) {
            return true;
        }

        return !instance.users.find((user) => {
            return user.id === this.props.member.id;
        })
    }

    onUpdateRoleMember(isAdmin) {
        return this.props.fetchUpdateRoleMember(this.props.organization.id, this.props.member.id, isAdmin);
    }

    render() {
        const member = this.props.member;
        const isPending = !member.name;
        const org = this.props.organization;

        const Header = <MemberDropdownHeader member={member}
                                             organization={org}
                                             onRemoveMemberInOrganization={this.removeMemberInOrganization}
                                             onUpdateRoleMember={this.onUpdateRoleMember}
                                             onRemoveInvitationToJoinAnOrg={this.removeInvitationToJoinAnOrg}/>;
        const Footer = !isPending && org.admin && <MemberDropdownFooter member={member}
                                                           instances={org.instances.filter(this.filterInstanceWithoutAccess)}
                                                           onAddAccessToInstance={this.addAccessToInstance}/>;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!isPending}>
            { org.admin &&
                <section className='dropdown-content'>
                    <ul className="list undecorated-list flex-col">
                        {
                            this.memberInstances().map((instance, i) => {
                                return <li key={instance.id}>
                                    <article className="item flex-row">
                                        <span className="name">{instance.name}</span>
                                        <span className="error-message">{this.state.errors[instance.id]}</span>
                                        <div className="options flex-row">
                                            <CustomTooltip title={this.context.t('tooltip.remove.instance')}>
                                                <button className="btn icon"
                                                        onClick={this.removeAccessToInstance} data-instance={instance.id}>
                                                    <i className="fa fa-trash option-icon delete"/>
                                                </button>
                                            </CustomTooltip>
                                        </div>
                                    </article>
                                </li>;
                            })
                        }
                    </ul>
                </section>
            }
        </DropDownMenu>
    }
}

const mapDispatchToProps = dispatch => {
    return {
        fetchCreateAcl(user, instance) {
            return dispatch(fetchCreateAcl(user, instance));
        },
        fetchDeleteAcl(user, instance) {
            return dispatch(fetchDeleteAcl(user, instance));
        },
        fetchDeleteMember(organizationId, memberId) {
            return dispatch(fetchDeleteMember(organizationId, memberId));
        },
        fetchUpdateRoleMember(organizationId, memberId, isAdmin) {
            return dispatch(fetchUpdateRoleMember(organizationId, memberId, isAdmin));
        },
        fetchRemoveOrganizationInvitation(orgId, invitation) {
            return dispatch(fetchRemoveOrganizationInvitation(orgId, invitation));
        }
    };
};

export default connect(null, mapDispatchToProps)(MemberDropdown);