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
        const isPending = !member.name;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">{member.name || member.email}</span>
                <span className="error-message">{this.state.error}</span>
                <div className="options flex-row end">

                    {
                        member.admin && !isPending &&
                        <button type="button" className="btn icon" onClick={this.memberRoleToggle}>
                            <i className="fa fa-chess-king option-icon"/>
                        </button>
                    }

                    {
                        !member.admin && !isPending &&
                        <button type="button" className="btn icon" onClick={this.memberRoleToggle}>
                            <i className="fa fa-chess-pawn option-icon"/>
                        </button>
                    }

                    {
                        isPending &&
                        <button type="button" className="btn icon">
                            <i className="fa fa-stopwatch option-icon loading"/>
                        </button>
                    }

                    <button type="button" className={`btn icon ${isPending && 'invisible' || ''}`}
                            onClick={!isPending && this.memberRoleToggle || null}>
                        <i className="fa fa-trash option-icon"/>
                    </button>
                </div>
            </form>
        </header>;
    }
}

export default MemberDropdownHeader;