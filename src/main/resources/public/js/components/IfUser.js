import React from 'react';
import { connect } from 'react-redux';

//actions
import { fetchUserInfo } from '../actions/user';

class IfUser extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            loading: true
        };
    }

    componentDidMount() {
        this.props.fetchUserInfo()
            .then(() => {
                this.setState({ loading: false });
            });
    }

    render() {
        debugger;
        return (!this.state.loading && this.props.userInfo.sub && this.props.children) || null;
    }
}

const mapStateToProps = state => {
  return {
      userInfo: state.userInfo
  };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchUserInfo() {
            return dispatch(fetchUserInfo());
        }
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(IfUser);