/** @jsx React.DOM */


var converter = new Showdown.converter({extensions: ['table']});

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
                    if (this.props.filter[category][i] == true) {
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
                    <GeoMultiSelect2Component className="form-control" ref="geoSearch" onChange={this.search} name="geoSearch" />
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

var AppModal = React.createClass({
    getInitialState: function () {
        return {
            app: {
                rating: 0,
                rateable: true,
                tos: '',
                policy: '',
                longdescription: '',
                screenshots: null
            },
            orgs: [],
            createOrg: false,
            buying: false,
            error: false
        };
    },
    componentDidMount: function () {
        $(this.refs.modal.getDOMNode()).on("hide.bs.modal", function (event) {
            history.pushState({}, "store", store_root);
        }.bind(this));
    },
    loadApp: function () {

        $.ajax({
            url: store_service + "/details/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.app = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadOrgs: function () {
        $.ajax({
            url: store_service + "/organizations/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.orgs = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    open: function () {
        this.setState(this.getInitialState());
        var href = store_root + "/" + this.props.app.type + "/" + this.props.app.id;
        if (typeof history.pushState == "function") {
            history.pushState({}, "application", href);
        }

        this.loadApp();
        if (logged_in) {
            this.loadOrgs();
        }

        this.refs.modal.open();
    },
    installApp: function (organization) {

        var state = this.state;
        state.buying = true;

        this.setState(state);

        var request = {appId: this.props.app.id, appType: this.props.app.type};
        if (organization) {
            request.organizationId = organization;
        }

        $.ajax({
            url: store_service + "/buy",
            type: 'post',
            data: JSON.stringify(request),
            contentType: 'application/json',
            success: function (data) {

                if (data.success) {
                    // redirect to the Dashboard
                    window.location = "/my/dashboard";
                } else {
                    var state = this.state;
                    state.buying = false;
                    state.error = true;
                    this.setState(state);
                }

            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                var state = this.state;
                state.buying = false;
                this.setState(state);
            }.bind(this)
        });

    },
    createNewOrg: function () {
        console.log("Create new organization");
        var state = this.state;
        state.createOrg = true;
        this.setState(state);
    },
    orgCreated: function (org) {
        var state = this.state;
        state.createOrg = false;
        if (org) {
            this.installApp(org.id);
        }
        this.setState(state);

    },
    doCreateOrg: function () {
        if (this.refs.createOrgForm) {
            this.refs.createOrgForm.saveOrganization();
        }
    },
    cancelCreateOrg: function () {
        var state = this.state;
        state.createOrg = false;
        this.setState(state);
    },
    rateApp: function (rate) {
        $.ajax({
            url: store_service + "/rate/" + this.props.app.type + "/" + this.props.app.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({rate: rate}),
            success: function () {
                var state = this.state;
                state.app.rateable = false;
                state.app.rating = rate;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },

    componentDidUpdate: function () {
        var desc = $(this.getDOMNode()).find(".app-description table");
        desc.addClass("table table-bordered table-condensed table-striped");
    },

    renderAppDescription: function () {
        var carousel = (this.state.app.screenshots && this.state.app.screenshots.length > 0) ? (
            <div className="row">
                <Carousel images={this.state.app.screenshots} />
            </div>
            ) : null;

        var error = this.state.error ? (
            <div className="alert alert-danger alert-dismissible" role="alert">
                <button type="button" className="close" data-dismiss="alert">
                    <span aria-hidden="true">&times;</span>
                    <span className="sr-only">{t('ui.close')}</span>
                </button>
                <strong>{t('sorry')}</strong> {t('could-not-install-app')}
            </div>
            ) : null;

        var rateInfo;
        if (logged_in) {
            rateInfo = this.state.app.rateable ? null : (<p>{t('already-rated')}</p>);
        } else {
            rateInfo = null;
        }

        var description = converter.makeHtml(this.state.app.longdescription);

        return (
            <div>
                <div className="row">
                    <div className="col-sm-1">
                        <img src={this.props.app.icon} alt={this.props.app.name} />
                    </div>
                    <div className="col-sm-7">
                        <div>
                            <p className="appname">{this.props.app.name}</p>
                            <p>{t('by')} {this.props.app.provider}</p>
                        </div>
                    </div>
                    <div className="col-sm-4 center-container install-application">
                        <InstallButton
                            app={this.props.app}
                            orgs={this.state.orgs}
                            url={this.state.app.serviceUrl}
                            installApp={this.installApp}
                            createNewOrg={this.createNewOrg}/>
                    </div>
                </div>
                <div className="row">
                    <Rating rating={this.state.app.rating} rateable={this.state.app.rateable} rate={this.rateApp} />
                    {rateInfo}
                </div>
                {error}
                {carousel}
                <div className="row">
                    <div className="col-md-6 app-description" dangerouslySetInnerHTML={{__html: description}}>

                    </div>
                    <div className="col-md-6">
                        <p>{t('agree-to-tos')}</p>
                        <p>
                            <a href={this.state.app.tos} target="_new">{t('tos')}</a>
                        </p>
                        <p>
                            <a href={this.state.app.policy} target="_new">{t('privacy')}</a>
                        </p>
                    </div>
                </div>
            </div>
            );
    },

    orgTypeRestriction: function () {

        return {
            company: this.props.app.target_companies,
            public_body: this.props.app.target_publicbodies
        };
    },

    renderCreateNew: function () {
        return (
            <div>
                <h3>{t('create-new-org')}</h3>
                <CreateOrganizationForm ref="createOrgForm" successHandler={this.orgCreated} typeRestriction={this.orgTypeRestriction()} />
                <div className="row">
                    <div className="col-sm-4 col-sm-offset-8">
                        <a className="btn btn-default" onClick={this.cancelCreateOrg}>{t('ui.cancel')}</a>
                        <a className="btn btn-primary" onClick={this.doCreateOrg}>{t('create')}</a>
                    </div>
                </div>
            </div>
            );
    },

    renderBuying: function () {
        return (
            <h3>
                <i className="fa fa-spinner fa-spin"></i> {t('buying')}</h3>
            );
    },

    render: function () {
        var content = null;
        if (this.state.buying) {
            content = this.renderBuying();
        } else if (this.state.createOrg) {
            content = this.renderCreateNew();
        } else {
            content = this.renderAppDescription();
        }

        return (

            <Modal ref="modal" large={true} infobox={true} title={this.props.app.name}>
            {content}
            </Modal>
            );
    }
});

var InstallButton = React.createClass({
    onlyForCitizens: function () {
        return this.props.app.target_citizens && !(this.props.app.target_companies) && !(this.props.app.target_publicbodies);
    },
    hasCitizens: function () {
        return this.props.app.target_citizens;
    },
    componentDidMount: function () {
        if (!this.onlyForCitizens()) {
            $("#install").popover({
                content: $("#install-app-popover").html(),
                html: true,
                placement: "bottom",
                trigger: "click"
            });
            $(this.getDOMNode()).find("button[data-toggle='dropdown']").dropdown();
        }
    },
    installApp: function (event) {
        event.preventDefault();
        this.props.installApp();
    },
    installAppForOrganization: function (orgId) {
        return function (event) {
            event.preventDefault();
            this.props.installApp(orgId);
        }.bind(this);
    },
    render: function () {
        if (logged_in) {
            if (this.props.app.type == "service") {
                if (this.props.app.installed) {
                    if (this.props.url) {
                        return <a className="btn btn-primary" href={this.props.url} target="_new">{t('launch')}</a>;
                    } else {
                        return <a className="btn btn-primary">
                            <i className="fa fa-spinner fa-spin"></i>
                        </a>;
                    }
                } else {
                    // install app straight on if it's a citizen-only application
                    return <a className="btn btn-primary" href="#" onClick={this.installApp}>{t('install')}</a>;
                }
            } else {

                var installForSelf = null;
                if (this.hasCitizens()) {
                    installForSelf = <button type="button" className="btn btn-default" onClick={this.installApp}>{t('for_myself')}</button>;
                }

                var installOnBehalf = null;
                if (!this.onlyForCitizens()) {

                    var organizations = this.props.orgs.map(function (org) {
                        return <li key={org.id}>
                            <a href="#" onClick={this.installAppForOrganization(org.id)}>{org.name}</a>
                        </li>;
                    }.bind(this));

                    installOnBehalf = [

                        <button key="behalfButton" type="button" className="btn btn-default" data-toggle="dropdown">{t('on_behalf_of')}
                            <span className="caret"></span>
                        </button>,

                        <ul key="behalfMenu" className="dropdown-menu" role="menu">
                        {organizations}
                            <li>
                                <a href="#" onClick={this.props.createNewOrg}>{t('create-new-org')}</a>
                            </li>
                        </ul>
                    ];
                }

                return (
                    <MyPop className="btn btn-primary" label={t('install')} >
                        <div className="row">
                            <div className="col-sm-2">
                                <img src={image_root + "my/app-store.png"} />
                            </div>
                            <div className="col-sm-10">
                                <h4>{t('install_this_app')}</h4>
                                <p>{t('confirm-install-this-app')}</p>
                                {this.props.app.paid ? <p>{t('confirm-install-this-app-paid')}</p> : null}
                                {installForSelf}
                                {installOnBehalf}
                            </div>
                        </div>
                    </MyPop>
                    );
            }
        } else {
            return <a className="btn btn-primary-inverse" href={store_root + "/login?appId=" + this.props.app.id + "&appType=" + this.props.app.type}>{t('install')}</a>;
        }
    }
});


var Carousel = React.createClass({
    getInitialState: function () {
        return {index: 0};
    },
    back: function () {
        var index = this.state.index;
        index = Math.max(0, index - 1);
        this.setState({index: index});
    },
    forward: function () {
        var index = this.state.index;
        index = Math.min(this.props.images.length, index + 1);
        this.setState({index: index});
    },
    render: function () {

        if (!this.props.images) {
            return null;
        }

        var back = null;
        if (this.state.index > 0) {
            back = <a className="back" onClick={this.back}>
                <i className="fa fa-chevron-left"></i>
            </a>;
        }

        var forward = null;
        if (this.state.index < this.props.images.length - 1) {
            forward = <a className="forward" onClick={this.forward}>
                <i className="fa fa-chevron-right"></i>
            </a>;
        }

        return (
            <div className="carousel">
            {back}
                <img src={this.props.images[this.state.index]} alt={this.state.index}/>
            {forward}
            </div>
            );
    }
});


var Rating = React.createClass({
    getInitialState: function () {
        return {};
    },
    startEditing: function () {
        if (this.props.rateable) {
            this.setState({editing: true, rating: 0});
        }
    },
    stopEditing: function () {
        this.setState({editing: false, rating: 0});
    },
    rate: function () {
        if (this.props.rateable) {
            this.props.rate(this.state.rating);
        }
    },
    mouseMove: function (event) {
        if (this.state.editing) {
            var rect = this.getDOMNode().getBoundingClientRect();
            var x = Math.floor(8 * (event.clientX - rect.left) / (rect.width)) / 2;
            if (rect.right - event.clientX < 5) {
                // the last 5 pixels are a cheat for the max grade
                x = 4;
            }
            this.setState({editing: true, rating: x});
        }
    },
    render: function () {
        var className;
        var rating;
        if (this.state.editing) {
            rating = this.state.rating;
        } else {
            rating = this.props.rating;
        }
        var rt = rating < 1 ? "0" + (rating * 10) : (rating * 10);
        className = "rating-static rating-" + rt;
        return (
            <div className={className}
            onMouseEnter={this.startEditing}
            onMouseLeave={this.stopEditing}
            onMouseMove={this.mouseMove}
            onClick={this.rate}>
            </div>
            );
    }
});

React.renderComponent(
    <AppStore />,
    document.getElementById("store")
);