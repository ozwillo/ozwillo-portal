/** @jsx React.DOM */

var MyApps = React.createClass({displayName: "MyApps",
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
            return React.createElement("p", {className: "text-center"}, 
                React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading'));
        }
        var auths = this.state.authorities.map(function (auth) {
            return (
                React.createElement(Authority, {name: auth.name, key: auth.id, openByDefault: auth.type == 'INDIVIDUAL'})
                );
        });
        return (
            React.createElement("div", {className: "container panel-group"}, 
                auths
            )
            );
    }


});
var Authority = React.createClass({displayName: "Authority",

    getInitialState: function () {
        return {open: this.props.openByDefault};
    },
    toggle: function () {
        this.setState({open: !this.state.open});
    },
    render: function () {
        var content;
        if (this.state.open) {
            content = React.createElement(InstanceList, {id: this.props.key, name: this.props.name, authority: this.props.key});
        } else {
            content = null;
        }

        return (
            React.createElement("div", {className: "panel panel-default"}, 
                React.createElement("div", {className: "panel-heading"}, 
                    React.createElement("h4", {className: "panel-title", onClick: this.toggle}, 
                        React.createElement("span", null, this.props.name), 
                        React.createElement(OpenAuthority, {callback: this.toggle, open: this.state.open})
                    )
                ), 
                React.createElement("div", {ref: "content"}, 
          content
                )
            )
            );
    }
});

var OpenAuthority = React.createClass({displayName: "OpenAuthority",
    click: function () {
        this.props.callback();
    },
    render: function () {
        var className = this.props.open ? 'caret' : 'caret inverse';
        return (
            React.createElement("a", {className: "authority-link pull-right", onClick: this.click}, 
                React.createElement("b", {className: className})
            )
            );
    }
});

var InstanceList = React.createClass({displayName: "InstanceList",
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
            return React.createElement("p", {className: "text-center"}, 
                React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading'));
        }

        var instances = this.state.instances;
        var authority = this.props.authority;
        var reload = this.reloadInstances;
        var result = instances.length != 0 ? instances.map(function (instance) {
            return React.createElement(Instance, {key: instance.id, instance: instance, authority: authority, reload: reload});
        }) : (
            React.createElement("div", {className: "text-center"}, 
                React.createElement("span", null, t('none'), " "), 
                React.createElement("b", null, this.props.name)
            )
            )

        return (
            React.createElement("div", {className: "panel collapse"}, 
                result
            )
            );
    }
});

var Instance = React.createClass({displayName: "Instance",
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
            return React.createElement(Service, {key: service.service.id, service: service, instance: instance});
        });

        var deprovision = null;
        if (devmode) {
            deprovision = (
                React.createElement("a", {className: "btn btn-danger pull-right", href: "#", onClick: this.deprovision}, t('ui.delete'))
                );
        }

        return (
            React.createElement("div", {className: "panel panel-instance"}, 
                React.createElement(Modal, {ref: "manageUsers", title: t('manage_users'), successHandler: this.saveUsers}, 
                    React.createElement(UserPicker, {ref: "users", users: this.loadUsers, source: this.queryUsers})
                ), 

                React.createElement("div", {className: "panel-heading"}, 
                    React.createElement("img", {height: "32", width: "32", alt: this.props.instance.name, src: this.props.instance.icon}), 
                    React.createElement("span", {className: "appname"}, this.props.instance.name), 
                    React.createElement("a", {className: "tip btn btn-default pull-right", href: "#", onClick: this.manageUsers, "data-toggle": "tooltip", "data-placement": "bottom", title: t('manage_users')}, 
                        React.createElement("li", {className: "fa fa-user"})
                    ), 
                    deprovision
                ), 
                React.createElement("div", {className: "panel-body"}, 
                    React.createElement("div", {className: "standard-form"}, 
                        React.createElement("div", {className: "row form-table-header"}, 
                            React.createElement("div", {class: "col-sm-10"}, t('services'))
                        ), 
                        services
                    )
                )
            )
            );
    }
});


React.renderComponent(
    React.createElement(MyApps, null), document.getElementById("myapps")
);
