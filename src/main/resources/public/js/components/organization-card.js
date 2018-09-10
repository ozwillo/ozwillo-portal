import React from "react";
import PropTypes from 'prop-types';
import {Link} from "react-router-dom";


export default class OrganizationCard extends React.PureComponent {
    render() {
        let {dcOrganizationId, name} = this.props.organization;
        if (dcOrganizationId) {
            return (
                <div>
                    <Link className="btn btn-default-inverse btn-pill" to={`/my/organization/${dcOrganizationId}/`}>
                        {name}
                    </Link>
                </div>
            )
        } else {
            return null;
        }
    }
}

OrganizationCard.propTypes = {
    organization: PropTypes.object
};