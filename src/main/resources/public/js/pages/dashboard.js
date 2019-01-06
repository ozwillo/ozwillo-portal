'use strict';

import React from 'react';
import {Link} from 'react-router-dom';
import {withRouter} from 'react-router';
import createClass from 'create-react-class';
import customFetch from "../util/custom-fetch";

import {ModalWithForm, Modal} from '../components/bootstrap-react';
import {
    fetchDashboards,
    fetchCreateDashboard,
    fetchRenameDashboard,
    fetchDeleteDashboard,
    fetchApps,
    fetchPendingApps,
    fetchReorderApps,
    fetchDeleteApp,
    fetchDeletePendingApp,
    moveToDash
} from '../actions/dashboard';

import UpdateTitle from '../components/update-title';

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

const Dashboard = withRouter(createClass({
    notificationsChecked: false,
    getInitialState: function () {
        return {
            dashboards: null,
            dash: null,
            apps: null,
            pendingApps: null,
            dragging: false,
            draggingPending: false,
            loadingDashboards: true,
            loadingApps: true
        };
    },
    componentDidMount: function () {
        const dashId = this.props.match.params.id;

        fetchDashboards()
            .then(dashboards => {
                const currentDash = dashboards.find(dash => {
                    return dash.id === dashId;
                }) || dashboards[0];

                this.setState({dashboards, dash: currentDash, loadingDashboards: false});
            });

        this.loadApps(dashId);

    },
    componentWillUnmount() {
        if (this.state.timeoutId) {
            clearTimeout(this.state.timeoutId);
        }
    },
    loadApps(dashId) {
        Promise.all([fetchApps(dashId), fetchPendingApps()])
            .then(data => {
                this.setState({
                    apps: data[0],
                    pendingApps: data[1],
                    loadingApps: false
                });
                this.initNotificationsCheck();
            });
    },
    checkNotifications() {
        if (this.state.apps) {
            customFetch("/my/api/dashboard/notifications")
                .then(appNotifs => {
                    const apps = this.state.apps;
                    for (let i = 0; i < apps.length; i++) {
                        const app = apps[i];
                        if (appNotifs[app.serviceId]) {
                            app.notificationCount = appNotifs[app.serviceId];
                        } else {
                            app.notificationCount = 0;
                        }
                    }
                    this.setState({ apps: apps });
                }).catch(err => console.error("Cannot check notifications", status, err));
        }
        const timeoutId = window.setTimeout(this.checkNotifications, 10000);
        this.setState({ timeoutId: timeoutId })
    },
    initNotificationsCheck() {
        if (!this.notificationsChecked) {
            this.notificationsChecked = true;
            this.checkNotifications();
        }
    },
    findById: function (array, obj) {
        return array.findIndex((item) => {
            return item.id === obj.id;
        });
    },
    startDrag: function (app) {
        return function (event) {
            event.dataTransfer.setData('application/json', JSON.stringify({app}));
            this.setState({dragging: true});
        }.bind(this);
    },
    startDragPending: function (app) {
        return function (event) {
            event.dataTransfer.setData('application/json', JSON.stringify({app}));
            this.setState({draggingPending: true});
        }.bind(this);
    },
    endDrag: function () {
        this.setState({dragging: false});
    },
    endDragPending: function () {
        this.setState({draggingPending: false});
    },
    reorderApps: function (before) {
        return function (app) {
            const oldApps = this.state.apps;
            const apps = Object.assign([], this.state.apps);

            const fr = this.findById(apps, app);
            const to = this.findById(this.state.apps, before);
            const removed = apps.splice(fr, 1);
            apps.splice(to, 0, removed[0]);

            this.setState({
                apps,
                dragging: false
            });

            fetchReorderApps(state.dash.id, apps)
                .catch((err) => { //cancel
                    console.error(err);
                    this.setState({apps: oldApps});
                });
        }.bind(this);
    },
    moveToDash: function (dash) {
        return function (app) {
            const oldApps = this.state.apps;
            const apps = Object.assign([], this.state.apps);

            //Remove app
            const i = this.findById(apps, app);
            apps.splice(i, 1);

            this.setState({
                apps,
                dragging: false
            });

            moveToDash(app.id, dash.id)
                .catch((err) => { //cancel
                    console.error(err);
                    this.setState({apps: oldApps});
                });
        }.bind(this);
    },

    deleteApp: function (app) {
        const oldApps = this.state.apps;
        const apps = Object.assign([], this.state.apps);

        //Remove app
        const i = this.findById(apps, app);
        apps.splice(i, 1);

        this.setState({
            apps,
            dragging: false
        });

        fetchDeleteApp(app.id)
            .catch((err) => { //cancel
                console.error(err);
                this.setState({apps: oldApps});
            });
    },

    deletePending: function (app) {
        const oldApps = this.state.apps;
        const apps = Object.assign([], this.state.apps);

        //Remove app
        const i = this.findById(apps, app);
        apps.splice(i, 1);

        this.setState({
            apps,
            dragging: false
        });

        fetchDeletePendingApp(app.id)
            .catch((err) => { //cancel
                console.error(err);
                this.setState({apps: oldApps});
            });
    },

    createDash: function (name) {
        fetchCreateDashboard(name)
            .then((dashboard) => {
                const dashboards = Object.assign([], this.state.dashboards);
                dashboards.push(dashboard);
                this.setState({dashboards});
            });
    },

    renameDash: function (dash) {
        return function (name) {
            const oldDashboards = this.state.dashboards;
            const dashboards = Object.assign([], this.state.dashboards);
            const i = this.findById(dashboards, dash);
            dashboards[i].name = name;
            this.setState({dashboards});

            fetchRenameDashboard(dash.id, name)
                .catch((err) => { // cancel
                    console.error(err);
                    this.setState({dashboards: oldDashboards});
                });

        }.bind(this);
    },
    removeDash: function (dash) {
        return function () {
            this.setState({loadingDashboards: true});

            fetchDeleteDashboard(dash.id)
                .then(() => {
                    const dashboards = Object.assign([], this.state.dashboards);
                    const i = this.findById(dashboards, dash);
                    dashboards.splice(i, 1);


                    this.setState({dashboards, loadingDashboards: false});
                    this.props.history.push('/my/');

                })
                .catch((err) => {
                    console.error(err);
                    this.setState({loadingDashboards: false});
                });
        }.bind(this);
    },
    componentWillReceiveProps: function (nextProps) {
        if (!this.state.dashboards) {
            return;
        }

        const currentDash = this.state.dashboards.find(dash => {
            return dash.id === nextProps.match.params.id;
        }) || this.state.dashboards[0];

        //Update dashboard
        this.setState({dash: currentDash, apps: [], loadingApps: true});

        //Load apps
        const oldState = this.state;
        fetchApps(currentDash.id)
            .then((apps) => {
                this.setState({apps, loadingApps: false});
            })
            .catch((err) => { //cancel
                console.error(err);
                this.setState(oldState);
            });
    },
    render: function () {
        return (
            <section id="dashboard" className="flex-col">
                <DashList
                    loading={this.state.loadingDashboards}
                    dashboards={this.state.dashboards}
                    currentDash={this.state.dash}
                    dragging={this.state.dragging}
                    draggingPending={this.state.draggingPending}
                    moveToDash={this.moveToDash}
                    renameDash={this.renameDash}
                    removeDash={this.removeDash}
                    createDash={this.createDash}/>
                <Desktop
                    dash={this.state.dash}
                    loading={this.state.loadingApps}
                    apps={this.state.apps}
                    pendingApps={this.state.pendingApps}
                    deleteApp={this.deleteApp}
                    deletePending={this.deletePending}
                    startDrag={this.startDrag}
                    startDragPending={this.startDragPending}
                    endDrag={this.endDrag}
                    endDragPending={this.endDragPending}
                    dragging={this.state.dragging}
                    draggingPending={this.state.draggingPending}
                    dropCallback={this.reorderApps}/>
            </section>
        );
    }
}));


const DashList = createClass({
    render: function () {
        if (this.props.loading) {
            return (
                <div className="text-center">
                    <i className="fa fa-spinner fa-spin loading"/> {i18n._(t`ui.loading`)}
                </div>
            );
        } else {
            const dashboards = this.props.dashboards.map(function (dash) {
                return (
                    <DashItem
                        key={dash.id}
                        dash={dash}
                        active={dash.id === this.props.currentDash.id}
                        dragging={this.props.dragging}
                        moveToDash={this.props.moveToDash(dash)}
                        rename={this.props.renameDash(dash)}
                        remove={this.props.removeDash(dash)}
                    />
                );
            }.bind(this));

            return (
                <ul className="nav nav-tabs" role="tablist">
                    {dashboards}
                    <CreateDashboard addDash={this.props.createDash}/>
                </ul>
            );
        }
    }
});


const DashItem = createClass({
    getInitialState: function () {
        return {over: false, editing: false};
    },
    over: function (isOver) {
        return function (event) {
            event.preventDefault();
            this.setState({over: isOver});
        }.bind(this);
    },
    drop: function (event) {
        const app = JSON.parse(event.dataTransfer.getData("application/json")).app;
        this.props.moveToDash(app);
        this.setState({over: false});
    },
    edit: function () {
        this.setState({editing: true});
    },
    cancelEditing: function () {
        this.setState({editing: false});
    },
    render: function () {
        if (this.props.active) {
            if (this.state.editing) {
                return <EditingDash name={this.props.dash.name} rename={this.props.rename}
                                    cancel={this.cancelEditing}/>;
            } else {
                return (
                    <li className="active" role="presentation">
                        <a>
                            {this.props.dash.name}
                            <DashActions remove={this.props.remove} edit={this.edit} primary={this.props.dash.main}/>
                        </a>
                    </li>
                );
            }
        } else {
            const className = this.props.dragging ? (this.state.over ? 'dragging over' : 'dragging') : '';
            return (
                <li role="presentation"
                    onDragOver={this.over(true)}
                    onDragLeave={this.over(false)}
                    onDrop={this.drop}>
                    <Link to={`/my/dashboard/${this.props.dash.id}`}
                          className={className}
                          data-toggle="tab"
                          role="tab"
                          aria-controls="profile">
                        {this.props.dash.name}
                    </Link>
                </li>
            );
        }
    }
});

const DashActions = createClass({

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
                <i className="fas fa-pencil-alt" onClick={this.props.edit}/>
            );
        } else {
            const buttonLabels = {"save": i18n._(t`ui.confirm`), "cancel": i18n._(t`ui.cancel`)};
            return (
                <span>
                    <Modal title={i18n._(t`my.confirm-delete-dash`)} successHandler={this.remove}
                           buttonLabels={buttonLabels} saveButtonClass="btn-default-inverse" ref="modal">
                        <span>{i18n._(t`my.confirm-delete-dash-long`)}</span>
                    </Modal>

                    <i className="fas fa-pencil-alt" onClick={this.props.edit}/>
                    <i className="fa fa-trash" onClick={this.showRemove}/>
                </span>
            );
        }
    }
});


const EditingDash = createClass({
    getInitialState: function () {
        return {val: this.props.name};
    },
    componentDidMount: function () {
        const input = this.refs.input;
        input.focus();
        input.select();
    },
    change: function (event) {
        this.setState({val: event.target.value});
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
            <li className="active">
                <form onSubmit={this.submit}>
                    <input ref="input" type="text" className="form-control" value={this.state.val}
                           onChange={this.change}
                           onClick={this.click} required={true} autoFocus={true}/>
                </form>
                <div className="dash-veil" onClick={this.props.cancel}/>
            </li>
        );
    }
});

const CreateDashboard = createClass({
    getInitialState: function () {
        return {
            val: '',
            error: false
        };
    },
    change: function (event) {
        this.setState({val: event.target.value});
    },
    showCreate: function () {
        this.refs.modal.open();
    },
    submit: function (event) {
        event.preventDefault();
        if (this.state.val === '') {
            this.setState({error: true});
        } else {
            this.props.addDash(this.state.val);
            this.setState(this.getInitialState());
            this.refs.modal.close();
        }
    },
    render: function () {
        const buttonLabels = {'save': i18n._(t`ui.confirm`), 'cancel': i18n._(t`ui.cancel`)};
        const formGroupClass = this.state.error ? 'form-group has-error' : 'form-group';

        return (
            <li>
                <ModalWithForm title={i18n._(t`create`)} successHandler={this.submit}
                               buttonLabels={buttonLabels} ref="modal">
                    <div className={formGroupClass}>
                        <label htmlFor="dashboardName" className="col-sm-4 control-label required">
                            {i18n._(t`my.name`)} *
                        </label>
                        <div className="col-sm-8">
                            <input type="text" id="dashboardName" className="form-control"
                                   placeholder={i18n._(t`name`)} onChange={this.change}/>
                        </div>
                    </div>
                </ModalWithForm>
                <a href="#" onClick={this.showCreate} className="create">
                    <i className="fa fa-plus"/>
                </a>
            </li>
        );
    }
});


const DeleteApp = createClass({
    getInitialState: function () {
        return {over: false, app: null, pending: false};
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
        const app = JSON.parse(event.dataTransfer.getData("application/json")).app;

        this.setState({over: true, app: app, pending: this.props.draggingPending});
        this.refs.modal.open();
    },
    removeApp: function () {
        const app = this.state.app;
        if (!this.state.pending) {
            this.props.delete(app);
        } else {
            this.props.deletePending(app);
        }
        this.refs.modal.close();
        this.setState(this.getInitialState());
    },
    cancel: function () {
        this.setState(this.getInitialState());
    },
    render: function () {
        const className = 'appzone' + (this.state.over ? ' over' : '');
        const buttonLabels = {save: i18n._(t`ui.confirm`), cancel: i18n._(t`ui.cancel`)};

        return (
            <div className={className}
                 onDragOver={this.over(true)} onDragLeave={this.over(false)} onDrop={this.drop}>
                <Modal title={i18n._(t`my.confirm-remove-app`)} successHandler={this.removeApp}
                       cancelHandler={this.cancel}
                       buttonLabels={buttonLabels} ref="modal">
                    <p>{i18n._(t`my.confirm-remove-app-long`)}</p>
                </Modal>
                <div className="app">
                    <div className="action-icon">
                        <span title={i18n._(t`my.drop-to-remove`)}>
                            <i className="fa fa-trash fa-3x fa-border"/>
                        </span>
                    </div>
                </div>
            </div>
        );
    }
});


const Desktop = createClass({
    getInitialState: function () {
        return {dragging: false};
    },
    render: function () {
        if (this.props.loading) {
            return (
                <div className="col-sm-12 text-center">
                    <i className="fa fa-spinner fa-spin loading"/> {i18n._(t`ui.loading`)}
                </div>
            );
        }

        return (
            <section className="all-apps">
                <ul className="list undecorated-list flex-row">
                    {/* Apps */}
                    {
                        this.props.apps.map(app => {
                            if (app.status === 'AVAILABLE') {
                                return <li key={app.id} className="item">
                                    <AppZone
                                        app={app}
                                        startDrag={this.props.startDrag}
                                        endDrag={this.props.endDrag}
                                        dragging={this.props.dragging}
                                        dropCallback={this.props.dropCallback}
                                    />
                                </li>
                            } else {
                                return <li key={app.id} className="item">
                                    <StoppedApp
                                        app={app}
                                        startDrag={this.props.startDragPending}
                                        endDrag={this.props.endDragPending}
                                    />
                                </li>;
                            }
                        })
                    }

                    {/*Pending apps*/}
                    {
                        this.props.pendingApps && this.props.dash.main &&
                        this.props.pendingApps.map(app => {
                            return <li key={app.id} className="item">
                                <PendingApp
                                    app={app}
                                    startDrag={this.props.startDragPending}
                                    endDrag={this.props.endDragPending}
                                />
                            </li>;
                        })
                    }
                    <li className="item">
                        <DeleteApp
                            key="delete-icon"
                            dragging={this.props.dragging}
                            draggingPending={this.props.draggingPending}
                            delete={this.props.deleteApp}
                            deletePending={this.props.deletePending}/>
                    </li>

                </ul>
            </section>
        );
    }
});


const AppZone = createClass({
    render: function () {
        return (
            <div className="appzone" title={this.props.app.name}>
                <DropZone dragging={this.props.dragging} dropCallback={this.props.dropCallback(this.props.app)}>
                    <AppIcon app={this.props.app} startDrag={this.props.startDrag} endDrag={this.props.endDrag}/>
                </DropZone>
            </div>
        );
    }
});

const PendingApp = createClass({
    render: function () {
        return (
            <div className="appzone">
                <div className="app pending disabled" draggable="true" onDragStart={this.props.startDrag(this.props.app)}
                     onDragEnd={this.props.endDrag}>
                    <img className="image" src={this.props.app.icon} alt={this.props.app.name} draggable="false"/>

                    <p>{this.props.app.name} <i className="fa fa-stopwatch"/></p>

                </div>
            </div>
        );
    }
});

const StoppedApp = createClass({
    render: function () {
        return (
            <div className="appzone">
                <div className="app pending disabled" draggable="true" onDragStart={this.props.startDrag(this.props.app)}
                     onDragEnd={this.props.endDrag}>
                    <img className="image" src={this.props.app.icon} alt={this.props.app.name} draggable="false"/>

                    <p>{this.props.app.name} <i className="fa fa-stop-circle"/></p>

                </div>
            </div>
        );
    }
});

const DropZone = createClass({
    getInitialState: function () {
        return {over: false};
    },
    dragOver: function (event) {
        if (this.props.dragging) {
            event.preventDefault();         // allow dropping here!
            this.setState({over: true});
        }
    },
    dragLeave: function () {
        this.setState({over: false});
    },
    drop: function (event) {
        const data = JSON.parse(event.dataTransfer.getData("application/json"));
        this.props.dropCallback(data.app);
        this.setState({over: false});
    },
    render: function () {
        const className = "dropzone" + (this.state.over ? " dragover" : "") + (this.props.dragging ? " dragging" : "");

        return <div className={className} onDragOver={this.dragOver} onDragLeave={this.dragLeave} onDrop={this.drop}>
            {this.props.children}
        </div>;
    }
});

const AppIcon = createClass({
    render: function () {
        let notif = null;
        const url = this.props.app.url;
        if (this.props.app.notificationCount !== 0) {
            notif = (
                <span className="badge badge-notifications">{this.props.app.notificationCount}</span>
            );
        }
        return (
            <div className="app" draggable="true" onDragStart={this.props.startDrag(this.props.app)}
                 onDragEnd={this.props.endDrag}>
                <a href={url} target="_blank" rel="noopener" className="app-link" draggable="false">
                    <img className="image" src={this.props.app.icon} alt={this.props.app.name}/>
                    {notif}
                </a>
                <p>{this.props.app.name}</p>
            </div>
        );
    }
});

class DashboardWrapper extends React.Component {


    render() {
        return <section className="oz-body wrapper flex-col">
            <UpdateTitle title={i18n._(t`my.dashboard`)}/>

            <header className="title">
                <span>{i18n._(t`my.dashboard`)}</span>
            </header>

            <Dashboard/>

            <div className="push"/>
        </section>;
    }
}

export default DashboardWrapper;
