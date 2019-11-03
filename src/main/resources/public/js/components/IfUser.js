import React from 'react';
import UserService from '../util/user-service';

class IfUser extends React.Component {

    constructor(props) {
        super(props);

        this._userService = new UserService();

        this.state = {
            isLoggedIn: false
        }
    }

    componentDidMount = async () => {
        const userInfo = await this._userService.fetchUserInfos();
        this.setState({ isLoggedIn: !!userInfo });
    }

    render() {
        if (!this.state.isLoggedIn) {
            return null;
        }

        return this.props.children;
    }
}

export default IfUser;
