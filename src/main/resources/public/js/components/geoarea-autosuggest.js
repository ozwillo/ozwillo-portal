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
        placeholder: PropTypes.string,
        initialValue: PropTypes.string,
        endpoint: PropTypes.string,
        countryUri: PropTypes.string,
        onChange: PropTypes.func.isRequired
    };
    static defaultProps = {
        placeholder: ''
    };
    state = {
        value: '',
        suggestions: []
    };
    componentDidMount() {
        this.setState({ value: this.props.initialValue || '' });
    }
    componentWillReceiveProps(nextProps) {
        if (this.props.initialValue !== nextProps.initialValue)
            this.setState({ value: nextProps.initialValue })
    }
    searchCities(query) {
        if (this.props.countryUri === undefined) return;

        $.ajax({
            url: `/api/store${this.props.endpoint}`,
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
        this.setState({ value: value });
        this.searchCities(value);
    }
    onSuggestionsClearRequested = () => {
        this.setState({ suggestions: [] })
    }
    onSuggestionSelected = (event, { suggestion, suggestionValue, sectionIndex, method }) => {
        this.setState({ value: suggestion.name });
        this.props.onChange(suggestion);
    }
    onChange(event, { newValue, method }) {
        if (method === "type" && newValue === '') {
            this.props.onChange(null)
        } else {
            this.setState({ value: newValue })
        }
    }
    render() {
        const inputProps = {
            value: this.state.value || '',
            onChange: this.onChange.bind(this),
            type: 'search',
            placeholder: this.props.placeholder,
            className: 'form-control'
        };

        return (
            <div className="input-group">
                <Autosuggest suggestions={this.state.suggestions}
                             onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                             onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                             onSuggestionSelected={this.onSuggestionSelected}
                             getSuggestionValue={suggestion => suggestion.name}
                             renderSuggestion={renderSuggestion}
                             inputProps={inputProps}
                             shouldRenderSuggestions={input => input != null && input.trim().length > 2}/>
                <span className="input-group-addon"><i className="fa fa-search"></i></span>
            </div>
        )
    }
}

export default GeoAreaAutosuggest;
