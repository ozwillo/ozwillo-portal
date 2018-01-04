import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import OrganizationInvitationForm from '../organization-invitation-form';

class MembersTabHeader extends React.Component {

    render() {
        return <header>Members</header>
    }

}

class MembersTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    render() {
        return <article className="members-tab">
            <section className="add-member">
                <header>Send an invitation</header>
                <div className="box">
                    <OrganizationInvitationForm organization={this.props.organization}/>
                </div>
            </section>
            <section className="search-service">
                <header>Search a service</header>
                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="members-list undecorated-list flex-col">
                    {
                        this.props.organization.members.map((member) => {
                            return <li key={service.id} className="member">
                                {member.firstname}
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
const MemberTabWithRedux = connect(mapStateToProps)(MembersTab);


export {
    MemberTabWithRedux as MembersTab,
    MembersTabHeader
};