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

const Select = ({ name, value, label, onChange, children, labelClassName, divClassName, error, errorMsg, isRequired, emptyValue }) =>
    <div className={error ? 'form-group has-error' : 'form-group'}>
        <label htmlFor={name} className={isRequired ? labelClassName + ' required' : labelClassName}>
            {label} {isRequired ? '* ' : ' '}
        </label>
        <div className={divClassName}>
            <select name={name} value={value} onChange={onChange} className="form-control">
                {renderIf(emptyValue)(
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
    emptyValue: React.PropTypes.bool
}

Select.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3',
    emptyValue: true
}

const SubmitButton = ({ label }) =>
    <div className="form-group">
        <div className="col-sm-9 col-sm-offset-3">
            <button type="submit" className="btn oz-btn-save">{label}</button>
        </div>
    </div>

SubmitButton.propTypes = {
    label: React.PropTypes.string.isRequired
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
    dropdownMode: 'scroll'
}


var CountrySelect = React.createClass({
    propTypes: {
        url: React.PropTypes.string.isRequired,
        onCountryChange: React.PropTypes.func.isRequired,
        countryUri: React.PropTypes.string
    },
    getInitialState: function() {
        return { countries: [] }
    },
    handleCountryChange: function(event) {
        var country_uri = event.target.value;
        var country = event.target.selectedOptions[0].label;
        this.props.onCountryChange(country, country_uri);
    },
    componentWillMount: function() {
        this.initUserCountry();
    },
    // TODO port the next two functions with promises
    initUserCountry: function() {
        // get countries from DC
        $.ajax({
            url: this.props.url,
            type: 'get',
            dataType: 'json',
            data: {q: ' '},
            success: function (data) {
                var areas = data.areas.filter(function (n) {
                    return n !== null;
                });
                var options = [{value: '', label: ''}];
                for (var i = 0; i < areas.length; i++) {
                    options.push({value: areas[i].uri, label: areas[i].name})
                }
                this.setState({ countries: options });
                this.getUserCountry();
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getUserCountry: function() {
        $.ajax({
            url: network_service + '/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                // Address is an optional field in user's profile
                if (data.address) {
                    // Try to match country with countries loaded from the DC
                    this.props.onCountryChange(data.address.country, this.getValue(data.address.country));
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getValue: function(label) {
        if (!label || label !== "") {
            for (var i = 0; i < this.state.countries.length; i++) {
                if (this.state.countries[i].label === label) {
                    return this.state.countries[i].value;
                }
            }
        }
        return null;
    },
    render: function() {
        var options = this.state.countries.map(function(country, index) {
            return <option key={index} value={country.value}>{country.label}</option>;
        });

        // the parameter "value=" is selected option. Default selected option can either be set here. Using browser-based function decodeURIComponent()
        return (
            <div className="form-group">
                <label htmlFor="country" className="col-sm-3 control-label required">
                    {t('search.organization.country')} *
                </label>
                <div className="col-sm-8">
                    <select className="form-control" id="country"
                            value={this.props.countryUri} onChange={this.handleCountryChange}>
                        {options}
                    </select>
                </div>
            </div>
        );
    }
});

module.exports = { Form, InputText, Select, SubmitButton, InputDatePicker }
