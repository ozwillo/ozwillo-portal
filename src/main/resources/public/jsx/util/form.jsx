import React from "react";
import renderIf from "render-if";
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

const Form = ({ id, onSubmit, children }) =>
    <form id={id} onSubmit={onSubmit} className="form-horizontal">
        {children}
    </form>

Form.propTypes = {
    id: React.PropTypes.string.isRequired,
    onSubmit: React.PropTypes.func.isRequired
}

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
    </div>

InputText.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    disabled: false
}

InputText.propTypes = {
    name: React.PropTypes.string.isRequired,
    value: React.PropTypes.string,
    label: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired,
    labelClassName: React.PropTypes.string,
    divClassName: React.PropTypes.string,
    error: React.PropTypes.bool,
    errorMsg: React.PropTypes.string,
    isRequired: React.PropTypes.bool,
    disabled: React.PropTypes.bool
}

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
    </div>

Select.propTypes = {
    name: React.PropTypes.string.isRequired,
    value: React.PropTypes.string,
    label: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired,
    labelClassName: React.PropTypes.string,
    divClassName: React.PropTypes.string,
    error: React.PropTypes.bool,
    errorMsg: React.PropTypes.string,
    isRequired: React.PropTypes.bool,
}

Select.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    emptyValue: true
}

const SubmitButton = ({ label, className }) =>
    <div className="form-group">
        <div className="col-sm-9 col-sm-offset-3">
            <button type="submit" className={className + ' btn oz-btn-save'}>{label}</button>
        </div>
    </div>

SubmitButton.propTypes = {
    label: React.PropTypes.string.isRequired,
    className: React.PropTypes.string
}


const InputDatePicker = ({ label, labelClassName, divClassName, name, startDate, onChange, dropdownMode }) =>
    <div className='form-group'>
        <label className={labelClassName}>
            {label}
        </label>
        <div className={divClassName}>
            <DatePicker selected={startDate}
                        dateFormat="DD/MM/YYYY"
                        onChange={onChange}
                        dropdownMode={dropdownMode}
                        peekNextMonth
                        showMonthDropdown
                        showYearDropdown
                        className="form-control" name={name} />
        </div>
    </div>

InputDatePicker.propTypes = {
    name: React.PropTypes.string.isRequired,
    value: React.PropTypes.string,
    dropdownMode: React.PropTypes.string,
    label: React.PropTypes.string.isRequired,
    startDate: React.PropTypes.object,
    labelClassName: React.PropTypes.string,
    divClassName: React.PropTypes.string,
    onChange: React.PropTypes.func
}

InputDatePicker.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    dropdownMode: 'select'
}

class CountrySelector extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            countries: []
        }
    }

    propTypes = {
        value: React.PropTypes.string,
        url: React.PropTypes.string.isRequired,
        onChange: React.PropTypes.func.isRequired,
        label: React.PropTypes.string
    }

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

module.exports = { Form, InputText, Select, SubmitButton, InputDatePicker, CountrySelector}
