import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import customFetch from "../util/custom-fetch";


class MyNav extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            config: {}
        }
    }

    componentDidMount = async () => {
        let config = await customFetch('/api/config');
        this.setState({ config: config });
    }

    render() {
        return <nav className="navbar navbar-default navbar-auth" id="oz-nav">
            <div className="container-fluid">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                            data-target="#ozwillo-navbar" aria-expanded="false" aria-controls="navbar">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar"/>
                        <span className="icon-bar"/>
                        <span className="icon-bar"/>
                    </button>
                </div>

                <div className="collapse navbar-collapse" id="ozwillo-navbar">
                    <ul className="nav navbar-nav">
                        <li className="menu">
                            <Link className="link" to="/my/">
                                <i className="fa fa-home icon" alt={i18n._(t`my.dashboard`)}/>
                                <span>{i18n._(t`my.dashboard`)}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <Link className="link" to="/my/profile">
                                <i className="fa fa-user icon" alt={i18n._(t`my.profile`)}/>
                                <span>{i18n._(t`my.profile`)}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <Link className="link" to="/my/organization">
                                <i className="fa fa-building icon" alt={i18n._(t`my.organization`)}/>
                                <span>{i18n._(t`my.organization`)}</span>
                            </Link>
                        </li>
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li className="menu">
                            <Link className="link" to={`/${this.state.config.language}/store`}>
                                <i className="fa fa-shopping-cart icon" alt="Store icon"/>
                                <span>{i18n._(t`ui.appstore`)}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.state.config.opendataEndPoint}/${this.state.config.language}`}>
                                <i className="fa fa-signal icon" alt="Data icon"/>
                                <span>{i18n._(t`ui.datastore`)}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href="/logout">
                                <i className="fas fa-sign-out-alt icon" alt="Logout icon"/>
                                <span>{i18n._(t`ui.logout`)}</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>;
    }
}

export default MyNav;
