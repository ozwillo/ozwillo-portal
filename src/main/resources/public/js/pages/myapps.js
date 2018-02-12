'use strict';

import '../util/csrf';

import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';


import { Modal } from '../components/bootstrap-react';
import Service from '../components/service-settings';
import ApplicationUsersManagement from '../components/instance-users';
import Loading from '../components/loading';

import moment from 'moment';

var MyApps = createClass({
    getInitialState: function () {
        return {
            loading: true,
            authorities: []
        };
    },
    componentDidMount: function () {
        $.ajax({
            url: '/my/api/myapps/authorities',
            dataType: "json",
            success: function (data) {
                this.setState({
                    loading: false,
                    authorities: data
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        if (this.state.loading) {
            return <Loading />
        }
        var auths = this.state.authorities.map(function (auth) {
            return (
                <Authority name={auth.name} key={auth.id} id={auth.id} isPersonal={auth.type === 'INDIVIDUAL'}/>
            );
        });
        return (
            <div className="container" id="myapps">
                <div className="authorities">{auths}</div>
            </div>

        );
    }
});

var Authority = createClass({

    render: function () {
        var title = this.props.isPersonal ?
            (<span>{this.context.t('apps-for-personal-use')}</span>) : (<span>{this.context.t('apps-for-organization')} {this.props.name}</span>);

        return (
            <div className="authority">
                <div className="row authority-header">
                    <div className="col-sm-12">
                        <h2>{title}</h2>
                    </div>
                </div>
                <div ref="content" className="row">
                    <InstanceList id={this.props.id} name={this.props.name} authority={this.props.id}/>
                </div>
            </div>
        );
    }
});
Authority.contextTypes = {
    t: PropTypes.func.isRequired
};

var InstanceList = createClass({
    getInitialState: function () {
        return {
            loading: true,
            instances: []
        };
    },
    componentDidMount: function () {
        this.reloadInstances();
    },
    reloadInstances: function () {
        this.setState(this.getInitialState());

        $.ajax({
            url: `/my/api/myapps/instances/${this.props.id}`,
            dataType: "json",
            success: function (data) {
                this.setState({
                    loading: false,
                    instances: data
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        if (this.state.loading) {
            return (
                <p className="text-center">
                    <i className="fa fa-spinner fa-spin loading"></i> {this.context.t('ui.loading')}
                </p>
            );
        }

        var instances = this.state.instances;
        var result = instances.length != 0 ? instances.map(function (instance) {
            return <InstanceWithRedux key={instance.id} id={instance.id} instance={instance} authority={this.props.authority}
                             reload={this.reloadInstances}/>;
        }.bind(this)) : (
            <p className="text-center authority-noapp">
                <span>{this.context.t('none')} </span>
                <strong>{this.props.name}</strong>
            </p>
        );

        return (
            <div className="col-sm-12 authority-body">
                {result}
            </div>
        );
    }
});
InstanceList.contextTypes = {
    t: PropTypes.func.isRequired
};

var Instance = createClass({
    getInitialState: function() {
        return {};
    },
    componentDidMount: function () {
        $("a.tip", ReactDOM.findDOMNode(this)).tooltip();
    },
    componentDidUpdate: function () {
        if (typeof this.state.errorMessage === 'string') {
            this.refs.errorDialog.open();
        }
    },
    manageUsers: function (event) {
        event.preventDefault();
        this.refs.manageUsers.open();
    },
    confirmTrash: function (event) {
        event.preventDefault();
        this.refs.confirmTrash.open();
    },
    trash: function () {
        this.refs.confirmTrash.close();
        this.setStatus('STOPPED');
    },
    confirmUntrash: function (event) {
        event.preventDefault();
        this.refs.confirmUntrash.open();
    },
    untrash: function () {
        this.refs.confirmUntrash.close();
        this.setStatus('RUNNING');
    },
    setStatus: function (status) {
        var instance = this.props.instance;
        instance = { applicationInstance: { id: instance.applicationInstance.id, status: status } }; // else if the whole instance, Spring REST says "syntaxically incorrect" i.e. can't unmarshall
        $.ajax({
            url: `/my/api/myapps/set-status/${this.props.id}`,
            //dataType: 'json', // else parsererror on return
            contentType: 'application/json',
            type: 'post',
            data: JSON.stringify(instance),
            success: function (data) {
                //data = "test"; // to easily test error return
                if (typeof data === 'string' && data.trim().length !== 0) {
                    // assuming it's a String message returned by the Kernel
                    var state = this.state;
                    state.errorMessage = data;
                    this.setState(state); // triggers update and therefore messageDialog opens, and on close props will reload
                } else {
                    this.props.reload();
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        // NB. user is necessarily admin of all displayed orgs, and apps not PENDING

        moment.locale(this.props.language);

        var applicationInstanceStatus = this.props.instance.applicationInstance.status;
        const isPending = applicationInstanceStatus === 'PENDING' ? true : false;
        
        var manageUsersButton = null;
        // don't display « manage users » button for personal organizations or stopped instances
        if (this.props.authority.startsWith('ORGANIZATION') && applicationInstanceStatus === 'RUNNING') {
            manageUsersButton = (
                <button key={this.props.id + '-manageUsers'} type="button" className="tip btn btn-default-inverse pull-right"
                        onClick={this.manageUsers} data-toggle="tooltip" data-placement="bottom" title={this.context.t('manage_users')}>
                    <i className="fa fa-user"></i>
                </button>
            );
        }
        
        var instance = this.props.id;
        var services = this.props.instance.services.map(function (service) {
            return <Service key={service.catalogEntry.id} service={service} instance={instance} status={applicationInstanceStatus}/>;
        });

        var buttons = [];
        var dialogs = [];
        if (applicationInstanceStatus === 'STOPPED') {
            var byDeleteRequesterOnDate = this.context.t('by') + " " + this.props.instance.status_change_requester_label
                  + " (" + moment(this.props.instance.applicationInstance.status_changed) + ")";
            buttons.push(
                <button key={this.props.id + '-untrash'} type="button" className="btn oz-btn-danger pull-right" onClick={this.confirmUntrash}>{this.context.t('ui.cancel')}</button>
            );
            buttons.push(
                <span key={this.props.id + '-untrashTtl'} style={{'color':'red', 'fontStyle':'Italic', 'marginLeft':'5px', 'marginRight':'5px'}} title={byDeleteRequesterOnDate}>
                    {this.context.t('will-be-deleted')} {moment(this.props.instance.deletion_planned).fromNow()}
                </span>
            );
            var confirmUntrashTitle = this.context.t('confirm-untrash.title') + ' ' + this.props.instance.name;
            dialogs.push(
                <Modal key={this.props.id + '-untrashModal'} ref="confirmUntrash" title={confirmUntrashTitle} successHandler={this.untrash} buttonLabels={{ 'cancel': this.context.t('ui.cancel'), 'save': this.context.t('ui.confirm') }} >
                    {this.context.t('confirm-untrash.body')}
                </Modal>
            );
        } else if (!isPending) {
            buttons.push(
                <button key={this.props.id + '-trash'} type="button" className="btn oz-btn-danger btn-line pull-right" onClick={this.confirmTrash}>{this.context.t('ui.delete')}</button>
            );
            var confirmTrashTitle = this.context.t('confirm-trash.title') + ' ' + this.props.instance.name;
            dialogs.push(
                <Modal key={this.props.id + '-trashModal'} ref="confirmTrash" title={confirmTrashTitle} successHandler={this.trash}
                       buttonLabels={{ 'cancel': this.context.t('ui.cancel'), 'save': this.context.t('ui.confirm') }} saveButtonClass="oz-btn-danger">
                    {this.context.t('confirm-trash.body')}
                </Modal>
            );
        }

        return (
            <div className="authority-app">
                <Modal ref="errorDialog" infobox={true} onClose={this.props.reload} buttonLabels={{'ok': this.context.t('ui.close')}} title={this.context.t('ui.unexpected_error')}>
                    {this.state.errorMessage}
                </Modal>
                <ApplicationUsersManagement ref="manageUsers" instanceId={this.props.id} authority={this.props.authority} />
                {dialogs}

                <div className="row authority-app-title authority-app-noservices">
                    <div className="col-sm-8">
                        <img height="32" width="32" alt={this.props.instance.name} src={this.props.instance.icon}></img>
                        <h3>{this.props.instance.name}</h3>
                        {(applicationInstanceStatus === 'PENDING') &&
                            <i> - { this.context.t('pending-install') }</i>
                        }
                    </div>
                    { !isPending ?
                        <div className="col-sm-4">
                            <div className="pull-right">
                                {manageUsersButton}
                                {buttons}
                            </div>
                        </div>
                    : '' }
                </div>
                { !isPending ?
                    <div className="row authority-app-services-title">
                        <div className="col-sm-12">
                            <h4> {this.context.t('services')} </h4>
                        </div>
                    </div>
                : '' }
                {services}
            </div>
        );
    }
});
Instance.contextTypes = {
    t: PropTypes.func.isRequired
};
const InstanceWithRedux = connect(state => {
    return { language: state.config.language }
})(Instance);

class MyAppsWrapper extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <div className="oz-body page-row page-row-expanded">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        <h1 className="text-center">
                            <span className="title">{this.context.t('my.apps')}</span>
                        </h1>
                    </div>
                </div>
            </div>

            <div className="oz-body-content">
                <MyApps/>
            </div>

            <div className="push"></div>
        </div>;
    }
}

export default MyAppsWrapper;