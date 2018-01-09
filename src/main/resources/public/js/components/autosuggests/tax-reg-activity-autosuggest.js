import React from 'react';
import PropTypes from 'prop-types';
import debounce from 'debounce';
import Autosuggest from 'react-autosuggest';


class TaxRegActivityAutosuggest extends React.Component {
    static propTypes = {
        name: PropTypes.string.isRequired,
        value: PropTypes.string,
        placeholder: PropTypes.string,
        countryUri: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,
        onTaxRegActivitySelected: PropTypes.func.isRequired
    };

    static defaultProps = {
        value: '',
        placeholder: ''
    };


    constructor(props) {
        super(props);

        this.state = {
            suggestions: []
        };

        //bind methods
        this.searchTaxRegActivities = this.searchTaxRegActivities.bind(this);
        this.renderSuggestion = this.renderSuggestion.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.shouldRenderSuggestions = this.shouldRenderSuggestions.bind(this);
    }

    componentWillReceiveProps (nextProps) {
        this.setState({ value: nextProps.initialValue });
    }

    searchTaxRegActivities (query) {
        if (query.trim().length < 3) return;

        $.ajax({
            url: "/api/store/dc-taxRegActivity",
            dataType: "json",
            data: { country_uri: this.props.countryUri, q: query },
            type: 'get',
            success: function(data) {
                this.setState({ suggestions : data.areas });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error("Error while searching for tax reg activities with query " + query, status, err.toString())
            }
        })
    }

    renderSuggestion(data) {
        return (
            <div>
                <p className="main-info">{data.name} - {data.label}</p>
            </div>
        )
    }

    onSuggestionsFetchRequested({ value, reason }) {
        if (reason !== 'enter' && reason !== 'click')
            debounce(this.searchTaxRegActivities(value), 500);
    }

    onSuggestionsClearRequested() {
        this.setState({ suggestions: [] })
    }

    onSuggestionSelected(event, { suggestion }) {
        this.props.onTaxRegActivitySelected(suggestion);
    }

    getSuggestionValue(suggestion) {
        return suggestion.name;
    }

    shouldRenderSuggestions(input) {
        return input.trim().length > 0;
    }

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
                         renderSuggestion={this.renderSuggestion}
                         getSuggestionValue={this.getSuggestionValue}
                         inputProps={inputProps}
                         shouldRenderSuggestions={this.shouldRenderSuggestions}/>
    }
}

export default TaxRegActivityAutosuggest;