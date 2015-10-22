/** @jsx React.DOM */

var MyApps = React.createClass({
    getInitialState: function () {
        return {
            loading: true,
            authorities: []
        };
    },
    componentDidMount: function () {
        $.ajax({
            url: apps_service + "/authorities",
            dataType: "json",
            success: function (data) {
                this.setState({
                    loading: false,
                    authorities: data
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        if (this.state.loading) {
            return <p className="text-center">
                <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}</p>;
        }
        var auths = this.state.authorities.map(function (auth) {
            return (
                <Authority name={auth.name} key={auth.id} id={auth.id} isPersonal={auth.type === 'INDIVIDUAL'}/>
                );
        });
        return (
            <div className="container panel-group">
                {auths}
            </div>
            );
    }
});

var Authority = React.createClass({

    render: function () {
        var content = <InstanceList id={this.props.id} name={this.props.name} authority={this.props.id}/>;
        var title = this.props.isPersonal ?
            (<span>{t('apps-for-personal-use')}</span>) : (<span>{t('apps-for-organization')} {this.props.name}</span>);

        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4 className="panel-title">
                        {title}
                    </h4>
                </div>
                <div ref="content">
                    {content}
                </div>
            </div>
        );
    }
});

var InstanceList = React.createClass({
    getInitialState: function () {
        return {
            loading: true,
            instances: []
        };
    },
    componentDidMount: function () {
        this.reloadInstances();
    },
    reloadInstances: function () {
        this.setState(this.getInitialState());

        $.ajax({
            url: apps_service + "/instances/" + this.props.id,
            dataType: "json",
            success: function (data) {
                this.setState({
                    loading: false,
                    instances: data
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    componentDidUpdate: function () {
        $(this.getDOMNode()).toggle("blind");
    },
    render: function () {
        if (this.state.loading) {
            return <p className="text-center">
                <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}</p>;
        }

        var instances = this.state.instances;
        var authority = this.props.authority;
        var reload = this.reloadInstances;
        var result = instances.length != 0 ? instances.map(function (instance) {
            return <Instance key={instance.id} id={instance.id} instance={instance} authority={authority} reload={reload}/>;
        }) : (
            <div className="text-center">
                <span>{t('none')} </span>
                <b>{this.props.name}</b>
            </div>
            );

        return (
            <div className="panel collapse">
                {result}
            </div>
            );
    }
});

var Instance = React.createClass({
    getInitialState: function() {
        return {};
    },
    manageUsers: function (event) {
        event.preventDefault();
        this.refs.users.init();
        this.refs.manageUsers.open();
    },
    saveUsers: function () {
        this.refs.manageUsers.close();
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.id,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.refs.users.getSelectedUsers()),
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.id, status, err.toString());
            }.bind(this)
        });
    },
    loadUsers: function (callback, error) {
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.id + "?app_admin=false", // only users that
            // are app_user (so not those that are !app_user app_admin)
            dataType: 'json',
            method: 'get',
            success: callback,
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.id, status, err.toString());
                if (error != undefined) {
                    error();
                }
            }.bind(this)
        });
    },
    queryUsers: function (query, callback) {
        $.ajax({
            url: apps_service + "/users/network/" + this.props.authority + "?q=" + query,
            dataType: 'json',
            method: 'get',
            success: callback,
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/network/" + this.props.authority + "?q=" + query, status, err.toString());
            }.bind(this)
        });
    },
    componentDidMount: function () {
        $("a.tip", this.getDOMNode()).tooltip();
    },
    componentDidUpdate: function () {
        if (typeof this.state.errorMessage === 'string') {
            this.refs.errorDialog.open();
        }
    },
    confirmTrash: function (event) {
        event.preventDefault();
        this.refs.confirmTrash.open();
    },
    trash: function () {
        this.refs.confirmTrash.close();
        this.setStatus('STOPPED');
    },
    confirmUntrash: function (event) {
        event.preventDefault();
        this.refs.confirmUntrash.open();
    },
    untrash: function () {
        this.refs.confirmUntrash.close();
        this.setStatus('RUNNING');
    },
    setStatus: function (status) {
        var instance = this.props.instance;
        instance = { applicationInstance: { id: instance.applicationInstance.id, status: status } }; // else if the whole instance, Spring REST says "syntaxically incorrect" i.e. can't unmarshall
        $.ajax({
            url: apps_service + "/set-status/" + this.props.id,
            //dataType: 'json', // else parsererror on return
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(instance),
            success: function (data) {
                //data = "test"; // to easily test error return
                if (typeof data === 'string' && data.trim().length !== 0) {
                    // assuming it's a String message returned by the Kernel
                    var state = this.state;
                    state.errorMessage = data;
                    this.setState(state); // triggers update and therefore messageDialog opens, and on close props will reload
                } else {
                    this.props.reload();
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        // NB. user is necessarily admin of all displayed orgs, and apps not PENDING
        
        var applicationInstanceStatus = this.props.instance.applicationInstance.status;
        
        var manageUsersButton = null;
        if (this.props.authority.slice(0, 'INDIVIDUAL:') !== 'INDIVIDUAL:') { // don't display it for personal organizations
            manageUsersButton = (
                <button className="tip btn btn-default pull-right" disabled={applicationInstanceStatus === 'STOPPED'}
                        onClick={this.manageUsers} data-toggle="tooltip" data-placement="bottom" title={t('manage_users')}>
                    <li className="fa fa-user"></li>
                </button>        
            );
        }
        
        var instance = this.props.id;
        var services = this.props.instance.services.map(function (service) {
            return <Service key={service.service.id} service={service} instance={instance} status={applicationInstanceStatus}/>;
        });

        var buttons = [];
        var dialogs = [];
        if (applicationInstanceStatus === 'STOPPED') {
            var byDeleteRequesterOnDate = t('by') + " " + this.props.instance.status_change_requester_label
                  + " (" + moment(this.props.instance.applicationInstance.status_changed) + ")";
            buttons.push(
                <a className="btn btn-danger pull-right" href="#" onClick={this.confirmUntrash}>{t('ui.cancel')}...</a>
            );
            buttons.push(
                <span key="untrashTtl" className="pull-right" style={{'color':'red', 'fontStyle':'Italic', 'marginLeft':'5px', 'marginRight':'5px'}} title={byDeleteRequesterOnDate}>
                    {t('will-be-deleted')} {moment(this.props.instance.deletion_planned).fromNow()}
                </span>
            );
            var confirmUntrashTitle = t('confirm-untrash.title') + ' ' + this.props.instance.name;
            dialogs.push(
                <Modal ref="confirmUntrash" title={confirmUntrashTitle} successHandler={this.untrash} buttonLabels={{ 'cancel': t('ui.cancel'), 'save': t('ui.confirm') }} >
                    {t('confirm-untrash.body')}
                </Modal>
            );
        } else {
            buttons.push(
                <a className="btn btn-danger pull-right" href="#" onClick={this.confirmTrash}>{t('ui.delete')}...</a>
            );
            var confirmTrashTitle = t('confirm-trash.title') + ' ' + this.props.instance.name;
            dialogs.push(
                <Modal ref="confirmTrash" title={confirmTrashTitle} successHandler={this.trash} buttonLabels={{ 'cancel': t('ui.cancel'), 'save': t('ui.confirm') }} >
                    {t('confirm-trash.body')}
                </Modal>
            );
        }

        return (
            <div className="panel panel-instance">
                <Modal ref="errorDialog" infobox={true} onClose={this.props.reload} buttonLabels={{'ok': t('ui.close')}} title={t('ui.unexpected_error')}>
                    {this.state.errorMessage}
                </Modal>
                <Modal ref="manageUsers" title={t('manage_users')} successHandler={this.saveUsers} >
                    <UserPicker ref="users" users={this.loadUsers} source={this.queryUsers}/>
                </Modal>
                {dialogs}

                <div className="panel-heading">
                    <img height="32" width="32" alt={this.props.instance.name} src={this.props.instance.icon}></img>
                    <span className="appname">{this.props.instance.name}</span>
                    {manageUsersButton}
                    {buttons}
                </div>
                <div className="panel-body">
                    <div className="standard-form">
                        <div className="row form-table-header">
                            <div className="col-sm-10">{t('services')}</div>
                        </div>
                        {services}
                    </div>
                </div>
            </div>
            );
    }
});


React.render( <MyApps />, document.getElementById("myapps") );
