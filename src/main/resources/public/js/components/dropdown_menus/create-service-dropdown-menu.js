import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import Select from 'react-select';
import DropDownMenu from "../dropdown-menu";

//Actions
import {} from "../../actions/service";
import service from "../../reducers/service";


class CreateServiceDropdownMenuHeader extends React.Component {

    static propTypes = {
        services: PropTypes.array.isRequired,
        service: PropTypes.object.isRequired,
        onAddService: PropTypes.func.isRequired,
        onChangeService: PropTypes.func.isRequired,
        onUpdateService: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            options: this.createOptions(this.props.services)
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.onCheckboxChange = this.onCheckboxChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    createOptions(services) {
        const options = [];

        services.forEach((service) => {
            options.push({
                value: service.id,
                label: service.name
            });
        });

        return options;
    }

    componentWillReceiveProps(nextProps){
        this.setState({
            options: this.createOptions(nextProps.services)
        });
    }

    onOptionChange(selectedOption) {
        this.props.onChangeService(selectedOption.value)
    }

    onCheckboxChange() {
        this.props.onUpdateService({ isPublic: !this.props.service.isPublic });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddService();
    }

    render() {
        const service = this.props.service;
        return <header className="create-service-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={service.id}
                    onChange={this.onOptionChange}
                    options={this.state.options}
                    clearable={false}
                    placeholder="Services"/>

                <div className="options flex-row end">
                    <label className="is-public">
                        Public
                        <input className="field" name="isPublic" type="checkbox"
                               checked={service.isPublic} onChange={this.onCheckboxChange}/>
                    </label>
                    <button className="btn icon"><i className="fa fa-plus add-icon"/></button>
                </div>
            </form>
        </header>;
    }
}

class CreateServiceDropdownMenuFooter extends React.Component {

    static propTypes = {
        members: PropTypes.array.isRequired,
        onAddMember: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: 0,
            options: this.createOptions(this.props.members)
        };

        //bind methods
        this.createOptions = this.createOptions.bind(this);
        this.onOptionChange = this.onOptionChange.bind(this)
        this.onAddMember = this.onAddMember.bind(this);
    }

    createOptions(members) {
        const options = [];

        members.forEach((member) => {
            options.push({
                value: member.id,
                label: `${member.firstname} ${member.lastname}`
            });
        });

        return options;
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            options: this.createOptions(nextProps.members)
        })
    }

    onOptionChange(selectedOption) {
        this.setState({ selectedOption: selectedOption.value });
    }

    onAddMember(e) {
        e.preventDefault();

        if(!this.state.selectedOption) {
            return;
        }

        const member = this.props.members.find((member) => {
            return member.id === this.state.selectedOption;
        });

        this.props.onAddMember(member);
    }

    render() {
        return <footer className="create-service-footer flex-row">
            <Select
                className="select"
                name="app"
                value={this.state.selectedOption}
                onChange={this.onOptionChange}
                options={this.state.options}
                clearable={false}
                placeholder="Members" />

            <div className="options flex-row end">
                <button className="btn icon" onClick={this.onAddMember}>
                    <i className="fa fa-user-plus add-user-icon"/>
                </button>
            </div>
        </footer>;
    }
}

class CreateServiceDropdownMenu extends React.Component {

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

        const Header = <CreateServiceDropdownMenuHeader
            services={services}
            service={service}
            onAddService={this.onAddService}
            onUpdateService={this.onUpdateService}
            onChangeService={this.onChangeService}/>;

        const Footer = (!service.isPublic &&
                <CreateServiceDropdownMenuFooter
                    members={membersWithoutAccess}
                    onAddMember={this.onAddMember}/>) || null;

        return <DropDownMenu header={Header} footer={Footer} isAvailable={!service.isPublic}>
            <section className='create-service-content'>
                <ul className="member-list undecorated-list flex-col">
                    {
                        service.members.map((member, i) => {
                            return <li key={member.id}>
                                <article className="member flex-row">
                                    <p className="name">{`${member.firstname} ${member.lastname}`}</p>

                                    <div className="options">
                                        <button className="btn icon" onClick={this.onRemoveMember} data-member={i}>
                                            <i className="fa fa-trash delete-icon"/>
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


export default connect(mapStateToProps, mapDispatchToProps) (CreateServiceDropdownMenu);
