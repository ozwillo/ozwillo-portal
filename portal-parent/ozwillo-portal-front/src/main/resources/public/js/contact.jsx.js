/** @jsx React.DOM */

var ContactLink = React.createClass({
    onClick: function(event) {
        this.refs.contactModal.open();
    },
    render: function () {
        return (
            <span>
                <a className="footer-link" href="#">
                    <span onClick={this.onClick}>{t('footer.contact')}</span>
                </a>
                <ContactModal ref="contactModal" />
            </span>
        )
    }
});

var ContactModal = React.createClass({
    componentDidMount: function () {
        $(this.getDOMNode()).modal({show: false});
        $(this.getDOMNode()).on("shown.bs.modal", function() {
            $("input#motive", this).focus();
        });
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).off('hidden');
    },
    close: function (event) {
        $(this.getDOMNode()).modal('hide');
    },
    open: function () {
        $(this.getDOMNode()).modal('show');
        this.refs.contactForm.resetFormValues();
    },
    render: function () {
        return (
            <div className="modal fade" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className="modal-dialog modal-lg">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"} /></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{t('contact.title')}</h4>
                        </div>
                        <ContactForm ref="contactForm" cancelHandler={this.close} />
                    </div>
                </div>
            </div>
        );
    }
});

var ContactForm = React.createClass({
    getInitialState: function () {
        return {
            errors: [],
            formSent: false,
            sending: false,
            fields: {
                motive: '',
                subject: '',
                body: '',
                copyToSender: false
            }
        };
    },
    resetFormValues: function() {
        this.setState({
            errors: [],
            fields: {
                motive: '',
                subject: '',
                body: '',
                copyToSender: false
            }
        });
    },
    closeModal: function(){
        this.props.cancelHandler();
        this.setState(this.getInitialState());
    },
    handleChange: function(field, value) {
        var fields = this.state.fields;
        fields[field] = value;
        this.setState({ fields: fields });
    },
    sendForm: function (event) {
        if (event) { event.preventDefault(); }
        if (this.state.sending) { return; } /* do nothing if we're already sending ... */

        var errors = [];
        if (!this.state.fields.motive) { errors.push("motive"); }
        if (!this.state.fields.subject) { errors.push("subject"); }
        if (!this.state.fields.body) { errors.push("body"); }

        if (errors.length == 0) {
            this.setState({sending: true});

            $.ajax({
                url: contact_service + '/send',
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify({
                    motive: this.state.fields.motive,
                    subject: this.state.fields.subject,
                    body: this.state.fields.body,
                    copyToSender: this.state.fields.copyToSender
                }),
                success: function (data) {
                    if (!data.error) {
                        this.setState({ sending: false, formSent: true, errors: [] });
                    } else {
                        this.setState({ sending: false, errors: ['technical'] });
                    }
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                    this.setState({ sending: false, errors: ['technical'] });
                }.bind(this)
            });
        } else {
            this.setState({ errors: errors });
        }
    },
    renderLabel: function(htmlFor, class_name, label){
        return (
            <label htmlFor={htmlFor} className="col-sm-3 control-label required">{label} * </label>
        );
    },
    renderSendingResult: function() {
        if (this.state.formSent) {
            return (
                <div className="alert alert-success text-center">{t('contact.form-sent')}</div>
            )
        }
    },
    renderGeneralErrorMessage: function() {
        if ($.inArray('technical', this.state.errors) != -1) {
            return (
                <div className="alert alert-danger text-center">{t('contact.technical-problem')}</div>
            )
        }
    },
    render: function () {
        var motiveOptions = [
            { value: "contact.form.motive.question", label: t('contact.form.motive.question')},
            { value: "contact.form.motive.feedback", label: t('contact.form.motive.feedback')},
            { value: "contact.form.motive.application-problem", label: t('contact.form.motive.application-problem')},
            { value: "contact.form.motive.other-problem", label: t('contact.form.motive.other-problem')},
            { value: "contact.form.motive.other", label: t('contact.form.motive.other')}
        ];
        return (
            <div>
                <div className="modal-body">
                    <form onSubmit={this.sendForm} className="form-horizontal">
                        <div className="form-group">
                            <ContactSelectField onChange={this.handleChange} renderLabel={this.renderLabel}
                                                name="motive" value={this.state.fields.motive} options={motiveOptions}
                                                errors={this.state.errors} />
                            <ContactTextInputField onChange={this.handleChange} renderLabel={this.renderLabel}
                                                   name="subject" value={this.state.fields.subject}
                                                   errors={this.state.errors} />
                            <ContactTextareaField onChange={this.handleChange} renderLabel={this.renderLabel}
                                                  name="body" value={this.state.fields.body}
                                                  errors={this.state.errors} />
                            <ContactCheckboxField onChange={this.handleChange} renderLabel={this.renderLabel}
                                                  name="copyToSender" labelName="copy-to-sender" value={this.state.fields.copyToSender}
                                                  errors={this.state.errors} />
                        </div>
                    </form>
                    {this.renderSendingResult()}
                    {this.renderGeneralErrorMessage()}
                </div>
                <Buttons cancelHandler={this.props.cancelHandler}
                         sendFormHandler={this.sendForm}
                         closeHandler={this.closeModal}
                         formSent={this.state.formSent} />
            </div>
        );
    }
});

var ContactSelectField = React.createClass({
    handleChange: function(event) {
        this.props.onChange(this.props.name, event.target.value);
    },
    render: function() {
        var options = this.props.options.map(function(option, index) {
            return <option key={index + 1} value={option.value}>{option.label}</option>;
        });
        var divClassName = ($.inArray(this.props.name, this.props.errors) != -1 ? 'form-group has-error' : 'form-group');

        return (
            <div className={divClassName}>
                {this.props.renderLabel(this.props.name, this.props.name, t('contact.form.' + this.props.name))}
                <div className="col-sm-8">
                    <select name={this.props.name} id={this.props.name} className="form-control" value={this.props.value} onChange={this.handleChange}>
                        <option key={0} value=""></option>
                        {options}
                    </select>
                </div>
            </div>
        );
    }
});

var ContactTextInputField = React.createClass({
    handleChange: function(event) {
        this.props.onChange(this.props.name, event.target.value);
    },
    render: function() {
        var divClassName = ($.inArray(this.props.name, this.props.errors) != -1 ? 'form-group has-error' : 'form-group');

        return (
            <div className={divClassName}>
                {this.props.renderLabel(this.props.name, this.props.name, t('contact.form.' + this.props.name))}
                <div className="col-sm-8">
                    <input type="text" name={this.props.name} id={this.props.name} className="form-control"
                           value={this.props.value} onChange={this.handleChange} />
                </div>
            </div>
        );
    }
});

var ContactTextareaField = React.createClass({
    handleChange: function(event) {
        this.props.onChange(this.props.name, event.target.value);
    },
    render: function() {
        var divClassName = ($.inArray(this.props.name, this.props.errors) != -1 ? 'form-group has-error' : 'form-group');

        return (
            <div className={divClassName}>
                {this.props.renderLabel(this.props.name, this.props.name, t('contact.form.' + this.props.name))}
                <div className="col-sm-8">
                    <textarea name={this.props.name} id={this.props.name} rows="10" cols="5" className="form-control"
                              value={this.props.value} onChange={this.handleChange}></textarea>
                </div>
            </div>
        );
    }
});

var ContactCheckboxField = React.createClass({
    handleChange: function(event) {
        this.props.onChange(this.props.name, event.target.checked);
    },
    render: function() {
        var divClassName = ($.inArray(this.props.name, this.props.errors) != -1 ? 'form-group has-error' : 'form-group');

        return (
            <div className={divClassName}>
                {this.props.renderLabel(this.props.labelName, this.props.labelName, t('contact.form.' + this.props.labelName))}
                <div className="col-sm-8">
                    <input type="checkbox" checked={this.props.value? "checked":""} name={this.props.name} id={this.props.name}
                           onChange={this.handleChange} />
                </div>
            </div>
        );
    }
});

var Buttons = React.createClass({
    renderCancelButton: function() {
        if (!this.props.formSent)
            return <button type="button" key="cancel" className="btn oz-btn-cancel" onClick={this.props.cancelHandler}>{t('ui.cancel')}</button>
    },
    renderSendButton: function() {
        if (!this.props.formSent)
            return <button type="submit" key="send" className="btn oz-btn-save" onClick={this.props.sendFormHandler}>{t('ui.send')}</button>
    },
    renderCloseButton: function() {
        if (this.props.formSent)
            return <button type="button" key="close" className="btn btn-default" onClick={this.props.closeHandler}>{t('ui.close')}</button>
    },
    render: function() {
        return (
            <div className="modal-footer">
                {this.renderCancelButton()}
                {this.renderSendButton()}
                {this.renderCloseButton()}
            </div>
        );
    }
});

React.render(
    <ContactLink /> ,
    document.getElementById("contact")
);
