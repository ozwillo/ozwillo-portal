'use strict';

import React from 'react';
import {connect} from 'react-redux';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';

import Autosuggest from 'react-autosuggest';
import debounce from 'debounce';

import renderIf from 'render-if';

import moment from 'moment';

export const UsersList = createClass({
    propTypes: {
        users: PropTypes.array.isRequired,
        removeUser: PropTypes.func.isRequired
    },
    render: function () {
        var usersList = this.props.users.map(user => {
            var userId = user.status === 'new_from_email' || !user.id ? user.email : user.id;
            return <UserWithRedux key={userId} user={user} removeUser={this.props.removeUser(userId)}/>;
        });

        return (
            <div className="users-table">
                <table className="table table-striped table-responsive">
                    <thead>
                    <tr>
                        <th>{this.context.t('name')}</th>
                        <th>{this.context.t('status')}</th>
                        <th>{this.context.t('actions')}</th>
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
UsersList.contextTypes = {
    t: PropTypes.func.isRequired
};

var User = createClass({
    propTypes: {
        user: PropTypes.object.isRequired,
        removeUser: PropTypes.func.isRequired
    },
    displayStatus: function (user) {
        if (['new_from_organization', 'new_from_email', 'new_to_push'].indexOf(user.status) !== -1)
            return this.context.t('settings.status.to-validate');
        else if (!user.id)
            return this.context.t('settings.status.pending')
        else
            return this.context.t('settings.status.member');
    },
    render: function () {
        moment.locale(this.props.currentLanguage);
        return (
            <tr>
                <td>{this.props.user.name || this.props.user.email}</td>
                <td>
                    {this.displayStatus(this.props.user)}
                    {renderIf(this.props.user.created !== null)(
                        <small> ({moment(this.props.user.created).format('lll')})</small>)}
                </td>
                <td>
                    <button className="btn icon delete" onClick={this.props.removeUser}>
                        <i className="fa fa-trash"></i>
                    </button>
                </td>
            </tr>
        );
    }
});
User.contextTypes = {
    t: PropTypes.func.isRequired
};
const UserWithRedux = connect(state => {
    return {currentLanguage: state.config.language};
})(User);

export const OrgUserPicker = createClass({
    propTypes: {
        addUser: PropTypes.func.isRequired,
        queryUsers: PropTypes.func.isRequired
    },
    getInitialState: function () {
        return {
            value: '',
            suggestions: []
        };
    },
    renderSuggestion: function (suggestion) {
        return (
            <div>{suggestion.name}</div>
        );
    },
    onSuggestionsFetchRequested: function ({value, reason}) {
        if (reason !== 'enter' && reason !== 'click')
            debounce(this.props.queryUsers(value, (suggestions) => this.setState({suggestions: suggestions})), 500);
    },
    onSuggestionsClearRequested: function () {
        this.setState({suggestions: []})
    },
    onSuggestionSelected: function (event, {suggestion, suggestionValue, method}) {
        this.setState({value: ''});
        this.props.addUser(suggestion);
    },
    render: function () {
        const inputProps = {
            value: this.state.value,
            onChange: (event, {newValue, method}) => this.setState({value: newValue}),
            type: 'search',
            placeholder: this.context.t('settings-add-a-user'),
            className: 'form-control'
        };

        return (
            <div className="row">
                <label htmlFor="search-user"
                       className="control-label col-sm-2">{this.context.t('setting-add-from-organization')}</label>

                <div className="col-sm-9">
                    <div className="input-group">
                        <Autosuggest suggestions={this.state.suggestions}
                                     onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                                     onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                                     onSuggestionSelected={this.onSuggestionSelected}
                                     getSuggestionValue={suggestion => suggestion.name}
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

OrgUserPicker.contextTypes = {
    t: PropTypes.func.isRequired
};
