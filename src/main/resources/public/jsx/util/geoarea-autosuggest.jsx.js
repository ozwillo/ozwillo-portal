'use strict';

import React from 'react';
import ReactDOM from 'react-dom';

import Autosuggest from 'react-autosuggest';
var debounce = require('debounce');

var GeoAreaAutosuggest = React.createClass({
    propTypes: {
        placeholder: React.PropTypes.string,
        initialValue: React.PropTypes.string,
        endpoint: React.PropTypes.string,
        countryUri: React.PropTypes.string,
        onChange: React.PropTypes.func.isRequired
    },
    getInitialState: function() {
        return {
            value: '',
            suggestions: []
        };
    },
    getDefaultProps: function() {
        return {
            placeholder: ''
        }
    },
    componentDidMount: function() {
        this.setState({ value: this.props.initialValue });
    },
    searchCities: function(query) {
        if (query.trim().length < 3) return;

        $.ajax({
            url: store_service + this.props.endpoint,
            dataType: "json",
            data: { country_uri: this.props.countryUri, q: query },
            type: 'get',
            success: function(data) {
                this.setState({ suggestions : data.areas });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error("Error while searching for cities with query " + query, status, err.toString())
            }
        })
    },
    renderSuggestion: function(data) {
        return (
            <div>
                <p className="main-info">{data.name}</p>
            </div>
        )
    },
    onSuggestionsUpdateRequested: function({ value, reason }) {
        this.setState({ value: value });
        if (reason !== 'enter' && reason !== 'click')
            debounce(this.searchCities(value), 500);
    },
    onSuggestionSelected: function(event, { suggestion, suggestionValue, method }) {
        this.setState({ value: suggestion.name });
        this.props.onChange(suggestion);
    },
    render: function() {
        const inputProps = {
            value: this.state.value || this.props.initialValue,
            onChange: (event, { newValue, method }) => this.setState({ value: newValue }),
            type: 'search',
            placeholder: this.props.placeholder,
            className: 'form-control'
        };

        return (
            <div className="input-group">
                <Autosuggest suggestions={this.state.suggestions}
                             onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                             onSuggestionSelected={this.onSuggestionSelected}
                             getSuggestionValue={suggestion => suggestion.name}
                             renderSuggestion={this.renderSuggestion}
                             inputProps={inputProps}
                             shouldRenderSuggestions={input => input != null && input.trim().length > 2}/>
                <span className="input-group-addon"><i className="fa fa-search"></i></span>
            </div>
        )
    }
});

module.exports = { GeoAreaAutosuggest };
