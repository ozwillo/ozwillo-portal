import React from 'react';
import { connect } from 'react-redux';

//Actions
import { fetchNotificationsCount } from '../actions/notifications';

import config from '../config/config';
const notificationsCountInterval = config.notificationsCountInterval;

class NotificationsCountLoader extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            intervalId: 0
        };
    }

    componentDidMount() {
        this.props.fetchNotificationsCount();
        const intervalId = setInterval(this.props.fetchNotificationsCount, notificationsCountInterval);
        this.setState({intervalId: intervalId});
    }

    componentWillUnmount() {
        clearInterval(this.state.intervalId);
    }

    render() {
        return null;
    }

}

const mapDispatchToProps = dispatch => {
    return {
        fetchNotificationsCount () {
            return dispatch(fetchNotificationsCount());
        }
    }
};

export default connect(null, mapDispatchToProps)(NotificationsCountLoader);