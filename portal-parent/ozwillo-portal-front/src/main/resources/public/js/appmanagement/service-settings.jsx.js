/** @jsx React.DOM */


var Service = React.createClass({
    getInitialState: function() {
        return {
            service: this.props.service,
            saved_service: this.props.service,
            field_errors: []
        };
    },
    updateServiceLocally: function(fieldname, fieldvalue) {
        var state = this.state;
        if(fieldname === 'geographical_areas'){
            state.service.service.geographical_areas = [fieldvalue];
        }else{
            state.service.service[fieldname] = fieldvalue;
        }
        this.setState(state);
    },
    saveService: function() {
        $.ajax({
            url: apps_service + "/service/" + this.props.service.service.id,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(this.state.service.service),
            success: function(data) {
                if (data.success) {
                    this.refs.settings.close();
                    this.reloadService();
                } else {
                    var s = this.state;
                    s.field_errors = data.errors;
                    this.setState(s);
                }
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(apps_service + "/service/" + this.props.service.service.id, status, err.toString());
            }.bind(this)
        });
    },
    reloadService: function() {
        $.ajax({
            url: apps_service + "/service/" + this.props.service.service.id,
            type: 'get',
            dataType: 'json',
            success: function(data) {
                var s = this.state;
                s.saved_service = data;
                s.field_errors = [];
                this.setState(s);
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    settings: function (event) {
        event.preventDefault(); // else scrolltop jumps to top #178
        var s = this.state;
        s.service = JSON.parse(JSON.stringify(s.saved_service));    // create a deep copy of the structure
        s.field_errors = [];
        this.setState(s);
        this.refs.settings.open();
    },
    pushToDash: function (event) {
        event.preventDefault(); // else scrolltop jumps to top #178
        this.refs.pushToDash.open();
    },
    componentDidMount: function() {
        $("a.tip", this.getDOMNode()).tooltip();
    },
    render: function () {
        var links = [];
        if (this.props.status !== 'STOPPED') {
            links.push(
                <span className="pull-right action-icon" onClick={this.settings}>
                    <i className="fa fa-cog fa-lg"></i>
                </span>
            );
        }

        if (!this.state.saved_service.service.visible && this.props.status !== 'STOPPED') {
            links.push(
                <span className="pull-right btn-line action-icon" onClick={this.pushToDash}>
                    <i className="fa fa-home fa-lg"></i>
                </span>
            );
        }

        return (
            <div className="row authority-app-services">
                <div className="col-sm-10">{this.state.saved_service.service.name}</div>
                <div className="col-sm-2">{links}</div>
                <ServiceSettings ref="settings" service={this.state.service} errors={this.state.field_errors} update={this.updateServiceLocally} save={this.saveService}/>
                <DashboardUsersManagement ref="pushToDash" serviceId={this.props.service.service.id} instanceId={this.props.instance} />
            </div>
        );
    }
});


var FormField = React.createClass({
    render: function() {
        var className = this.props.error ? "form-group has-error" : "form-group";

        return (
            <div className={className}>
                <label htmlFor={this.props.name} className="control-label col-sm-3">{t(this.props.name)}</label>
                <div className="col-sm-9">
                    {this.props.children}
                </div>
            </div>
        );
    }
});

var ServiceSettings = React.createClass({
    getInitialState: function() {
        return {
            service: this.props.service,
            refreshedIconUrl: this.props.service.iconUrl
        };
    },
    handleChange: function(field, checkbox) {
        return function(event) {
            if (checkbox) {
                this.props.update(field, event.target.checked);
            }else if(field === 'geographical_areas'){
                this.props.update(field, event.val); // added.name/uri
            } else {
                this.props.update(field, event.target.value);
            }
        }.bind(this);
    },
    open: function() {
        this.refs.modal.open();
    },
    close: function() {
        this.refs.modal.close();
    },
    updateUploadedIcon: function(servedImageUrlData) {
        // for save :
        this.props.update('icon', servedImageUrlData);
        this.props.update('iconUrl', servedImageUrlData); // used to display icon in modal ; NOT using virtual URL
        // for uploaded icon display :
        var state = this.state;
        //var virtualIconUrl = '/media/' + this.props.service.service.id + '/icon.png';
        this.state.refreshedIconUrl = servedImageUrlData + '#' + new Date().getTime(); // refreshes (actually
        // not required because different filenames, but could be if stayed the same ex. citizenkin.png or icon.png)
        this.state.service.service.icon = servedImageUrlData; // the Kernel service's
        this.state.service.iconUrl = servedImageUrlData; // not required, inited by server to the Kernel service's
        this.setState(state);
    },
    render: function() {
        var divIconClassName = "form-group";
        if ($.inArray("icon", this.props.errors) != -1) {
            divIconClassName = divIconClassName + " has-error";
        }

        var visibility = null;
        if (!this.props.service.service.restricted) {
            visibility = (
                <div className="form-group">
                    <label htmlFor="published" className="control-label col-sm-3">{this.props.service.service.visible ? t('published') : t('notpublished')}</label>
                    <div className="col-sm-9">
                        <input className="switch" type="checkbox" id="published" checked={this.props.service.service.visible} onChange={this.handleChange('visible', true)} />
                    </div>
                </div>
            );
        } else {
            visibility = (
                <div className="form-group">
                    <div className="col-sm-9 col-sm-offset-3">
                        <p>{t('restricted-service')}</p>
                    </div>
                </div>
            );
        }

        return (
            <Modal title={this.props.service.name} ref="modal" successHandler={this.props.save} large={true}>
                <form className="form-horizontal"  role="form">
                    <FormField name="name" error={$.inArray("name", this.props.errors) != -1}>
                        <input type="text" name="name" id="name" className="form-control" value={this.props.service.service.name}
                               onChange={this.handleChange("name")}></input>
                    </FormField>
                    <FormField name="description" error={$.inArray("description", this.props.errors) != -1}>
                        <textarea name="description" id="description" className="form-control" value={this.props.service.service.description}
                            onChange={this.handleChange("description")}></textarea>
                    </FormField>

                    <div className={divIconClassName}>
                        <label htmlFor="icon" className="control-label col-sm-3">{t('icon')}</label>
                        <div className="col-sm-1">
                            <img src={this.state.refreshedIconUrl} />
                        </div>
                        <div className="col-sm-8">
                            <input name="icon" type="text" id="icon" className="form-control" value={this.props.service.service.icon}
                                   onChange={this.handleChange('icon')}/>
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="col-sm-9 col-sm-offset-3">
                            <div className="form-control btn btn-default btn-upload">
                                <label>{t('upload')}</label>
                                <FileUploadInput className="upload" uploadUrl={"/media/objectIcon/" + this.props.service.service.id}
                                                 success={this.updateUploadedIcon} />
                            </div>
                        </div>
                    </div>

                    <FormField name="geographical-area-of-interest" error={$.inArray("description", this.props.errors) != -1}>
                        <GeoSingleSelect2Component className="form-control" ref="geoSearch" name="geoAreaOfInterest"
                            key={this.props.service.service.geographical_areas}
                            urlResources={store_service + "/geographicalAreas"}
                            onChange={this.handleChange("geographical_areas")}
                            countryFilter={ {country_uri:''} }
                            placeholder={ !this.props.service.service.geographical_areas || this.props.service.service.geographical_areas.length === 0 ? ' '
                                : decodeURIComponent(this.props.service.service.geographical_areas[0])
                                        .substring(this.props.service.service.geographical_areas[0].lastIndexOf("/")+1) } />
                     </FormField>

                    {visibility}
                </form>
            </Modal>
        );
    }
});
