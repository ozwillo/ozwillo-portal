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
            <header className="tab-header">Members</header>
        </Link>;
    }

}
const MembersTabHeaerWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(MembersTabHeader);

class MembersTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    render() {
        return <article className="members-tab">
            <section className="add-member">
                <header>Send an invitation</header>
                <div className="box">
                    <OrganizationInvitationForm className="" organization={this.props.organization}/>
                </div>
            </section>
            <section className="search-member">
                <header>Search a member</header>
                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="members-list undecorated-list flex-col">
                    {
                        this.props.organization.members.map((member) => {
                            return <li key={member.id} className="member">
                                <MemberDropdown member={member} organization={this.props.organization}
                                                services={this.props.services}/>
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
        organization: state.organization.current,
        services: state.service.services
    };
})(MembersTab);


export {
    MemberTabWithRedux as MembersTab,
    MembersTabHeaerWithRedux as MembersTabHeader
};