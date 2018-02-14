import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

//Components
import OrganizationInvitationForm from '../forms/organization-invitation-form';
import MemberDropdown from '../dropdown_menus/member/member-dropdown';

class MembersTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/members`}>
            <header className="tab-header"><span>Members</span></header>
        </Link>;
    }

}
const MembersTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(MembersTabHeader);

class MembersTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    render() {
        const org = this.props.organization;

        return <article className="members-tab">
            <section className="add-member">
                <header className="sub-title">Send an invitation</header>
                <div className="box">
                    <OrganizationInvitationForm className="" organization={this.props.organization}/>
                </div>
            </section>
            <section className="search-member">
                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="members-list undecorated-list flex-col">
                    {
                        org.members && org.members.map((member) => {
                            return <li key={member.id} className="member">
                                <MemberDropdown member={member} organization={this.props.organization}/>
                            </li>
                        })
                    }
                </ul>
            </section>
        </article>;
    }
}

const MemberTabWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(MembersTab);


export {
    MemberTabWithRedux as MembersTab,
    MembersTabHeaderWithRedux as MembersTabHeader
};