import React from 'react';
import PropTypes from 'prop-types';

//Components
import DropDownMenu from '../../dropdown-menu';
import ServiceInvitationForm from '../../forms/service-invitation-form';
import ServiceDropdownHeader from '../../dropdown_menus/service/service-dropdown-header';


class ServiceDropdown extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired
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
            return user.userid === member.id;
        })
    }

    render() {
        const service = this.state.service;
        const membersWithoutAccess = this.props.members.filter(this.filterMemberWithoutAccess);

        const Header = <ServiceDropdownHeader
                            service={service}
                            onClickConfigIcon={this.onClickConfigIcon}
                            onRemoveService={this.onRemoveService}
                            onUpdateService={this.onUpdateService}/>;

        const Footer = (!service.isPublic && <footer>
            <ServiceInvitationForm members={membersWithoutAccess} service={this.props.service}/>
        </footer>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!service.isPublic}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        service.users && service.users.map((user, i) => {
                            return <li key={user.userid}>
                                <article className="item flex-row">
                                    <p className="name">{`${user.fullname}`}</p>

                                    <div className="options">
                                        <button className="btn icon" onClick={this.onRemoveMember} data-member={i}>
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

export default ServiceDropdown;
