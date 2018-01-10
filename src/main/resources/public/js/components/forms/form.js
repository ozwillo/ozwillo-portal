import React from "react";
import PropTypes from 'prop-types';
import renderIf from "render-if";
import DatePicker from 'react-datepicker';

const Form = ({ id, onSubmit, children }) =>
    <form id={id} onSubmit={onSubmit} className="form-horizontal">
        {children}
    </form>;

Form.propTypes = {
    id: PropTypes.string.isRequired,
    onSubmit: PropTypes.func.isRequired
};

const InputText = ({ name, value, label, onChange, labelClassName, divClassName, error, errorMsg, isRequired, disabled }) =>
    <div className={error ? 'form-group has-error' : 'form-group'}>
        <label htmlFor={name} className={isRequired ? labelClassName + ' required' : labelClassName}>
            {label} {isRequired ? '* ' : ' '}
        </label>
        <div className={divClassName}>
            <input className="form-control" name={name} type="text" value={value} onChange={onChange} disabled={disabled} />
            {renderIf(error && errorMsg)(
                <span className="help-block">{errorMsg}</span>
            )}
        </div>
    </div>;

InputText.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    disabled: false,
    value: ''
};

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

const Select = ({ name, value, label, onChange, children, labelClassName, divClassName, error, errorMsg, isRequired }) =>
    <div className={error ? 'form-group has-error' : 'form-group'}>
        <label htmlFor={name} className={isRequired ? labelClassName + ' required' : labelClassName}>
            {label} {isRequired ? '* ' : ' '}
        </label>
        <div className={divClassName}>
            <select name={name} value={value} onChange={onChange} className="form-control">
                {renderIf(!isRequired)(
                    <option key="" value=""></option>
                )}
                {children}
            </select>
        </div>
    </div>;

Select.propTypes = {
    name: PropTypes.string.isRequired,
    value: PropTypes.string,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    labelClassName: PropTypes.string,
    divClassName: PropTypes.string,
    error: PropTypes.bool,
    errorMsg: PropTypes.string,
    isRequired: PropTypes.bool,
};

Select.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    emptyValue: true,
    value: ''
};

const SubmitButton = ({ label, className }) =>
    <div className="form-group">
        <div className="col-sm-9 col-sm-offset-3">
            <button type="submit" className={className + ' btn oz-btn-save'}>{label}</button>
        </div>
    </div>

SubmitButton.propTypes = {
    label: PropTypes.string.isRequired,
    className: PropTypes.string
};


const InputDatePicker = ({ label, labelClassName, divClassName, name, startDate, onChange, dropdownMode }) =>
    <div className='form-group'>
        <label className={labelClassName}>
            {label}
        </label>
        <div className={divClassName}>
            <DatePicker selected={startDate}
                        onChange={onChange}
                        dropdownMode={dropdownMode}
                        peekNextMonth
                        showMonthDropdown
                        showYearDropdown
                        className="form-control" name={name} />
        </div>
    </div>;

InputDatePicker.propTypes = {
    name: PropTypes.string.isRequired,
    value: PropTypes.string,
    dropdownMode: PropTypes.string,
    label: PropTypes.string.isRequired,
    startDate: PropTypes.object,
    labelClassName: PropTypes.string,
    divClassName: PropTypes.string,
    onChange: PropTypes.func
};

InputDatePicker.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    dropdownMode: 'select'
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
        label: PropTypes.string
    };

    componentWillMount () {
        // get countries from DC
        $.ajax({
            url: this.props.url,
            type: 'get',
            dataType: 'json',
            data: {q: ' '}
        })
            .done(data => {

                let options = data.areas
                    .filter(n => n !== null)
                    .map((area, key) => Object.assign({}, {key:key ,value: area.uri, label: area.name}))

                this.setState({ countries: options });
            })
            .fail((xhr, status, err) => {
                    this.setState({ error : "Unable to retrieve countries info" + err.toString() })
                }
            )
    }
    render() {
        return (
            <Select name="country" value={this.props.value}
                    onChange={this.props.onChange}
                    label={this.props.label}>
                {this.state.countries.map(option =>
                    <option key={option.key} value={option.value}>{option.label}</option>)
                }
            </Select>
        )
    }
}

class GenderSelector extends React.Component {
    static propTypes = {
        value: PropTypes.string,
        onChange: PropTypes.func.isRequired
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        const genders = ['male','female']
        return (
            <Select name="gender" value={this.props.value}
                    label={this.context.t('my.profile.personal.gender')}
                    onChange={this.props.onChange}>
                {
                    genders.map(option =>
                        <option key={option} value={option}>{this.context.t('my.profile.personal.gender.' + option)}</option>
                    )
                }
            </Select>
        )
    }
}

export { Form, InputText, Select, SubmitButton, InputDatePicker, CountrySelector, GenderSelector }
