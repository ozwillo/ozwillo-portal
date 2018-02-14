import React from 'react';
import { connect } from 'react-redux';
import PropTypes from "prop-types";


class Nav extends React.Component {

    render() {
        return <nav className="navbar navbar-default navbar-noauth" id="oz-nav">
            <div className="container-fluid">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                            data-target="#ozwillo-navbar" aria-expanded="false" aria-controls="navbar">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar" />
                        <span className="icon-bar" />
                        <span className="icon-bar" />
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
                                            <a href="#" className="link dropdown-toggle" data-toggle="dropdown" role="button"
                                               aria-expanded="false" aria-haspopup="true" href={item.url}>
                                                <span data-th-text="${item.label}">{item.label}</span>
                                                <span className="caret" />
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
                                <img className="icon" src="/img/store-icon-white.png" alt="Apps store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.props.opendatEndPoint}/${this.props.language}`}>
                                <img className="icon" src="/img/data-icon-white.png" alt="Data icon"/>
                                <span>{this.context.t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`/${this.props.language}/store/login`}>
                                <img className="icon" src="/img/login-icon-purple.png" alt="Login icon"/>
                                <span>{this.context.t('ui.login')}</span>
                            </a>
                        </li>
                        <li className="menu dropdown">
                            <a href="#" className="link nav-link dropdown-toggle" data-toggle="dropdown">
                                <span>{this.props.language}</span>
                                <i className="caret" />
                            </a>
                            <ul className="dropdown-menu">
                                <li className="menu">
                                    {
                                        this.props.languages && this.props.languages.map((lang, index) => {
                                            return <a className="link" key={index} href={`${lang}/store`}
                                                      data-th-text="${lang.name}">{lang}</a>
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
        siteMapHeader: state.config.siteMapHeader,
        opendatEndPoint: state.config.opendatEndPoint
    };
};

export default connect(mapStateToProps)(Nav);