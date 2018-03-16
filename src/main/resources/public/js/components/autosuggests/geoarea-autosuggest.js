import React, {Component} from 'react'
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';
import Config from '../../config/config';

const sizeQueryBeforeFetch = Config.sizeQueryBeforeFetch;

const renderSuggestion = suggestion => (
    <div>
        <p className="main-info">{suggestion.name}</p>
    </div>
);

class GeoAreaAutosuggest extends Component {
    static propTypes = {
        name: PropTypes.string.isRequired,
        endpoint: PropTypes.string.isRequired,
        placeholder: PropTypes.string,
        value: PropTypes.string,
        countryUri: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,
        onGeoAreaSelected: PropTypes.func,
        onBlur: PropTypes.func,
        required: PropTypes.bool
    };

    static defaultProps = {
        value: '',
        placeholder: '',
        required: false
    };

    state = {
        suggestions: []
    };

    searchCities(query) {
        $.ajax({
            url: `/api/store${this.props.endpoint}`,
            dataType: 'json',
            data: {country_uri: this.props.countryUri, q: query},
            type: 'get',
            success: function (data) {
                this.setState({suggestions: data.areas});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error("Error while searching for cities with query " + query, status, err.toString())
            }
        })
    }

    onSuggestionsFetchRequested = ({value}) => {
        this.searchCities(value);
    };

    onSuggestionsClearRequested = () => {
        this.setState({suggestions: []})
    };

    onSuggestionSelected = (event, {suggestion}) => {
        this.props.onGeoAreaSelected(suggestion);
    };

    getSuggestionValue = suggestion => suggestion.name || GeoAreaAutosuggest.defaultProps.value;

    shouldRenderSuggestions = (input) => {
        return input && (input.trim().length >= sizeQueryBeforeFetch);
    };

    render() {
        const inputProps = {
            name: this.props.name,
            value: this.props.value || GeoAreaAutosuggest.defaultProps.value,
            onChange: this.props.onChange,
            onBlur: this.props.onBlur,
            type: 'search',
            placeholder: this.props.placeholder || GeoAreaAutosuggest.defaultProps.placeholder,
            className: `form-control ${this.props.className}`,
            required: this.props.required
        };


        return <Autosuggest
            suggestions={this.state.suggestions}
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
