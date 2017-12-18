'use strict';

import '../util/csrf';
import '../util/my';

import React from 'react';
import createClass from 'create-react-class';

import config from '../config/config';
import PropTypes from "prop-types";
const notificationInterval = config.notificationInterval;


var NotificationTable = createClass({
    getInitialState: function() {
        return {
            n: [],
            apps: [],
            currentSort: {
                prop: 'date', // date, changing to dataText to test issue #217
                dir: -1
            },
            filter: {
                appId: null,
                status: "UNREAD"
            }
        };
    },
    componentDidMount: function() {
        this.loadNotifications();
        setInterval(this.loadNotifications, notificationInterval);
    },
    loadNotifications: function() {
        $.ajax({
            url: notificationService,
            data: {status: this.state.filter.status},
            dataType: 'json',
            success: (data) =>
                this.setState({ apps: data.apps, n: data.notifications.sort(this.sortElement) }),
            error: function(xhr, status, err) {
                console.error(notificationService, status, err.toString());
            }.bind(this)
        });
    },
    sortBy: function(criterion) {
        var component = this;
        return function() {
            var currentSort = component.state.currentSort;

            var sortDirection = -1;
            if (currentSort.prop == criterion) {
                // we are already sorting by the given criterion, so let's sort inversely
                sortDirection = currentSort.dir * -1;
            }
            currentSort.prop = criterion;
            currentSort.dir = sortDirection;

            var n = component.state.n.sort(this.sortElement);
            var state = component.state;
            state.n = n;
            state.currentSort = currentSort;
            component.setState(state);
        }.bind(this);
    },
    sortElement: function(a,b){
        var currentSort = this.state.currentSort;

        if (typeof a[currentSort.prop] == typeof b[currentSort.prop]) {
            if (typeof a[currentSort.prop] == "number") {
                return (a[currentSort.prop] - b[currentSort.prop]) * currentSort.dir;
            }
            if (typeof a[currentSort.prop] == "string") {
                return a[currentSort.prop].localeCompare(b[currentSort.prop]) * currentSort.dir;
            } else {
                return currentSort.dir;
            }
        }

        if (typeof a[currentSort.prop] == "undefined"){
            return -currentSort.dir;
        }
        if (typeof b[currentSort.prop] == "undefined"){
            return currentSort.dir;
        }

        // In all other case, a basic and hazardous comparaison
        return  (a[currentSort.prop] < b[currentSort.prop] ? -1 : a[currentSort.prop] > b[currentSort.prop] ? 1 : 0) * currentSort.dir;

    },
    filterByStatus: function (event) {
        event.preventDefault();
        var state = this.state;
        state.filter.status = event.target.value;
        this.setState(state);
        this.loadNotifications();
    },
    filterByApp: function (event) {
        event.preventDefault();
        var state = this.state;
        var appId = event.target.value;
        if (appId == "all") {
            appId = null;
        }
        state.filter.appId = appId;
        this.setState(state);
    },
    removeNotif: function(id) {
        var notifs = this.state.n.filter(function(n) {return n.id != id;});

        this.setState({n:notifs});

        $.ajax({
            url: notificationService + "/" + id,
            method: 'delete',
            datatype: 'json',
            success: function(data) {
                // nothing much to say is there?
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(notificationService, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        var callback = this.removeNotif;
        var appId = this.state.filter.appId;
        var status = this.state.filter.status;
        var notificationNodes = this.state.n
            .filter(notif => appId == null || notif.applicationId == appId)
            .map(notif => <Notification key={notif.id} notif={notif} status={status} onRemoveNotif={callback}/>
            );

        if (notificationNodes.length == 0) {
            notificationNodes = <tr><td className="message" colSpan="4">{this.context.t('no-notification')}</td></tr>;
        }

        return (
            <section className="container" id="notifications">
                <div className="row">
                    <div className="col-md-12">
                        <NotificationHeader filter={this.state.filter} updateStatus={this.filterByStatus}
                                            updateAppFilter={this.filterByApp} apps={this.state.apps}/>
                    </div>
                    <div className="col-md-12">
                        <table className="table">
                            <thead>
                            <tr>
                                <SortableHeader name="date" label="date" size="2" sortBy={this.sortBy}
                                                sort={this.state.currentSort}/>
                                <SortableHeader name="appName" size="2" label="app" sortBy={this.sortBy}
                                                sort={this.state.currentSort}/>
                                <th className="col-sm-5">{this.context.t('message')}</th>
                                <th className="col-sm-3"></th>
                            </tr>
                            </thead>
                            <tbody>
                            {notificationNodes}
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        );
    }

});
NotificationTable.contextTypes = {
    t: PropTypes.func.isRequired
};

var SortableHeader = createClass({
    render: function () {
        var className = "col-sm-" + this.props.size + " sortable color";
        var sortIcon = <i className="fa fa-sort"></i>;
        if (this.props.sort.prop == this.props.name) {
            if (this.props.sort.dir == -1) {
                sortIcon = <i className="fa fa-sort-desc"></i>;
            } else {
                sortIcon = <i className="fa fa-sort-asc"></i>;
            }
        }

        return (
            <th className={className} onClick={this.props.sortBy(this.props.name)}>{this.context.t(this.props.label)} {sortIcon}</th>
        );
    }

});
SortableHeader.contextTypes = {
    t: PropTypes.func.isRequired
};

var NotificationHeader = createClass({
    render: function () {
        return (
            <div className="pull-right">
                <form className="form-inline">
                    <div className="form-group btn-line">
                        <AppFilter apps={this.props.apps} onChange={this.props.updateAppFilter}/>
                    </div>
                    <div className="form-group">
                        <select name="status" className="form-control" onChange={this.props.updateStatus}>
                            <option value="UNREAD">{this.context.t('unread')}</option>
                            <option value="READ">{this.context.t('read')}</option>
                            <option value="ANY">{this.context.t('any')}</option>
                        </select>
                    </div>
                </form>
            </div>
        );
    }
});
NotificationHeader.contextTypes = {
    t: PropTypes.func.isRequired
};

var AppFilter = createClass({
    render: function () {
        var options = this.props.apps.map(function (app) {
            return <option key={app.id} value={app.id}>{app.name}</option>;
        });
        return (
            <select name="app" className="form-control" onChange={this.props.onChange}>
                <option value="all">{this.context.t('all-apps')}</option>
                {options}
            </select>
        );
    }
});
AppFilter.contextTypes = {
    t: PropTypes.func.isRequired
};

var Notification = createClass({
    displayName: "Notification",
    removeNotif: function () {
        this.props.onRemoveNotif(this.props.notif.id);
    },
    render: function() {
        var action_by_url = null;
        var action_archive = null;

        if (this.props.notif.url) {
            var className = "btn btn-default pull-right";
            if (this.props.notif.status !== "READ")
                className = className + " btn-line";
            action_by_url = <a href={this.props.notif.url} target="_new" className={className} >{this.props.notif.actionText}</a>;
        }
        if(this.props.notif.status !== "READ") {
            action_archive = <a href="#" className="btn btn-default pull-right" onClick={this.removeNotif}>{this.context.t('archive')}</a>;
        }

        return (
            <tr>
                <td className="col-sm-2 date">{this.props.notif.dateText}</td>
                <td className="col-sm-2 app">{this.props.notif.appName}</td>
                <td className="col-sm-5 message" dangerouslySetInnerHTML={{__html: this.props.notif.formattedText}}></td>
                <td className="col-sm-3">
                    {action_archive} {action_by_url}
                </td>
            </tr>
            );
    }
});
Notification.contextTypes = {
    t: PropTypes.func.isRequired
};

class NotificationTableWrapper extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <div className="oz-body page-row page-row-expanded">

            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        <h1 className="text-center">
                            <img src="/img/bell.png" />
                            <span>{this.context.t('ui.notifications')}</span>
                        </h1>
                    </div>
                </div>
            </div>

            <div className="oz-body-content">
                <NotificationTable/>
            </div>

            <div className="push"></div>
        </div>;
    }
}


export default NotificationTableWrapper;
