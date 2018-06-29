import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import { withRouter } from 'react-router';
import {Link} from 'react-router-dom';
import {fetchSetLanguage} from '../actions/config';


class Nav extends React.Component {

    componentWillReceiveProps(nextProps) {
        if(this.props.language != nextProps.match.params.lang) {
            this.props.fetchSetLanguage(nextProps.match.params.lang);
        }
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
                    <ul className="nav navbar-nav">
                        {
                            this.props.siteMapHeader && this.props.siteMapHeader.contentItems.map((item, index) => {
                                const isSubMenu = item.type === 'submenu';
                                return <li className={`menu ${(isSubMenu && 'dropdown') || ''}`} key={index}>
                                    {
                                        isSubMenu &&
                                        <a href="#" className="link dropdown-toggle" data-toggle="dropdown"
                                           role="button"
                                           aria-expanded="false" aria-haspopup="true" href={item.url}>
                                            <span data-th-text="${item.label}">{item.label}</span>
                                            <span className="caret"/>
                                        </a>
                                    }
                                    {
                                        isSubMenu &&
                                        <ul className="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
                                            {
                                                item.items.map((subMenu, index) => {
                                                    return <li className="menu" role="presentation" key={index}>
                                                        <a className="link" role="menuitem" tabIndex="-1"
                                                           href={subMenu.url}>{subMenu.label}</a>
                                                    </li>
                                                })
                                            }
                                        </ul>
                                    }
                                    {
                                        !isSubMenu &&
                                        <a className="link" role="menuitem" tabIndex="-1" href={item.url}>
                                            {item.label}
                                        </a>
                                    }
                                </li>
                            })
                        }

                    </ul>

                    <ul className="nav navbar-nav navbar-right">
                        <li className="menu">
                            <a className="link" href={`/${this.props.language}/store`}>
                                <i className="fa fa-shopping-cart icon" alt="Apps store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.props.opendatEndPoint}/${this.props.language}`}>
                                <i className="fa fa-signal icon" alt="Data icon"/>
                                <span>{this.context.t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`/${this.props.language}/store/login`}>
                                <i className="fa fa-sign-in icon" alt="Login icon"/>
                                <span>{this.context.t('ui.login')}</span>
                            </a>
                        </li>
                        <li className="menu dropdown">
                            <a href="#" className="link nav-link dropdown-toggle" data-toggle="dropdown">
                                <span>{this.context.t(`store.language.${this.props.language}`)}</span>
                                <i className="caret"/>
                            </a>
                            <ul className="dropdown-menu">
                                <li className="menu">
                                    {
                                        this.props.languages && this.props.languages.map((lang, index) => {
                                            return <Link className="link" key={index} to={`/${lang}/store`}
                                                      data-th-text="${lang.name}">{this.context.t(`store.language.${lang}`)}</Link>
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

Nav.contextTypes = {
    t: PropTypes.func.isRequired
};

const mapStateToProps = state => {
    return {
        language: state.config.language,
        languages: state.config.languages,
        siteMapHeader: state.config.currentSiteMapHeader,
        opendatEndPoint: state.config.opendatEndPoint
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchSetLanguage(lang) {
            return dispatch(fetchSetLanguage(lang));
        }
    };
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Nav));