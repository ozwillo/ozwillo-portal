/** @jsx React.DOM */


(function () {

    var Dashboard = React.createClass({displayName: "Dashboard",
        notificationsChecked: false,
        checkNotifications: function () {
            if (this.state.apps) {
                $.ajax({
                    url: dash_service + "/notifications",
                    type: 'get',
                    dataType: 'json',
                    success: function (appNotifs) {

                        var state = this.state;
                        for (var i = 0; i < state.apps.length; i++) {
                            var app = state.apps[i];

                            if (appNotifs[app.serviceId]) {
                                app.notificationCount = appNotifs[app.serviceId];
                            } else {
                                app.notificationCount = 0;
                            }
                        }
                        this.setState(state);
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.log("Cannot check notifications", status, err);
                    }.bind(this)
                });
            }
            window.setTimeout(this.checkNotifications, 2000);
        },
        initNotificationsCheck: function () {
            if (this.notificationsChecked) {
                console.log("Already checking notifications");
            } else {
                this.notificationsChecked = true;
                this.checkNotifications();
            }
        },
        componentDidMount: function () {
            this.initNotificationsCheck();

            $.ajax({
                url: dash_service + "/dashboards",
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    this.state.dashboards = data;
                    this.state.dash = data[0];
                    this.state.loadingDashboards = false;
                    this.setState(this.state);
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error("Error", status, err);
                }.bind(this)
            });

            $.ajax({
                url: dash_service + "/apps",
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    this.state.apps = data;
                    this.state.loadingApps = false;
                    this.setState(this.state);
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error("Error", status, err);
                }.bind(this)
            });

            $.ajax({
                url: dash_service + "/pending-apps",
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    this.state.pendingApps = data;
                    this.setState(this.state);
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error("Error", status, err);
                }.bind(this)
            });
        },
        getInitialState: function () {
            return {
                dashboards: null,
                dash: null,
                apps: null,
                pendingApps: null,
                dragging: false,
                loadingDashboards: true,
                loadingApps: true
            };
        },
        findById: function (array, obj) {
            for (var i in array) {
                if (array[i].id == obj.id) {
                    return i;
                }
            }
        },
        startDrag: function (app) {
            return function (event) {
                event.dataTransfer.setData("application/json", JSON.stringify({app: app}));
                var state = this.state;
                state.dragging = true;
                this.setState(state);
            }.bind(this);
        },
        endDrag: function () {
            var state = this.state;
            state.dragging = false;
            this.setState(state);
        },
        reorderApps: function (before) {
            return function (app) {

                var state = this.state;
                state.dragging = false;

                var fr = this.findById(this.state.apps, app);
                if (before != "last") {
                    var to = this.findById(this.state.apps, before);
                    if (to != fr) {
                        // remove
                        var removed = state.apps.splice(fr, 1);
                        var to = this.findById(this.state.apps, before);
                        state.apps.splice(to, 0, removed[0]);
                    }
                } else {
                    var removed = state.apps.splice(fr, 1);
                    state.apps.splice(state.apps.length, 0, removed[0]);
                }

                $.ajax({
                    url: dash_service + "/apps/" + state.dash.id,
                    type: 'put',
                    contentType: 'application/json',
                    data: JSON.stringify(state.apps),
                    success: function () {
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                        // reload everything
                        this.setState(this.getInitialState());
                        this.componentDidMount();
                    }.bind(this)
                });

                this.setState(state);

            }.bind(this);
        },

        switchToDashboard: function (dash) {
            return function () {
                var state = this.state;
                state.dash = dash;
                state.loadingApps = true;
                state.pendingApps = null;
                this.setState(state);

                $.ajax({
                    url: dash_service + "/apps/" + dash.id,
                    type: 'get',
                    dataType: 'json',
                    success: function (data) {
                        state.apps = data;
                        state.loadingApps = false;
                        this.setState(state);
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                    }.bind(this)
                });

                $.ajax({
                    url: dash_service + "/pending-apps",
                    type: 'get',
                    dataType: 'json',
                    success: function (data) {
                        state.pendingApps = data;
                        this.setState(state);
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                    }.bind(this)
                });
            }.bind(this);
        },

        moveToDash: function (dash) {
            return function (app) {

                $.ajax({
                    url: dash_service + "/apps/move/" + app.id + "/to/" + dash.id,
                    type: 'post',
                    success: function () {
                    },
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                        this.setState(this.getInitialState());
                        this.componentDidMount();
                    }.bind(this)
                });

                var state = this.state;
                var index = this.findById(state.apps, app);
                state.apps.splice(index, 1);
                state.dragging = false;

                this.setState(state);

            }.bind(this);
        },

        deleteApp: function (app) {
            var state = this.state;
            state.apps.splice(this.findById(state.apps, app), 1);
            state.dragging = false;

            this.setState(state);


            $.ajax({
                url: dash_service + "/apps/remove/" + app.id,
                type: 'delete',
                success: function() {}.bind(this),
                error: function(xhr, status, err) {
                    console.error("Error", status, err);
                    this.setState(this.getInitialState());
                    this.componentDidMount();
                }.bind(this)
            });
        },

        createDash: function (name) {
            var state = this.state;

            $.ajax({
                url: dash_service + "/dashboards",
                type: 'post',
                dataType: 'json',
                data: JSON.stringify({name:name}),
                contentType: 'application/json',
                success: function(userContext) {
                    state.dashboards.push(userContext);
                    this.setState(state);
                    this.switchToDashboard(userContext);
                }.bind(this),
                error: function(xhr, status, err) {
                    console.error("Error", status, err);
                    this.setState(this.getInitialState());
                    this.componentDidMount();
                }.bind(this)

            });

        },

        renameDash: function (dash) {
            return function (name) {
                var idx = this.findById(this.state.dashboards, dash);
                this.state.dashboards[idx].name = name;
                this.setState(this.state);

                $.ajax({
                    url: dash_service + "/dashboard/" + dash.id,
                    type: 'put',
                    contentType: 'application/json',
                    data: JSON.stringify(this.state.dashboards[idx]),
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                        this.setState(this.getInitialState());
                        this.componentDidMount();
                    }.bind(this)
                });

            }.bind(this);
        },
        removeDash: function (dash) {
            return function () {
                var state = this.state;
                state.loadingDashboards = true;
                this.setState(state);

                $.ajax({
                    url: dash_service + "/dashboard/" + dash.id,
                    type: 'delete',
                    success: function () {
                        this.setState(this.getInitialState());
                        this.componentDidMount();
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.error("Error", status, err);
                        this.setState(this.getInitialState());
                        this.componentDidMount();
                    }.bind(this)
                });
            }.bind(this);
        },
        render: function () {
            return (
                React.createElement("div", {className: "row"}, 
                    React.createElement(SideBar, {
                        loading: this.state.loadingDashboards, 
                        dashboards: this.state.dashboards, 
                        currentDash: this.state.dash, 
                        dragging: this.state.dragging, 
                        switchToDashboard: this.switchToDashboard, 
                        moveToDash: this.moveToDash, 
                        deleteApp: this.deleteApp, 
                        createDash: this.createDash, 
                        renameDash: this.renameDash, 
                        removeDash: this.removeDash}
                    ), 
                    React.createElement(Desktop, {
                        dash: this.state.dash, 
                        loading: this.state.loadingApps, 
                        apps: this.state.apps, 
                        pendingApps: this.state.pendingApps, 
                        startDrag: this.startDrag, 
                        endDrag: this.endDrag, 
                        dragging: this.state.dragging, 
                        dropCallback: this.reorderApps}
                    )
                )
            );
        }
    });


    var SideBar = React.createClass({displayName: "SideBar",
        render: function () {
            if (this.props.loading) {
                return (
                    React.createElement("div", {className: "col-sm-2 text-center dash-switcher"}, 
                        React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading')
                    )
                );
            } else {

                var dashboards = this.props.dashboards.map(function (dash) {
                    return (
                        React.createElement(DashItem, {
                            key: dash.id, 
                            dash: dash, 
                            active: dash == this.props.currentDash, 
                            dragging: this.props.dragging, 
                            switchCallback: this.props.switchToDashboard(dash), 
                            moveToDash: this.props.moveToDash(dash), 
                            rename: this.props.renameDash(dash), 
                            remove: this.props.removeDash(dash)}
                        )
                    );
                }.bind(this));

                return (
                    React.createElement("div", {className: "col-sm-2 text-center dash-switcher"}, 
                        React.createElement("img", {src: image_root + "my/switch-dash.png"}), 
                        React.createElement("p", null, t('switch-dash')), 
                        React.createElement("ul", {className: "nav nav-pills nav-stacked text-left"}, 
                dashboards
                        ), 
                        React.createElement(CreateDashboard, {addDash: this.props.createDash}), 
                        React.createElement(DeleteApp, {
                            dragging: this.props.dragging, 
                            delete: this.props.deleteApp}
                        )
                    )
                );
            }
        }
    });

    var DashItem = React.createClass({displayName: "DashItem",
        getInitialState: function () {
            return {over: false, editing: false};
        },
        select: function (event) {
            event.preventDefault();
            if (this.props.switchCallback) {
                this.props.switchCallback();
            }
        },
        over: function (isOver) {
            return function (event) {
                event.preventDefault();
                var state = this.state;
                state.over = isOver;
                this.setState(state);
            }.bind(this);
        },
        drop: function (event) {
            var app = JSON.parse(event.dataTransfer.getData("application/json")).app;
            this.props.moveToDash(app);
            var state = this.state;
            state.over = false;
            this.setState(state);
        },
        edit: function () {
            this.state.editing = true;
            this.setState(this.state);
        },
        cancelEditing: function () {
            this.state.editing = false;
            this.setState(this.state);
        },
        render: function () {
            if (this.props.active) {
                if (this.state.editing) {
                    return React.createElement(EditingDash, {name: this.props.dash.name, rename: this.props.rename, cancel: this.cancelEditing});
                } else {

                    return (
                        React.createElement("li", {className: "active"}, 
                            React.createElement("a", {onClick: function (e) {
                                e.preventDefault();
                            }}, this.props.dash.name, 
                                React.createElement(DashActions, {remove: this.props.remove, edit: this.edit, primary: this.props.dash.main})
                            )
                        )
                    );
                }
            } else {
                var className = this.props.dragging ? (this.state.over ? 'dragging over' : 'dragging') : '';

                return (
                    React.createElement("li", null, 
                        React.createElement("a", {href: "#", 
                            className: className, 
                            onDragOver: this.over(true), 
                            onDragLeave: this.over(false), 
                            onClick: this.select, 
                            onDrop: this.drop
                        }, 
                    this.props.dash.name
                        )
                    )
                );
            }
        }
    });

    var DashActions = React.createClass({displayName: "DashActions",

        remove: function () {
            this.props.remove();
            this.refs.modal.close();
        },
        showRemove: function () {
            this.refs.modal.open();
        },
        render: function () {

            if (this.props.primary) {
                return (
                    React.createElement("div", {className: "pull-right"}, 
                        React.createElement("i", {className: "fa fa-pencil", onClick: this.props.edit})
                    )
                );

            } else {
                var buttonLabels = {"save": t('ui.yes'), "cancel": t('ui.cancel')};
                return (
                    React.createElement("div", {className: "pull-right"}, 
                        React.createElement(Modal, {title: t('confirm-delete-dash'), successHandler: this.remove, buttonLabels: buttonLabels, ref: "modal"}, 
                            React.createElement("p", null, t('confirm-delete-dash-long'))
                        ), 

                        React.createElement("i", {className: "fa fa-pencil", onClick: this.props.edit}), " ", React.createElement("i", {className: "fa fa-trash", onClick: this.showRemove})
                    )
                );

            }

        }

    });

    var EditingDash = React.createClass({displayName: "EditingDash",
        getInitialState: function () {
            return {val: this.props.name};
        },
        componentDidMount: function () {
            var input = $(this.getDOMNode()).find("input");
            input.focus();
            input.select();
        },
        change: function (event) {
            var state = this.state;
            state.val = event.target.value;
            this.setState(state);
        },
        click: function (event) {
            event.preventDefault();
            event.stopPropagation();
        },
        submit: function (event) {
            event.preventDefault();
            this.props.rename(this.state.val);
            this.props.cancel();
        },
        render: function () {
            return (
                React.createElement("li", {className: "active"}, 
                    React.createElement("form", {onSubmit: this.submit}, 
                        React.createElement("input", {type: "text", className: "form-control", value: this.state.val, onChange: this.change, onClick: this.click})
                    ), 
                    React.createElement("div", {className: "dash-veil", onClick: this.props.cancel})
                )
            );
        }
    });

    var CreateDashboard = React.createClass({displayName: "CreateDashboard",
        getInitialState: function () {
            return {val: ""};
        },
        change: function (event) {
            this.setState({val: event.target.value});
        },
        submit: function (event) {
            event.preventDefault();
            this.props.addDash(this.state.val);
            this.setState(this.getInitialState());
        },
        render: function () {
            return (
                React.createElement("form", {role: "form", id: "create-dash", onSubmit: this.submit}, 
                    React.createElement("div", {className: "form-group"}, 
                        React.createElement("label", {htmlFor: "dashboardname"}, t('create')), 
                        React.createElement("input", {type: "text", name: "dashboardname", id: "dashboardname", className: "form-control", value: this.state.val, onChange: this.change})
                    ), 
                    React.createElement("div", {className: "text-right"}, 
                        React.createElement("input", {type: "image", src: image_root + "/icon/plus.png", alt: t('ui.ok'), onClick: this.submit})
                    )
                )
            );
        }
    });

    var DeleteApp = React.createClass({displayName: "DeleteApp",
        getInitialState: function () {
            return {over: false, app: null};
        },
        over: function (isOver) {
            return function (event) {
                if (isOver) {
                    event.preventDefault();
                }
                this.setState({over: isOver});
            }.bind(this);
        },
        drop: function (event) {
            var app = JSON.parse(event.dataTransfer.getData("application/json")).app;

            this.setState({over: true, app: app});
            this.refs.modal.open();

            //this.props.delete(app);
            //this.setState({over: false});
        },
        removeApp: function () {
            var app = this.state.app;
            this.props.delete(app);
            this.refs.modal.close();
            this.setState(this.getInitialState());


        },
        cancel: function () {
            this.setState(this.getInitialState());
        },
        render: function () {
            if (this.props.dragging || this.state.app) {
                var className = "delete-app" + (this.state.over ? " over" : "");
                var buttonLabels = {"save": t('ui.yes'), "cancel": t('ui.cancel')};
                return (
                    React.createElement("div", {
                        className: className, 
                        onDragOver: this.over(true), 
                        onDragLeave: this.over(false), 
                        onDrop: this.drop
                    }, 
                        React.createElement(Modal, {title: t('confirm-remove-app'), successHandler: this.removeApp, cancelHandler: this.cancel, buttonLabels: buttonLabels, ref: "modal"}, 
                            React.createElement("p", null, 
                        t('confirm-remove-app-long')
                            )
                        ), 
                        React.createElement("span", null, 
                            React.createElement("i", {className: "fa fa-trash"})
                        )
                    )
                );
            } else return null;
        }

    });

    var Desktop = React.createClass({displayName: "Desktop",
        getInitialState: function () {
            return {dragging: false};
        },
        render: function () {

            if (this.props.loading) {
                return (
                    React.createElement("div", {className: "col-sm-10 desktop"}, 
                        React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading')
                    )
                );
            } else {

                var icons = this.props.apps.map(function (app) {
                    return React.createElement(AppZone, {
                        key: app.id, 
                        app: app, 
                        startDrag: this.props.startDrag, 
                        endDrag: this.props.endDrag, 
                        dragging: this.props.dragging, 
                        dropCallback: this.props.dropCallback}
                    );
                }.bind(this));


                if (this.props.pendingApps && this.props.dash.main) {
                    for (var i in this.props.pendingApps) {
                        var app = this.props.pendingApps[i];
                        icons.push(
                            React.createElement(PendingApp, {
                                key: app.id, 
                                app: app}
                            )
                        );
                    }
                }

                icons.push(
                    React.createElement(AddNew, {
                        key: "last", 
                        dragging: this.props.dragging, 
                        dropCallback: this.props.dropCallback}
                    )
                );


                return (
                    React.createElement("div", {className: "col-sm-10 desktop"}, 
            icons
                    )
                );
            }
        }
    });

    var AppZone = React.createClass({displayName: "AppZone",
        render: function () {
            return (
                React.createElement("div", {className: "appzone"}, 
                    React.createElement(DropZone, {dragging: this.props.dragging, dropCallback: this.props.dropCallback(this.props.app)}), 
                    React.createElement(AppIcon, {app: this.props.app, startDrag: this.props.startDrag, endDrag: this.props.endDrag})
                )
            );
        }
    });

    var AddNew = React.createClass({displayName: "AddNew",
        render: function () {
            return (
                React.createElement("div", {className: "appzone"}, 
                    React.createElement(DropZone, {dragging: this.props.dragging, dropCallback: this.props.dropCallback("last")}), 
                    React.createElement("div", {className: "app text-center"}, 
                        React.createElement("a", {href: store_root, className: "add-more", draggable: "false"}, 
                            React.createElement("img", {src: image_root + "icon/plus.png"})
                        ), 
                        React.createElement("p", null, t('ui.add'))
                    )
                )
            );
        }
    });

    var PendingApp = React.createClass({displayName: "PendingApp",
        render: function () {
            return (
                React.createElement("div", {className: "appzone"}, 
                    React.createElement("div", {className: "dropzone"}), 
                    React.createElement("div", {className: "app text-center", draggable: "false"}, 
                        React.createElement("img", {src: this.props.app.icon, alt: this.props.app.name, draggable: "false", className: "pending"}), 
                        React.createElement("p", null, this.props.app.name)
                    )
                )
            );
        }
    });

    var DropZone = React.createClass({displayName: "DropZone",
        getInitialState: function () {
            return {over: false};
        },
        dragOver: function (event) {
            event.preventDefault();         // allow dropping here!
            this.setState({over: true});
        },
        dragLeave: function () {
            this.setState({over: false});
        },
        drop: function (event) {
            var data = JSON.parse(event.dataTransfer.getData("application/json"));
            this.props.dropCallback(data.app);
            this.setState({over: false});
        },
        render: function () {
            var className = "dropzone" + (this.state.over ? " dragover" : "") + (this.props.dragging ? " dragging" : "");

            return React.createElement("div", {className: className, onDragOver: this.dragOver, onDragLeave: this.dragLeave, onDrop: this.drop});
        }
    });

    var AppIcon = React.createClass({displayName: "AppIcon",
        render: function () {
            var notif = null;
            var url = this.props.app.url;
            if (this.props.app.notificationCount != 0) {
                notif = (
                    React.createElement("span", {className: "badge badge-notifications"}, this.props.app.notificationCount)
                );
                url = this.props.app.notificationUrl;
            }
            return (
                React.createElement("div", {className: "app text-center", draggable: "true", onDragStart: this.props.startDrag(this.props.app), onDragEnd: this.props.endDrag}, 
                    React.createElement("img", {src: this.props.app.icon, alt: this.props.app.name}), 
                    React.createElement("a", {href: url, target: "_new", className: "app-link", draggable: "false"}), 
                    notif, 
                    React.createElement("p", null, this.props.app.name)
                )
            );
        }
    });

    React.renderComponent(
        React.createElement(Dashboard, null),
        document.getElementById("dashboard")
    );


}());