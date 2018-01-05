import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';


class MemberDropdownFooter extends React.Component {

    static propTypes = {
        services: PropTypes.array.isRequired,
        onAddAccessToService: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null
        };

        //binds methods
        this.onSubmit = this.onSubmit.bind(this);
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddAccessToService();
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
                    options={this.props.services}
                    clearable={false}
                    valueKey="id"
                    labelKey="name"
                    placeholder="Services"/>

                <div className="options flex-row end">
                    <button type="submit" className="btn" className="btn-default">Send</button>
                </div>
            </form>
        </header>;
    }

}


export default MemberDropdownFooter;