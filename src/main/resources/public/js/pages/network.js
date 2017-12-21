'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';
import moment from 'moment';

import '../util/csrf';

import { Modal } from '../components/bootstrap-react';
import SearchOrganizationModal from '../components/search-organization-modal';
import { CreateOrModifyOrganizationModal } from '../components/create-or-modify-organization';
import Loading from '../components/loading';

var MyNetwork = createClass({
    openSearchOrgDialog: function() {
        this.refs.searchOrgDialog.open();
    },
    openCreateOrgDialog: function(organization) {
        this.refs.createOrgDialog.open(organization);
    },
    reload: function() {
        this.refs.orgs.loadOrganizations();
    },
    render: function() {
        return (
            <div className="container" id="mynetwork">
                <SearchOrganizationModal ref="searchOrgDialog" successHandler={this.openCreateOrgDialog} />
                <CreateOrModifyOrganizationModal ref="createOrgDialog" successHandler={this.reload} />
                <SearchOrCreateHeader showDialog={this.openSearchOrgDialog}/>
                <OrganizationsList ref="orgs"/>
            </div>
        );
    }
});

const SearchOrCreateHeader = ({ showDialog }, context) =>
    <div className="row add-organization-action">
        <div className="col-md-12">
            <button type="button" className="btn oz-btn-save pull-right"
                    onClick={showDialog}>
                {context.t('my.network.add-organization')}
            </button>
        </div>
    </div>;
SearchOrCreateHeader.contextTypes = {
    t: PropTypes.func.isRequired
};

var OrganizationsList = createClass({
    getInitialState: function() {
        return {
            loading: true,
            organizations: [],
            generalError: false,
            organizationError: null
        };
    },
    componentDidMount: function() {
        this.loadOrganizations();
    },
    componentDidUpdate: function () {
        if (typeof this.state.organizationError === 'string') {
            this.refs.errorDialog.open();
        }
    },
    loadOrganizations: function() {
        $.ajax({
            url: '/my/api/network/organizations',
            type: 'get',
            dataType: 'json',
            success: function(data) {
                this.setState({ organizations: data, loading: false });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
                this.setState({ generalError: true });
            }.bind(this)
        });
    },
    updateOrganization: function(updatedOrganization, optionalOperation) {
        var url = `/my/api/network/organization/${updatedOrganization.id}`;
        if (optionalOperation) {
            url += '/' + optionalOperation;
        }
        $.ajax({
            url: url,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(updatedOrganization),
            success: function(data) {
                if (typeof data === 'string' && data.trim().length !== 0) {
                    // assuming it's a String message returned by the Kernel
                    this.setState({ organizationError: data });
                } else {
                    var updatedOrganizations = this.state.organizations.map(organization => {
                        if (organization.id === updatedOrganization.id)
                            return updatedOrganization;
                        else
                            return organization;
                    });
                    this.setState({ organizations: updatedOrganizations });
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                // if there's an error, reload everything
                this.loadOrganizations();
            }.bind(this)
        });
    },
    closeErrorDialog: function () {
        this.setState({ organizationError: null });
        // if there's an error, reload everything
        this.loadOrganizations();
    },
    render: function() {
        if (this.state.generalError) {
            return <p className="alert alert-danger">{this.context.t('ui.general-error')}</p>
        } else if (this.state.loading) {
            return <p className="text-center">
                <i className="fa fa-spinner fa-spin"></i> {this.context.t('ui.loading')}</p>
        } else {
            var orgs = this.state.organizations.map(org =>
                <Organization key={org.id} org={org}
                              reload={this.loadOrganizations}
                              updateOrganization={this.updateOrganization} />
            );
            return (
                <div className="organizations">
                    <Modal ref="errorDialog" infobox={true} cancelHandler={this.closeErrorDialog}
                           buttonLabels={{'ok': this.context.t('ui.close')}} title={this.context.t('my.network.information')}>
                        {this.state.organizationError}
                    </Modal>
                    {orgs}
                </div>
            );
        }
    }
});
OrganizationsList.contextTypes = {
    t: PropTypes.func.isRequired
};

var Organization = createClass({
    propTypes: {
        org: PropTypes.object.isRequired,
        reload: PropTypes.func.isRequired,
        updateOrganization: PropTypes.func.isRequired
    },
    getInitialState: function() {
        return {
            invite: { email: "", errors: [] },
            members: [],
            pending_members: []
        };
    },
    componentDidMount: function() {
        this.loadMembers();
        if (this.props.org.admin)
            this.loadPendingMembers();
    },
    loadMembers: function () {
        $.ajax({
            url: `/my/api/network/organization/${this.props.org.id}/members`,
            dataType: 'json',
            type: 'get'
        }).done(data => {
            this.setState({ members: data })
        }).fail((xhr, status, err) => {
            console.error(status, err.toString());
        })
    },
    loadPendingMembers: function () {
        $.ajax({
            url: `/my/api/network/organization/${this.props.org.id}/pending-members`,
            dataType: 'json',
            type: 'get'
        }).done(data => {
            this.setState({ pending_members: data })
        }).fail((xhr, status, err) => {
            console.error(status, err.toString());
        })
    },
    updateInvitation: function(event) {
        this.setState({ invite: { email: event.target.value, errors: [] } });
    },
    invite: function() {
        var state = this.state;
        if (this.state.invite.email.trim() == '' || this.state.invite.email.indexOf('@') == -1) {
            state.invite.errors = ["email"];
            this.setState(state);
        }
        else {
            $.ajax({
                url: `/my/api/network/invite/${this.props.org.id}`,
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify({email: state.invite.email}),
                success: function(data) {
                    var state = this.state;
                    state.invite = {email: "", errors: []};
                    this.setState(state);
                    this.refs.inviteDialog.close();
                    this.loadPendingMembers();
                }.bind(this),
                error: function(xhr, status, err) {
                    console.error(status, err.toString());
                    var state = this.state;
                    state.invite.errors = ["general"];
                    this.setState(state);
                }.bind(this)
            });
        }
    },
    openInvitation: function() {
        this.setState({ invite: { email:"", errors:[] } });
        this.refs.inviteDialog.open();
    },
    confirmLeave: function() {
        this.refs.leaveDialog.open();
    },
    removeInvitation: function(pMember) {
        $.ajax({
            url: `/my/api/network/remove-invitation/${this.props.org.id}`,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({id: pMember.id, email: pMember.email, etag: pMember.pending_membership_etag}),
            success: function() {
                this.setState({ pending_members: this.state.pending_members.filter(pm => pm.id != pMember.id) })
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }
        });
    },
    leave: function() {
        $.ajax({
            url: "/my/api/network/leave",
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({organization: this.props.org.id}),
            success: function() {
                // TODO : do not reload all orgs just to remove one from the list
                this.props.reload();
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }
        });

        this.refs.leaveDialog.close();
    },
    showInformation: function() {
        this.refs.infoDialog.open();
    },
    confirmTrash: function() {
        this.refs.confirmTrashDialog.open();
    },
    trash: function () {
        var org = Object.assign({}, this.props.org, { status: 'DELETED' });
        this.props.updateOrganization(org, 'set-status');
        this.refs.confirmTrashDialog.close();
    },
    confirmUntrash: function() {
        this.refs.confirmUntrashDialog.open();
    },
    untrash: function () {
        var org = Object.assign({}, this.props.org, { status: 'AVAILABLE' });
        this.props.updateOrganization(org, 'set-status');
        this.refs.confirmUntrashDialog.close();
    },
    removeMember: function(member) {
        return function (event) {
            if (event) {
                event.preventDefault();
            }
            $.ajax({
                url: `/my/api/network/organization/${this.props.org.id}/membership/${member.id}`,
                type: 'delete'
            }).done(() => {
                this.setState({ members: this.state.members.filter(m => m.id != member.id) })
            }).fail((xhr, status, err) => {
                console.error(status, err.toString());
            })
        }.bind(this);
    },
    updateMember: function (member, admin) {
        $.ajax({
            url: `/my/api/network/organization/${this.props.org.id}/membership/${member.id}`,
            type: 'post',
            data: { admin: admin }
        }).fail((xhr, status, err) => {
            console.error(status, err.toString());
        })
    },
    renderMembers: function() {
        if (this.state.members.length === 0) {
            return <Loading className="organization-member-row"/>
        } else if (this.props.org.admin) {
            return this.state.members.map(member =>
                <Member key={member.id} member={member}
                        remove={this.removeMember} updateMember={this.updateMember}/>
            );
        } else {
            return this.state.members.map(member => <ReadOnlyMember key={member.id} member={member} />);
        }
    },
    renderPendingMemberships: function() {
        if (this.props.org.admin) {
            return this.state.pending_members.map(pMember =>
                <PendingMembership key={pMember.id} pMember={pMember}
                                   removeInvitation={this.removeInvitation}  />
            );
        }
    },
    render: function() {

        var buttons = [];
        var dialogs = [];

        dialogs.push(
            <Modal ref="errorDialog" infobox={true} onClose={this.closeErrorDialog} buttonLabels={{'ok': this.context.t('ui.close')}} title={this.context.t('my.network.information')}>
                {this.state.errorMessage}
            </Modal>
        );

        if (this.props.org.status === 'DELETED') {
            moment.locale(currentLanguage);
            var byDeleteRequesterOnDate = this.context.t('my.network.by') + " " + this.props.org.status_change_requester_label
                + " (" + moment(this.props.org.status_changed) + ")";
            buttons.push(
                <span key="untrashTtl" style={{'color':'red', 'fontStyle':'Italic', 'marginLeft':'5px', 'marginRight':'5px'}} title={byDeleteRequesterOnDate}>
                    {this.context.t('my.network.will-be-deleted')} {moment(this.props.org.deletion_planned).fromNow()}
                </span>
            ); // (not a button per se)

            if (this.props.org.admin) {
                buttons.push(
                    <button type="button" key="confirmUntrash" className="btn oz-btn-danger btn-sm pull-right" onClick={this.confirmUntrash}>{this.context.t('ui.cancel')}</button>
                );

                var confirmUntrashTitle = this.context.t('my.network.confirm-untrash.title') + ' ' + this.props.org.name;
                dialogs.push(
                    <Modal ref="confirmUntrashDialog" title={confirmUntrashTitle} successHandler={this.untrash}
                           buttonLabels={{ 'cancel': this.context.t('ui.cancel'), 'save': this.context.t('ui.confirm') }} >
                        {this.context.t('my.network.confirm-untrash.body')}
                    </Modal>
                );
            }

        } else {

            buttons.push(
                <button type="button" key="info" className="btn btn-default btn-sm btn-line" onClick={this.showInformation}>{this.context.t('my.network.information')}</button>
            );

            dialogs.push(
                <LeaveDialog ref="leaveDialog" onSubmit={this.leave}/>,
                <InformationDialog ref="infoDialog" org={this.props.org} onUpdate={this.props.reload} />
            );

            if (this.props.org.admin) {
                buttons.push(
                    <button type="button" key="invite" className="btn btn-default btn-sm btn-line" onClick={this.openInvitation}>{this.context.t('my.network.invite')}</button>
                );

                if (this.state.members.length > 0 && this.state.members.filter(m => m.admin).length != 1) {
                    // admins can leave only if there will still be another admin
                    buttons.push(
                        <button type="button" key="leave" className="btn btn-warning btn-sm btn-line" onClick={this.confirmLeave}>{this.context.t('my.network.leave')}</button>
                    );
                }

                dialogs.push(
                    <InviteDialog ref="inviteDialog"
                                  admin={this.props.org.admin}
                                  onSubmit={this.invite}
                                  onChange={this.updateInvitation}
                                  email={this.state.invite.email}
                                  errors={this.state.invite.errors}/>
                );

                buttons.push(
                    <button type="button" key="confirmTrash" className="btn oz-btn-danger btn-sm" onClick={this.confirmTrash}>{this.context.t('ui.delete')}</button>
                );

                var confirmTrashTitle = this.context.t('my.network.confirm-trash.title') + ' ' + this.props.org.name;
                dialogs.push(
                    <Modal ref="confirmTrashDialog" title={confirmTrashTitle} successHandler={this.trash}
                           buttonLabels={{ 'cancel': this.context.t('ui.cancel'), 'save': this.context.t('ui.confirm') }} saveButtonClass="oz-btn-danger" >
                        {this.context.t('my.network.confirm-trash.body')}
                    </Modal>
                );


            } else {
                // non-admins can leave at any time
                buttons.push(
                    <button type="button" key="leave" className="btn btn-warning btn-sm" onClick={this.confirmLeave}>{this.context.t('my.network.leave')}</button>
                );
            }
        }

        return (
            <div className="organization">
                {
                    dialogs.map((item, index) => {
                        return React.cloneElement(item, { key: index });
                    })
                }

                <div className="row organization-header">
                    <div className="col-sm-6">
                        <h4 title={this.props.org.id}>{this.props.org.name}</h4>
                    </div>
                    <div className="col-sm-6">
                        <div className="pull-right">
                            {buttons}
                        </div>
                    </div>
                </div>
                {this.renderMembers()}
                {this.renderPendingMemberships()}
            </div>
        );
    }
});
Organization.contextTypes = {
    t: PropTypes.func.isRequired
};

const PendingMembership = ({pMember, removeInvitation}) =>
    <div className="row organization-pending-member-row">
        <div className="col-sm-3">{pMember.email}</div>
        <div className="col-sm-3">{this.context.t('my.network.organization.pending-invitation') }</div>
        <div className="col-sm-6">
            <span className="pull-right action-icon" onClick={() => removeInvitation(pMember)}>
                <i className="fa fa-trash"></i>
            </span>
        </div>
    </div>;

var Member = createClass({
    getInitialState: function() {
        return {
            edit: false,
            admin: this.props.member.admin
        };
    },
    toggleEdit: function (event) {
        if (event != null) {
            event.preventDefault();
        }
        this.setState({ edit: !this.state.edit })
    },
    save: function (event) {
        event.preventDefault();
        this.props.updateMember(this.props.member, this.state.admin);
        this.toggleEdit(null);
    },
    changeUserRole: function(event) {
        this.setState({ admin: event.target.value === 'admin' });
    },
    renderAdmin: function() {
        var admin = this.state.admin;

        if (!this.state.edit) {
            return admin ? this.context.t('my.network.admin') : this.context.t('my.network.user');
        } else {
            return (
                <div>
                    <select name="role-switch" onChange={this.changeUserRole} value={admin ? 'admin' : 'user'}>
                        <option value="admin">{this.context.t('my.network.admin')}</option>
                        <option value="user">{this.context.t('my.network.user')}</option>
                    </select>
                </div>
            );
        }
    },
    render: function() {
        var member = this.props.member;

        var actions = null;
        if (! member.self) {
            if (! this.state.edit) {
                actions = (
                    <div className="col-sm-6">
                        <span className="pull-right action-icon" onClick={this.props.remove(member)}>
                            <i className="fa fa-trash"></i>
                        </span>
                        <span className="pull-right btn-line action-icon" onClick={this.toggleEdit}>
                            <i className="fa fa-pencil"></i>
                        </span>
                    </div>
                );
            } else {
                actions = (
                    <div className="col-sm-6">
                        <button type="button" className="btn oz-btn-cancel btn-xs pull-right" onClick={this.toggleEdit}>{this.context.t('ui.cancel')}</button>
                        <button type="button" className="btn oz-btn-save btn-xs btn-line pull-right" onClick={this.save}>{this.context.t('ui.save')}</button>
                    </div>
                );
            }
        }

        var adminStatus = this.renderAdmin();

        return (
            <div key={member.id} className="row organization-member-row">
                <div className="col-sm-3">{member.name}</div>
                <div className="col-sm-3">{adminStatus}</div>
                {actions}
            </div>
        );
    }
});
Member.contextTypes = {
    t: PropTypes.func.isRequired
};

const ReadOnlyMember = ({ member }, context) =>
    <div className="row organization-member-row">
        <div className="col-sm-3">{member.name}</div>
        <div className="col-sm-3">{member.self ? context.t('my.network.user') : context.t('my.network.admin')}</div>
    </div>;

ReadOnlyMember.contextTypes = {
    t: PropTypes.func.isRequired
};

var InviteDialog = createClass({
    open: function() {
        this.refs.modal.open();
    },
    close: function() {
        this.refs.modal.close();
    },
    componentDidMount: function() {
        if (this.refs.modal) {
            $(ReactDOM.findDOMNode(this.refs.modal)).on("shown.bs.modal", function () {
                $("input", this).first().focus();
            });
        }
    },
    render: function() {
        if (this.props.admin) {
            var inviteButtonLabels = {"save": this.context.t('my.network.invite'), "cancel": this.context.t('ui.cancel')};
            var formGroupClass = $.inArray("email", this.props.errors) !== -1 ? 'form-group has-error' : 'form-group';
            var generalError = ($.inArray("general", this.props.errors) != -1 ? <div className="alert alert-danger" role="alert">{this.context.t('ui.general-error')}</div> : null);

            return (
                <Modal ref="modal" title={this.context.t('my.network.invite')} successHandler={this.props.onSubmit} buttonLabels={inviteButtonLabels}>
                    <form className="form-horizontal" onSubmit={this.props.onSubmit}>
                        <div className={formGroupClass}>
                            <label htmlFor="email" className="col-sm-4 control-label required">{this.context.t('my.network.email')} * </label>
                            <div className="col-sm-8">
                                <input className="form-control" id="email" type="text" value={this.props.email} onChange={this.props.onChange} placeholder="name@domain.eu"/>
                            </div>
                        </div>
                        {generalError}
                    </form>
                </Modal>
            );
        } else return null;
    }
});
InviteDialog.contextTypes = {
    t: PropTypes.func.isRequired
};

var LeaveDialog = createClass({
    open: function() {
        this.refs.modal.open();
    },
    close: function(){
        this.refs.modal.close();
    },
    render: function() {
        var leaveButtonLabels = {'cancel': this.context.t('ui.cancel'), 'save': this.context.t('my.network.yes-i-want-to-leave')};
        return (
            <Modal ref="modal" title={this.context.t('my.network.leave')} successHandler={this.props.onSubmit} buttonLabels={leaveButtonLabels} saveButtonClass="oz-btn-danger">
                <p>{this.context.t('my.network.confirm-leave')}</p>
            </Modal>
        );
    }
});
LeaveDialog.contextTypes = {
    t: PropTypes.func.isRequired
};

var InformationDialog = createClass({
    getInitialState: function() {
        return {organization: undefined};
    },
    open: function() {
        this.loadDCOrganizations();
    },
    close: function(){
        this.refs.modalModifyKAndDCOrg.close();
    },
    onError: function(srvErrCode, srvErrMessage){
        this.state.srvErrCode = srvErrCode;
        this.state.errMessage = srvErrMessage;

        if(srvErrCode == 403){
            this.state.errMessage = this.context.t('error.datacore.forbidden');
        }
        this.setState(this.state);
        this.refs.modalOrgInfoError.open();
    },
    loadDCOrganizations: function() {
        if (this.props.org.dc_id) {
            $.ajax({
                url: 'my/api/network/organization',
                type: 'get',
                contentType: 'json',
                data: {dc_id: this.props.org.dc_id},
                success: function (data) {
                    if(data){
                        data.inModification = true;
                        this.setState({ organization: data });
                        this.refs.modalModifyKAndDCOrg.open(data);
                    }
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }
            });
        }
    },
    createOrModifOrg: function(event){
        return (this.refs.tabbedFormModify.createOrModifOrg(event) ); // return true if the organization has been created successful
    },

    render: function() {
        var errorModal = (
            <Modal ref="modalOrgInfoError" title={this.context.t('my.network.information')} infobox={true} cancelHandler={null/*this.close()*/} >
                {/*<div><h5>{this.context.t('error.datacore.forbidden')}</h5></div>*/}
                <br/><div><h5>{this.state.errMessage}</h5></div>
            </Modal>
        );
        var modal = undefined;
        if(this.state.organization){
            modal = (
                <CreateOrModifyOrganizationModal ref="modalModifyKAndDCOrg" successHandler={this.props.onUpdate} />
            );
        }else{
            {/* This part is to show a short message to users of very old organizations that doesnt exist in DC */}
            var territoryId = (this.props.org.territory_id) ? (<p>{this.context.t('ui.location')} : {this.props.org.territory_label}</p>) : '';

            modal = (
                <Modal ref="modalModifyKAndDCOrg" infobox={true} buttonLabels={{'ok': this.context.t('ui.close')}} title={this.context.t('my.network.information')}>
                    <h4><span title={this.props.org.id}>{this.props.org.name}</span></h4>
                    <p>{this.context.t('my.network.organization-type.' + this.props.org.type)}</p>
                    {territoryId}
                    <p><i>{this.context.t('my.network.no-information-available')}</i></p>
                </Modal>
            );
        }

        return (<div> {modal} {errorModal} </div>);
    }
});

InformationDialog.contextTypes = {
    t: PropTypes.func.isRequired
};


class NetworkWrapper extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    render() {
        return <div className="oz-body page-row page-row-expanded">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        <h1 className="text-center">
                            <img src="/img/network-lg.png" />
                            <span>{this.context.t('my.network')}</span>
                        </h1>
                    </div>
                </div>
            </div>

            <div className="oz-body-content">
                <MyNetwork/>
            </div>

            <div className="push"></div>
        </div>;
    }
}


export default NetworkWrapper;

