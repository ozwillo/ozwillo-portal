import React from 'react';
import {connect} from 'react-redux';

//Actions
import {
    fetchConfig,
    fetchCsrf
} from "../actions/config";

class BootLoader extends React.Component {

    componentDidMount() {
        this.props.fetchConfig();
        this.props.fetchCsrf();
    }

    render() {
        return this.props.children;
    }

}

const mapStateToProps = state => {
    return {
        user: state.userInfo,
        config: state.config,
        csrf: state.csrf
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchConfig() {
            return dispatch(fetchConfig());
        },
        fetchCsrf() {
            return dispatch(fetchCsrf());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(BootLoader);