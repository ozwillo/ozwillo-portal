import React from 'react';
import {connect} from 'react-redux';

class IfUser extends React.Component {

    render() {
        if (!this.props.userInfo.sub) {
            return null;
        }

        return this.props.children;
    }
}

const mapStateToProps = state => {
    return {
        userInfo: state.userInfo
    };
};

export default connect(mapStateToProps)(IfUser);