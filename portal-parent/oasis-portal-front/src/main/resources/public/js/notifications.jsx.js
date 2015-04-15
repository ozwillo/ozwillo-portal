/** @jsx React.DOM */

/** Custom translation */
function t(key) {
    if (typeof _i18n != 'undefined') {
        var v = _i18n[key];
        if (v != null) {
            return v;
        } else {
            v = _i18n["notif." + key];
            if (v != null) {
                return v;
            }
        }
    }
    return key;
}



var NotificationTable = React.createClass({

    getInitialState: function() {
        return {
            n: [],
            recentlyRemoved: [],
            currentSort: {
                prop: 'date',
                dir: -1
            },
            filter: {
                app: null,
                status: "UNREAD"
            }
        };
    },

    loadNotifications: function() {
        $.ajax({
            url: this.props.url,
            data: {status: this.state.filter.status},
            datatype: 'json',
            success: function(data) {
                var s = this.state;
                var recentlyRemoved = s.recentlyRemoved;
                var notifs = data.filter(function(notif) {
                    return $.inArray(notif.id, recentlyRemoved) == -1;
                });

                var currentSort = s.currentSort;
                s.n = notifs.sort(function (a, b) {
                    if (typeof a[currentSort.prop] == "number") {
                        return (a[currentSort.prop] - b[currentSort.prop]) * currentSort.dir;
                    } else {
                        return a[currentSort.prop].localeCompare(b[currentSort.prop]) * currentSort.dir;
                    }
                });
                this.setState(s);
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });


    },
    componentDidMount: function() {
        this.loadNotifications();
        setInterval(this.loadNotifications, this.props.pollInterval);
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

            var n = component.state.n.sort(function (a, b) {
                if (typeof a[currentSort.prop] == "number") {
                    return (a[currentSort.prop] - b[currentSort.prop]) * currentSort.dir;
                } else {
                    return a[currentSort.prop].localeCompare(b[currentSort.prop]) * currentSort.dir;
                }
            });
            var state = component.state;
            state.n = n;
            state.currentSort = currentSort;
            component.setState(state);
        };
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
        console.error("Not implemented");
    },
    removeNotif: function(id) {
        var notifs = this.state.n.filter(function(n) {return n.id != id;});
        var recentlyRemoved = this.state.recentlyRemoved;
        recentlyRemoved.push(id);


        this.setState({n:notifs, recentlyRemoved: recentlyRemoved});

        $.ajax({
            url: this.props.url + "/" + id,
            method: 'delete',
            datatype: 'json',
            success: function(data) {
                // nothing much to say is there?
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        var callback = this.removeNotif;
        if (this.state.n.length == 0) {
            return (
                <div className="standard-form">
                {t('no-notification')}
                </div>
                );
        } else {
            var notificationNodes = this.state.n.map(function (notif) {
                return (
                    <Notification key={notif.id} notif={notif} onRemoveNotif={callback}/>
                    );
            });
            return (
                <div>
                    <NotificationHeader filter={this.state.filter} updateStatus={this.filterByStatus} updateApp={this.filterByApp}/>
                    <div className="standard-form">
                        <div className="row form-table-header">
                            <div className="col-sm-2 sortable" onClick={this.sortBy('date')}>{t('date')}</div>
                            <div className="col-sm-2 sortable" onClick={this.sortBy('appName')}>{t('app')}</div>
                            <div className="col-sm-6 sortable" onClick={this.sortBy('formattedText')}>{t('message')}</div>
                        </div>
                    {notificationNodes}
                    </div>
                </div>
                );
        }
    }
});

var NotificationHeader = React.createClass({
    render: function () {
        return (
            <div className="row">
                <h2 className="col-sm-6">{t('ui.notifications')}</h2>
                <div className="col-sm-6">
                    <form className="form-inline header-form">
                        <select name="status" className="form-control" onChange={this.props.updateStatus}>
                            <option value="UNREAD">{t('unread')}</option>
                            <option value="READ">{t('read')}</option>
                            <option value="ANY">{t('any')}</option>
                        </select>
                    </form>
                </div>
            </div>
        );
    }
});

var Notification = React.createClass({
    displayName: "Notification",
    removeNotif: function () {
        this.props.onRemoveNotif(this.props.notif.id);
    },
    render: function() {
        var action = null;
        if (this.props.notif.url) {
            action = <a href={this.props.notif.url} target="_new" className="btn btn-primary" >{this.props.notif.actionText}</a>;
        }
        return (
            <div className="row form-table-row">
                <div className="col-sm-2">{this.props.notif.dateText}</div>
                <div className="col-sm-2">{this.props.notif.appName}</div>
                <div className="col-sm-6" dangerouslySetInnerHTML={{__html: this.props.notif.formattedText}}></div>
                <div className="col-sm-2">
                    {action}
                    <a href="#" className="btn btn-primary" onClick={this.removeNotif}>{t('archive')}</a>
                </div>
            </div>
            );
    }
});


React.renderComponent(
    <NotificationTable url={notificationService} pollInterval={2000}/>
    , document.getElementById("notifications"));