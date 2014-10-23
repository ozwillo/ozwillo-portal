/** @jsx React.DOM */


var Service = React.createClass({
    getInitialState: function() {
        return {
            service: this.props.service,
            saved_service: this.props.service,
            field_errors: []
        };
    },
    updateServiceLocally: function(fieldname, fieldvalue) {
        var state = this.state;
        state.service.service[fieldname] = fieldvalue;
        this.setState(state);
    },
    saveService: function() {

        $.ajax({
            url: apps_service + "/service/" + this.props.service.service.id,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(this.state.service.service),
            success: function(data) {
                if (data.success) {
                    this.refs.settings.close();
                    this.reloadService();
                } else {
                    var s = this.state;
                    s.field_errors = data.errors;
                    this.setState(s);
                }
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(apps_service + "/service/" + this.props.service.service.id, status, err.toString());
            }.bind(this)
        });


    },
    reloadService: function() {
        $.ajax({
            url: apps_service + "/service/" + this.props.service.service.id,
            type: 'get',
            dataType: 'json',
            success: function(data) {
                var s = this.state;
                s.saved_service = data;
                s.field_errors = [];
                this.setState(s);
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    settings: function () {
        var s = this.state;
        s.service = JSON.parse(JSON.stringify(s.saved_service));    // create a deep copy of the structure
        s.field_errors = [];
        this.setState(s);
        this.refs.settings.open();
    },
    pushToDash: function () {
        this.refs.users.init();
        this.refs.pushToDash.open();
    },
    savePushToDash: function () {
        this.refs.pushToDash.close();
        $.ajax({
            url: apps_service + "/users/service/" + this.props.service.service.id,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.refs.users.getSelectedUsers()),
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/service/" + this.props.service.service.id, status, err.toString());
            }.bind(this)
        });
    },
    loadUsers: function(callback, error) {
        $.ajax({
            url: apps_service + "/users/service/" + this.props.service.service.id,
            dataType:'json',
            type:'get',
            success:callback,
            error: function(xhr, status, err) {
                console.error(apps_service + "/users/service/" + this.props.service.service.id, status, err.toString());
                if (error != undefined) {
                    error();
                }
            }.bind(this)
        });
    },
    queryUsers: function(query, callback) {
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.instance + "?q=" + query,
            dataType:"json",
            type:'get',
            success:callback,
            error: function(xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.instance + "?q=" + query, status, err.toString())
            }.bind(this)
        })
    },
    render: function () {
        var links = [
            <a onClick={this.settings} href="#">{t('settings')}</a>
        ];
        if (! this.state.saved_service.service.visible) {
            links.push(<a onClick={this.pushToDash} href="#">{t('users')}</a>);
        }

        return (
            <li className="service">
                {this.state.saved_service.service.name}
                <span className="pull-right">
                {links}
                </span>
                <ServiceSettings ref="settings" service={this.state.service} errors={this.state.field_errors} update={this.updateServiceLocally} save={this.saveService}/>
                <Modal title={t('users')} ref="pushToDash" successHandler={this.savePushToDash}>
                    <UserPicker ref="users" users={this.loadUsers} source={this.queryUsers} />
                </Modal>
            </li>
            );
    }
});


var FormField = React.createClass({
    render: function() {
        var className;
        if (this.props.error) {
            className = "control-label col-sm-2 error";
        } else {
            className = "control-label col-sm-2";
        }
        return (
            <div className="form-group">
                <label htmlFor={this.props.name} className={className}>{t(this.props.name)}</label>
                <div className="col-sm-10">
                {this.props.children}
                </div>
            </div>
            );
    }
});

var ServiceSettings = React.createClass({
    handleChange: function(field, checkbox) {
        return function(event) {
            if (checkbox) {
                this.props.update(field, event.target.checked);
            } else {
                this.props.update(field, event.target.value);
            }
        }.bind(this);
    },
    open: function() {
        this.refs.modal.open();
    },
    close: function() {
        this.refs.modal.close();
    },
    render: function() {
        var iconClassName = "control-label col-sm-2";
        if ($.inArray("icon", this.props.errors) != -1) {
            iconClassName = iconClassName + " error";
        }
        return (
            <Modal title={this.props.service.name} ref="modal" successHandler={this.props.save} large={true}>
                <form className="form-horizontal"  role="form">
                    <FormField name="name" error={$.inArray("name", this.props.errors) != -1}>
                        <input type="text" name="name" id="name" className="form-control" value={this.props.service.service.name} onChange={this.handleChange("name")}></input>
                    </FormField>
                    <FormField name="description" error={$.inArray("description", this.props.errors) != -1}>
                        <textarea name="description" id="description" className="form-control" value={this.props.service.service.description} onChange={this.handleChange("description")}></textarea>
                    </FormField>

                    <div className="form-group">
                        <label htmlFor="icon" className={iconClassName}>{t('icon')}</label>
                        <div className="controls col-sm-1">
                            <img src={this.props.service.iconUrl} />
                        </div>
                        <div className="controls col-sm-9">
                            <input name="icon" type="text" id="icon" className="form-control" value={this.props.service.service.icon} onChange={this.handleChange('icon')}/>
                        </div>
                    </div>

                    <FormField name="published">
                        <input type="checkbox" id="published" checked={this.props.service.service.visible} onChange={this.handleChange('visible', true)} />
                    </FormField>
                </form>
            </Modal>
            );

    }
});