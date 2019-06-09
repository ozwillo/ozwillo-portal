'use strict';

import React from 'react';
import PropTypes from "prop-types";

import UpdateTitle from '../components/update-title';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import customFetch, {urlBuilder} from '../util/custom-fetch';

class NotificationTable extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            notifications: [],
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
    }

    componentDidMount = async () => {
        this.loadNotifications();
    }

    loadNotifications = async () => {
        customFetch(urlBuilder('/my/api/notifications', { status: this.state.filter.status } )).then(data => this.setState(data));
    }

    archive = async (id) => {
        customFetch(`/my/api/notifications/${id}`, {
            method: 'DELETE'
        }).then(() => {
            const notifications = this.state.notifications;
            this.setState({ notifications: notifications.filter(n => n.id !== id) })
        });
    }

    sortBy = async (criterion) => {
        const currentSort = this.state.currentSort;

        let sortDirection = -1;
        if (currentSort.prop === criterion) {
            // we are already sorting by the given criterion, so let's sort inversely
            sortDirection = currentSort.dir * -1;
        }
        currentSort.prop = criterion;
        currentSort.dir = sortDirection;

        this.setState({
            notifications: this.state.notifications.sort((a, b) => {
                if (currentSort.prop === 'appName') {
                    if (a['appName'] === null)
                        return -currentSort.dir;
                    else
                        return a['appName'].localeCompare(b['appName']) * currentSort.dir;
                } else if (currentSort.prop === 'date') {
                    return a['date'].localeCompare(b['date']) * currentSort.dir;
                } else {
                    return a - b;
                }
            }),
            currentSort
        });
    }

    filterByStatus = async (event) => {
        event.preventDefault();
        const state = this.state;
        state.filter.status = event.target.value;
        this.setState(state);
        this.loadNotifications();
    }

    filterByApp = async (event) => {
        event.preventDefault();
        const state = this.state;
        let appId = event.target.value;
        if (appId == "all") {
            appId = null;
        }
        state.filter.appId = appId;
        this.setState(state);
    }

    render() {
        const appId = this.state.filter.appId;
        const status = this.state.filter.status;
        let notificationNodes = this.state.notifications
            .filter(notif => appId == null || notif.applicationId == appId)
            .map(notif => <Notification key={notif.id} notif={notif} status={status} onRemoveNotif={this.archive}/>
            );

        if (notificationNodes.length == 0) {
            notificationNodes = <tr>
                <td className="message" colSpan="4">{i18n._('notif.no-notification')}</td>
            </tr>;
        }

        return (
            <section id="notifications" className="box">
                <div className="flex-col">
                    <div className="flex-row end">
                        <NotificationHeader filter={this.state.filter} updateStatus={this.filterByStatus}
                                            updateAppFilter={this.filterByApp} apps={this.state.apps}/>
                    </div>

                    <table className="table table-striped">
                        <thead>
                        <tr>
                            <SortableHeader name="date" label={i18n._('notif.date')} size="2" sortBy={this.sortBy}
                                            sort={this.state.currentSort}/>
                            <SortableHeader name="appName" size="2" label={i18n._('notif.app')} sortBy={this.sortBy}
                                            sort={this.state.currentSort}/>
                            <th className="col-sm-5">{i18n._('notif.message')}</th>
                            <th className="col-sm-3"></th>
                        </tr>
                        </thead>
                        <tbody>
                        {notificationNodes}
                        </tbody>
                    </table>
                </div>
            </section>
        );
    }

};

class SortableHeader extends React.Component {
    render() {
        const className = "col-sm-" + this.props.size + " sortable color";
        let sortIcon = <i className="fa fa-sort"></i>;
        if (this.props.sort.prop == this.props.name) {
            if (this.props.sort.dir == -1) {
                sortIcon = <i className="fa fa-sort-desc"></i>;
            } else {
                sortIcon = <i className="fa fa-sort-asc"></i>;
            }
        }

        return (
            <th className={className}
                onClick={() => this.props.sortBy(this.props.name)}>{i18n._(this.props.label)} {sortIcon}</th>
        );
    }
};


class NotificationHeader extends React.Component {
    render() {
        return (
            <div className="pull-right">
                <form className="form-inline">
                    <div className="form-group btn-line">
                        <AppFilter apps={this.props.apps} onChange={this.props.updateAppFilter}/>
                    </div>
                    <div className="form-group">
                        <select name="status" className="form-control" onChange={this.props.updateStatus}>
                            <option value="UNREAD">{i18n._('notif.unread')}</option>
                            <option value="READ">{i18n._('notif.read')}</option>
                            <option value="ANY">{i18n._('notif.any')}</option>
                        </select>
                    </div>
                </form>
            </div>
        );
    }
};


class AppFilter extends React.Component {
    render() {
        const options = this.props.apps.map(function (app) {
            return <option key={app.id} value={app.id}>{app.name}</option>;
        });
        return (
            <select name="app" className="form-control" onChange={this.props.onChange}>
                <option value="all">{i18n._('notif.all-apps')}</option>
                {options}
            </select>
        );
    }
};

class Notification extends React.Component {
    removeNotif = async () => {
        this.props.onRemoveNotif(this.props.notif.id);
    }

    render() {
        let action_by_url = null;
        let action_archive = null;

        if (this.props.notif.url) {
            const className = "btn btn-default pull-right";
            action_by_url =
                <a href={this.props.notif.url} target="_new" className={className}>{this.props.notif.actionText}</a>;
        }
        if (this.props.notif.status !== "READ") {
            action_archive = <a href="#" className="btn btn-default pull-right"
                                onClick={this.removeNotif}>{i18n._('notif.archive')}</a>;
        }

        return (
            <tr>
                <td className="col-sm-2 date">{this.props.notif.dateText}</td>
                <td className="col-sm-2 app">{this.props.notif.appName}</td>
                <td className="col-sm-5 message"
                    dangerouslySetInnerHTML={{__html: this.props.notif.formattedText}}></td>
                <td className="col-sm-3">
                    {action_archive} {action_by_url}
                </td>
            </tr>
        );
    }
};

class NotificationTableWrapper extends React.Component {

    render() {
        return <div className="oz-body wrapper flex-col">

            <UpdateTitle title={i18n._(t`ui.notifications`)}/>

            <header className="title">
                <span>{i18n._(t`ui.notifications`)}</span>
            </header>

            <NotificationTable />

            <div className="push"/>
        </div>;
    }
}


export default NotificationTableWrapper;
