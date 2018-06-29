import React from 'react';
import {connect} from 'react-redux';

//Actions
import {fetchNotificationsCount} from '../actions/notifications';

import config from '../config/config';

const notificationsCountInterval = config.notificationsCountInterval;

class NotificationsCountLoader extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            intervalId: 0
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.notificationsEnabled && !this.state.intervalId) {
            this.props.fetchNotificationsCount();
            const intervalId = setInterval(this.props.fetchNotificationsCount, notificationsCountInterval);
            this.setState({intervalId: intervalId});
        }
    }

    componentWillUnmount() {
        if (this.state.intervalId) {
            clearInterval(this.state.intervalId);
        }
    }

    render() {
        return null;
    }

}

const mapStateToProps = dispatch => {
    return {
        notificationsEnabled: dispatch.config.notificationsEnabled
    }
};

const mapDispatchToProps = dispatch => {
    return {
        fetchNotificationsCount() {
            return dispatch(fetchNotificationsCount());
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(NotificationsCountLoader);