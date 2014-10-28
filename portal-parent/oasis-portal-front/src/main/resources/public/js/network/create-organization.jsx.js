/** @jsx React.DOM */

var CreateOrganization = React.createClass({
    componentDidMount: function() {
        $(this.refs.modal.getDOMNode()).on("shown.bs.modal", function() {
            $("input", this).first().focus();
        });
    },
    getInitialState: function(){
        return { organization: {name: '', type: ''}, errors: [], saving: false };
    },
    saveOrganization: function() {
        if (this.state.saving) {
            return; // do nothing if we're already saving...
        }
        var org = this.state.organization;
        var errors = [];
        if (org.name.trim() == '') {
            errors.push("name");
        }
        if (org.type.trim() == '') {
            errors.push("type");
        }

        if (errors.length == 0) {
            this.state.saving = true;
            this.setState(this.state);
            $.ajax({
                url: network_service + "/create-organization",
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify(this.state.organization),
                success: function() {
                    if (this.props.successHandler) {
                        this.props.successHandler();
                    }
                    this.refs.modal.close();
                }.bind(this),
                error: function(xhr, status, err) {
                    console.error(status, err.toString());
                    var state = this.state;
                    state.errors = ["general"];
                    state.saving = false;
                    this.setState(state);
                }.bind(this)
            });
        } else {
            this.state.errors = errors;
            this.setState(this.state);
        }
    },
    changeInput: function(fieldname) {
        return function(event) {
            var org = this.state.organization;
            org[fieldname] = event.target.value;
            this.setState({organization: org, errors: [], saving: false});
        }.bind(this);

    },
    toggleType: function(event) {
        var org = this.state.organization;
        org.type = event.target.value;
        this.setState({organization: org, errors: [], saving: false});
    },
    show: function() {
        this.setState(this.getInitialState());
        this.refs.modal.open();
    },
    render: function() {
        var nameClassName = ($.inArray('name', this.state.errors) != -1 ? 'error' : '');
        var typeClassName = ($.inArray('type', this.state.errors) != -1 ? 'error' : '');
        var errorMessage = ($.inArray('general', this.state.errors) != -1) ? <p className="alert alert-danger" role="alert">{t('ui.general-error')}</p> : null;
        return (
            <div>
                <Modal ref="modal" title={t('find-or-create-organization')} successHandler={this.saveOrganization}>
                    <form onSubmit={this.saveOrganization}>
                        <div className="form-group">
                            <label htmlFor="organization-name" className={nameClassName}>{t('organization-name')}</label>
                            <input type="text" className="form-control" value={this.state.organization.name} onChange={this.changeInput('name')} placeholder={t('organization-name')}/>
                        </div>
                        <div className="form-group">
                            <label htmlFor="organization-type" className={typeClassName}>{t('organization-type')}</label>
                            <div className="radio">
                                <label>
                                    <input type="radio" value="PUBLIC_BODY" checked={this.state.organization.type == 'PUBLIC_BODY'} onChange={this.toggleType}>{t('organization-type.PUBLIC_BODY')}</input>
                                </label>
                            </div>
                            <div className="radio">
                                <label>
                                    <input type="radio" value="COMPANY" checked={this.state.organization.type == 'COMPANY'} onChange={this.toggleType}>{t('organization-type.COMPANY')}</input>
                                </label>
                            </div>
                        </div>
                        {errorMessage}
                    </form>
                </Modal>
            </div>
            );
    }
});