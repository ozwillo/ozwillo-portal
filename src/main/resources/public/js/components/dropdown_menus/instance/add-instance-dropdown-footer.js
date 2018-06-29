import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class AddInstanceDropdownFooter extends React.Component {

    static propTypes = {
        members: PropTypes.array.isRequired,
        onAddMember: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this)
        this.onAddMember = this.onAddMember.bind(this);
    }

    onOptionChange(selectedOption) {
        this.setState({selectedOption});
    }

    onAddMember(e) {
        e.preventDefault();

        if (!this.state.selectedOption) {
            return;
        }

        this.setState({selectedOption: null});
        this.props.onAddMember(this.state.selectedOption);
    }

    render() {
        return <footer className="dropdown-footer flex-row">
            <Select
                className="select"
                name="members"
                value={this.state.selectedOption}
                onChange={this.onOptionChange}
                options={this.props.members}
                labelKey="name"
                placeholder="Members"/>

            <div className="options flex-row end">
                <button className="btn icon" onClick={this.onAddMember}>
                    <i className="fa fa-user-plus option-icon"/>
                </button>
            </div>
        </footer>;
    }
}

export default AddInstanceDropdownFooter;