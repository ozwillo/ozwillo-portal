import React from 'react';
import { connect } from 'react-redux';

//Components
import DropDownMenu from "../../dropdown-menu";
import AddServiceDropdownHeader from './add-service-dropdown-header';
import AddServiceDropdownFooter from './add-service-dropdown-footer';



class AddServiceDropdown extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            services: this.props.services,
            service: (this.props.services.length && this.props.services[0]) || this.defaultService
        };

        //bind methods
        this.onAddService = this.onAddService.bind(this);
        this.onAddMember = this.onAddMember.bind(this);
        this.onRemoveMember = this.onRemoveMember.bind(this);
        this.onUpdateService = this.onUpdateService.bind(this);
        this.onChangeService = this.onChangeService.bind(this);
        this.filterMembersWithoutAccess = this.filterMembersWithoutAccess.bind(this);
    }

   get defaultService() {
        return {
            isPublic: true,
            members: []
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            services: nextProps.services
        })
    }

    onAddService(service) {
        console.log('TODO onAddService :', service);
    }

    onAddMember(member) {
        const members = Object.assign([], this.state.service.members);
        members.push(member);
        this.setState({
            service: Object.assign({}, this.state.service, { members })
        });
    }

    onRemoveMember(e) {
        const memberIndex = parseInt(e.currentTarget.dataset.member, 10);
        const members = Object.assign([], this.state.service.members);
        members.splice(memberIndex, 1);
        this.setState({
            service: Object.assign({}, this.state.service, { members })
        });
    }

    onChangeService(id) {
        const service = this.state.services.find((service) => {
            return service.id === id;
        });


        this.setState({ service });
    }

    onUpdateService(service) {
        this.setState({
            service:  Object.assign({}, this.state.service, service)
        });
    }

    filterMembersWithoutAccess(member) {
        return !this.state.service.members.find((m) => {
            return member.id === m.id;
        });
    }

    render() {
        const services =  this.state.services;
        const service = this.state.service;
        const membersWithoutAccess = this.props.members.filter(this.filterMembersWithoutAccess);

        const Header = <AddServiceDropdownHeader
            services={services}
            service={service}
            onAddService={this.onAddService}
            onUpdateService={this.onUpdateService}
            onChangeService={this.onChangeService}/>;

        const Footer = (!service.isPublic &&
            <AddServiceDropdownFooter
                members={membersWithoutAccess}
                onAddMember={this.onAddMember}/>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!service.isPublic}>
            <section className='dropdown-content'>
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
        services: state.service.services,
        members: state.member.members
    };
};

const mapDispatchToProps = dispatch => {
    return {};
};


export default connect(mapStateToProps, mapDispatchToProps) (AddServiceDropdown);
