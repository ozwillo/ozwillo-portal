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
import InstanceService from "../../util/instance-service";


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
            isLoading: false
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

    render() {
        const org = this.props.organization;
        const header = <header className="dropdown-header">
            <OrganizationInvitationForm organization={this.props.organization} hideTitle={true}/>
        </header>;

        return <article className="members-tab">
            {
                org.admin &&
                <section className="add-member">
                    <DropDownMenu header={header} className="action-header"/>
                </section>
            }
            <section>

                <ul className="members-list undecorated-list flex-col">
                    {
                        !this.state.isLoading && this.props.members && this.props.members.map(member => {
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



