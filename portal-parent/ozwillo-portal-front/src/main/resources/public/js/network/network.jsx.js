/** @jsx React.DOM */

var MyNetwork = React.createClass({
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
                <div>
                    <SearchOrganizationModal ref="searchOrgDialog" successHandler={this.openCreateOrgDialog} />
                    <CreateOrModifyOrganizationModal ref="createOrgDialog" successHandler={this.reload} />
                    <SearchOrCreateHeader showDialog={this.openSearchOrgDialog}/>
                    <OrganizationsList ref="orgs"/>
                </div>
            );
    }
});

var SearchOrCreateHeader = React.createClass({
    render: function() {
        return (
            <div className="row add-organization-action">
                <div className="col-md-12">
                    <button type="button" className="btn oz-btn-save pull-right" onClick={this.props.showDialog}>
                        {t('my.network.add-organization')}
                    </button>
                </div>
            </div>
        );
    }
});

var OrganizationsList = React.createClass({
    getInitialState: function() {
        return {loading: true, organizations: []};
    },
    loadOrganizations: function() {
        $.ajax({
            url: network_service + "/organizations",
            type: 'get',
            dataType: 'json',
            success: function(data) {
                this.setState({organizations: data, loading: false});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
                this.setState({generalError: true});
            }.bind(this)
        });
    },
    componentDidMount: function() {
        this.loadOrganizations();
    },
    render: function() {
        if (this.state.generalError) {
            return <p className="alert alert-danger">{t('ui.general-error')}</p>
        } else if (this.state.loading) {
            return <p className="text-center">
                <i className="fa fa-spinner fa-spin"></i> {t('ui.loading')}</p>
        } else {
            var reload = this.loadOrganizations;
            var orgs = this.state.organizations.map(function(org) {
                return (
                    <Organization key={org.id} org={org} reload={reload}/>
                );
            });
            return (
                <div className="organizations">{orgs}</div>
            );
        }
    }
});

var Organization = React.createClass({
    getInitialState: function() {
        //return {invite:{email: "", errors:[]}};
        var state = this.props.org;
        state.invite = {email: "", errors:[]};
        return state;
    },
    componentDidUpdate: function () {
        if (typeof this.state.errorMessage === 'string') {
            this.refs.errorDialog.open();
        }
    },
    updateInvitation: function(event) {
        var state = this.state;
        state.invite.email = event.target.value;
        state.invite.errors = [];
        this.setState(state);
    },
    updateOrganization: function(organization, optionalOperation) {
        // NB. not updating locally (setState()) first because it would require rendering
        // BEFORE doing remote ajax changes which would AGAIN render
        // which would then block scrolling, see #221

        // then remotely
        var url = network_service + "/organization/" + organization.id;
        if (optionalOperation) {
            url += '/' + optionalOperation;
        }
        $.ajax({
            url: url,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(organization),
            success: function (data) {
                var state = organization;
                //data = "test"; // to easily test error return
                if (typeof data === 'string' && data.trim().length !== 0) {
                    // assuming it's a String message returned by the Kernel
                    state.errorMessage = data;
                }
                this.props.org = state;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                // if there's an error, reload everything
                this.props.reload();
            }.bind(this)
        });

    },
    invite: function() {
        var state = this.state;
        if (this.state.invite.email.trim() == '' || this.state.invite.email.indexOf('@') == -1) {
            state.invite.errors = ["email"];
            this.setState(state);
        }
        else {
            $.ajax({
                url: network_service + "/invite/" + this.props.org.id,
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify({email: state.invite.email}),
                success: function(data) {
                    var state = this.state;
                    state.invite = {email: "", errors: []};
                    this.refs.inviteDialog.close();
                    this.props.reload();
                    this.setState(state);

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
        var state = this.state;
        state.invite = {email:"", errors:[]};
        this.setState(state);
        this.refs.inviteDialog.open();
    },
    confirmLeave: function() {
        this.refs.leaveDialog.open();
    },
    removeInvitation: function(pMember) {
        $.ajax({
            url: network_service + "/remove-invitation/"+ this.props.org.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({id: pMember.id, email: pMember.email,
                        etag: pMember.pending_membership_etag}),
            success: function() {
                this.props.reload();
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    leave: function() {
        $.ajax({
            url: network_service + "/leave",
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({organization: this.props.org.id}),
            success: function() {
                this.props.reload();
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
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
        var org = this.state;
        org.status = 'DELETED';
        this.updateOrganization(org, 'set-status');
        this.refs.confirmTrashDialog.close();
    },
    confirmUntrash: function() {
        this.refs.confirmUntrashDialog.open();
    },
    untrash: function () {
        var org = this.state;
        org.status = 'AVAILABLE';
        this.updateOrganization(org, 'set-status');
        this.refs.confirmUntrashDialog.close();
    },
    closeErrorDialog: function () { // required else if onClose=this.props.reload() directly loops on displaying errorDialog
        var state = this.state;
        state.errorMessage = null;
        this.setState(state);
        // if there's an error, reload everything
        this.props.reload();
    },
    removeMember: function(member) {
        return function (event) {
            if (event) {
                event.preventDefault();
            }
            var org = this.props.org;
            org.members = org.members.filter(function(m) {return m.id != member.id;});
            this.updateOrganization(org);

        }.bind(this);
    },
    updateMember: function (member) {
        var org = this.props.org;

        for (var i in org.members) {
            if (org.members[i].id == member.id) {
                org.members[i].admin = member.admin;
                break;
            }
        }
        this.updateOrganization(org);
    },
    renderMembers: function() {
        if (this.props.org.admin) {
            var remove = this.removeMember;
            var updateMember = this.updateMember;
            return this.props.org.members.map(function (member) {
                return <Member key={member.id} member={member}
			             remove={remove} updateMember={updateMember}/>
            });
        } else {
            return this.props.org.members.map(function (member) {
                return <ReadOnlyMember key={member.id} member={member} />
            });
        }
    },
    renderPendingMemberships: function() {  //Pending Membership
        if (this.props.org.admin) {
	        var removeInvitation = this.removeInvitation;
            return this.props.org.pendingMemberships.map(function (pMember) {
	            return <PendingMembership key={pMember.id} pMember={pMember}
                        removeInvitation={removeInvitation}  />
            });
        }
    },
    render: function() {

        var buttons = [];
        var dialogs = [];

        dialogs.push(
            <Modal ref="errorDialog" infobox={true} onClose={this.closeErrorDialog} buttonLabels={{'ok': t('ui.close')}} title={t('my.network.information')}>
                {this.state.errorMessage}
            </Modal>
        );

        var membersList = this.renderMembers();
        var pendingMembershipList = this.renderPendingMemberships();

        if (this.props.org.status === 'DELETED') {
            // trashed
            var byDeleteRequesterOnDate = t('my.network.by') + " " + this.props.org.status_change_requester_label
                  + " (" + moment(this.props.org.status_changed) + ")";
            buttons.push(
                <span key="untrashTtl" style={{'color':'red', 'fontStyle':'Italic', 'marginLeft':'5px', 'marginRight':'5px'}} title={byDeleteRequesterOnDate}>
                    {t('my.network.will-be-deleted')} {moment(this.props.org.deletion_planned).fromNow()}
                </span>
            ); // (not a button per se)
            
            if (this.props.org.admin) {
                buttons.push(
                    <button type="button" key="confirmUntrash" className="btn oz-btn-danger btn-sm pull-right" onClick={this.confirmUntrash}>{t('ui.cancel')}</button>
                );
                
                var confirmUntrashTitle = t('my.network.confirm-untrash.title') + ' ' + this.props.org.name;
                dialogs.push(
                    <Modal ref="confirmUntrashDialog" title={confirmUntrashTitle} successHandler={this.untrash}
                                      buttonLabels={{ 'cancel': t('ui.cancel'), 'save': t('ui.confirm') }} >
                        {t('my.network.confirm-untrash.body')}
                    </Modal>
                );
            }
            
        } else {

            buttons.push(
                <button type="button" key="info" className="btn btn-default btn-sm btn-line" onClick={this.showInformation}>{t('my.network.information')}</button>
            );

            dialogs.push(
                <LeaveDialog ref="leaveDialog" onSubmit={this.leave}/>,
                <InformationDialog ref="infoDialog" org={this.props.org} onUpdate={this.props.reload} />
            );
            
	        if (this.props.org.admin) {
                buttons.push(
                    <button type="button" key="invite" className="btn btn-default btn-sm btn-line" onClick={this.openInvitation}>{t('my.network.invite')}</button>
                );

                if (this.props.org.members.filter(function (m) {
                    return m.admin;
                }).length != 1) {
                    // admins can leave only if there will still be another admin
                    buttons.push(
                        <button type="button" key="leave" className="btn btn-warning btn-sm btn-line" onClick={this.confirmLeave}>{t('my.network.leave')}</button>
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
                    <button type="button" key="confirmTrash" className="btn oz-btn-danger btn-sm" onClick={this.confirmTrash}>{t('ui.delete')}</button>
                );

                var confirmTrashTitle = t('my.network.confirm-trash.title') + ' ' + this.props.org.name;
                dialogs.push(
                    <Modal ref="confirmTrashDialog" title={confirmTrashTitle} successHandler={this.trash}
                           buttonLabels={{ 'cancel': t('ui.cancel'), 'save': t('ui.confirm') }} saveButtonClass="oz-btn-danger" >
                        {t('my.network.confirm-trash.body')}
                    </Modal>
                );


	        } else {
	            // non-admins can leave at any time
	            buttons.push(
	                <button type="button" key="leave" className="btn btn-warning btn-sm" onClick={this.confirmLeave}>{t('my.network.leave')}</button>
	            );
	        }
        }

        return (
            <div className="organization">
                {dialogs}
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
                {membersList}
                {pendingMembershipList}
            </div>
        );
    }
});

var PendingMembership = React.createClass({
    getInitialState: function() {
        return {pMember: this.props.pMember};
    },
    removeMembershipInvitation: function (event) {
        event.preventDefault();
        this.props.removeInvitation(this.state.pMember);
    },
    render: function() {
        var pMember = this.state.pMember;

        return (
            <div key={pMember.id} className="row organization-pending-member-row">
                <div className="col-sm-3">{pMember.email}</div>
                <div className="col-sm-3">{t('my.network.organization.pending-invitation') }</div>
                <div className="col-sm-6">
                    <span className="pull-right action-icon" onClick={this.removeMembershipInvitation}>
                        <i className="fa fa-trash"></i>
                    </span>
                </div>
            </div>
        );
    }
});

var Member = React.createClass({
    getInitialState: function() {
        return {edit:false, member: this.props.member};
    },
    toggleEdit: function (event) {
        if (event != null) {
            event.preventDefault();
        }
        if (this.state.edit) {
            this.setState({edit:false, member: JSON.parse(JSON.stringify(this.props.member))});
        } else {
            this.setState({edit:true, member: JSON.parse(JSON.stringify(this.props.member))});
        }
    },
    save: function (event) {
        event.preventDefault();
        this.props.updateMember(this.state.member);
        this.toggleEdit(null);
    },
    changeUserRole: function(event) {
        var member = this.state.member;
        member.admin = event.target.value === 'admin';
        this.setState({ member: member});
    },
    renderAdmin: function() {
        var admin = this.state.member.admin;
        var edit = this.state.edit;

        if (!edit) {
            return admin ? t('my.network.admin') : t('my.network.user');
        } else {
            return (
                <div>
                    <select name="role-switch" onChange={this.changeUserRole} value={admin ? 'admin' : 'user'}>
                        <option value="admin">{t('my.network.admin')}</option>
                        <option value="user">{t('my.network.user')}</option>
                    </select>
                </div>
            );
        }
    },
    render: function() {
        var member = this.state.member;

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
                        <button type="button" className="btn oz-btn-cancel btn-xs pull-right" onClick={this.toggleEdit}>{t('ui.cancel')}</button>
                        <button type="button" className="btn oz-btn-save btn-xs btn-line pull-right" onClick={this.save}>{t('ui.save')}</button>
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

var ReadOnlyMember = React.createClass({
    render: function () {
        return (
            <div key={this.props.member.id} className="row organization-member-row">
                <div className="col-sm-3">{this.props.member.name}</div>
                <div className="col-sm-3">{this.props.member.self ? t('my.network.user') : t('my.network.admin')}</div>
            </div>
        );
    }
});

var InviteDialog = React.createClass({
    open: function() {
        this.refs.modal.open();
    },
    close: function() {
        this.refs.modal.close();
    },
    componentDidMount: function() {
        if (this.refs.modal) {
            $(this.refs.modal.getDOMNode()).on("shown.bs.modal", function () {
                $("input", this).first().focus();
            });
        }
    },
    render: function() {
        if (this.props.admin) {
            var inviteButtonLabels = {"save": t('my.network.invite'), "cancel": t('ui.cancel')};
            var formGroupClass = $.inArray("email", this.props.errors) !== -1 ? 'form-group has-error' : 'form-group';
            var generalError = ($.inArray("general", this.props.errors) != -1 ? <div className="alert alert-danger" role="alert">{t('ui.general-error')}</div> : null);

            return (
                <Modal ref="modal" title={t('my.network.invite')} successHandler={this.props.onSubmit} buttonLabels={inviteButtonLabels}>
                    <form className="form-horizontal" onSubmit={this.props.onSubmit}>
                        <div className={formGroupClass}>
                            <label htmlFor="email" className="col-sm-4 control-label required">{t('my.network.email')} * </label>
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

var LeaveDialog = React.createClass({
    open: function() {
        this.refs.modal.open();
    },
    close: function(){
        this.refs.modal.close();
    },
    render: function() {
        var leaveButtonLabels = {'cancel': t('ui.cancel'), 'save': t('my.network.yes-i-want-to-leave')};
        return (
            <Modal ref="modal" title={t('my.network.leave')} successHandler={this.props.onSubmit} buttonLabels={leaveButtonLabels} saveButtonClass="oz-btn-danger">
                <p>{t('my.network.confirm-leave')}</p>
            </Modal>
            );
    }
});

var InformationDialog = React.createClass({
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
            this.state.errMessage = t('error.datacore.forbidden');
        }
        this.setState(this.state);
        this.refs.modalOrgInfoError.open();
    },
    loadDCOrganizations: function() {
       if (this.props.org.dc_id) {
           $.ajax({
               url: network_service + "/search-organization-by-id",
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
               }.bind(this)
           });
       }
    },
    createOrModifOrg: function(event){
        return (this.refs.tabbedFormModify.createOrModifOrg(event) ); // return true if the organization has been created successful
    },

    render: function() {
        var errorModal = (
            <Modal ref="modalOrgInfoError" title={t('my.network.information')} infobox={true} cancelHandler={null/*this.close()*/} >
                {/*<div><h5>{t('error.datacore.forbidden')}</h5></div>*/}
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
           var territoryId = (this.props.org.territory_id) ? (<p>{t('ui.location')} : {this.props.org.territory_label}</p>) : '';

            modal = (
                <Modal ref="modalModifyKAndDCOrg" infobox={true} buttonLabels={{'ok': t('ui.close')}} title={t('my.network.information')}>
                   <h4><span title={this.props.org.id}>{this.props.org.name}</span></h4>
                   <p>{t('my.network.organization-type.' + this.props.org.type)}</p>
                   {territoryId}
                   <p><i>{t('my.network.no-information-available')}</i></p>
                </Modal>
                );
        }

        return (<div> {modal} {errorModal} </div>);
    }
});

React.render(<MyNetwork />, document.getElementById("mynetwork"));
