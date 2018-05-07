import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';
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
                        <span className="icon-bar"/>
                        <span className="icon-bar"/>
                        <span className="icon-bar"/>
                    </button>
                </div>

                <div className="collapse navbar-collapse" id="ozwillo-navbar">
                    <ul className="nav navbar-nav">
                        <li className="menu">
                            <Link className="link" to="/my/">
                                <i className="fa fa-home icon" alt={this.context.t('my.dashboard')}/>
                                <span>{this.context.t('my.dashboard')}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <Link className="link" to="/my/profile">
                                <i className="fa fa-user icon" alt={this.context.t('my.profile')}/>
                                <span>{this.context.t('my.profile')}</span>
                            </Link>
                        </li>
                        <li className="menu hidden">
                            <Link className="link" to="/my/network">
                                <img className="icon" src="/img/network.png"
                                     alt={this.context.t('my.network')}/>
                                <span>{this.context.t('my.network')}</span>
                            </Link>
                        </li>
                        <li className="menu hidden">
                            <Link className="link" to="/my/apps">
                                <img className="icon" src="/img/apps.png"
                                     alt={this.context.t('my.apps')}/>
                                <span>{this.context.t('my.apps')}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <Link className="link" to="/my/organization">
                                <i className="fa fa-building icon" alt={this.context.t('my.apps')}/>
                                <span>{this.context.t('organization.form.title')}</span>
                            </Link>
                        </li>
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li className="menu">
                            <Link className="link" to={`/${this.props.language}/store`}>
                                <i className="fa fa-shopping-cart icon" alt="Store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </Link>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.props.opendatEndPoint}/${this.props.language}`}>
                                <i className="fa fa-signal icon" alt="Data icon"/>
                                <span>{this.context.t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href="/logout">
                                <i className="fas fa-sign-out-alt icon" alt="Logout icon"/>
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