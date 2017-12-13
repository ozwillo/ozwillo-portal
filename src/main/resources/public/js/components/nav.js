import React from 'react';
import { Link } from 'react-router-dom';

import t from '../util/message';

class Nav extends React.Component {

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
                        {/*<li data-th-each="item : ${navigation}">
                            <a href="#" data-th-className="${item.isActive()}? 'active' : ''" data-th-href="@{'/my/' + ${item.id}}">
                                <img data-th-src="@{'/img/' + ${item.id} + '.png'}"
                                     alt="" data-th-alt="#{'my.' + ${item.id}}"/>
                                <span data-th-text="#{'my.' + ${item.id}}">Menu</span>
                            </a>
                        </li>*/}
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li>
                            <a href="">
                                <img src="/img/store-icon-white.png" alt="Store icon"/>
                                <span>{t('ui.appstore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href="">
                                <img src="/img/data-icon-white.png" alt="Data icon"/>
                                <span>{t('ui.datastore')}</span>
                            </a>
                        </li>
                        <li>
                            <a href="/logout">
                                <img src="/img/close.png" alt="Logout icon"/>
                                <span>{t('ui.logout')}</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>;
    }
}

export default Nav;