import React from 'react';
import PropTypes from "prop-types";

import Autosuggest from 'react-autosuggest';
import debounce from "debounce";


class LegalNameAutosuggest extends React.Component {
    static propTypes = {
        name: PropTypes.string.isRequired,
        placeholder: PropTypes.string,
        countryUri: PropTypes.string.isRequired,
        value: PropTypes.string,
        onChange: PropTypes.func.isRequired,
        onOrganizationSelected: PropTypes.func.isRequired
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            suggestions: []
        };

        // bind methods
        this.searchOrganizations = this.searchOrganizations.bind(this);
        this.renderSuggestion = this.renderSuggestion.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.shouldRenderSuggestions = this.shouldRenderSuggestions.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
    }

    searchOrganizations(query) {
        if (query.trim().length < 3) return;

        $.ajax({
            url: "/my/api/network/search-organizations",
            dataType: 'json',
            data: { country_uri: this.props.countryUri, query: query },
            type: 'get',
            success: function(data) {
                this.setState({ suggestions : data });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error("Error while searching for organizations with query " + query, status, err.toString())
            }
        })
    }

    renderSuggestion(data) {
        return (
            <div>
                <p className="main-info">{data.legal_name}</p>
                <p className="complementary-info">{data.city}</p>
            </div>
        )
    }

    onSuggestionsFetchRequested ({ value, reason }) {
        if (reason !== 'enter' && reason !== 'click') {
            debounce(this.searchOrganizations(value), 500);
        }

    }

    onSuggestionsClearRequested() {
        this.setState({ suggestions: [] })
    }

    onSuggestionSelected(event, { suggestion }) {
        this.props.onOrganizationSelected(suggestion);
    }

    shouldRenderSuggestions(input) {
        return input.trim().length > 0;
    }

    getSuggestionValue(suggestion) {
        return suggestion.legal_name;
    }

    render() {
        const inputProps = {
            name: this.props.name,
            value: this.props.value || '',
            onChange: this.props.onChange,
            type: 'search',
            placeholder: this.props.placeholder || '',
            className: `form-control ${this.props.className}`
        };

        return <Autosuggest suggestions={this.state.suggestions}
                            onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                            onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                            onSuggestionSelected={this.onSuggestionSelected}
                            renderSuggestion={this.renderSuggestion}
                            getSuggestionValue={this.getSuggestionValue}
                            inputProps={inputProps}
                            shouldRenderSuggestions={this.shouldRenderSuggestions}/>;
    }
}

export default LegalNameAutosuggest;