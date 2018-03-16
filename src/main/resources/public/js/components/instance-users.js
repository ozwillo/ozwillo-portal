'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';

import {UsersList, OrgUserPicker} from './common-users.js';

var ApplicationUsersManagement = createClass({
    propTypes: {
        instanceId: PropTypes.string.isRequired,
        authority: PropTypes.string.isRequired
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
            // only users that are app_user (so not those that are !app_user or app_admin)
            url: `/my/api/myapps/users/instance/${this.props.instanceId}?app_admin=false&pending=true`,
            dataType: 'json',
            method: 'get'
        })
            .done(users => this.setState({users: users}))
            .fail((xhr, status, err) => {
                console.error('/my/api/myapps/users/instance/' + this.props.instanceId, status, err.toString());
                this.setState({users: []});
            });
    },
    queryUsers: function (query, callback) {
        $.ajax({
            url: `/my/api/myapps/users/network/${this.props.authority}?q=${query}`,
            dataType: 'json',
            method: 'get',
            success: callback,
            error: function (xhr, status, err) {
                console.error(`/my/api/myapps/users/network${this.props.authority}?q=${query}`, status, err.toString());
            }.bind(this)
        });
    },
    addUser: function (user) {
        if (this.state.users.filter(u => u.id === user.id).length == 0) {
            user.status = 'new_from_organization';
            this.setState({users: [user].concat(this.state.users)});
        }
    },
    addUsersEmail: function (usersEmail) {
        var newUsers = [];
        usersEmail.forEach(email => {
            if (this.state.users.filter(u => u.email === email).length == 0) {
                newUsers.push({
                    email: email.trim(),
                    status: 'new_from_email'
                });
            }
        });
        this.setState({users: newUsers.concat(this.state.users)});
    },
    removeUser: function (userId) {
        return function () {
            this.setState({
                users: this.state.users.filter(function (user) {
                    if (user.status === 'new_from_email' || !user.id)
                        return user.email !== userId;
                    else
                        return user.id != userId;
                })
            });
        }.bind(this);
    },
    saveUsers: function () {
        ``
        $.ajax({
            url: `/my/api/myapps/users/instance/${this.props.instanceId}`,
            dataType: 'json',
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(this.state.users),
            error: function (xhr, status, err) {
                console.error(`/my/api/myapps/users/instance/${this.props.instanceId}`, status, err.toString());
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
                            <h4 className="modal-title" id="modalLabel">{this.context.t('manage_users')}</h4>
                        </div>
                        <div className="modal-body">
                            <OrgUserPicker addUser={this.addUser} queryUsers={this.queryUsers}/>
                            <UsersEmailSelector addUsers={this.addUsersEmail}/>
                            <UsersList users={this.state.users} removeUser={this.removeUser}/>
                        </div>
                        <div className="modal-footer">
                            <button type="button" key="cancel" className="btn btn-default"
                                    onClick={this.close}>{this.context.t('ui.cancel')}</button>
                            <button type="submit" key="success" className="btn btn-default"
                                    onClick={this.saveUsers}>{this.context.t('ui.save')}</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});
ApplicationUsersManagement.contextTypes = {
    t: PropTypes.func.isRequired
};

var UsersEmailSelector = createClass({
    propTypes: {
        addUsers: PropTypes.func.isRequired
    },
    getInitialState: function () {
        return {
            hasError: false,
            userInput: ''
        };
    },
    updateUserInput: function (event) {
        this.setState({userInput: event.target.value});
    },
    handleAddUsers: function () {
        var usersEmails = this.state.userInput.split(',');
        var areEmailsValid = usersEmails.every(email => {
            // basic email validation, just want to spot typos, not more
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
        });
        if (areEmailsValid) {
            this.setState({hasError: false, userInput: ''});
            this.props.addUsers(usersEmails);
        } else {
            this.setState({hasError: true});
        }
    },
    render: function () {
        var className = this.state.hasError ? "row form-group has-error" : "row form-group";

        return (
            <div className={className} style={{marginTop: '1em'}}>
                <label htmlFor="usersEmail"
                       className="control-label col-sm-2">{this.context.t('settings-invite-by-email')}</label>
                <div className="col-sm-7">
                    <textarea id="usersEmail" className="form-control" cols="30" rows="5"
                              title={this.context.t('settings-invite-by-email-title')}
                              placeholder={this.context.t('settings-invite-by-email-title')}
                              value={this.state.userInput} onChange={this.updateUserInput}/>
                </div>
                <div className="col-sm-2">
                    <button type="submit" className="btn btn-default"
                            onClick={this.handleAddUsers}>{this.context.t('ui.add')}</button>
                </div>
            </div>
        )
    }
});
UsersEmailSelector.contextTypes = {
    t: PropTypes.func.isRequired
};

export default ApplicationUsersManagement;
