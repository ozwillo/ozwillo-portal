import React from "react";
import renderIf from "render-if";

const Form = ({ id, onSubmit, children }) =>
    <form id={id} onSubmit={onSubmit} className="form-horizontal">
        {children}
    </form>

Form.propTypes = {
    id: React.PropTypes.string.isRequired,
    onSubmit: React.PropTypes.func.isRequired
}

const InputText = ({ name, value, label, onChange, labelClassName, divClassName, error, errorMsg, isRequired }) =>
    <div className={error ? 'form-group has-error' : 'form-group'}>
        <label htmlFor={name} className={isRequired ? labelClassName + ' required' : labelClassName}>
            {label} {isRequired ? '* ' : ' '}
        </label>
        <div className={divClassName}>
            <input className="form-control" name={name} type="text" value={value} onChange={onChange} />
            {renderIf(error && errorMsg)(
                <span className="help-block">{errorMsg}</span>
            )}
        </div>
    </div>

InputText.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3'
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
    isRequired: React.PropTypes.bool
}

const Select = ({ name, value, label, onChange, children, labelClassName, divClassName, error, errorMsg, isRequired }) =>
    <div className={error ? 'form-group has-error' : 'form-group'}>
        <label htmlFor={name} className={isRequired ? labelClassName + ' required' : labelClassName}>
            {label} {isRequired ? '* ' : ' '}
        </label>
        <div className={divClassName}>
            <select name={name} value={value} onChange={onChange} className="form-control">
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
    isRequired: React.PropTypes.bool
}

Select.defaultProps = {
    divClassName: 'col-sm-7',
    labelClassName: 'control-label col-sm-3'
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

module.exports = { Form, InputText, Select, SubmitButton }
