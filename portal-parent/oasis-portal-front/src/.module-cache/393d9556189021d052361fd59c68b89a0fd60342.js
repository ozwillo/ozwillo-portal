/** @jsx React.DOM */


var converter = new Showdown.converter({extensions: ['table']});

var AppStore = React.createClass({displayName: "AppStore",
    componentDidMount: function () {
        this.updateApps(true, true, true, true, true);
    },
    getInitialState: function () {
        return {
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
    updateApps: function (target_citizens, target_publicbodies, target_companies, paid, free) {
        $.ajax({
            url: store_service + "/applications",
            data: {target_citizens: target_citizens, target_publicbodies: target_publicbodies, target_companies: target_companies, free: free, paid: paid},
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var defaultAppMustStillBeLoaded = false;
                if (default_app) {
                    defaultAppMustStillBeLoaded = true;
                    for (var app in data.apps) {
                        if (default_app.type == app.type && default_app.id == app.id) {
                           defaultAppMustStillBeLoaded = false;
                           break;
                        }
                    }
                }
                if (defaultAppMustStillBeLoaded) {
                    // TODO one more ajax call
                } else {
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
                }
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
            return (
                React.createElement("div", null, 
                    React.createElement("div", {className: "row"}, 
                        React.createElement(SideBar, {
                            updateApps: this.updateApps, 
                            filter: this.state.filter}
                        ), 
                        React.createElement(AppList, {
                            apps: this.state.apps}
                        )
                    ), 
                    React.createElement("div", {className: "row"}, 
                        React.createElement(LoadMore, {
                            loading: this.state.loading, 
                            maybeMoreApps: this.state.maybeMoreApps, 
                            loadMoreApps: this.loadMoreApps}
                        )
                    )
                )
                );
        //}
    }
});


var SideBar = React.createClass({displayName: "SideBar",

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
        return (
            React.createElement("div", {className: "col-md-4"}, 
                React.createElement("h2", null, 
                    React.createElement("img", {src: image_root + "my/app-store.png"}), " ", t('ui.appstore')), 
                React.createElement("div", {className: "checkbox"}, 
                    React.createElement("label", null, 
                        React.createElement("input", {type: "checkbox", checked: this.props.filter.audience.citizens, onChange: this.change('audience', 'citizens')}), t('citizens')
                    )
                ), 
                React.createElement("div", {className: "checkbox"}, 
                    React.createElement("label", null, 
                        React.createElement("input", {type: "checkbox", checked: this.props.filter.audience.publicbodies, onChange: this.change('audience', 'publicbodies')}), t('publicbodies')
                    )
                ), 
                React.createElement("div", {className: "checkbox"}, 
                    React.createElement("label", null, 
                        React.createElement("input", {type: "checkbox", checked: this.props.filter.audience.companies, onChange: this.change('audience', 'companies')}), t('companies')
                    )
                ), 
                React.createElement("div", null), 

                React.createElement("div", {className: "checkbox"}, 
                    React.createElement("label", null, 
                        React.createElement("input", {type: "checkbox", checked: this.props.filter.payment.free, onChange: this.change('payment', 'free')}), t('free')
                    )
                ), 
                React.createElement("div", {className: "checkbox"}, 
                    React.createElement("label", null, 
                        React.createElement("input", {type: "checkbox", checked: this.props.filter.payment.paid, onChange: this.change('payment', 'paid')}), t('paid')
                    )
                )
            )
            );
    }
});

var AppList = React.createClass({displayName: "AppList",
    render: function () {
        var apps = this.props.apps.map(function (app) {
            return (
                React.createElement(App, {key: app.id, app: app})
                );
        });


        return (
            React.createElement("div", {className: "col-md-8 app-store-result"}, 
            apps
            )
            );
    }
});

var LoadMore = React.createClass({displayName: "LoadMore",
    render: function () {
        var loading = null;
        if (this.props.loading) {
            loading = (
                React.createElement("div", {className: "text-center"}, 
                    React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading')
                )
            );
        } else if (this.props.maybeMoreApps) {
            loading = (
                React.createElement("div", {className: "text-center"}, 
                    React.createElement("button", {className: "btn btn-primary", onClick: this.props.loadMoreApps}, "Load more")
                )
            );
        }

        return (
            React.createElement("div", {className: "col-md-8 col-md-offset-4"}, 
            loading
            )
        );

    }
});

var App = React.createClass({displayName: "App",
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
            React.createElement("div", {className: "public-service-indicator"}, 
                React.createElement("div", {className: "triangle"}), 
                React.createElement("div", {className: "label"}, 
                    React.createElement("i", {className: "triangle fa fa-institution"})
                )
            ) : null;
        return (
            React.createElement("div", null, 
                React.createElement(AppModal, {ref: "appmodal", app: this.props.app}), 
                React.createElement("div", {className: "hit text-center", onClick: this.openApp}, 

                    pubServiceIndicator, 
                    React.createElement("img", {src: this.props.app.icon}), 
                    React.createElement("p", {className: "appname"}, this.props.app.name), 
                    React.createElement("div", {className: "caption"}, 
                        React.createElement("p", null, this.props.app.provider), 
                        React.createElement("p", {className: "appdescription"}, this.props.app.description)
                    ), 
                    React.createElement(Indicator, {status: indicatorStatus})
                )
            )
            );
    }
});

var Indicator = React.createClass({displayName: "Indicator",
    render: function () {
        var btns;
        var status = this.props.status;
        if (status == "installed") {
            btns = [
                React.createElement("button", {key: "indicator_button", className: "btn btn-indicator btn-indicator-installed"}, t('installed')),
                React.createElement("button", {key: "indicator_icon", className: "btn btn-indicator btn-indicator-installed-icon"}, 
                    React.createElement("i", {className: "fa fa-check"})
                )
            ];
        } else if (status == "free") {
            btns = [
                React.createElement("button", {key: "indication_button", className: "btn btn-indicator btn-indicator-available"}, t('free'))
            ];
        } else {
            btns = [
                React.createElement("button", {key: "indicator_button", className: "btn btn-indicator btn-indicator-available"}, t('paid')),
                React.createElement("button", {key: "indicator_icon", className: "btn btn-indicator btn-indicator-available-icon"}, 
                    React.createElement("i", {className: "fa fa-eur"})
                )
            ];
        }

        return (
            React.createElement("div", {className: "btn-group indicator"}, 
            btns
            )
            );
    }
});

var AppModal = React.createClass({displayName: "AppModal",
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
            url: store_service + "/organizations/" + this.props.app.id,
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
            React.createElement("div", {className: "row"}, 
                React.createElement(Carousel, {images: this.state.app.screenshots})
            )
            ) : null;

        var error = this.state.error ? (
            React.createElement("div", {className: "alert alert-danger alert-dismissible", role: "alert"}, 
                React.createElement("button", {type: "button", className: "close", "data-dismiss": "alert"}, 
                    React.createElement("span", {"aria-hidden": "true"}, "×"), 
                    React.createElement("span", {className: "sr-only"}, t('ui.close'))
                ), 
                React.createElement("strong", null, t('sorry')), " ", t('could-not-install-app')
            )
            ) : null;

        var rateInfo;
        if (logged_in) {
            rateInfo = this.state.app.rateable ? null : (React.createElement("p", null, t('already-rated')));
        } else {
            rateInfo = null;
        }

        var description = converter.makeHtml(this.state.app.longdescription);

        return (
            React.createElement("div", null, 
                React.createElement("div", {className: "row"}, 
                    React.createElement("div", {className: "col-sm-1"}, 
                        React.createElement("img", {src: this.props.app.icon, alt: this.props.app.name})
                    ), 
                    React.createElement("div", {className: "col-sm-7"}, 
                        React.createElement("div", null, 
                            React.createElement("p", {className: "appname"}, this.props.app.name), 
                            React.createElement("p", null, t('by'), " ", this.props.app.provider)
                        )
                    ), 
                    React.createElement("div", {className: "col-sm-4 center-container install-application"}, 
                        React.createElement(InstallButton, {
                            app: this.props.app, 
                            orgs: this.state.orgs, 
                            url: this.state.app.serviceUrl, 
                            installApp: this.installApp, 
                            createNewOrg: this.createNewOrg})
                    )
                ), 
                React.createElement("div", {className: "row"}, 
                    React.createElement(Rating, {rating: this.state.app.rating, rateable: this.state.app.rateable, rate: this.rateApp}), 
                    rateInfo
                ), 
                error, 
                carousel, 
                React.createElement("div", {className: "row"}, 
                    React.createElement("div", {className: "col-md-6 app-description", dangerouslySetInnerHTML: {__html: description}}

                    ), 
                    React.createElement("div", {className: "col-md-6"}, 
                        React.createElement("p", null, t('agree-to-tos')), 
                        React.createElement("p", null, 
                            React.createElement("a", {href: this.state.app.tos, target: "_new"}, t('tos'))
                        ), 
                        React.createElement("p", null, 
                            React.createElement("a", {href: this.state.app.policy, target: "_new"}, t('privacy'))
                        )
                    )
                )
            )
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
            React.createElement("div", null, 
                React.createElement("h3", null, t('create-new-org')), 
                React.createElement(CreateOrganizationForm, {ref: "createOrgForm", successHandler: this.orgCreated, typeRestriction: this.orgTypeRestriction()}), 
                React.createElement("div", {className: "row"}, 
                    React.createElement("div", {className: "col-sm-4 col-sm-offset-8"}, 
                        React.createElement("a", {className: "btn btn-default", onClick: this.cancelCreateOrg}, t('ui.cancel')), 
                        React.createElement("a", {className: "btn btn-primary", onClick: this.doCreateOrg}, t('create'))
                    )
                )
            )
            );
    },

    renderBuying: function () {
        return (
            React.createElement("h3", null, 
                React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('buying'))
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

            React.createElement(Modal, {ref: "modal", large: true, infobox: true, title: this.props.app.name}, 
            content
            )
            );
    }
});

var InstallButton = React.createClass({displayName: "InstallButton",
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
                        return React.createElement("a", {className: "btn btn-primary", href: this.props.url, target: "_new"}, t('launch'));
                    } else {
                        return React.createElement("a", {className: "btn btn-primary"}, 
                            React.createElement("i", {className: "fa fa-spinner fa-spin"})
                        );
                    }
                } else {
                    // install app straight on if it's a citizen-only application
                    return React.createElement("a", {className: "btn btn-primary", href: "#", onClick: this.installApp}, t('install'));
                }
            } else {

                var installForSelf = null;
                if (this.hasCitizens()) {
                    installForSelf = React.createElement("button", {type: "button", className: "btn btn-default", onClick: this.installApp}, t('for_myself'));
                }

                var installOnBehalf = null;
                if (!this.onlyForCitizens()) {

                    var organizations = this.props.orgs.map(function (org) {
                        return React.createElement("li", {key: org.id}, 
                            React.createElement("a", {href: "#", onClick: this.installAppForOrganization(org.id)}, org.name)
                        );
                    }.bind(this));

                    installOnBehalf = [

                        React.createElement("button", {key: "behalfButton", type: "button", className: "btn btn-default", "data-toggle": "dropdown"}, t('on_behalf_of'), 
                            React.createElement("span", {className: "caret"})
                        ),

                        React.createElement("ul", {key: "behalfMenu", className: "dropdown-menu", role: "menu"}, 
                        organizations, 
                            React.createElement("li", null, 
                                React.createElement("a", {href: "#", onClick: this.props.createNewOrg}, t('create-new-org'))
                            )
                        )
                    ];
                }

                return (
                    React.createElement(MyPop, {className: "btn btn-primary", label: t('install')}, 
                        React.createElement("div", {className: "row"}, 
                            React.createElement("div", {className: "col-sm-2"}, 
                                React.createElement("img", {src: image_root + "my/app-store.png"})
                            ), 
                            React.createElement("div", {className: "col-sm-10"}, 
                                React.createElement("h4", null, t('install_this_app')), 
                                React.createElement("p", null, t('confirm-install-this-app')), 
                                this.props.app.paid ? React.createElement("p", null, t('confirm-install-this-app-paid')) : null, 
                                installForSelf, 
                                installOnBehalf
                            )
                        )
                    )
                    );
            }
        } else {
            return React.createElement("a", {className: "btn btn-primary-inverse", href: store_root + "/login?appId=" + this.props.app.id + "&appType=" + this.props.app.type}, t('install'));
        }
    }
});


var Carousel = React.createClass({displayName: "Carousel",
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
            back = React.createElement("a", {className: "back", onClick: this.back}, 
                React.createElement("i", {className: "fa fa-chevron-left"})
            );
        }

        var forward = null;
        if (this.state.index < this.props.images.length - 1) {
            forward = React.createElement("a", {className: "forward", onClick: this.forward}, 
                React.createElement("i", {className: "fa fa-chevron-right"})
            );
        }

        return (
            React.createElement("div", {className: "carousel"}, 
            back, 
                React.createElement("img", {src: this.props.images[this.state.index], alt: this.state.index}), 
            forward
            )
            );
    }
});


var Rating = React.createClass({displayName: "Rating",
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
            React.createElement("div", {className: className, 
            onMouseEnter: this.startEditing, 
            onMouseLeave: this.stopEditing, 
            onMouseMove: this.mouseMove, 
            onClick: this.rate}
            )
            );
    }
});

React.renderComponent(
    React.createElement(AppStore, null),
    document.getElementById("store")
);