/** @jsx React.DOM */

var __apps = [
    {"id": "ck", "name": "Citizen Kin", "type": "service", "icon": image_root + "noicon.png", "public_service": true, "description": "Lorem ipsum dolor sit amet", "provider": "OpenWide", "paid": false, "installed": true, "target_citizens": true, "target_publicbodies": false, "target_companies": false},
    {"id": "dl", "name": "Ditizen Lin", "type": "application", "icon": image_root + "noicon.png", "public_service": false, "description": "Lorem ipsum dolor sit amet", "provider": "OpenWide", "paid": true, "installed": true, "target_citizens": false, "target_publicbodies": true, "target_companies": true},
    {"id": "em", "name": "Eitizen Min", "type": "service", "icon": image_root + "noicon.png", "public_service": true, "description": "Lorem ipsum dolor sit amet", "provider": "OpenWide", "paid": true, "installed": false, "target_citizens": true, "target_publicbodies": false, "target_companies": false},
    {"id": "fn", "name": "Fitizen Nin", "type": "service", "icon": image_root + "noicon.png", "public_service": false, "description": "Lorem ipsum dolor sit amet", "provider": "OpenWide", "paid": false, "installed": false, "target_citizens": false, "target_publicbodies": true, "target_companies": false}
];

var AppStore = React.createClass({
    componentDidMount: function () {
        this.updateApps(true, true, true, true, true);
    },
    getInitialState: function () {
        return {apps: []};
    },
    updateApps: function (target_citizens, target_publicbodies, target_companies, paid, free) {
        $.ajax({
            url: store_service + "/applications",
            data: {target_citizens: target_citizens, target_publicbodies: target_publicbodies, target_companies: target_companies, free: free, paid: paid},
            type: 'get',
            dataType: 'json',
            success: function (data) {
                this.setState({apps: data});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        if (this.state.apps.length == 0) {
            // loading...
            return <div className="row text-center">
                <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}</div>
        }
        else {
            return (
                <div className="row">
                    <SideBar updateApps={this.updateApps}/>
                    <AppList apps={this.state.apps}/>
                </div>
                );
        }
    }
});


var SideBar = React.createClass({
    getInitialState: function () {
        return {
            audience: {
                citizens: true,
                publicbodies: true,
                companies: true
            },
            payment: {
                paid: true,
                free: true
            }
        };
    },
    change: function (category, item) {
        return function () {
            // check that we can indeed change the box
            var canChange = false;
            for (var i in this.state[category]) {
                if (i != item) {
                    if (this.state[category][i] == true) {
                        canChange = true;
                        break;
                    }
                }
            }
            if (!canChange) return;

            var state = this.state;
            state[category][item] = !(state[category][item]);
            this.setState(state);

            this.props.updateApps(state.audience.citizens, state.audience.publicbodies, state.audience.companies, state.payment.paid, state.payment.free);
        }.bind(this);
    },
    render: function () {
        return (
            <div className="col-md-4">
                <h2>
                    <img src={image_root + "my/app-store.png"} /> {t('ui.appstore')}</h2>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.state.audience.citizens} onChange={this.change('audience', 'citizens')}/>{t('citizens')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.state.audience.publicbodies} onChange={this.change('audience', 'publicbodies')}/>{t('publicbodies')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.state.audience.companies} onChange={this.change('audience', 'companies')} />{t('companies')}
                    </label>
                </div>
                <div></div>

                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.state.payment.free} onChange={this.change('payment', 'free')} />{t('free')}
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input type="checkbox" checked={this.state.payment.paid} onChange={this.change('payment', 'paid')} />{t('paid')}
                    </label>
                </div>
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
        });

        return (
            <div className="col-md-8 app-store-result">
            {apps}
            </div>
            );
    }
});

var App = React.createClass({
    componentDidMount: function () {
        if (default_app && default_app.type == this.props.app.type && default_app.id == this.props.app.id) {
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
                tos: '',
                policy: '',
                longdescription: '',
                screenshots: null
            },
            orgs: [],
            createOrg: false,
            buying: false
        };
    },
    componentDidMount: function () {
        $(this.refs.modal.getDOMNode()).on("hide.bs.modal", function (event) {
            history.pushState({}, "store", store_root);
        });
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
            url: network_service + "/organizations",
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
    renderAppDescription: function () {
        var carousel = this.state.app.screenshots ? (
            <div className="row">
                <Carousel images={this.state.app.screenshots} />
            </div>
            ) : null;

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
                        <InstallButton app={this.props.app} orgs={this.state.orgs} installApp={this.installApp} createNewOrg={this.createNewOrg}/>
                    </div>
                </div>
                <div className="row">
                    <Ratings rating={this.state.app.rating} />
                </div>
                {carousel}
                <div className="row">
                    <div className="col-md-6" dangerouslySetInnerHTML={{__html: this.state.app.longdescription}}>

                    </div>
                    <div className="col-md-6">
                        <p>{t('agree-to-tos')}</p>
                        <p>
                            <a href={this.state.app.tos}>{t('tos')}</a>
                        </p>
                        <p>
                            <a href={this.state.app.policy}>{t('privacy')}</a>
                        </p>
                    </div>
                </div>
            </div>
            );
    },

    renderCreateNew: function () {
        return (
            <div>
                <h3>{t('create-new-org')}</h3>
                <CreateOrganizationForm ref="createOrgForm" successHandler={this.orgCreated} />
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
            if (this.onlyForCitizens()) {
                // install app straight on if it's a citizen-only application
                return <a className="btn btn-primary" href="#" onClick={this.installApp}>{t('install')}</a>;
            } else {
                var organizations = this.props.orgs.map(function (org) {
                    return <li key={org.id}>
                        <a href="#" onClick={this.installAppForOrganization(org.id)}>{org.name}</a>
                    </li>;
                }.bind(this));
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
                                <button type="button" className="btn btn-default" onClick={this.installApp}>{t('for_myself')}</button>
                                <button type="button" className="btn btn-default" data-toggle="dropdown">{t('on_behalf_of')}
                                    <span className="caret"></span>
                                </button>
                                <ul className="dropdown-menu" role="menu">
                                    {organizations}
                                    <li>
                                        <a href="#" onClick={this.props.createNewOrg}>{t('create-new-org')}</a>
                                    </li>
                                </ul>
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


var Ratings = React.createClass({
    render: function () {

        var className = this.props.rating == 0 ? "rating-static rating-00" : "rating-static rating-" + (10 * this.props.rating);
        return (
            <div className="col-sm-12">
                <span className={className}></span>
            </div>
            );
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

React.renderComponent(
    <AppStore />,
    document.getElementById("store")
);