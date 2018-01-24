'use strict';

import React from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';

import GeoAreaAutosuggest from './autosuggests/geoarea-autosuggest';
import { Modal } from './bootstrap-react';
import FileUploadInput from './fileuploadinput';
import DashboardUsersManagement from './dashboard-users';

var Service = createClass({
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
            state.service.catalogEntry.geographical_areas = [fieldvalue];
        }else{
            state.service.catalogEntry[fieldname] = fieldvalue;
        }
        this.setState(state);
    },
    saveService: function() {
        $.ajax({
            url: `/my/api/myapps/service/${this.props.service.catalogEntry.id}`,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(this.state.service.catalogEntry),
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
                console.error(`/my/api/myapps/service/${this.props.service.catalogEntry.id}`, status, err.toString());
            }.bind(this)
        });
    },
    reloadService: function() {
        $.ajax({
            url: `/my/api/myapps/service/${this.props.service.catalogEntry.id}`,
            type: 'get',
            dataType: 'json',
            success: function(data) {
                let s = this.state;
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
        let s = this.state;
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
        $("a.tip", ReactDOM.findDOMNode(this)).tooltip();
    },
    render: function () {
        var links = [];
        if (this.props.status !== 'STOPPED') {
            links.push(
                <span key={this.props.service.catalogEntry.id + '-settings'} className="pull-right action-icon" onClick={this.settings}>
                    <i className="fa fa-cog fa-lg"/>
                </span>,
                <span key={this.props.service.catalogEntry.id + '-pushToDash'} className="pull-right btn-line action-icon" onClick={this.pushToDash}>
                    <i className="fa fa-home fa-lg"/>
                </span>
            );
        }

        return (
            <div className="row authority-app-services">
                <div className="col-sm-10">{this.state.saved_service.catalogEntry.name}</div>
                <div className="col-sm-2">{links}</div>
                <ServiceSettings ref="settings" service={this.state.service} errors={this.state.field_errors} update={this.updateServiceLocally} save={this.saveService}/>
                <DashboardUsersManagement ref="pushToDash" serviceId={this.props.service.catalogEntry.id} instanceId={this.props.instance} />
            </div>
        );
    }
});


let FormField = createClass({
    render: function() {
        const className = this.props.error ? "form-group has-error" : "form-group";

        return (
            <div className={className}>
                <label htmlFor={this.props.name} className="control-label col-sm-3">{this.context.t(this.props.name)}</label>
                <div className="col-sm-9">
                    {this.props.children}
                </div>
            </div>
        );
    }
});
FormField.contextTypes = {
    t: PropTypes.func.isRequired
};

let ServiceSettings = createClass({
    getInitialState: function() {
        return {
            service: this.props.service,
            refreshedIconUrl: this.props.service.iconUrl
        };
    },
    handleChange: function(field, checkbox) {
        return function(event) {
            if (checkbox) {
                if (event.target.checked) {
                    this.props.update(field, "VISIBLE");
                } else {
                    this.props.update(field, "HIDDEN");
                }
            }else if(field === 'geographical_areas'){
                this.props.update(field, event.val); // added.name/uri
            } else {
                this.props.update(field, event.target.value);
            }
        }.bind(this);
    },
    handleGeoAreaChange: function(geoArea) {
        this.props.update("geographical_areas", geoArea.uri);
    },
    getGeoAreInitialValue: function() {
        if (!this.props.service.catalogEntry.geographical_areas || this.props.service.catalogEntry.geographical_areas.length === 0)
            return ''
        else {
            const decodedGeoArea = decodeURIComponent(this.props.service.catalogEntry.geographical_areas[0]);
            return decodedGeoArea.substring(decodedGeoArea.lastIndexOf("/") + 1)
        }
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
        let state = this.state;
        //var virtualIconUrl = '/media/' + this.props.service.catalogEntry.id + '/icon.png';
        this.state.refreshedIconUrl = servedImageUrlData + '#' + new Date().getTime(); // refreshes (actually
        // not required because different filenames, but could be if stayed the same ex. citizenkin.png or icon.png)
        this.state.service.catalogEntry.icon = servedImageUrlData; // the Kernel service's
        this.state.service.iconUrl = servedImageUrlData; // not required, inited by server to the Kernel service's
        this.setState(state);
    },
    render: function() {
        let divIconClassName = "form-group";
        if (this.props.errors.indexOf("icon") !== -1) {
            divIconClassName = divIconClassName + " has-error";
        }

        let visibility = null;
        if (this.props.service.catalogEntry.visibility !== "NEVER_VISIBLE") {
            visibility = (
                <div className="form-group">
                    <label htmlFor={"published-" + this.props.service.catalogEntry.id} className="control-label col-sm-3">
                        {this.context.t('published')}
                    </label>
                    <div className="col-sm-9">
                        <input className="switch" type="checkbox" id={"published-" + this.props.service.catalogEntry.id}
                               checked={this.props.service.catalogEntry.visibility === "VISIBLE"}
                               onChange={this.handleChange('visibility', true)} />
                    </div>
                </div>
            );
        } else {
            visibility = (
                <div className="form-group">
                    <div className="col-sm-9 col-sm-offset-3">
                        <p>{this.context.t('restricted-service')}</p>
                    </div>
                </div>
            );
        }

        return (
            <Modal title={this.props.service.name} ref="modal" successHandler={this.props.save} large={true}>
                <form className="form-horizontal"  role="form">
                    <FormField name="name" error={this.props.errors.indexOf("name") !== -1}>
                        <input type="text" name="name" id="name" className="form-control" value={this.props.service.catalogEntry.name}
                               onChange={this.handleChange("name")} />
                    </FormField>
                    <FormField name="description" error={this.props.errors.indexOf("description") !== -1}>
                        <textarea name="description" id="description" className="form-control" value={this.props.service.catalogEntry.description}
                            onChange={this.handleChange("description")} />
                    </FormField>

                    <div className={divIconClassName}>
                        <label htmlFor="icon" className="control-label col-sm-3">{this.context.t('icon')}</label>
                        <div className="col-sm-1">
                            <img src={this.state.refreshedIconUrl} />
                        </div>
                        <div className="col-sm-8">
                            <input name="icon" type="text" id="icon" className="form-control" value={this.props.service.catalogEntry.icon}
                                   onChange={this.handleChange('icon')}/>
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="col-sm-9 col-sm-offset-3">
                            <div className="form-control btn btn-default btn-upload">
                                <label>{this.context.t('upload')}</label>
                                <FileUploadInput className="upload" uploadUrl={"/media/objectIcon/" + this.props.service.catalogEntry.id}
                                                 success={this.updateUploadedIcon} />
                            </div>
                        </div>
                    </div>

                    <FormField name="geographical-area-of-interest" error={this.props.errors.indexOf("description") !== -1}>
                        <GeoAreaAutosuggest name="geographical-area-of-interest" countryUri=""
                            endpoint="/geographicalAreas"
                            onChange={this.handleGeoAreaChange}
                            initialValue={this.getGeoAreInitialValue()}/>
                     </FormField>

                    {visibility}
                </form>
            </Modal>
        );
    }
});
ServiceSettings.contextTypes = {
    t: PropTypes.func.isRequired
};

export default Service;
