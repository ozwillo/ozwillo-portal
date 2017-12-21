import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from "prop-types";


class Nav extends React.Component {

    render() {
        return <nav className="navbar navbar-default navbar-noauth" id="oz-nav">
            <div className="container-fluid">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                            data-target="#ozwillo-navbar" aria-expanded="false" aria-controls="navbar">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                    </button>
                    <Link className="navbar-brand" to="/">
                        <img src="/img/logo-ozwillo.png" alt="Logo Ozwillo"/>
                    </Link>
                </div>

                <div className="collapse navbar-collapse" id="ozwillo-navbar">

                    <ul className="nav navbar-nav">
                        {
                            this.props.siteMapHeader && this.props.siteMapHeader.contentItems.map((item, index) => {
                                const isSubMenu = item.type === 'submenu';
                                return <li className={(isSubMenu && 'dropdpwn') || ''} key={index}>
                                        {
                                            isSubMenu &&
                                            <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button"
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
                                                        return <li role="presentation" key={index}>
                                                            <a role="menuitem" tabIndex="-1" href={subMenu.url}>{subMenu.label}</a>
                                                        </li>
                                                    })
                                                }
                                            </ul>
                                        }
                                    {
                                        !isSubMenu &&
                                        <a role="menuitem" tabIndex="-1" href={item.url}>
                                            {item.label}
                                        </a>
                                    }
                                </li>
                            })
                        }

                    </ul>

                    <ul className="nav navbar-nav navbar-right">
                        <li>
                            <a href={`/${this.props.language}/store`}>
                                <img src="/img/store-icon-color.png" alt="Apps store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href={`${this.props.opendatEndPoint}/${this.props.language}`}>
                                <img src="/img/data-icon-purple.png" alt="Data icon"/>
                                <span>{this.context.t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href={`/${this.props.language}/store/login`}>
                                <img src="/img/login-icon-purple.png" alt="Login icon"/>
                                <span>{this.context.t('ui.login')}</span>
                            </a>
                        </li>
                        <li className="dropdown">
                            <a href="#" className="nav-link dropdown-toggle" data-toggle="dropdown">
                                <span>{this.props.language}</span>
                                <i className="caret"></i>
                            </a>
                            <ul className="dropdown-menu">
                                <li>

                                    {
                                        this.props.languages && this.props.languages.map((lang, index) => {
                                            return <a key={index} href={`${lang}/store`}
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