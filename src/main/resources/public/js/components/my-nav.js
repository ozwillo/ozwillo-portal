import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import {connect} from "react-redux";

class MyNav extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <nav className="navbar navbar-default navbar-auth" id="oz-nav">
            <div className="container-fluid">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                            data-target="#ozwillo-navbar" aria-expanded="false" aria-controls="navbar">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                    </button>
                </div>

                <div className="collapse navbar-collapse" id="ozwillo-navbar">
                    <ul className="nav navbar-nav">
                        <li>
                            <Link to="/my/">
                                <img src="/img/dashboard.png"
                                     alt={this.context.t('my.dashboard')}/>
                                <span>{this.context.t('my.dashboard')}</span>
                            </Link>
                        </li>
                        <li>
                            <Link to="/my/profile">
                                <img src="/img/profile.png"
                                     alt={this.context.t('my.profile')}/>
                                <span>{this.context.t('my.profile')}</span>
                            </Link>
                        </li>
                        <li>
                            <Link to="/my/network">
                                <img src="/img/network.png"
                                     alt={this.context.t('my.network')}/>
                                <span>{this.context.t('my.network')}</span>
                            </Link>
                        </li>
                        <li>
                            <Link to="/my/apps">
                                <img src="/img/apps.png"
                                     alt={this.context.t('my.apps')}/>
                                <span>{this.context.t('my.apps')}</span>
                            </Link>
                        </li>
                        <li>
                            <Link to="/my/organization">
                                <img src="/img/apps.png"
                                     alt={this.context.t('my.apps')}/>
                                <span>{'Organization'}</span>
                            </Link>
                        </li>
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li>
                            <Link to={`/${this.props.language}/store`}>
                                <img src="/img/store-icon-white.png" alt="Store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </Link>
                        </li>
                        <li>
                            <a href={`${this.props.opendatEndPoint}/${this.props.language}`}>
                                <img src="/img/data-icon-white.png" alt="Data icon"/>
                                <span>{this.context.t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href="/logout">
                                <img src="/img/close.png" alt="Logout icon"/>
                                <span>{this.context.t('ui.logout')}</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>;
    }
}

MyNav.contextTypes = {
    t: PropTypes.func.isRequired
};

const mapStateToProps = state => {
    return {
        language: state.config.language,
        opendatEndPoint: state.config.opendatEndPoint
    };
};

export default connect(mapStateToProps)(MyNav);