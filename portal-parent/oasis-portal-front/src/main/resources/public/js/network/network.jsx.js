/** @jsx React.DOM */

var MyNetwork = React.createClass({
    openCreateOrgDialog: function() {
        this.refs.createOrgDialog.show();
    },
    reload: function() {
        this.refs.orgs.loadOrganizations();
    },
    render: function() {
        return (
                <div>
                    <CreateOrganization ref="createOrgDialog"  successHandler={this.reload} />
                    <SearchOrCreateHeader showDialog={this.openCreateOrgDialog}/>
                    <OrganizationsList ref='orgs'/>
                </div>
            );
    }
});

var SearchOrCreateHeader = React.createClass({
    render: function() {
        return <h2>{t('find-or-create-organization')} <a className="btn btn-success" href="#" onClick={this.props.showDialog}>{t('ui.go')}</a></h2>
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
    updateOrganization: function(organization) {
        // first update locally
        var orgs = this.state.organizations;
        var idx;
        for (var i in orgs) {
            if (orgs[i].id == organization.id) {
                idx = i;
                break;
            }
        }

        orgs[idx] = organization;
        this.setState({loading: false, organizations: orgs});

        // then remotely
        $.ajax({
            url: network_service + "/organization/" + organization.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(organization),
            success: function () {
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                // if there's an error, reload everything
                this.loadOrganizations();
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
            var updateOrg = this.updateOrganization;
            var orgs = this.state.organizations.map(function(org) {
                return (
                    <Organization key={org.id} org={org} reload={reload} updateOrganization={updateOrg}/>
                    );
            });
            return (<div className="organizations">{orgs}</div>);
        }
    }
});

var Organization = React.createClass({
    getInitialState: function() {
        return {invite:{email: "", errors:[]}};
    },
    updateInvitation: function(event) {
        var state = this.state;
        state.invite.email = event.target.value;
        state.invite.errors = [];
        this.setState(state);
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
                    this.state.invite.errors = ["general"];
                    this.setState(this.state);
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
    deleteOrganization: function () {
        $.ajax({
            url: network_service + "/organization/" + this.props.org.id,
            type: 'delete',
            success: function () {
                this.props.reload();
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    removeMember: function(member) {
        return function (event) {
            if (event) {
                event.preventDefault();
            }
            var org = this.props.org;
            org.members = org.members.filter(function(m) {return m.id != member.id;});
            this.props.updateOrganization(org);


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
        this.props.updateOrganization(org);
    },
    renderMembers: function() {
        var remove = this.removeMember;
        var updateMember = this.updateMember;
        var members = this.props.org.members.map(function(member) {
            return <Member key={member.id} member={member} remove={remove} updateMember={updateMember}/>
        });

        return members;
    },
    render: function() {

        var buttons = [
            <a key="info" className="btn btn-primary-inverse" onClick={this.showInformation}>{t('information')}</a>
        ];
        var dialogs = [
            <LeaveDialog ref="leaveDialog" onSubmit={this.leave}/>,
            <InformationDialog ref="infoDialog" org={this.props.org} />
        ];

        var membersList = null;

        if (this.props.org.admin) {
            if (this.props.org.members.filter(function (m) {
                return m.admin;
            }).length != 1) {
                buttons.push(
                    <a key="leave" className="btn btn-warning-inverse" onClick={this.confirmLeave}>{t('leave')}</a>
                );
            }

            buttons.push(
                <a key="invite" className="btn btn-success-inverse" onClick={this.openInvitation}>{t('invite')}</a>
            );
            if (devmode) {
                buttons.push(
                    <a key="delete" className="btn btn-danger" onClick={this.deleteOrganization}>{t('delete')}</a>
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

            membersList = this.renderMembers();
        } else {
            // non-admins can leave at any time
            buttons.push(
                <a key="leave" className="btn btn-warning-inverse" onClick={this.confirmLeave}>{t('leave')}</a>
            );
        }

        return (
            <div className="standard-form">
                {dialogs}
                <div className="row form-table-header">
                    <div className="col-sm-6">{this.props.org.name}</div>
                    <div className="col-sm-6">
                    {buttons}
                    </div>
                </div>
                {membersList}
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
    renderAdmin: function() {
        var admin = this.state.member.admin;
        var edit = this.state.edit;

        if (!edit) {
            return admin ? t('admin') : t('user');
        } else {
            return (
                <div>
                    <input className="switch" type="checkbox" checked={admin} onChange={function () {
                        var state = this.state;
                        state.member.admin = !admin;
                        this.setState(state);
                    }.bind(this)}></input>
                    <label>{admin ? t('admin') : t('user')}</label>
                </div>
                );
        }
    },
    render: function() {
        var member = this.state.member;
        var remove = this.props.remove;
        var toggleEdit = this.toggleEdit;
        var save = this.save;

        var actions = null;
        if (! member.self) {
            if (! this.state.edit) {
                actions = (
                    <div className="col-sm-4 col-sm-offset-2">
                        <a className="lnk edit" href="#" onClick={toggleEdit}><i className="fa fa-pencil"></i>{t('ui.edit')}</a>
                        <a className="lnk remove" href="#"  onClick={remove(member)}><i className="fa fa-trash"></i>{t('ui.remove')}</a>
                    </div>
                    );
            } else {
                actions = (
                    <div className="col-sm-4 col-sm-offset-2">
                        <a className="lnk accept" href="#" onClick={save}>
                            <i className="fa fa-check"></i>{t('ui.save')}</a>
                        <a className="lnk cancel" href="#" onClick={toggleEdit}><i className="fa fa-times"></i>{t('ui.cancel')}</a>
                    </div>
                    );
            }
        }

        var adminStatus = this.renderAdmin();

        return (
            <div key={member.id} className="row form-table-row">
                <div className="col-sm-3">{member.name}</div>
                <div className="col-sm-3">{adminStatus}</div>
                    {actions}
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
            var inviteButtonLabels = {"save": t('invite'), "cancel": t('ui.cancel')};
            var labelClassName = ($.inArray("email", this.props.errors) == -1 ? '' : 'error');
            var generalError = ($.inArray("general", this.props.errors) != -1 ? <p className="alert alert-danger" role="alert">{t('ui.general-error')}</p> : null)
            return (
                <Modal ref="modal" title={t('invite')} successHandler={this.props.onSubmit} buttonLabels={inviteButtonLabels}>
                    <form className="form-inline" onSubmit={this.props.onSubmit}>
                        <div className="form-group">
                            <label htmlFor="email" className={labelClassName}>{t('email')}</label>
                            <input className="form-control" id="email" type="text" value={this.props.email} onChange={this.props.onChange} placeholder="name@domain.eu"/>
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
        var leaveButtonLabels = {'cancel': t('ui.cancel'), 'save': t('yes-i-want-to-leave')};
        return (
            <Modal ref="modal" title={t('leave')} successHandler={this.props.onSubmit} buttonLabels={leaveButtonLabels}>
                <p>{t('confirm-leave')}</p>
            </Modal>
            );
    }
});

var InformationDialog = React.createClass({
    open: function() {
        this.refs.modal.open();
    },
    render: function() {
        return (
            <Modal ref="modal" infobox={true} buttonLabels={{'ok': t('ui.close')}} title={t('information')}>
                <h4>{this.props.org.name}</h4>
                <p>{t('organization-type.' + this.props.org.type)}</p>
            </Modal>
            );
    }
});

React.renderComponent(<MyNetwork />, document.getElementById("mynetwork"));