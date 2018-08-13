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
} from "../../actions/organization";


class MembersTabHeader extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/members`}>
            <header className="tab-header">
                <span>{this.context.t('organization.desc.members')}</span>
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

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        this.state = {
            membersFilter: '',
            isLoading: false
        };

        this.handleChange = this.handleChange.bind(this);
        this.filterMembers = this.filterMembers.bind(this);
    }

    componentDidMount() {
        if(this.props.organization.id) {
            if (!this.props.organization.members) {
                this.setState({isLoading: true});
                this.props.fetchOrganizationMembers(this.props.organization.id);
            } else {
                //we have members to display BUT we want to check if new ones are available
                this.props.fetchOrganizationMembers(this.props.organization.id);
            }
        }
    }

    componentWillReceiveProps(nextProps){
        if(nextProps.members) {
            this.setState({isLoading: false})
        }
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({[el.name]: el.value});
    }

    filterMembers(members, filter) {
        if (!filter) {
            return members;
        }

        const filterUpperCase = filter.toUpperCase();

        return members.filter((member) => {
            return member.name && member.name.toUpperCase().indexOf(filterUpperCase) >= 0 ||
                member.email && member.email.toUpperCase().indexOf(filterUpperCase) >= 0
        });
    }

    render() {
        const org = this.props.organization;
        const membersFilter = this.state.membersFilter;

        const header = <header className="dropdown-header">
            <OrganizationInvitationForm organization={this.props.organization} hideTitle={true}/>
        </header>;

        return <article className="members-tab">
            {
                org.admin &&
                <section className="add-member">
                    <header className="sub-title">{this.context.t('organization.desc.send-invitation')}</header>
                    <DropDownMenu header={header}/>
                </section>
            }
            <section className="search-member">
                <form className="search oz-form">
                    <input name="membersFilter" className="field form-control" type="text"
                           placeholder={this.context.t('ui.search')} onChange={this.handleChange}/>
                </form>

                <ul className="members-list undecorated-list flex-col">
                    {
                        !this.state.isLoading && this.props.members && this.filterMembers(this.props.members, membersFilter).map((member) => {
                            return <li key={member.id} className="member">
                                <MemberDropdown member={member} organization={this.props.organization}/>
                            </li>
                        })
                    }
                    {
                        this.state.isLoading &&
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



