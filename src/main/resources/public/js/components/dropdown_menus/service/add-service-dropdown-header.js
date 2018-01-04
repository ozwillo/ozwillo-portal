import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class AddServiceDropdownHeader extends React.Component {

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
        return <header className="dropdown-header">
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
                    <label>
                        Public
                        <input className="field" name="isPublic" type="checkbox"
                               checked={service.isPublic} onChange={this.onCheckboxChange}/>
                    </label>
                    <button type="submit" className="btn icon"><i className="fa fa-plus option-icon"/></button>
                </div>
            </form>
        </header>;
    }
}

export default AddServiceDropdownHeader;