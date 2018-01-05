import React from 'react';
import PropTypes from 'prop-types';

class MemberDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        member: PropTypes.object.isRequired,
        onRemoveMemberInOrganization: PropTypes.func.isRequired,
        onSendInvitation: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.onRemoveMemberInOrganization = this.onRemoveMemberInOrganization.bind(this);
        this.onSendInvitation = this.onSendInvitation.bind(this);
    }

    onRemoveMemberInOrganization() {
        this.props.onRemoveMemberInOrganization();
    }

    onSendInvitation() {
        this.props.onSendInvitation();
    }

    render() {
        const member = this.props.member;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">{`${member.firstname} ${member.lastname}`}</span>

                <div className="options flex-row end">
                    {
                        member.isPending &&
                        <button type="button" className="btn icon">
                            <i className="fa fa-spinner fa-spin option-icon"/>
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
                            <i className="fa fa-trash option-icon delete"/>
                        </button>
                    }
                </div>
            </form>
        </header>;
    }
}

export default MemberDropdownHeader;