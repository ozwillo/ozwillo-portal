import React from 'react';
import PropTypes from 'prop-types';

class MemberDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        member: PropTypes.object.isRequired,
        onRemoveMemberInOrganization: PropTypes.func.isRequired,
        onUpdateRoleMember: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            error: ''
        };

        this.onRemoveMemberInOrganization = this.onRemoveMemberInOrganization.bind(this);
        this.memberRoleToggle = this.memberRoleToggle.bind(this);
    }

    onRemoveMemberInOrganization() {
        this.props.onRemoveMemberInOrganization(this.props.member.id)
            .then(() => {
                this.setState({ error: '' })
            })
            .catch((err) => {
                this.setState({ error: err.error });
            });
    }

    memberRoleToggle() {
        this.props.onUpdateRoleMember(!this.props.member.admin);
    }

    render() {
        const member = this.props.member;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">{member.name}</span>
                <span className="error-message">{this.state.error}</span>
                <div className="options flex-row end">

                    {
                        member.admin &&
                        <button type="button" className="btn icon" onClick={this.memberRoleToggle}>
                            <i className="fa fa-superpowers option-icon"/>
                        </button>
                    }

                    {
                        !member.admin &&
                        <button type="button" className="btn icon" onClick={this.memberRoleToggle}>
                            <i className="fa fa-user option-icon"/>
                        </button>
                    }


                    {
                        member.isPending &&
                        <button type="button" className="btn icon">
                            <i className="fa fa-spinner fa-spin option-icon loading"/>
                        </button>
                    }
                    {
                        member.isPending &&
                        <button type="button" className="btn icon" onClick={this.onSendInvitation}>
                            <i className="fa fa-paper-plane option-icon"/>
                        </button>
                    }
                    {
                        !member.isPending &&
                        <button type="button" className="btn icon" onClick={this.onRemoveMemberInOrganization}>
                            <i className="fa fa-trash option-icon"/>
                        </button>
                    }
                </div>
            </form>
        </header>;
    }
}

export default MemberDropdownHeader;