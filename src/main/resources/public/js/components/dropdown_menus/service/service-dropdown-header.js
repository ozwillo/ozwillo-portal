import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class ServiceDropdownHeader extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired,
        onRemoveService: PropTypes.func.isRequired,
        onUpdateService: PropTypes.func.isRequired,
        onClickConfigIcon: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.onCheckboxChange = this.onCheckboxChange.bind(this);
        this.onClickConfigIcon = this.onClickConfigIcon.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    onCheckboxChange() {
        this.props.onUpdateService({ isPublic: !this.props.service.isPublic });
    }

    onSubmit(e) {
        e.preventDefault();
        this.props.onRemoveService(this.props.service);
    }

    onClickConfigIcon() {
        this.props.onClickConfigIcon(this.props.service);
    }

    render() {
        const service = this.props.service;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">{service.name}</span>

                <div className="options flex-row end">
                    <label>
                        Public
                        <input className="field" className="field" name="isPublic" type="checkbox"
                               checked={service.isPublic} onChange={this.onCheckboxChange}/>
                    </label>
                    <button type="button" className="btn icon" onClick={this.onClickConfigIcon}>
                        <i className="fa fa-cog option-icon"/>
                    </button>
                    <button type="submit" className="btn icon">
                        <i className="fa fa-trash option-icon delete"/>
                    </button>
                </div>
            </form>
        </header>;
    }
}

export default ServiceDropdownHeader;