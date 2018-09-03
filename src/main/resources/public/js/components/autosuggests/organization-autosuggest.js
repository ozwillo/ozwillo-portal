import React from 'react';
import PropTypes from "prop-types";
import Autosuggest from 'react-autosuggest';
import debounce from "debounce";
import Config from '../../config/config';

const sizeQueryBeforeFetch = Config.sizeQueryBeforeFetch;


export default class OrganizationAutoSuggest extends React.Component {
    defaultProps = {
        value: '',
        placeholder: '',
        required: false
    };

    constructor(props) {
        super(props);

        this.state = {
            suggestions: []
        };
    }

    searchOrganizations(query) {
        //TODO change all the $.ajax with fetch
        $.ajax({
            url: "/my/api/organization/searchFromUserAndQuery",
            dataType: 'json',
            data: {query: query},
            type: 'get',
            success: function (data) {
                this.setState({suggestions: data});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error("Error while searching for organizations with query " + query, status, err.toString())
            }
        })
    }

    renderSuggestion = (data) => {
        return (
            <div>
                <p>{data.organization_name}</p>
            </div>
        )
    };

    onSuggestionsFetchRequested = ({value, reason}) => {
        if (reason !== 'enter' && reason !== 'click') {
            debounce(this.searchOrganizations(value), 500);
        }

    };

    onSuggestionsClearRequested = () => {
        this.setState({suggestions: []})
    };

    onSuggestionSelected = (event, {suggestion}) => {
        this.props.onOrganizationSelected(suggestion);
    };

    shouldRenderSuggestions = (input) => {
        return input && (input.trim().length >= sizeQueryBeforeFetch);
    };

    getSuggestionValue = (suggestion) => {
        return suggestion.legal_name || this.defaultProps.value;
    };

    handleOnChange = (event, {newValue}) => {
        this.props.onChange(event, newValue);
    };

    render() {
        const inputProps = {
            name: this.props.name,
            value: this.props.value || this.defaultProps.value,
            onChange: this.handleOnChange,
            type: 'search',
            placeholder: this.props.placeholder,
            className: `form-control ${this.props.className}`,
            required: this.props.required
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                onSuggestionSelected={this.onSuggestionSelected}
                renderSuggestion={this.renderSuggestion}
                getSuggestionValue={this.getSuggestionValue}
                inputProps={inputProps}
                shouldRenderSuggestions={this.shouldRenderSuggestions}/>
        )
    }
}

OrganizationAutoSuggest.propTypes = {
    name: PropTypes.string.isRequired,
    placeholder: PropTypes.string,
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    onOrganizationSelected: PropTypes.func.isRequired,
    required: PropTypes.bool
};

