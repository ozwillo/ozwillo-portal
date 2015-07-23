/** @jsx React.DOM */

var AppStore = React.createClass({
    componentDidMount: function () {
        this.updateApps(true, true, true, true, true);
    },
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
                }
            }
        };
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
    updateApps: function (target_citizens, target_publicbodies, target_companies, paid, free) {
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
            var state = this.state;
            state.defaultApp = null;
            this.setState(state);
        }
        var supported_locales = [];
        if (this.refs.sideBar.state.selectedLanguage !== 'all') {
            supported_locales.push(this.refs.sideBar.state.selectedLanguage);
        }
        var geographicalAreaUris = this.refs.sideBar.refs.geoSearch.state.value;
        /*var geographicalAreaUris = []; // alt looking in select2 DOM rather than react state
        var geographicalAreas = $(this.refs.sideBar.refs.geoSearch.getDOMNode()).find(".select2-container").select2('data');
        //this.refs.sideBar.refs.searchDiv.props.select2Object
        $.map(geographicalAreas, function(area, i) {
            geographicalAreaUris.push(area.uri);
        }.bind(this))*/
        var searchText = this.refs.sideBar.state.searchText;
        //var searchText = $(this.refs.sideBar.refs.searchDiv.getDOMNode()).find("input.select2-input").val(); // alt looking in select2 DOM rather than react state
        var queryParams = {target_citizens: target_citizens, target_publicbodies: target_publicbodies, target_companies: target_companies,
                free: free, paid: paid,
                supported_locales: supported_locales,
                geographical_areas: geographicalAreaUris,
                category_ids: [],
                q: searchText}; // q being empty is ok
        $.ajax({
            url: store_service + "/applications",
            data: queryParams,
            traditional: true, // else geographical_areas%5B%5D=http... see http://stackoverflow.com/questions/6011284/problem-with-brackets-in-jquery-form-data-when-sending-data-as-json
            type: 'get',
            dataType: 'json',
            success: function (data) {
                this.setState({
                    apps: data.apps,
                    maybeMoreApps: data.maybeMoreApps,
                    loading: false,
                    filter: {
                        audience: {
                            citizens: target_citizens,
                            publicbodies: target_publicbodies,
                            companies: target_companies
                        },
                        payment: {
                            free: free,
                            paid: paid
                        }
                    }
                });
            }.bind(this),
            error: function (xhr, status, err) {
                this.setState({apps: [], loading: false});
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadMoreApps: function () {
        var state = this.state;
        state.loading = true;
        this.setState(state);

        $.ajax({
            url: store_service + "/applications",
            data: {
                last: this.state.apps.length,
                target_citizens: this.state.filter.audience.citizens,
                target_publicbodies: this.state.filter.audience.publicbodies,
                target_companies: this.state.filter.audience.companies,
                free: this.state.filter.payment.free,
                paid: this.state.filter.payment.paid
            },
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
        //if (this.state.loading) {
        //    // loading...
        //    return <div className="row text-center">
        //        <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}</div>
        //}
        //else {
            var languagesAndAll = JSON.parse(JSON.stringify(languages));
            languagesAndAll.unshift('all');
            var initialSelectedLanguage = currentLanguage; // filled by PortalController from Profile's if any else 'en'
            return (
                <div>
                    <div className="row">
                        <SideBar ref="sideBar"
                            currentLanguage={initialSelectedLanguage}
                            languages={languagesAndAll}
                            updateApps={this.updateApps}
                            filter={this.state.filter}
                        />
                        <AppList
                            apps={this.mergeAppsWithDefaultAppFirst()}
                        />
                    </div>
                    <div className="row">
                        <LoadMore
                            loading={this.state.loading}
                            maybeMoreApps={this.state.maybeMoreApps}
                            loadMoreApps={this.loadMoreApps}
                        />
                    </div>
                </div>
                );
        //}
    }
});


var SideBar = React.createClass({
    getInitialState: function () {
        return {
            selectedLanguage: 'en',
            geographicalAreaUris: [],
            searchText: '',
            }; // TODO or in top level state ??
    },
    componentDidMount: function () {
        var s = this.state;
        s.selectedLanguage = this.props.currentLanguage; // init to current language
        this.setState(s);
    },
    handleLanguageClicked: function (language) {
        var s = this.state;
        s.selectedLanguage = language;
        this.setState(s);
        this.search();
    },
    fullTextSearchChanged: function (event) {
        var s = this.state;
        s.searchText = event.target.value;
        this.setState(s);
    },
    search: function (event) {
        var filter = this.props.filter;
        this.props.updateApps(filter.audience.citizens, filter.audience.publicbodies, filter.audience.companies, filter.payment.paid, filter.payment.free); // NB. also reinits
    },
    searchOnEnterDown: function (event) {
        if (event.keyCode === 13) { // Enter key
            this.search(event);
        }
    },
    change: function (category, item) {
        return function () {
            // check that we can indeed change the box
            var canChange = false;
            for (var i in this.props.filter[category]) {
                if (i != item) {
                    if (this.props.filter[category][i] === true) {
                        canChange = true;
                        break;
                    }
                }
            }
            if (!canChange) return;

            var filter = this.props.filter;
            filter[category][item] = !(filter[category][item]);

            //var state = this.state;
            //state[category][item] = !(state[category][item]);
            //this.setState(state);

            this.props.updateApps(filter.audience.citizens, filter.audience.publicbodies, filter.audience.companies, filter.payment.paid, filter.payment.free);
        }.bind(this);
    },
    render: function () {
        var languageComponents = this.props.languages.map(function(language, i) {
            return (
                <li><a onClick={this.handleLanguageClicked.bind(this, language)} href="#">{ t(language) }</a></li>
            );
        }.bind(this)); // else TypeError: this.handleLanguageClicked is undefined ; and if no previous bind, doesn't click
        return (
            <div className="col-md-4 sidebar">
                <h2>
                    <img src={image_root + "my/app-store.png"} /> {t('ui.appstore')}</h2>

                <div className="locale-filter">
                    <span>{t('languages-supported-by-applications')}</span>
                    <div className="btn-group">
                        <button type="button" className="btn btn-sm btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            <span>{ t(this.state.selectedLanguage) }</span> <span className="caret"></span>
                        </button>
                        <ul className="dropdown-menu" role="menu">
                            { languageComponents }
                        </ul>
                    </div>
                </div>
                
                <div>
                <label htmlFor="geoSearch" className="">{t('look-for-an-application')}</label>
                </div>
                
                <div className="col-lg-13" style={ (!devmode) ? { display: "none" } : {}}>
                
                <div>
                    <GeoSingleSelect2Component className="form-control" ref="geoSearch" onChange={this.search} name="geoSearch"
                            urlResources={store_service + "/geographicalAreas"}/>
                </div>

                <div className="input-group">
                    <input type="text" className="form-control" onChange={this.fullTextSearchChanged} onKeyDown={this.searchOnEnterDown} placeholder={t('keywords')} name="fullTextSearch"/>
                    <span className="input-group-btn">
                        <button className="btn btn-default" type="button" onClick={this.search}><img className="btn-search" src="/img/icon/btn-search.png"/></button>
                    </span>
                </div>
                  
                </div>
                
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.props.filter.audience.citizens} onChange={this.change('audience', 'citizens')}/>{t('citizens')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.props.filter.audience.publicbodies} onChange={this.change('audience', 'publicbodies')}/>{t('publicbodies')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.props.filter.audience.companies} onChange={this.change('audience', 'companies')} />{t('companies')}
                    </label>
                </div>
                <div></div>

                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.props.filter.payment.free} onChange={this.change('payment', 'free')} />{t('free')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.props.filter.payment.paid} onChange={this.change('payment', 'paid')} />{t('paid')}
                    </label>
                </div>
            </div>
            );
    },
    renderOldLanguageDropdown: function () {
        return (
                <div className="">
                <ul className="nav navbar-nav">
                <li className=""><label htmlFor="searchLocale" className="">{t('languages-supported-by-applications')}</label></li>
                <li className="dropdown dropdown-lang search-locale" name="searchLocale">
                    <a style={{  color: '#636884', fontSize: '18px' }} href="#" className="dropdown-toggle" data-toggle="dropdown">
                        <span>{ t(this.state.selectedLanguage) }</span>
                        <i className="caret"></i></a>
                    <ul className="dropdown-menu">
                        { languageComponents }
                    </ul>
                </li>
                </ul>
                </div>
            );
    }
});

var AppList = React.createClass({
    render: function () {
        var apps = this.props.apps.map(function (app) {
            return (
                <App key={app.id} app={app} />
                );
        }.bind(this));


        return (
            <div className="col-md-8 app-store-result">
            {apps}
            </div>
            );
    }
});

var LoadMore = React.createClass({
    render: function () {
        var loading = null;
        if (this.props.loading) {
            loading = (
                <div className="text-center">
                    <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}
                </div>
            );
        } else if (this.props.maybeMoreApps) {
            loading = (
                <div className="text-center">
                    <button className="btn btn-primary" onClick={this.props.loadMoreApps}>Load more</button>
                </div>
            );
        }

        return (
            <div className="col-md-8 col-md-offset-4">
            {loading}
            </div>
        );

    }
});

var App = React.createClass({
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
            <div>
                <AppModal ref="appmodal" app={this.props.app}/>
                <div className="hit text-center" onClick={this.openApp}>

                    {pubServiceIndicator}
                    <img src={this.props.app.icon} />
                    <p className="appname">{this.props.app.name}</p>
                    <div className="caption">
                        <p>{this.props.app.provider}</p>
                        <p className="appdescription">{this.props.app.description}</p>
                    </div>
                    <Indicator status={indicatorStatus} />
                </div>
            </div>
            );
    }
});

var Indicator = React.createClass({
    render: function () {
        var btns;
        var status = this.props.status;
        if (status == "installed") {
            btns = [
                <button key="indicator_button" className="btn btn-indicator btn-indicator-installed">{t('installed')}</button>,
                <button key="indicator_icon" className="btn btn-indicator btn-indicator-installed-icon">
                    <i className="fa fa-check"></i>
                </button>
            ];
        } else if (status == "free") {
            btns = [
                <button key="indication_button" className="btn btn-indicator btn-indicator-available">{t('free')}</button>
            ];
        } else {
            btns = [
                <button key="indicator_button" className="btn btn-indicator btn-indicator-available">{t('paid')}</button>,
                <button key="indicator_icon" className="btn btn-indicator btn-indicator-available-icon">
                    <i className="fa fa-eur"></i>
                </button>
            ];
        }

        return (
            <div className="btn-group indicator">
            {btns}
            </div>
            );
    }
});

React.render( <AppStore />, document.getElementById("store") );