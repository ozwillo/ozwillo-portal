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
            return (
                <p className="text-center">
                    <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}
                </p>
            );
        }
        var auths = this.state.authorities.map(function (auth) {
            return (
                <Authority name={auth.name} key={auth.id} id={auth.id} isPersonal={auth.type === 'INDIVIDUAL'}/>
            );
        });
        return (
            <div className="authorities">{auths}</div>
        );
    }
});

var Authority = React.createClass({

    render: function () {
        var title = this.props.isPersonal ?
            (<span>{t('apps-for-personal-use')}</span>) : (<span>{t('apps-for-organization')} {this.props.name}</span>);

        return (
            <div className="authority">
                <div className="row authority-header">
                    <div className="col-sm-12">
                        <h2>{title}</h2>
                    </div>
                </div>
                <div ref="content" className="row">
                    <InstanceList id={this.props.id} name={this.props.name} authority={this.props.id}/>
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
    render: function () {
        if (this.state.loading) {
            return (
                <p className="text-center">
                    <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}
                </p>
            );
        }

        var instances = this.state.instances;
        var result = instances.length != 0 ? instances.map(function (instance) {
            return <Instance key={instance.id} id={instance.id} instance={instance} authority={this.props.authority}
                             reload={this.reloadInstances}/>;
        }.bind(this)) : (
            <p className="text-center authority-noapp">
                <span>{t('none')} </span>
                <strong>{this.props.name}</strong>
            </p>
        );

        return (
            <div className="col-sm-12 authority-body">
                {result}
            </div>
        );
    }
});

var Instance = React.createClass({
    getInitialState: function() {
        return {};
    },
    componentDidMount: function () {
        $("a.tip", this.getDOMNode()).tooltip();
    },
    componentDidUpdate: function () {
        if (typeof this.state.errorMessage === 'string') {
            this.refs.errorDialog.open();
        }
    },
    manageUsers: function (event) {
        event.preventDefault();
        this.refs.manageUsers.open();
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
        if (this.props.authority.slice(0, 'INDIVIDUAL:') !== 'INDIVIDUAL:' && applicationInstanceStatus !== 'STOPPED') { // don't display it for personal organizations
            manageUsersButton = (
                <button type="button" className="tip btn btn-default-inverse pull-right"
                        onClick={this.manageUsers} data-toggle="tooltip" data-placement="bottom" title={t('manage_users')}>
                    <i className="fa fa-user"></i>
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
                <button type="button" className="btn oz-btn-danger pull-right" onClick={this.confirmUntrash}>{t('ui.cancel')}</button>
            );
            buttons.push(
                <span key="untrashTtl" style={{'color':'red', 'fontStyle':'Italic', 'marginLeft':'5px', 'marginRight':'5px'}} title={byDeleteRequesterOnDate}>
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
                <button type="button" className="btn oz-btn-danger btn-line pull-right" onClick={this.confirmTrash}>{t('ui.delete')}</button>
            );
            var confirmTrashTitle = t('confirm-trash.title') + ' ' + this.props.instance.name;
            dialogs.push(
                <Modal ref="confirmTrash" title={confirmTrashTitle} successHandler={this.trash}
                       buttonLabels={{ 'cancel': t('ui.cancel'), 'save': t('ui.confirm') }} saveButtonClass="oz-btn-danger">
                    {t('confirm-trash.body')}
                </Modal>
            );
        }

        return (
            <div className="authority-app">
                <Modal ref="errorDialog" infobox={true} onClose={this.props.reload} buttonLabels={{'ok': t('ui.close')}} title={t('ui.unexpected_error')}>
                    {this.state.errorMessage}
                </Modal>
                <ApplicationUsersManagement ref="manageUsers" instanceId={this.props.id} authority={this.props.authority} />
                {dialogs}

                <div className="row authority-app-title">
                    <div className="col-sm-8">
                        <img height="32" width="32" alt={this.props.instance.name} src={this.props.instance.icon}></img>
                        <h3>{this.props.instance.name}</h3>
                    </div>
                    <div className="col-sm-4">
                        <div className="pull-right">
                            {manageUsersButton}
                            {buttons}
                        </div>
                    </div>
                </div>
                <div className="row authority-app-services-title">
                    <div className="col-sm-12">
                        <h4>{t('services')}</h4>
                    </div>
                </div>
                {services}
            </div>
        );
    }
});


React.render( <MyApps />, document.getElementById("myapps") );
