import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Components
import Select from 'react-select';
import DropDownMenu from "../dropdown-menu";

//Actions
import {} from "../../actions/service";


class CreateServiceDropdownMenuHeader extends React.Component {

    static propTypes = {
        services: PropTypes.array.isRequired,
        onAddService: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: '',
            isPublic: true,
            options: this.createOptions(this.props.services)
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.onCheckboxChange = this.onCheckboxChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    createOptions(services) {
        const options = [ { value: '', label: 'Services' }];

        services.forEach((service) => {
            options.push({
                value: service.name,
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
        this.setState({ selectedOption: selectedOption.value });
    }

    onCheckboxChange() {
        this.setState({ isPublic: !this.state.isPublic });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddService();
    }

    render() {
        return <header className="create-service-header">
            <form ref="form" className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={this.state.selectedOption}
                    onChange={this.onOptionChange}
                    options={this.state.options}
                    clearable={false}/>

                <div className="options flex-row end">
                    <label className="is-public">
                        Public
                        <input className="field" name="isPublic" type="checkbox" value={this.state.isPublic}/>
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
            selectedOption: '',
            options: this.createOptions(this.props.members)
        };

        //bind methods
        this.createOptions = this.createOptions.bind(this);
        this.onOptionChange = this.onOptionChange.bind(this)
        this.onSubmit = this.onSubmit.bind(this);
    }

    createOptions(members) {
        const options = [ { value: '', label: 'Members' }];

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

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddMember();
    }

    render() {
        return <footer className="create-service-footer">
            <form ref="form" className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={this.state.selectedOption}
                    onChange={this.onOptionChange}
                    options={this.state.options}
                    clearable={false}/>

                <div className="options flex-row end">
                    <button className="btn icon"><i className="fa fa-user-plus add-icon"/></button>
                </div>
            </form>
        </footer>;
    }
}

class CreateServiceDropdownMenu extends React.Component {

    constructor(props) {
        super(props);

        //bind methods
        this.onAddService = this.onAddService.bind(this);
    }

    onAddService(service) {
        console.log('TODO onAddService :', service);
    }

    onAddMember(member) {
        console.log('TODO onAddMember :', member);
    }

    render() {
        return <DropDownMenu
            header={<CreateServiceDropdownMenuHeader
                        services={this.props.services}
                        onAddService={this.onAddService}/>}
            footer={<CreateServiceDropdownMenuFooter
                        members={this.props.members}
                        onAddMember={this.onAddMember}/>}>
            <h3>Services...</h3>
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
