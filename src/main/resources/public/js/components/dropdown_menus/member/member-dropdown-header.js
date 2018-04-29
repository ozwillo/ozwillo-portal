import React from 'react';
import PropTypes from 'prop-types';
import Popup from "react-popup";
import CustomTooltip from '../../custom-tooltip';

class MemberDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        member: PropTypes.object.isRequired,
        onRemoveMemberInOrganization: PropTypes.func.isRequired,
        onUpdateRoleMember: PropTypes.func.isRequired,
        onRemoveInvitationToJoinAnOrg: PropTypes.func.isRequired
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            error: ''
        };

        this.onRemoveMemberInOrganization = this.onRemoveMemberInOrganization.bind(this);
        this.onRemoveInvitationToJoinAnOrg = this.onRemoveInvitationToJoinAnOrg.bind(this);
        this.memberRoleToggle = this.memberRoleToggle.bind(this);
    }

    onRemoveMemberInOrganization() {
        this.props.onRemoveMemberInOrganization(this.props.member.id)
            .catch((err) => {
                this.setState({error: err.error});
            });
    }

    onRemoveInvitationToJoinAnOrg() {
        this.props.onRemoveInvitationToJoinAnOrg(this.props.member)
            .catch((err) => {
                this.setState({error: err.error});
            });
    }

    memberRoleToggle() {
        this.props.onUpdateRoleMember(!this.props.member.admin)
            .catch((err) => {
                if (err.status === 403) {

                    const error = err.error.format(this.props.member.name);

                    Popup.create({
                        title: this.props.organization.name,
                        content: <p className="alert-message">
                            {
                                error.split('\n').map(line => {
                                    return <span className="line">{line}</span>
                                })
                            }
                        </p>,
                        buttons: {
                            right: [{
                                text: this.context.t('ui.ok'),
                                action: () => {
                                    Popup.close();
                                }
                            }]
                        }

                    });
                }
            });
    }

    render() {
        const member = this.props.member;
        const isPending = !member.name;
        const isOrgAdmin = this.props.organization.admin;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <p className="dropdown-name">
                    <span>{member.name}</span>
                    {isOrgAdmin &&
                        <span className={`email ${(member.name && 'separator') || ''}`}>{member.email}</span>
                    }
                </p>

                <span className="error-message">{this.state.error}</span>
                <div className="options flex-row end">

                    {
                        isPending &&
                        <CustomTooltip title={this.context.t('tooltip.pending')}>
                            <button type="button" className="btn icon">
                                <i className="fa fa-stopwatch option-icon loading"/>
                            </button>
                        </CustomTooltip>
                    }

                    {
                        member.admin &&
                        <CustomTooltip title={this.context.t('tooltip.remove.right.admin')}>
                            <button type="button" className="btn icon"
                                    onClick={!isPending && isOrgAdmin && this.memberRoleToggle || null}>
                                <i className="fa fa-chess-king option-icon"/>
                            </button>
                        </CustomTooltip>
                    }

                    {
                        !member.admin && !isPending &&
                        <CustomTooltip title={this.context.t('tooltip.add.right.admin')}>
                            <button type="button" className="btn icon"
                                    onClick={!isPending && isOrgAdmin && this.memberRoleToggle || null}>
                                <i className="fa fa-chess-pawn option-icon"/>
                            </button>
                        </CustomTooltip>
                    }

                    {
                        isOrgAdmin &&
                        <CustomTooltip title={this.context.t('tooltip.delete.member')}>
                            <button type="button" className="btn icon"
                                    onClick={!isPending && this.onRemoveMemberInOrganization || this.onRemoveInvitationToJoinAnOrg}>
                                <i className="fa fa-trash option-icon"/>
                            </button>
                        </CustomTooltip>
                    }
                </div>
            </form>
        </header>;
    }
}

export default MemberDropdownHeader;