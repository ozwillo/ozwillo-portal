import React, {Component} from 'react'
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';

const renderSuggestion = suggestion => (
    <div>
        <p className="main-info">{suggestion.name}</p>
    </div>
);

class GeoAreaAutosuggest extends Component {
    static propTypes = {
        name: PropTypes.string.isRequired,
        placeholder: PropTypes.string,
        value: PropTypes.string,
        countryUri: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,
        onGeoAreaSelected: PropTypes.func.isRequired
    };

    static defaultProps = {
        value: '',
        placeholder: ''
    };

    state = {
        suggestions: []
    };

    searchCities(query) {
        if (!this.props.countryUri) return;

        $.ajax({
            url: `/api/store/geographicalAreas`,
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
    }

    onSuggestionsFetchRequested = ({ value }) => {
        this.searchCities(value);
    };

    onSuggestionsClearRequested = () => {
        this.setState({ suggestions: [] })
    };

    onSuggestionSelected = (event, { suggestion }) => {
        this.props.onGeoAreaSelected(suggestion);
    };

    getSuggestionValue = suggestion => suggestion.name;

    shouldRenderSuggestions = (input) => {
        return !input && input.trim().length > 2;
    };

    render() {
        const inputProps = {
            name: this.props.name,
            value: this.props.value,
            onChange: this.props.onChange,
            type: 'search',
            placeholder: this.props.placeholder,
            className: `form-control ${this.props.className}`
        };

        return <Autosuggest suggestions={this.state.suggestions}
                     onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                     onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                     onSuggestionSelected={this.onSuggestionSelected}
                     getSuggestionValue={this.getSuggestionValue}
                     renderSuggestion={renderSuggestion}
                     inputProps={inputProps}
                     shouldRenderSuggestions={this.shouldRenderSuggestions}/>
    }
}

export default GeoAreaAutosuggest;
