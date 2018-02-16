import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom'

class OrganizationDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };


    render() {
        const organization = this.props.organization;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <span className="dropdown-name">
                    <Link className="link" to={`/my/organization/${organization.id}/`}>
                        {organization.name}
                    </Link>
                </span>

                <div className="options flex-row end">
                    <Link className="btn icon" to={`/my/organization/${organization.id}/instances`}>
                        <i className="fa fa-list-alt option-icon"/>
                    </Link>

                    <Link className="btn icon" to={`/my/organization/${organization.id}/members`}>
                        <i className="fa fa-users option-icon"/>
                    </Link>

                    <Link className="btn icon" to={`/my/organization/${organization.id}/admin`}>
                        <i className="fa fa-info-circle option-icon"/>
                    </Link>
                </div>
            </form>
        </header>;
    }
}

export default OrganizationDropdownHeader;