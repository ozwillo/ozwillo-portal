import React from 'react';
import PropTypes from 'prop-types';

//Components
import DropDownMenu from '../../dropdown-menu';
import ServiceInvitationForm from '../../forms/service-invitation-form';
import ServiceDropdownHeader from '../../dropdown_menus/service/service-dropdown-header';


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
            service: this.props.service
        };

        //bind methods
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onRemoveService = this.onRemoveService.bind(this);
        this.onUpdateService = this.onUpdateService.bind(this);
        this.filterMemberWithoutAccess = this.filterMemberWithoutAccess.bind(this);
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
        if(!this.state.service.users) {
            return true;
        }

        return !this.state.service.users.find((user) => {
            return user.id === member.id;
        })
    }

    render() {
        const isAdmin = this.props.isAdmin;
        const service = this.state.service;
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
                            return <li key={user.id}>

                                {
                                    user.id &&
                                    <article className="item flex-row">
                                        <p className="name">{`${user.name}`}</p>

                                        <div className="options">
                                            <button className="btn icon" onClick={this.onRemoveMember} data-member={i}>
                                                <i className="fa fa-trash option-icon delete"/>
                                            </button>
                                        </div>
                                    </article>
                                }

                                {
                                    !user.id &&
                                    <article className="item flex-row">
                                        <p className="name">{`${user.email}`}</p>

                                        <div className="options">
                                            <i className="fa fa-spinner fa-spin option-icon"/>

                                            <button className="btn icon" onClick={this.onRemoveMember} data-member={i}>
                                                <i className="fa fa-trash option-icon delete"/>
                                            </button>
                                        </div>
                                    </article>
                                }

                            </li>;
                        })
                    }
                </ul>
            </section>
        </DropDownMenu>;
    }
}

export default ServiceDropdown;
