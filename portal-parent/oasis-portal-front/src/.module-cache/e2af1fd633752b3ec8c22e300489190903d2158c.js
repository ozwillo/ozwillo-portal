/** @jsx React.DOM */

var MyNetwork = React.createClass({displayName: "sMyNetwork",
    openCreateOrgDialog: function() {
        this.refs.createOrgDialog.show();
    },
    reload: function() {
        this.refs.orgs.loadOrganizations();
    },
    render: function() {
        return (
                React.createElement("div", null, 
                    React.createElement(CreateOrganization, {ref: "createOrgDialog", successHandler: this.reload}), 
                    React.createElement(SearchOrCreateHeader, {showDialog: this.openCreateOrgDialog}), 
                    React.createElement(OrganizationsList, {ref: "orgs"})
                )
            );
    }
});

var SearchOrCreateHeader = React.createClass({displayName: "SearchOrCreateHeader",
    render: function() {
        return React.createElement("h2", null, t('find-or-create-organization'), " ", React.createElement("a", {className: "btn btn-success", href: "#", onClick: this.props.showDialog}, t('ui.go')))
    }
});

var OrganizationsList = React.createClass({displayName: "OrganizationsList",
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
            return React.createElement("p", {className: "alert alert-danger"}, t('ui.general-error'))
        } else if (this.state.loading) {
            return React.createElement("p", {className: "text-center"}, 
                React.createElement("i", {className: "fa fa-spinner fa-spin"}), " ", t('ui.loading'))
        } else {
            var reload = this.loadOrganizations;
            var updateOrg = this.updateOrganization;
            var orgs = this.state.organizations.map(function(org) {
                return (
                    React.createElement(Organization, {key: org.id, org: org, reload: reload, updateOrganization: updateOrg})
                    );
            });
            return (React.createElement("div", {className: "organizations"}, orgs));
        }
    }
});

var Organization = React.createClass({displayName: "Organization",
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
        if (this.props.org.admin) {
            var remove = this.removeMember;
            var updateMember = this.updateMember;
            var members = this.props.org.members.map(function (member) {
                return React.createElement(Member, {key: member.id, member: member, remove: remove, updateMember: updateMember})
            });

            return members;
        } else {
            return this.props.org.members.map(function (member) {
                return React.createElement(ReadOnlyMember, {key: member.id, member: member})
            });
        }
    },
    render: function() {

        var buttons = [
            React.createElement("a", {key: "info", className: "btn btn-primary-inverse", onClick: this.showInformation}, t('information'))
        ];
        var dialogs = [
            React.createElement(LeaveDialog, {ref: "leaveDialog", onSubmit: this.leave}),
            React.createElement(InformationDialog, {ref: "infoDialog", org: this.props.org})
        ];

        var membersList = this.renderMembers();

        if (this.props.org.admin) {
            if (this.props.org.members.filter(function (m) {
                return m.admin;
            }).length != 1) {
                buttons.push(
                    React.createElement("a", {key: "leave", className: "btn btn-warning-inverse", onClick: this.confirmLeave}, t('leave'))
                );
            }

            buttons.push(
                React.createElement("a", {key: "invite", className: "btn btn-success-inverse", onClick: this.openInvitation}, t('invite'))
            );
            if (devmode) {
                buttons.push(
                    React.createElement("a", {key: "delete", className: "btn btn-danger", onClick: this.deleteOrganization}, t('delete'))
                );
            }

            dialogs.push(
                React.createElement(InviteDialog, {ref: "inviteDialog", 
                admin: this.props.org.admin, 
                onSubmit: this.invite, 
                onChange: this.updateInvitation, 
                email: this.state.invite.email, 
                errors: this.state.invite.errors})
            );


        } else {
            // non-admins can leave at any time
            buttons.push(
                React.createElement("a", {key: "leave", className: "btn btn-warning-inverse", onClick: this.confirmLeave}, t('leave'))
            );
        }

        return (
            React.createElement("div", {className: "standard-form"}, 
                dialogs, 
                React.createElement("div", {className: "row form-table-header"}, 
                    React.createElement("div", {className: "col-sm-6"}, this.props.org.name), 
                    React.createElement("div", {className: "col-sm-6"}, 
                    buttons
                    )
                ), 
                membersList
            )
            );
    }
});

var Member = React.createClass({displayName: "Member",
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
                React.createElement("div", null, 
                    React.createElement("input", {className: "switch", type: "checkbox", checked: admin, onChange: function () {
                        var state = this.state;
                        state.member.admin = !admin;
                        this.setState(state);
                    }.bind(this)}), 
                    React.createElement("label", null, admin ? t('admin') : t('user'))
                )
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
                    React.createElement("div", {className: "col-sm-4 col-sm-offset-2"}, 
                        React.createElement("a", {className: "lnk edit", href: "#", onClick: toggleEdit}, React.createElement("i", {className: "fa fa-pencil"}), t('ui.edit')), 
                        React.createElement("a", {className: "lnk remove", href: "#", onClick: remove(member)}, React.createElement("i", {className: "fa fa-trash"}), t('ui.remove'))
                    )
                    );
            } else {
                actions = (
                    React.createElement("div", {className: "col-sm-4 col-sm-offset-2"}, 
                        React.createElement("a", {className: "lnk accept", href: "#", onClick: save}, 
                            React.createElement("i", {className: "fa fa-check"}), t('ui.save')), 
                        React.createElement("a", {className: "lnk cancel", href: "#", onClick: toggleEdit}, React.createElement("i", {className: "fa fa-times"}), t('ui.cancel'))
                    )
                    );
            }
        }

        var adminStatus = this.renderAdmin();

        return (
            React.createElement("div", {key: member.id, className: "row form-table-row"}, 
                React.createElement("div", {className: "col-sm-3"}, member.name), 
                React.createElement("div", {className: "col-sm-3"}, adminStatus), 
                    actions
            )
            );
    }
});

var ReadOnlyMember = React.createClass({displayName: "ReadOnlyMember",
    render: function () {
        return (
            React.createElement("div", {key: this.props.member.id, className: "row form-table-row"}, 
                React.createElement("div", {className: "col-sm-3"}, this.props.member.name), 
                React.createElement("div", {className: "col-sm-3"}, t('admin'))
            )
        );
    }
});

var InviteDialog = React.createClass({displayName: "InviteDialog",
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
            var labelClassName = ($.inArray("email", this.props.errors) == -1 ? 'col-sm-4' : 'col-sm-4 error');
            var generalError = ($.inArray("general", this.props.errors) != -1 ? React.createElement("p", {className: "alert alert-danger", role: "alert"}, t('ui.general-error')) : null)
            return (
                React.createElement(Modal, {ref: "modal", title: t('invite'), successHandler: this.props.onSubmit, buttonLabels: inviteButtonLabels}, 
                    React.createElement("form", {className: "form-horizontal", onSubmit: this.props.onSubmit}, 
                        React.createElement("div", {className: "form-group"}, 
                            React.createElement("label", {htmlFor: "email", className: labelClassName}, t('email')), 
                            React.createElement("div", {className: "col-sm-8"}, 
                                React.createElement("input", {className: "form-control", id: "email", type: "text", value: this.props.email, onChange: this.props.onChange, placeholder: "name@domain.eu"})
                            )
                        ), 
                        generalError
                    )
                )
                );
        } else return null;
    }
});

var LeaveDialog = React.createClass({displayName: "LeaveDialog",
    open: function() {
        this.refs.modal.open();
    },
    close: function(){
        this.refs.modal.close();
    },
    render: function() {
        var leaveButtonLabels = {'cancel': t('ui.cancel'), 'save': t('yes-i-want-to-leave')};
        return (
            React.createElement(Modal, {ref: "modal", title: t('leave'), successHandler: this.props.onSubmit, buttonLabels: leaveButtonLabels}, 
                React.createElement("p", null, t('confirm-leave'))
            )
            );
    }
});

var InformationDialog = React.createClass({displayName: "InformationDialog",
    open: function() {
        this.refs.modal.open();
    },
    render: function() {
        return (
            React.createElement(Modal, {ref: "modal", infobox: true, buttonLabels: {'ok': t('ui.close')}, title: t('information')}, 
                React.createElement("h4", null, this.props.org.name), 
                React.createElement("p", null, t('organization-type.' + this.props.org.type))
            )
            );
    }
});

React.renderComponent(React.createElement(MyNetwork, null), document.getElementById("mynetwork"));