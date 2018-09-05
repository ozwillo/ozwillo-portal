import React from "react";
import PropTypes from 'prop-types';
import {Link} from "react-router-dom";


export default class OrganizationCard extends React.Component {

    constructor() {
        super()
    }

    state = {
        organization: null
    };

    componentDidMount() {}

    render() {
        let {organizationId, name} = this.props.organization;
        if (organizationId) {
            return (
                <div>
                    <Link className="btn btn-default-inverse btn-pill" to={`/my/organization/${organizationId}/`}>
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
    organization: PropTypes.string
};