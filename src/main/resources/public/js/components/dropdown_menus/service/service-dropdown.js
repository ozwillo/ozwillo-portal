import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

//Components
import DropDownMenu from '../../dropdown-menu';
import ServiceInvitationForm from '../../forms/service-invitation-form';
import ServiceDropdownHeader from '../../dropdown_menus/service/service-dropdown-header';


//action
import { fetchDeleteAcl } from "../../../actions/acl";

class ServiceDropdown extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired,
        isAdmin: PropTypes.bool
    };

    static defaultProps = {
        isAdmin: false
    };

    constructor(props){
        super(props);

        this.state = {
            error: null
        };

        //bind methods
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveService = this.onRemoveService.bind(this);
        this.onUpdateService = this.onUpdateService.bind(this);
        this.filterMemberWithoutAccess = this.filterMemberWithoutAccess.bind(this);
        this.removeUserAccessToService = this.removeUserAccessToService.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            service: nextProps.service,
            members: nextProps.members
        });
    }

    onClickConfigIcon(service) {
        console.log('onClickConfigIcon ', service);
    }

    onRemoveService(service) {
        console.log('onRemoveService ', service);
    }

    onUpdateService(service) {
        this.setState({ service: Object.assign({}, this.props.service, service)});
    }

    filterMemberWithoutAccess(member) {
        if(!this.props.service.users) {
            return true;
        }

        return !this.props.service.users.find((user) => {
            return user.id === member.id;
        })
    }

    removeUserAccessToService(e) {
        const i = e.currentTarget.dataset.member;
        const member = this.props.service.users[i];

        this.props.fetchDeleteAcl(member, this.props.service)
            .then(() => {
                this.setState({ error: null});
            })
            .catch((err) => {
                this.setState({
                    error: { memberIndex: i, message: err.error}
                });
            });
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const service = this.props.service;
        const membersWithoutAccess = this.props.members.filter(this.filterMemberWithoutAccess);

        const Header = <ServiceDropdownHeader
                            isAdmin={isAdmin}
                            service={service}
                            onClickConfigIcon={this.onClickConfigIcon}
                            onRemoveService={this.onRemoveService}
                            onUpdateService={this.onUpdateService}/>;

        const Footer = (isAdmin && !service.isPublic && <footer>
            <ServiceInvitationForm members={membersWithoutAccess} service={this.props.service}/>
        </footer>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={isAdmin && !service.isPublic}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        service.users && service.users.map((user, i) => {
                            return <li key={user.id || user.email}>
                                <article className="item flex-row">
                                    <p className="name">{`${(user.id && user.name) || user.email}`}</p>

                                    {
                                        this.state.error && i === this.state.error.memberIndex &&
                                        <span className="error">
                                            {this.state.error.message}
                                        </span>
                                    }

                                    <div className="options">
                                        {
                                            !user.id &&
                                            <i className="fa fa-spinner fa-spin option-icon"/>
                                        }

                                        <button className="btn icon" data-member={i}
                                                onClick={this.removeUserAccessToService}>
                                            <i className="fa fa-trash option-icon delete"/>
                                        </button>
                                    </div>
                                </article>
                            </li>;
                        })
                    }
                </ul>
            </section>
        </DropDownMenu>;
    }
}

const mapDispatchToProps = dispatch => {
    return {
        fetchDeleteAcl(user, service) {
            return dispatch(fetchDeleteAcl(user, service));
        }
    };
};

export default connect(null, mapDispatchToProps)(ServiceDropdown);
