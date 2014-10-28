/** @jsx React.DOM */

/** Custom translation */
function t(key) {
    if (typeof _i18n != 'undefined') {
        var v = _i18n[key];
        if (v != null) return v;
    }
    return key;
}



var NotificationTable = React.createClass({

    getInitialState: function() {
        return {
            n: [],
            recentlyRemoved: []
        };
    },
    loadNotifications: function() {
        $.ajax({
            url: this.props.url,
            datatype: 'json',
            success: function(data) {
                var s = this.state;
                var recentlyRemoved = s.recentlyRemoved;
                var notifs = data.filter(function(notif) {
                    return $.inArray(recentlyRemoved, notif.id) == -1;
                });

                this.setState({n:notifs});
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
            var n = component.state.n.sort(function (a, b) {
                return a[criterion].localeCompare(b[criterion]);
            });
            component.setState({n: n, recentlyRemoved: this.state.recentlyRemoved});
        };
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
                <div className="standard-form">
                    <div className="row form-table-header">
                        <div className="col-sm-2" onClick={this.sortBy('date')}>{t('date')}</div>
                        <div className="col-sm-2" onClick={this.sortBy('appName')}>{t('app')}</div>
                        <div className="col-sm-6" onClick={this.sortBy('formattedText')}>{t('message')}</div>
                    </div>
                {notificationNodes}
                </div>
                );
        }
    }
});

var Notification = React.createClass({
    displayName: "Notification",
    removeNotif: function(e) {
        this.props.onRemoveNotif(this.props.notif.id);
    },
    render: function() {
        return (
            <div className="row form-table-row">
                <div className="col-sm-2">{this.props.notif.dateText}</div>
                <div className="col-sm-2">{this.props.notif.appName}</div>
                <div className="col-sm-6" dangerouslySetInnerHTML={{__html: this.props.notif.formattedText}}></div>
                <div className="col-sm-2">
                    <a href={this.props.notif.url} target="_new" className="btn btn-primary" >{t('manage')}</a>
                    <a href="#" className="btn btn-primary" onClick={this.removeNotif}>{t('archive')}</a>
                </div>
            </div>
            );
    }
});


React.renderComponent(
    <NotificationTable url={notificationService} pollInterval={2000}/>
    , document.getElementById("notifications"));