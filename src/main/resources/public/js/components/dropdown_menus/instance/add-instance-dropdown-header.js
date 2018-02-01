import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class AddInstanceDropdownHeader extends React.Component {

    static propTypes = {
        instances: PropTypes.array.isRequired,
        instance: PropTypes.object.isRequired,
        onAddInstance: PropTypes.func.isRequired,
        onChangeInstance: PropTypes.func.isRequired,
        onUpdateInstance: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            options: this.createOptions(this.props.instances)
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.onCheckboxChange = this.onCheckboxChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    createOptions(instances) {
        const options = [];

        instances.forEach((instance) => {
            options.push({
                value: instance.id,
                label: instance.name
            });
        });

        return options;
    }

    componentWillReceiveProps(nextProps){
        this.setState({
            options: this.createOptions(nextProps.instances)
        });
    }

    onOptionChange(selectedOption) {
        this.props.onChangeInstance(selectedOption.value)
    }

    onCheckboxChange() {
        this.props.onUpdateInstance({ isPublic: !this.props.instance.isPublic });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onAddInstance();
    }

    render() {
        const instance = this.props.instance;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    className="select"
                    name="app"
                    value={instance.id}
                    onChange={this.onOptionChange}
                    options={this.state.options}
                    clearable={false}
                    placeholder="Instances"/>

                <div className="options flex-row end">
                    <label>
                        Public
                        <input className="field" name="isPublic" type="checkbox"
                               checked={instance.isPublic} onChange={this.onCheckboxChange}/>
                    </label>
                    <button type="submit" className="btn icon"><i className="fa fa-plus option-icon"/></button>
                </div>
            </form>
        </header>;
    }
}

export default AddInstanceDropdownHeader;