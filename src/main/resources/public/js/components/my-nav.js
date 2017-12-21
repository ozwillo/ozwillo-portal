import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

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
                            {/*className="${item.isActive()}? 'active' : ''"*/}
                            <Link to="/my/">
                                <img src="/img/dashboard.png"
                                     alt={this.context.t('my.dashboard')}/>
                                <span>{this.context.t('my.dashboard')}</span>
                            </Link>
                        </li>
                        <li>
                            {/*className="${item.isActive()}? 'active' : ''"*/}
                            <Link to="/my/profile">
                                <img src="/img/profile.png"
                                     alt={this.context.t('my.profile')}/>
                                <span>{this.context.t('my.profile')}</span>
                            </Link>
                        </li>
                        <li>
                            {/*className="${item.isActive()}? 'active' : ''"*/}
                            <Link to="/my/network">
                                <img src="/img/network.png"
                                     alt={this.context.t('my.network')}/>
                                <span>{this.context.t('my.network')}</span>
                            </Link>
                        </li>
                        <li>
                            {/*className="${item.isActive()}? 'active' : ''"*/}
                            <Link to="/my/apps">
                                <img src="/img/apps.png"
                                     alt={this.context.t('my.apps')}/>
                                <span>{this.context.t('my.apps')}</span>
                            </Link>
                        </li>
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li>
                            <a href="">
                                <img src="/img/store-icon-white.png" alt="Store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href="">
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

export default MyNav;