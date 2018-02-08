import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';


class MemberDropdownFooter extends React.Component {

    static propTypes = {
        instances: PropTypes.array.isRequired,
        onAddAccessToInstance: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null
        };

        //binds methods
        this.onSubmit = this.onSubmit.bind(this);
        this.onOptionChange = this.onOptionChange.bind(this);
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddAccessToInstance(this.state.selectedOption);
    }

    onOptionChange(selectedOption) {
        this.setState({ selectedOption });
    }

    render() {
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={this.state.selectedOption}
                    onChange={this.onOptionChange}
                    options={this.props.instances}
                    clearable={false}
                    valueKey="id"
                    labelKey="name"
                    placeholder="Instances"/>

                <div className="options flex-row end">
                    <button type="submit" className="btn" className="btn-default">Send</button>
                </div>
            </form>
        </header>;
    }

}


export default MemberDropdownFooter;