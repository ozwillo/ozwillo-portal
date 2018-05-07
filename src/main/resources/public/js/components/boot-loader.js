import React from 'react';
import {connect} from 'react-redux';

//Actions
import {
    fetchConfig,
    fetchMyConfig
} from "../actions/config";

class BootLoader extends React.Component {

    componentDidMount() {
        if (this.props.user.sub) {
            this.props.fetchMyConfig();
        } else {
            this.props.fetchConfig();
        }

    }

    render() {
        return this.props.children;
    }

}

const mapStateToProps = state => {
    return {
        user: state.userInfo,
        config: state.config
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchMyConfig() {
            return dispatch(fetchMyConfig());
        },
        fetchConfig() {
            return dispatch(fetchConfig());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(BootLoader);