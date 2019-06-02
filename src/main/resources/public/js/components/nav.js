import React from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';
import {Link} from 'react-router-dom';
import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import customFetch from "../util/custom-fetch";


class Nav extends React.Component {

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
        return <nav className="navbar navbar-default navbar-noauth" id="oz-nav">
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
                    <ul className="nav navbar-nav navbar-right">
                        <li className="menu">
                            <a className="link" href={`/${this.state.config.language}/store`}>
                                <i className="fa fa-shopping-cart icon" alt="Apps store icon"/>
                                <span>{i18n._(t`ui.appstore`)}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.state.config.opendataEndPoint}/${this.state.config.language}`}>
                                <i className="fa fa-signal icon" alt="Data icon"/>
                                <span>{i18n._(t`ui.datastore`)}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`/${this.state.config.language}/store/login`}>
                                <i className="fa fa-sign-in icon" alt="Login icon"/>
                                <span>{i18n._(t`ui.login`)}</span>
                            </a>
                        </li>
                        <li className="menu dropdown">
                            <a href="#" className="link nav-link dropdown-toggle" data-toggle="dropdown">
                                <span>{i18n._(`store.language.${this.state.config.language}`)}</span>
                                <i className="caret"/>
                            </a>
                            <ul className="dropdown-menu">
                                <li className="menu">
                                    {
                                        this.state.config.languages && this.state.config.languages.map((lang, index) => {
                                            return <Link className="link" key={index} to={`/${lang}/store`}>{i18n._(`store.language.${lang}`)}</Link>
                                        })
                                    }
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

    }
}

export default withRouter(Nav);
