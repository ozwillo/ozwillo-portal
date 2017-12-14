import React from 'react';
import { connect } from 'react-redux';

//Actions
import { fetchConfig } from "../actions/config";

class ConfigLoader extends React.Component {

    componentDidMount() {
        this.props.fetchConfig();
    }

    render() {
        return null;
    }

}

const mapDispatchToProps = dispatch => {
    return {
        fetchConfig() {
            return dispatch(fetchConfig());
        }
    };
};

export default connect(null, mapDispatchToProps)(ConfigLoader);