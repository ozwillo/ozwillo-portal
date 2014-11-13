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
                <Authority name={auth.name} key={auth.id} openByDefault={auth.type == 'INDIVIDUAL'}/>
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

    getInitialState: function () {
        return {open: this.props.openByDefault};
    },
    toggle: function () {
        this.setState({open: !this.state.open});
    },
    render: function () {
        var content;
        if (this.state.open) {
            content = <InstanceList id={this.props.key} name={this.props.name} authority={this.props.key}/>;
        } else {
            content = null;
        }

        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4 className="panel-title" onClick={this.toggle}>
                        <span>{this.props.name}</span>
                        <OpenAuthority callback={this.toggle} open={this.state.open} />
                    </h4>
                </div>
                <div ref="content">
          {content}
                </div>
            </div>
            );
    }
});

var OpenAuthority = React.createClass({
    click: function () {
        this.props.callback();
    },
    render: function () {
        var className = this.props.open ? 'caret' : 'caret inverse';
        return (
            <a className="authority-link pull-right" onClick={this.click}>
                <b className={className}></b>
            </a>
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
            return <Instance key={instance.id} instance={instance} authority={authority} reload={reload}/>;
        }) : (
            <div className="text-center">
                <span>{t('none')} </span>
                <b>{this.props.name}</b>
            </div>
            )

        return (
            <div className="panel collapse">
                {result}
            </div>
            );
    }
});

var Instance = React.createClass({
    manageUsers: function (event) {
        event.preventDefault();
        this.refs.users.init();
        this.refs.manageUsers.open();
    },
    saveUsers: function () {
        this.refs.manageUsers.close();
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.key,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.refs.users.getSelectedUsers()),
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.key, status, err.toString());
            }.bind(this)
        });
    },
    loadUsers: function (callback, error) {
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.key,
            dataType: 'json',
            method: 'get',
            success: callback,
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.key, status, err.toString());
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
    deprovision: function () {
        $.ajax({
            url: apps_service + "/deprovision/" + this.props.key,
            type: 'post',
            success: function () {
                this.props.reload();
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        var instance = this.props.key;
        var services = this.props.instance.services.map(function (service) {
            return <Service key={service.service.id} service={service} instance={instance}/>;
        });

        var deprovision = null;
        if (devmode) {
            deprovision = (
                <a className="btn btn-danger pull-right" href="#" onClick={this.deprovision}>{t('ui.delete')}</a>
                );
        }

        return (
            <div className="panel panel-instance">
                <Modal ref="manageUsers" title={t('manage_users')} successHandler={this.saveUsers} >
                    <UserPicker ref="users" users={this.loadUsers} source={this.queryUsers}/>
                </Modal>

                <div className="panel-heading">
                    <img height="32" width="32" alt={this.props.instance.name} src={this.props.instance.icon}></img>
                    <span className="appname">{this.props.instance.name}</span>
                    <a className="tip btn btn-default pull-right" href="#" onClick={this.manageUsers} data-toggle="tooltip" data-placement="bottom" title={t('manage_users')}>
                        <li className="fa fa-user"></li>
                    </a>
                    {deprovision}
                </div>
                <div className="panel-body">
                    <div className="standard-form">
                        <div className="row form-table-header">
                            <div class="col-sm-10">{t('services')}</div>
                        </div>
                        {services}
                    </div>
                </div>
            </div>
            );
    }
});


React.renderComponent(
    <MyApps />, document.getElementById("myapps")
);
