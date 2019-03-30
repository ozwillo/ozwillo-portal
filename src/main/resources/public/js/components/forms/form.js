import React from "react";
import PropTypes from 'prop-types';
import renderIf from "render-if";
import DatePicker from 'react-datepicker';
import Select from 'react-select';

import { i18n } from "../../config/i18n-config"
import { t } from "@lingui/macro"

const Form = ({id, onSubmit, children}) =>
    <form id={id} onSubmit={onSubmit} className="oz-form">
        {children}
    </form>;

Form.propTypes = {
    id: PropTypes.string.isRequired,
    onSubmit: PropTypes.func.isRequired
};

const Label = ({ children, required, ...rest }) =>
    <label {...rest}>{children}{required && ' *'}</label>

const InputText = ({name, value, label, onChange, labelClassName, divClassName, error, errorMsg, isRequired, disabled}) =>
    <div className={`flex-row ${(error && 'has-error') || ''}`}>
        <Label htmlFor={name} required={isRequired} className={`label ${labelClassName} ${(isRequired && 'required') || ''}`}>
            {label}
        </Label>
        <input className="form-control field" name={name} type="text" value={value} onChange={onChange}
               disabled={disabled}/>
        {
            renderIf(error && errorMsg)(
                <span className="help-block">{errorMsg}</span>
            )
        }
    </div>;

InputText.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    disabled: false,
    value: ''
};
``

InputText.propTypes = {
    name: PropTypes.string.isRequired,
    value: PropTypes.string,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    labelClassName: PropTypes.string,
    divClassName: PropTypes.string,
    error: PropTypes.bool,
    errorMsg: PropTypes.string,
    isRequired: PropTypes.bool,
    disabled: PropTypes.bool
};

const SubmitButton = ({label, className}) =>
    <div className="flex-row">
        <button type="submit" className={`submit btn btn-submit ${className || ''}`}>{label}</button>
    </div>

SubmitButton.propTypes = {
    label: PropTypes.string.isRequired,
    className: PropTypes.string
};


const InputDatePicker = ({label, name, value, onChange, dropdownMode, disabled, required = false}) =>
    <div className='flex-row'>
        <Label htmlFor={name} className='label' required={required}>
            {label}
        </Label>
        <div className="field">
            <DatePicker selected={value}
                        onChange={onChange}
                        dropdownMode={dropdownMode}
                        peekNextMonth
                        showMonthDropdown
                        showYearDropdown
                        className="form-control"
                        name={name}
                        disabled={disabled} />
        </div>
    </div>;

InputDatePicker.propTypes = {
    name: PropTypes.string.isRequired,
    value: PropTypes.object,
    dropdownMode: PropTypes.string,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func,
    disabled: PropTypes.bool
};

InputDatePicker.defaultProps = {
    dropdownMode: 'select',
    disabled: false
};

class CountrySelector extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            countries: []
        }
    }

    static propTypes = {
        value: PropTypes.string,
        url: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,
        label: PropTypes.string,
        required: PropTypes.bool
    };
    static defaultProps = {
        required: false
    };

    componentWillMount() {
        // get countries from DC
        $.ajax({
            url: this.props.url,
            type: 'get',
            dataType: 'json',
            data: {q: ' '}
        }).done(data => {
            let options = data.areas
                .filter(n => n !== null)
                .map((area, key) => Object.assign({}, {key: key, value: area.uri, label: area.name}))

            this.setState({countries: options});
        })
            .fail((xhr, status, err) => {
                    this.setState({error: "Unable to retrieve countries info" + err.toString()})
                }
            )
    }

    render() {
        return (
            <div className='flex-row'>
                <Label htmlFor="gender" className="label" required={this.props.required}>
                    {i18n._(t`my.profile.personal.country`)}
                </Label>
                <Select name="country" value={this.props.value} placeholder=""
                        onChange={this.props.onChange} className="select field"
                        options={this.state.countries}/>
            </div>
        )
    }
}

class GenderSelector extends React.Component {
    static propTypes = {
        value: PropTypes.string,
        onChange: PropTypes.func.isRequired,
        required: PropTypes.bool
    };
    static defaultProps = {
        required: false
    };

    constructor(props) {
        super(props);

        this.state = {
            options: this.createOptions()
        };

        this.createOptions = this.createOptions.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            options: this.createOptions()
        });
    }

    createOptions() {
        return [{
            label: i18n._(t`my.profile.personal.gender.male`),
            value: 'male'
        }, {
            label: i18n._(t`my.profile.personal.gender.female`),
            value: 'female'
        }];
    }

    render() {
        return <div className='flex-row'>
            <Label htmlFor="gender" className="label" required={this.props.required}>
                {i18n._(t`my.profile.personal.gender`)}
            </Label>
            <Select name="gender" value={this.props.value} placeholder=""
                    clearable={false} options={this.state.options}
                    onChange={option => this.props.onChange(option.value)} className="select field"/>
        </div>;
    }
}

export {Form, InputText, SubmitButton, InputDatePicker, CountrySelector, GenderSelector, Label}
