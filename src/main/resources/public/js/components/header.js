import React from 'react';
import {Link} from 'react-router-dom';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import customFetch from "../util/custom-fetch";
import config from '../config/config';
import UserService from '../util/user-service';

class Header extends React.Component {

    constructor(props) {
        super(props);

        this._userService = new UserService();

        this.state = {
            userInfo: {},
            intervalId: 0
        }
    }

    componentDidMount() {
        this._userService.fetchUserInfos()
            .then((userInfo) => {
                this.setState({ userInfo: userInfo })
                const isLogged = !!userInfo;
                if (isLogged) {
                    this.loadNotificationsCount();
                    const intervalId = setInterval(this.loadNotificationsCount, config.notificationsCountInterval);
                    this.setState({ intervalId: intervalId });
                }
            });
    }

    loadNotificationsCount = async () => {
        const res = await customFetch('/my/api/notifications/summary');
        this.setState({
            notificationsCount: res
        });
    }

    render() {
        const isLogged = !!this.state.userInfo;
        return <header className="oz-header flex-row">

            {
                isLogged &&
                <div className="my-oasis">
                    <p className="text-center welcome" data-toggle="tooltip" data-placement="bottom"
                       title={this.props.message}>
                        <Link to="/my/notif">
                            <span>{i18n._(t`ui.welcome`)} {this.state.userInfo.nickname} </span>
                            <span className="badge badge-notifications">
                                {this.state.notificationsCount}
                                <span className="sr-only">{i18n._(`my.n_notifications`,{value: this.state.notificationsCount})}</span>
                            </span>
                        </Link>
                    </p>
                </div>
            }
        </header>
    }

}

export default Header;
