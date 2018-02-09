import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import MemberDropdownHeader from './member-dropdown-header';
import MemberDropdownFooter from './member-dropdown-footer';

//Actions
import { fetchCreateAcl, fetchDeleteAcl } from "../../../actions/acl";
import { fetchDeleteMember } from "../../../actions/member";

class MemberDropdown extends React.Component {

    static propTypes = {
        member: PropTypes.object.isRequired,
        organization: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        //bind methods
        this.removeAccessToInstance= this.removeAccessToInstance.bind(this);
        this.addAccessToInstance = this.addAccessToInstance.bind(this);
        this.removeMemberInOrganization = this.removeMemberInOrganization.bind(this);
        this.sendInvitation = this.sendInvitation.bind(this);
        this.memberInstances = this.memberInstances.bind(this);
        this.filterInstanceWithoutAccess = this.filterInstanceWithoutAccess.bind(this);
    }

    addAccessToInstance(instance) {
        if(!instance) {
            return;
        }

        this.props.fetchCreateAcl(this.props.member, instance);
    }

    removeAccessToInstance(e) {
        const instanceId = e.currentTarget.dataset.instance;
        const instance = this.props.organization.instances.find((instance) => {
            return instance.id === instanceId;
        });

        this.props.fetchDeleteAcl(this.props.member, instance);
    }

    removeMemberInOrganization(memberId) {
        this.props.fetchDeleteMember(this.props.organization.id, memberId);
    }

    sendInvitation() {
        console.log('sendInvitation')
    }

    memberInstances() {
        const instances = [];

        this.props.organization.instances.forEach(instance => {
            if(!instance.users) {
                return;
            }

            const u = instance.users.find((user) => {
                return user.id === this.props.member.id;
            });

            if(u) {
                instances.push(instance);
            }
        });

        return instances;
    }

    filterInstanceWithoutAccess(instance) {
        if(!instance.users) {
            return true;
        }

        return !instance.users.find((user) => {
            return user.id === this.props.member.id;
        })
    }

    render() {
        const member = this.props.member;
        const org = this.props.organization;
        const memberInstances = this.memberInstances();
        const instancesWithoutAccess = org.instances.filter(this.filterInstanceWithoutAccess);

        const Header = <MemberDropdownHeader member={member}
                                             organization={org}
                                             onRemoveMemberInOrganization={this.removeMemberInOrganization}
                                             onSendInvitation={this.sendInvitation}/>;
        const Footer = <MemberDropdownFooter member={member}
                                             instances={instancesWithoutAccess}
                                             onAddAccessToInstance={this.addAccessToInstance}/>

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!member.isPending}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        memberInstances.map((instance, i) => {
                            return <li key={instance.id}>
                                <article className="item flex-row">
                                    <p className="name">{instance.name}</p>

                                    <div className="options flex-row">
                                        <button className="btn icon" onClick={this.removeAccessToInstance} data-instance={instance.id}>
                                            <i className="fa fa-trash option-icon delete"/>
                                        </button>
                                    </div>
                                </article>
                            </li>;
                        })
                    }
                </ul>
            </section>
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
        }
    };
};

export default connect(null, mapDispatchToProps)(MemberDropdown);