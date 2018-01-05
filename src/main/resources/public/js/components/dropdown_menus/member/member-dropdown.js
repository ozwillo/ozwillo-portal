import React from 'react';
import PropTypes from 'prop-types';

//Action
import DropDownMenu from '../../dropdown-menu';
import MemberDropdownHeader from './member-dropdown-header';
import MemberDropdownFooter from './member-dropdown-footer';

class MemberDropdown extends React.Component {

    static propTypes = {
        member: PropTypes.object.isRequired,
        organization: PropTypes.object.isRequired,
        services: PropTypes.array.isRequired
    };

    constructor(props) {
        super(props);

        //bind methods
        this.addServiceIconToDesktop = this.addServiceIconToDesktop.bind(this);
        this.removeAccessToService = this.removeAccessToService.bind(this);
        this.addAccessToService = this.addAccessToService.bind(this);
        this.removeMemberInOrganization = this.removeMemberInOrganization.bind(this);
        this.sendInvitation = this.sendInvitation.bind(this);
    }

    addServiceIconToDesktop() {
        console.log('addServiceIconToDesktop')
    }

    removeAccessToService() {
        console.log('removeAccessToService')
    }

    addAccessToService() {
        console.log('addAccessToService')
    }

    removeMemberInOrganization() {
        console.log('removeMemberInOrganization')
    }

    sendInvitation() {
        console.log('sendInvitation')
    }

    render() {
        const member = this.props.member;
        const organization = this.props.organization;
        const services = this.props.services;

        const Header = <MemberDropdownHeader member={member}
                                             organization={organization}
                                             onRemoveMemberInOrganization={this.removeMemberInOrganization}
                                             onSendInvitation={this.sendInvitation}/>;
        const Footer = <MemberDropdownFooter member={member}
                                             services={services}
                                             onAddAccessToService={this.addAccessToService}/>

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!member.isPending}>
            <section className='dropdown-content'>
                <ul className="list undecorated-list flex-col">
                    {
                        member.services.map((service, i) => {
                            return <li key={service.id}>
                                <article className="item flex-row">
                                    <p className="name">{service.name}</p>

                                    <div className="options flex-row">
                                        <button className="btn icon" onClick={this.addServiceIconToDesktop} data-member={i}>
                                            <i className="fa fa-desktop option-icon"/>
                                        </button>
                                        <button className="btn icon" onClick={this.removeAccessToService} data-member={i}>
                                            <i className="fa fa-trash option-icon delete"/>
                                        </button>
                                    </div>
                                </article>
                            </li>;
                        })
                    }
                </ul>
            </section>
        </DropDownMenu>
    }
}

export default MemberDropdown;