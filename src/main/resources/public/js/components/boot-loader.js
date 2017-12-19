import React from 'react';
import { connect } from 'react-redux';

//Actions
import { fetchConfigAndUserInfo } from "../actions/main";

class BootLoader extends React.Component {

    componentDidMount() {
        this.props.fetchConfigAndUserInfo();
    }

    render() {
        return null;
    }

}

const mapDispatchToProps = dispatch => {
    return {
        fetchConfigAndUserInfo() {
            return dispatch(fetchConfigAndUserInfo());
        }
    };
};

export default connect(null, mapDispatchToProps)(BootLoader);