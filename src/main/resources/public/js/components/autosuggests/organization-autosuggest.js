import React from 'react';
import PropTypes from "prop-types";
import Autosuggest from 'react-autosuggest';
import debounce from "debounce";
import Config from '../../config/config';
import customFetch from "../../util/custom-fetch";

const sizeQueryBeforeFetch = Config.sizeQueryBeforeFetch;


export default class OrganizationAutoSuggest extends React.Component {

    static defaultProps = {
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
        customFetch(
            `/my/api/organization/memberships?query=${query}`,
            {
                "Content-Type": "application/json"
            }
        ).then(res => {
            this.setState({suggestions: res});
        }).catch(err => {
            console.error("Error while searching for organizations with query " + query, err.toString())
        });
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
        return suggestion['organization_name'] || OrganizationAutoSuggest.defaultProps.value;
    };

    handleOnChange = (event, {newValue}) => {
        this.props.onChange(event, newValue);
    };

    render() {
        const inputProps = {
            name: this.props.name,
            value: this.props.value || OrganizationAutoSuggest.defaultProps.value,
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

