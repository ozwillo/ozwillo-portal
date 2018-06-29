'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';

import {UsersList, OrgUserPicker} from './common-users';

var DashboardUsersManagement = createClass({
    propTypes: {
        serviceId: PropTypes.string.isRequired,
        instanceId: PropTypes.string.isRequired
    },
    getInitialState: function () {
        return {
            users: []
        };
    },
    open: function () {
        $(ReactDOM.findDOMNode(this)).modal('show');
        this.loadUsers();
    },
    close: function () {
        $(ReactDOM.findDOMNode(this)).modal('hide');
    },
    loadUsers: function () {
        $.ajax({
            url: `/my/api/myapps/users/service/${this.props.serviceId}`,
            dataType: 'json',
            type: 'get',
            success: function (data) {
                this.setState({users: data});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(`/my/api/myapps/users/service/${this.props.serviceId}`, status, err.toString());
            }.bind(this)
        });
    },
    queryUsers: function (query, callback) {
        $.ajax({
            url: `/my/api/myapps/users/instance/${this.props.instanceId}?app_admin=true&q=${query}`, // also app_admin !app_user users
            dataType: 'json',
            type: 'get',
            success: callback,
            error: function (xhr, status, err) {
                console.error(`/my/api/myapps/users/instance/${this.props.instanceId}?app_admin=true&q=${query}`, status, err.toString())
            }.bind(this)
        })
    },
    addUser: function (user) {
        if (this.state.users.filter(u => u.id == user.id).length == 0) {
            user.status = 'new_to_push';
            this.setState({users: [user].concat(this.state.users)});
        }
    },
    removeUser: function (userId) {
        return function () {
            this.setState({
                users: this.state.users.filter(function (user) {
                    return user.id != userId;
                })
            });
        }.bind(this);
    },
    savePushToDash: function () {
        $.ajax({
            url: `/my/api/myapps/users/service/${this.props.serviceId}`,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.state.users),
            error: function (xhr, status, err) {
                console.error(`/my/api/myapps/users/service/${this.props.serviceId}`, status, err.toString());
            }.bind(this)
        });
        this.close();
    },
    render: function () {
        return (
            <div className="modal fade oz-simple-modal" tabIndex="-1" role="dialog" aria-labelledby="modalLabel">
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"
                                    onClick={this.close}>
                                <span aria-hidden="true"><i className="fas fa-times icon"/></span>
                            </button>
                            <h4 className="modal-title" id="modalLabel">{this.context.t('users')}</h4>
                        </div>
                        <div className="modal-body">
                            <OrgUserPicker addUser={this.addUser} queryUsers={this.queryUsers}/>
                            <UsersList users={this.state.users} removeUser={this.removeUser}/>
                        </div>
                        <div className="modal-footer">
                            <button type="button" key="cancel" className="btn btn-default"
                                    onClick={this.close}>{this.context.t('ui.cancel')}</button>
                            <button type="submit" key="success" className="btn btn-default"
                                    onClick={this.savePushToDash}>{this.context.t('ui.save')}</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

DashboardUsersManagement.contextTypes = {
    t: PropTypes.func.isRequired
};

export default DashboardUsersManagement;
