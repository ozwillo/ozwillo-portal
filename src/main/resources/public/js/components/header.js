import React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import PropTypes from "prop-types";
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

class Header extends React.Component {

    render() {
        const isLogged = !!this.props.userInfo.sub;
        return <header className="oz-header flex-row">

            {
                isLogged &&
                <div className="my-oasis">
                    <p className="text-center welcome" data-toggle="tooltip" data-placement="bottom"
                       title={this.props.message}>
                        <Link to="/my/notif">
                            <span>{i18n._(t`ui.welcome`)} {this.props.userInfo.nickname} </span>
                            <span className="badge badge-notifications">
                            {this.props.notificationsCount}
                                <span className="sr-only">{i18n._(`my.n_notifications`,{value: this.props.notificationsCount})}</span>
                        </span>
                        </Link>
                    </p>
                </div>
            }
        </header>
    }

}

const mapStateToProps = (state) => {
    return {
        notificationsCount: state.notifications.count,
        userInfo: state.userInfo,
        config: state.config
    }
};

export default connect(mapStateToProps)(Header);
