'use strict';

import React from 'react';
import {Link} from 'react-router-dom';
import customFetch from "../util/custom-fetch";
import config from '../config/config';

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
} from '../util/dashboard';

import UpdateTitle from '../components/update-title';

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

class Dashboard extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            notificationsChecked: false,
            dashboards: null,
            dash: null,
            apps: null,
            pendingApps: null,
            dragging: false,
            draggingPending: false,
            loadingDashboards: true,
            loadingApps: true
        }
    }

    componentDidMount = () => {
        const dashId = this.props.id;

        fetchDashboards()
            .then(dashboards => {
                const currentDash = dashboards.find(dash => {
                    return dash.id === dashId;
                }) || dashboards[0];

                this.setState({dashboards, dash: currentDash, loadingDashboards: false});
            });

        this.loadApps(dashId);
    }

    componentWillReceiveProps = (nextProps) => {
        if (!this.state.dashboards) {
            return;
        }

        const currentDash = this.state.dashboards.find(dash => {
            return dash.id === nextProps.id;
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
    }

    componentWillUnmount = () => {
        if (this.state.timeoutId) {
            clearTimeout(this.state.timeoutId);
        }
    }

    loadApps = (dashId) => {
        Promise.all([fetchApps(dashId), fetchPendingApps()])
            .then(data => {
                this.setState({
                    apps: data[0],
                    pendingApps: data[1],
                    loadingApps: false
                });
                this.initNotificationsCheck();
            });
    }

    checkNotifications = () => {
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
        const timeoutId = window.setTimeout(this.checkNotifications, config.dashboardNotificationsInterval);
        this.setState({ timeoutId: timeoutId })
    }

    initNotificationsCheck = () => {
        if (!this.notificationsChecked) {
            this.notificationsChecked = true;
            this.checkNotifications();
        }
    }

    findById = (array, obj) => {
        return array.findIndex((item) => {
            return item.id === obj.id;
        });
    }

    startDrag = (app) => {
        return (event) => {
            event.dataTransfer.setData('application/json', JSON.stringify({app}));
            this.setState({dragging: true});
        };
    }

    startDragPending = (app) => {
        return (event) => {
            event.dataTransfer.setData('application/json', JSON.stringify({app}));
            this.setState({draggingPending: true});
        };
    }

    endDrag = () => {
        this.setState({dragging: false});
    }

    endDragPending = () => {
        this.setState({draggingPending: false});
    }

    reorderApps = (before) => {
        return (app) => {
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

            fetchReorderApps(this.state.dash.id, apps)
                .catch((err) => { //cancel
                    console.error(err);
                    this.setState({apps: oldApps});
                });
        };
    }

    moveToDash = (dash) => {
        return (app) => {
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
        };
    }

    deleteApp = (app) => {
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
    }

    deletePending = (app) => {
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
    }

    createDash = (name) => {
        fetchCreateDashboard(name)
            .then((dashboard) => {
                const dashboards = Object.assign([], this.state.dashboards);
                dashboards.push(dashboard);
                this.setState({dashboards});
            });
    }

    renameDash = (dash) => {
        return (name) => {
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

        };
    }

    removeDash = (dash) => {
        return () => {
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
        };
    }

    render() {
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
};


function DashList(props) {
    if (props.loading) {
        return (
            <div className="text-center">
                <i className="fa fa-spinner fa-spin loading"/> {i18n._(t`ui.loading`)}
            </div>
        );
    } else {
        const dashboards = props.dashboards.map(dash => {
            return (
                <DashItem
                    key={dash.id}
                    dash={dash}
                    active={dash.id === props.currentDash.id}
                    dragging={props.dragging}
                    moveToDash={props.moveToDash(dash)}
                    rename={props.renameDash(dash)}
                    remove={props.removeDash(dash)}
                />
            );
        });

        return (
            <ul className="nav nav-tabs" role="tablist">
                {dashboards}
                <CreateDashboard addDash={props.createDash}/>
            </ul>
        );
    }
};


class DashItem extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            over: false,
            editing: false
        }
    }

    onDragEnterOrOver = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: true });
    }

    onDragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: false });
    }

    drop = async (e) => {
        e.stopPropagation();
        e.preventDefault();
        const app = JSON.parse(e.dataTransfer.getData("application/json")).app;
        this.props.moveToDash(app);
        this.setState({over: false});
    }

    edit = async () => {
        this.setState({editing: true});
    }

    cancelEditing = async () => {
        this.setState({editing: false});
    }

    render() {
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
                    onDragEnter={this.onDragEnterOrOver}
                    onDragOver={this.onDragEnterOrOver}
                    onDragLeave={this.onDragLeave}
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
};

class DashActions extends React.Component {

    remove = () => {
        this.props.remove();
        this.refs.modal.close();
    }

    showRemove = () => {
        this.refs.modal.open();
    }

    render() {
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
};

class EditingDash extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            val: this.props.name
        }
    }

    componentDidMount = () => {
        const input = this.refs.input;
        input.focus();
        input.select();
    }

    change = (event) => {
        this.setState({val: event.target.value});
    }

    click = (event) => {
        event.preventDefault();
        event.stopPropagation();
    }

    submit = (event) => {
        event.preventDefault();
        this.props.rename(this.state.val);
        this.props.cancel();
    }

    render() {
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
};

class CreateDashboard extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            val: '',
            error: false
        }
    }

    change = (event) => {
        this.setState({val: event.target.value});
    }

    showCreate = () => {
        this.refs.modal.open();
    }

    submit = (event) => {
        event.preventDefault();
        if (this.state.val === '') {
            this.setState({ error: true });
        } else {
            this.props.addDash(this.state.val);
            this.setState({ val: '', error: false });
            this.refs.modal.close();
        }
    }

    render() {
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
};


class DeleteApp extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            over: false,
            app: null,
            pending: false
        }
    }

    onDragEnterOrOver = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: true });
    }

    onDragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: false });
    }

    drop = async (e) => {
        e.stopPropagation();
        e.preventDefault();

        const app = JSON.parse(e.dataTransfer.getData("application/json")).app;
        this.setState({over: true, app: app, pending: this.props.draggingPending});
        this.refs.modal.open();
    }

    removeApp = () => {
        const app = this.state.app;
        if (!this.state.pending) {
            this.props.delete(app);
        } else {
            this.props.deletePending(app);
        }
        this.refs.modal.close();
        this.setState({ over: false, app: null, pending: false });
    }

    cancel = () => {
        this.setState({ over: false, app: null, pending: false });
    }

    render() {
        const className = 'appzone' + (this.state.over ? ' over' : '');
        const buttonLabels = {save: i18n._(t`ui.confirm`), cancel: i18n._(t`ui.cancel`)};

        return (
            <div className={className}
                 onDragOver={this.onDragEnterOrOver} onDragEnter={this.onDragEnterOrOver}
                 onDragLeave={this.onDragLeave} onDrop={this.drop}>
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
};


class Desktop extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            dragging: false
        }
    }

    render() {
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
};


function AppZone(props) {
    return (
        <div className="appzone" title={props.app.name}>
            <DropZone dragging={props.dragging} dropCallback={props.dropCallback(props.app)}>
                <AppIcon app={props.app} startDrag={props.startDrag} endDrag={props.endDrag}/>
            </DropZone>
        </div>
    );
};

function PendingApp(props) {
    return (
        <div className="appzone">
            <div className="app pending disabled" draggable="true" onDragStart={props.startDrag(props.app)}
                 onDragEnd={props.endDrag}>
                <img className="image" src={props.app.icon} alt={props.app.name} draggable="false"/>

                <p>{props.app.name} <i className="fa fa-stopwatch"/></p>

            </div>
        </div>
    );
};

function StoppedApp(props) {
    return (
        <div className="appzone">
            <div className="app pending disabled" draggable="true" onDragStart={props.startDrag(props.app)}
                 onDragEnd={props.endDrag}>
                <img className="image" src={props.app.icon} alt={props.app.name} draggable="false"/>

                <p>{props.app.name} <i className="fa fa-stop-circle"/></p>

            </div>
        </div>
    );
};

class DropZone extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            over: false
        }
    }

    onDragEnterOrOver = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: true });
    }

    onDragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        this.setState({ over: false });
    }

    drop = async (e) => {
        e.stopPropagation();
        e.preventDefault();
        const data = JSON.parse(e.dataTransfer.getData("application/json"));
        this.props.dropCallback(data.app);
        this.setState({over: false});
    }

    render() {
        const className = "dropzone" + (this.state.over ? " dragover" : "") + (this.props.dragging ? " dragging" : "");

        return <div className={className}
                    onDragEnter={this.onDragEnterOrOver}
                    onDragOver={this.onDragEnterOrOver}
                    onDragLeave={this.onDragLeave}
                    onDrop={this.drop}>
            {this.props.children}
        </div>;
    }
};

function AppIcon(props) {
    let notif = null;
    const url = props.app.url;
    if (props.app.notificationCount !== 0) {
        notif = (
            <span className="badge badge-notifications">{props.app.notificationCount}</span>
        );
    }
    return (
        <div className="app" draggable="true" onDragStart={props.startDrag(props.app)}
             onDragEnd={props.endDrag}>
            <a href={url} target="_blank" rel="noopener" className="app-link" draggable="false">
                <img className="image" src={props.app.icon} alt={props.app.name}/>
                {notif}
            </a>
            <p>{props.app.name}</p>
        </div>
    );
};

function DashboardWrapper(props) {

    return <section className="oz-body wrapper flex-col">
        <UpdateTitle title={i18n._(t`my.dashboard`)}/>

        <header className="title">
            <span>{i18n._(t`my.dashboard`)}</span>
        </header>

        <Dashboard id={props.match.params.id} history={props.history}/>

        <div className="push"/>
    </section>;
}

export default DashboardWrapper;
