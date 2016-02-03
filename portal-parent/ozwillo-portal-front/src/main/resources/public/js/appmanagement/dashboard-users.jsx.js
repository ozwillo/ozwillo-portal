/** @jsx React.DOM */

var DashboardUsersManagement = React.createClass({
    propTypes: {
        serviceId: React.PropTypes.string.isRequired,
        instanceId: React.PropTypes.string.isRequired
    },
    getInitialState: function() {
        return {
            users:[]
        };
    },
    open: function() {
        $(this.getDOMNode()).modal('show');
        this.loadUsers();
    },
    close: function() {
        $(this.getDOMNode()).modal('hide');
    },
    loadUsers: function() {
        $.ajax({
            url: apps_service + "/users/service/" + this.props.serviceId,
            dataType:'json',
            type:'get',
            success: function(data) {
                this.setState({ users: data });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(apps_service + "/users/service/" + this.props.serviceId, status, err.toString());
            }.bind(this)
        });
    },
    queryUsers: function(query, syncCallback, asyncCallback) {
        $.ajax({
            url: apps_service + "/users/instance/" + this.props.instanceId + "?app_admin=true&q=" + query, // also app_admin !app_user users
            dataType:"json",
            type:'get',
            success:asyncCallback,
            error: function(xhr, status, err) {
                console.error(apps_service + "/users/instance/" + this.props.instanceId + "?q=" + query, status, err.toString())
            }.bind(this)
        })
    },
    addUser: function(user) {
        if (this.state.users.filter(function(u) { return u.userid == user.userid;}).length == 0) {
            this.setState({ users: [user].concat(this.state.users) });
        }
    },
    removeUser: function(userId) {
        return function() {
            this.setState({
                users: this.state.users.filter(function (user) {
                    return user.userid != userId;
                })
            });
        }.bind(this);
    },
    savePushToDash: function () {
        $.ajax({
            url: apps_service + "/users/service/" + this.props.serviceId,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.state.users),
            error: function (xhr, status, err) {
                console.error(apps_service + "/users/service/" + this.props.serviceId, status, err.toString());
            }.bind(this)
        });
        this.close();
    },
    render: function() {
        return (
            <div className="modal fade oz-simple-modal" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"
                                    onClick={this.close}>
                                <span aria-hidden="true"><img src={image_root + "new/cross.png"}/></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{t('users')}</h4>
                        </div>
                        <div className="modal-body">
                            <OrgUserPicker addUser={this.addUser} queryUsers={this.queryUsers} />
                            <UsersList users={this.state.users} removeUser={this.removeUser} />
                        </div>
                        <div className="modal-footer">
                            <button type="button" key="cancel" className="btn oz-btn-cancel"
                                    onClick={this.close}>{t('ui.cancel')}</button>
                            <button type="submit" key="success" className="btn oz-btn-save"
                                    onClick={this.savePushToDash}>{t('ui.save')}</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});
