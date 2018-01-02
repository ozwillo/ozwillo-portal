import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class AddServiceDropdownFooter extends React.Component {

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

export default AddServiceDropdownFooter;