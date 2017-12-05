'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';

import '../../css/specific.css';

import '../csrf';
import '../my';
import t from '../util/message';

import { GeoAreaAutosuggest } from '../util/geoarea-autosuggest.jsx';
import { AppModal } from './store-install-app.jsx'

var AppStore = createClass({
    getInitialState: function () {
        return {
            defaultApp : null,
            apps: [],
            loading: true,
            maybeMoreApps: false,
            filter: {
                audience: {
                    citizens: true,
                    publicbodies: true,
                    companies: true
                },
                payment: {
                    paid: true,
                    free: true
                },
                selectedLanguage: currentLanguage,
                geoAreaAncestorsUris: [],
                searchText: ''
            }
        };
    },
    componentDidMount: function () {
        this.updateApps();
    },
    mergeAppsWithDefaultAppFirst : function() {
        if (!this.state.defaultApp) {
            return this.state.apps;
        }
        return $.merge([ this.state.defaultApp ], $.map(this.state.apps, function(app, i) {
            if(app.id == this.state.defaultApp.id) return;
            return app;
        }.bind(this)));
    },
    updateFilter: function(category, key, value) {
        var filter = this.state.filter;
        if (category) {
            var filterCategory = filter[category];
            filterCategory[key] = value;
        } else {
            filter[key] = value;
        }
        this.setState({ filter: filter});
        this.updateApps();
    },
    getSearchFilters: function() {
        var supported_locales = [];
        if (this.state.filter.selectedLanguage !== 'all') {
            supported_locales.push(this.state.filter.selectedLanguage);
        }
        var filter = this.state.filter;
        return {
            target_citizens: filter.audience.citizens, target_publicbodies: filter.audience.publicbodies, target_companies: filter.audience.companies,
            free: filter.payment.free, paid: filter.payment.paid,
            supported_locales: supported_locales,
            geoArea_AncestorsUris: filter.geoAreaAncestorsUris,
            category_ids: [],
            q: filter.searchText  // q being empty is ok
        };
    },
    updateApps: function() {
        if (default_app) {
            $.ajax({
                url: store_service + "/application/" + default_app.type + "/" + default_app.id,
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    default_app = null; // only once
                    var state = this.state;
                    state.defaultApp = data;
                    state.defaultApp.isDefault = true; // triggers opening modal
                    this.setState(state);
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }.bind(this)
            });
        } else {
            this.setState({ defaultApp: null});
        }

        $.ajax({
            url: store_service + "/applications",
            data: this.getSearchFilters(),
            traditional: true, // else geographical_areas%5B%5D=http... see http://stackoverflow.com/questions/6011284/problem-with-brackets-in-jquery-form-data-when-sending-data-as-json
            type: 'get',
            dataType: 'json',
            success: function (data) {
                this.setState({
                    apps: data.apps,
                    maybeMoreApps: data.maybeMoreApps,
                    loading: false
                });
            }.bind(this),
            error: function (xhr, status, err) {
                this.setState({ apps: [], loading: false });
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadMoreApps: function () {
        this.setState({ loading: true });

        var searchFilters = this.getSearchFilters();
        searchFilters.last = this.state.apps.length;
        $.ajax({
            url: store_service + "/applications",
            data: searchFilters,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.apps = state.apps.concat(data.apps);
                state.loading = false;
                state.maybeMoreApps = data.maybeMoreApps;
                this.setState(state);
            }.bind(this),
            error: function (data) {
                // maybe not such an error, could be there's no more data to get...
                var state = this.state;
                state.loading = false;
                state.maybeMoreApps = false;
                this.setState(state);
            }.bind(this)
        });
    },
    render: function () {
        var languagesAndAll = JSON.parse(JSON.stringify(languages));
        languagesAndAll.unshift('all');

        return (
            <div>
                <SearchAppsForm ref="searchAppsForm"
                                languages={languagesAndAll}
                                updateApps={this.updateApps}
                                filter={this.state.filter}
                                updateFilter={this.updateFilter}/>
                <AppList apps={this.mergeAppsWithDefaultAppFirst()}/>
                <LoadMore
                    loading={this.state.loading}
                    maybeMoreApps={this.state.maybeMoreApps}
                    loadMoreApps={this.loadMoreApps}/>
            </div>
        );
    }
});


var SearchAppsForm = createClass({
    handleLanguageClicked: function(event) {
        this.props.updateFilter(null, "selectedLanguage", event.target.value);
    },
    fullTextSearchChanged: function (event) {
        this.props.updateFilter(null, "searchText", event.target.value);
    },
    onGeoChange: function (geoArea) {
        this.props.updateFilter(null, "geoAreaAncestorsUris", geoArea.ancestors);
    },
    onAudienceChange: function(event) {
        this.props.updateFilter("audience", event.target.name, event.target.checked);
    },
    onPaymentChange: function(event) {
        this.props.updateFilter("payment", event.target.name, event.target.checked);
    },
    render: function () {
        var languageComponents = this.props.languages.map(language =>
            <option key={language} value={language}>{ t(language) }</option>
        );

        return (
            <div className="container">

                <div className="row form-horizontal" id="store-search">
                    <div className="col-md-6">
                        <div className="form-group">
                            <label htmlFor="language"
                                   className="col-sm-4 control-label">{t('languages-supported-by-applications')}</label>

                            <div className="col-sm-8">
                                <select id="language" className="form-control" onChange={this.handleLanguageClicked}
                                        value={this.props.filter.selectedLanguage}>
                                    {languageComponents}
                                </select>
                            </div>
                        </div>

                        {/* geo-filer - filtering by jurisdiction (geoArea) */}
                        <div className="form-group">
                            <label htmlFor="geoSearch" className="col-sm-4 control-label">{t('geoarea')}</label>

                            <div className="col-sm-8">
                                <GeoAreaAutosuggest countryUri=""
                                                    endpoint="/geographicalAreas"
                                                    onChange={this.onGeoChange} />
                            </div>
                        </div>
                        <div className="form-group">
                            <label className="col-sm-4 control-label">{t('mode')}</label>

                            <div className="col-sm-8">
                                <label className="checkbox-inline">
                                    <input type="checkbox" name="free" checked={this.props.filter.payment.free}
                                           onChange={this.onPaymentChange}/>{t('free')}
                                </label>
                                <label className="checkbox-inline">
                                    <input type="checkbox" name="paid" checked={this.props.filter.payment.paid}
                                           onChange={this.onPaymentChange}/>{t('paid')}
                                </label>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-6">
                        <div className="form-group">
                            <label htmlFor="fulltext" className="col-sm-4 control-label">{t('keywords')}</label>

                            <div className="col-sm-8">
                                <input type="text" id="fulltext" className="form-control"
                                       onChange={this.fullTextSearchChanged}
                                       placeholder={t('keywords')} name="fullTextSearch"/>
                            </div>
                        </div>
                        <div className="form-group">
                            <label className="col-sm-4 control-label">{t('audience')}</label>

                            <div className="col-sm-8">
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="citizens"
                                               checked={this.props.filter.audience.citizens}
                                               onChange={this.onAudienceChange}/>{t('citizens')}
                                    </label>
                                </div>
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="publicbodies"
                                               checked={this.props.filter.audience.publicbodies}
                                               onChange={this.onAudienceChange}/>{t('publicbodies')}
                                    </label>
                                </div>
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="companies"
                                               checked={this.props.filter.audience.companies}
                                               onChange={this.onAudienceChange}/>{t('companies')}
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

var AppList = createClass({
    renderApps: function() {
        return this.props.apps.map(function (app) {
            return (
                <App key={app.id} app={app}/>
            );
        });
    },
    render: function() {
        if (this.props.apps.length === 0)
            return (<div></div>);
        else
            return (
                <div className="container-fluid">
                    <div className="row" id="store-apps">
                        <div className="col-md-12">
                            <div className="row clearfix">
                                {this.renderApps()}
                            </div>
                        </div>
                    </div>
                </div>
            );
    }
});

var LoadMore = createClass({
    renderLoading: function() {
        if (this.props.loading) {
            return (
                <div className="text-center">
                    <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}
                </div>
            );
        } else if (this.props.maybeMoreApps) {
            return (
                <div className="text-center">
                    <button className="btn btn-lg oz-btn-loadmore" onClick={this.props.loadMoreApps}>{t('load-more')}</button>
                </div>
            );
        }
    },
    render: function () {
        if (!this.props.loading && !this.props.maybeMoreApps)
            return (<div></div>);
        else
            return (
                <div className="container-fluid">
                    <div className="row" id="store-loadmore">
                        <div className="col-md-12">
                            {this.renderLoading()}
                        </div>
                    </div>
                </div>
            );
    }
});

var App = createClass({
    componentDidMount: function () {
        if (this.props.app.isDefault) {
            this.openApp();
        }
    },
    openApp: function () {
        this.refs.appmodal.open();
    },
    render: function () {
        var indicatorStatus = this.props.app.installed ? "installed" : (this.props.app.paid ? "paid" : "free");
        var pubServiceIndicator = this.props.app.public_service ?
            <div className="public-service-indicator">
                <div className="triangle"></div>
                <div className="label">
                    <i className="triangle fa fa-institution"></i>
                </div>
            </div> : null;

        return (
            <div className="col-lg-2 col-md-3 col-sm-6 col-xs-12">
                <AppModal ref="appmodal" app={this.props.app}/>
                <div className="logo">
                    <img src={this.props.app.icon} />
                </div>
                <div className="app">
                    <div className="description" onClick={this.openApp}>
                        {pubServiceIndicator}
                        <div className="app-header">
                            <span className="app-name">{this.props.app.name}</span>
                            <p className="app-provider">{this.props.app.provider}</p>
                        </div>
                        <p className="app-description">{this.props.app.description}</p>
                        <Indicator status={indicatorStatus} />
                    </div>
                </div>
            </div>
        );
    }
});

var Indicator = createClass({
    render: function () {
        var btns;
        var status = this.props.status;
        if (status === "installed") {
            btns = [
                <button type="button" key="indicator_button" className="btn btn-lg btn-installed">{t('installed')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-installed-indicator">
                    <i className="fa fa-check"></i>
                </button>
            ];
        } else if (status === "free") {
            btns = [
                <button type="button" key="indicator_button" className="btn btn-lg btn-free">{t('free')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-free-indicator">
                    <i className="fa fa-gift"></i>
                </button>
            ];
        } else {
            btns = [
                <button type="button" key="indicator_button" className="btn btn-lg btn-buy">{t('paid')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-buy-indicator">
                    <i className="fa fa-eur"></i>
                </button>
            ];
        }

        return (
            <div className="app-status text-center">
                {btns}
            </div>
        );
    }
});

ReactDOM.render( <AppStore />, document.getElementById("store") );

module.exports = { AppStore };
