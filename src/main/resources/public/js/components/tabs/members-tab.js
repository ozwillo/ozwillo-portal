import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';

import OrganizationInvitationForm from '../forms/organization-invitation-form';
import MemberDropdown from '../dropdown_menus/member/member-dropdown';
import DropDownMenu from '../dropdown-menu';
import InstanceService from '../../util/instance-service';

import {i18n} from '../../config/i18n-config'
import {t} from '@lingui/macro'
import { CSSTransition, TransitionGroup} from 'react-transition-group';
import customFetch from "../../util/custom-fetch";


class MembersTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/members`}>
            <header className="tab-header">
                <span>{i18n._(t`organization.desc.members`)}</span>
            </header>
        </Link>;
    }

}

class MembersTab extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            members: [],
            organizationInstances: []
        };

        this._instanceService = new InstanceService();
    }

    componentDidMount() {
        if (this.props.organization.admin) {
            this._instanceService.fetchInstancesOfOrganization(this.props.organization.id)
                .then(instances => this.setState({ organizationInstances: instances }));
        }
        customFetch(`/my/api/organization/${this.props.organization.id}/members`)
            .then(members => this.setState({ members: members }));
    }

    _updateMembers = (newMembers) => {
        let {members} = this.state;
        const mergedArray = members.concat(newMembers);
        this.setState({members: mergedArray});
    };

    _removeMember = (member) => {
        let {members} = this.state;
        members.splice(members.findIndex(elem => elem.id === member.id), 1);
        this.setState({members});
    };

    _updateMemberRole = (memberId, isAdmin) => {
        let {members} = this.state;
        let memberToUpdate = members.find(elem => elem.id === memberId);
        memberToUpdate.admin = isAdmin;
        this.setState({ members });
    };

    render() {
        const {members, isLoading} = this.state;
        const org = this.props.organization;
        const header =
            <OrganizationInvitationForm
                organization={this.props.organization}
                callBackMembersInvited={this._updateMembers}
                members={members}
            />;


        return <article className="members-tab">
            {
                org.admin &&
                <section className="add-member">
                    <DropDownMenu header={header} className="action-header"/>
                </section>
            }
            <section>

                <ul className="members-list undecorated-list flex-col">
                    <TransitionGroup
                    >
                    {
                        !isLoading && members && members.map((member,index) => {
                            return (
                                <CSSTransition
                                    timeout={50 * (index+1)}
                                    key={member.id}
                                    classNames={"fade"}
                                    >
                                    {(state) => {
                                        return (
                                            <li className="member">
                                                <MemberDropdown
                                                    member={member}
                                                    organization={this.props.organization}
                                                    organizationInstances={this.state.organizationInstances}
                                                    memberRemoved={this._removeMember}
                                                    memberRoleUpdated={this._updateMemberRole}
                                                />
                                            </li>
                                        )
                                    }}
                                </CSSTransition>
                            )
                        })
                    }
                    </TransitionGroup>
                    {
                        isLoading &&
                        <div className="container-loading text-center">
                            <i className="fa fa-spinner fa-spin loading"/>
                        </div>
                    }
                </ul>
            </section>
        </article>;
    }
}

export { MembersTabHeader, MembersTab };



