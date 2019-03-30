import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

//Components
import OrganizationInvitationForm from '../forms/organization-invitation-form';
import MemberDropdown from '../dropdown_menus/member/member-dropdown';
import DropDownMenu from '../dropdown-menu';
import {
    fetchOrganizationMembers,
} from '../../actions/organization';
import InstanceService from '../../util/instance-service';

import {i18n} from '../../config/i18n-config'
import {t} from '@lingui/macro'
import { CSSTransition, TransitionGroup} from 'react-transition-group';


class MembersTabHeader extends React.Component {


    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/members`}>
            <header className="tab-header">
                <span>{i18n._(t`organization.desc.members`)}</span>
            </header>
        </Link>;
    }

}

const MembersTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(MembersTabHeader);

class MembersTab extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            members: this.props.organization.members ? this.props.organization.members : []
        };

        this._instanceService = new InstanceService();
    }

    componentDidMount() {
        //TODO find a solution to supress that (new method in the kernel which allow to get all the instances of one user) cf: TODO in MemberDropdown
        if (this.props.organization.admin) {
            this.props.organization.instances.forEach(async (instance) => {
                instance.users = await this._instanceService.fetchUsersOfInstance(instance.id);
            });
        }
        if (this.props.organization.id) {
            if (!this.props.organization.members) {
                this.setState({isLoading: true});
                this.props.fetchOrganizationMembers(this.props.organization.id);
            } else {
                //we have members to display BUT we want to check if new ones are available
                this.props.fetchOrganizationMembers(this.props.organization.id);
            }
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.members) {
            this.setState({isLoading: false, members: nextProps.members})
        }
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
                                                    memberRemoved={this._removeMember}
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

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
        members: state.organization.current.members
    }
};

const mapDispatchToProps = dispatch => {
    return {
        fetchOrganizationMembers(organizationId) {
            return dispatch(fetchOrganizationMembers(organizationId));
        }
    };
};

const MemberTabWithRedux = connect(mapStateToProps, mapDispatchToProps)(MembersTab);


export {
    MembersTabHeaderWithRedux as MembersTabHeader,
    MemberTabWithRedux as MembersTab
};



