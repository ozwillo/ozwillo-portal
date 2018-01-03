import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import DropDownMenu from '../../dropdown-menu';
import ServiceInvitationForm from '../../service-invitation-form';
import ServiceDropdownHeader from '../../dropdown_menus/service/service-dropdown-header';


class ServiceDropdown extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired
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

    render() {
        const service = this.state.service;

        const Header = <ServiceDropdownHeader
                            service={service}
                            onClickConfigIcon={this.onClickConfigIcon}
                            onRemoveService={this.onRemoveService}
                            onUpdateService={this.onUpdateService}/>;

        const Footer = (!service.isPublic && <footer>
            <ServiceInvitationForm members={this.props.members} service={this.props.service}/>
        </footer>) || null;

        return <DropDownMenu header={Header} footer={Footer}>
            <section className='service-content'>
                <ul className="member-list undecorated-list flex-col">
                    {
                        service.members.map((member, i) => {
                            return <li key={member.id}>
                                <article className="member flex-row">
                                    <p className="name">{`${member.firstname} ${member.lastname}`}</p>

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

const mapStateToProps = state => {
    return {
        members: state.member.members
    }
};

export default connect(mapStateToProps)(ServiceDropdown);
