import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import { withRouter } from 'react-router';
import {Link} from 'react-router-dom';
import {fetchSetLanguage} from '../actions/config';


class Nav extends React.Component {

    componentWillReceiveProps(nextProps) {
        if(this.props.language !== nextProps.match.params.lang) {
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
                    <ul className="nav navbar-nav navbar-right">
                        <li className="menu">
                            <a className="link" href={`/${this.props.language}/store`}>
                                <i className="fa fa-shopping-cart icon" alt="Apps store icon"/>
                                <span>{this.context.t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li className="menu">
                            <a className="link" href={`${this.props.opendataEndPoint}/${this.props.language}`}>
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
        opendataEndPoint: state.config.opendataEndPoint
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