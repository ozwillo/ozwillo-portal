'use strict';

import React from 'react';

import Autosuggest from 'react-autosuggest';
var debounce = require('debounce');

import renderIf from 'render-if';

var moment = require('moment');

import t from '../util/message';

var UsersList = React.createClass({
    propTypes: {
        users: React.PropTypes.array.isRequired,
        removeUser: React.PropTypes.func.isRequired
    },
    render: function() {
        var usersList = this.props.users.map(user => {
            var userId = user.status === 'new_from_email' || !user.userid ? user.email : user.userid;
            return <User key={userId} user={user} removeUser={this.props.removeUser(userId)} />;
        });

        return (
            <div className="users-table">
                <table className="table table-striped table-responsive">
                    <thead>
                    <tr>
                        <th>{t('name')}</th>
                        <th>{t('status')}</th>
                        <th>{t('actions')}</th>
                    </tr>
                    </thead>
                    <tbody>
                        {usersList}
                    </tbody>
                </table>
            </div>
        );
    }
});

var User = React.createClass({
    propTypes: {
        user: React.PropTypes.object.isRequired,
        removeUser: React.PropTypes.func.isRequired
    },
    displayStatus: function(user) {
        if (['new_from_organization', 'new_from_email', 'new_to_push'].indexOf(user.status) !== -1)
            return t('settings.status.to-validate');
        else if (!user.userid)
            return t('settings.status.pending')
        else
            return t('settings.status.member');
    },
    render: function() {
        moment.locale(currentLanguage);
        return (
            <tr>
                <td>{this.props.user.fullname || this.props.user.email}</td>
                <td>
                    {this.displayStatus(this.props.user)}
                    {renderIf(this.props.user.created !== null)(<small> ({moment(this.props.user.created).format('lll')})</small>)}
                </td>
                <td>
                    <button className="btn oz-btn-danger" onClick={this.props.removeUser}>
                        <i className="fa fa-trash"></i>
                    </button>
                </td>
            </tr>
        );
    }
});

var OrgUserPicker = React.createClass({
    propTypes: {
        addUser: React.PropTypes.func.isRequired,
        queryUsers: React.PropTypes.func.isRequired
    },
    getInitialState: function() {
        return {
            value: '',
            suggestions: []
        };
    },
    renderSuggestion: function(suggestion) {
        return (
            <div>{suggestion.fullname}</div>
        );
    },
    onSuggestionsUpdateRequested: function({ value, reason }) {
        if (reason !== 'enter' && reason !== 'click')
            debounce(this.props.queryUsers(value, (suggestions) => this.setState({suggestions: suggestions})), 500);
    },
    onSuggestionSelected: function(event, { suggestion, suggestionValue, method }) {
        this.setState({ value: '' });
        this.props.addUser(suggestion);
    },
    render: function() {
        const inputProps = {
            value: this.state.value,
            onChange: (event, { newValue, method }) => this.setState({ value: newValue }),
            type: 'search',
            placeholder: t('settings-add-a-user'),
            className: 'form-control'
        };

        return (
            <div className="row">
                <label htmlFor="search-user"
                       className="control-label col-sm-2">{t('setting-add-from-organization')}</label>

                <div className="col-sm-9">
                    <div className="input-group">
                        <Autosuggest suggestions={this.state.suggestions}
                                     onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                                     onSuggestionSelected={this.onSuggestionSelected}
                                     getSuggestionValue={suggestion => suggestion.fullname}
                                     renderSuggestion={this.renderSuggestion}
                                     inputProps={inputProps}
                                     shouldRenderSuggestions={input => true}/>
                        <span className="input-group-addon"><i className="fa fa-search"></i></span>
                    </div>
                </div>
            </div>
        );
    }
});

module.exports = { UsersList, OrgUserPicker };
