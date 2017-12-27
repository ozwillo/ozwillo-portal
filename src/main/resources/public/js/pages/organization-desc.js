import React from 'react';
import { connect } from 'react-redux';

//actions
import { fetchOrganizationWithId } from "../actions/organization";


class OrganizationDesc extends React.Component {

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.fetchOrganizationWithId();
    }

    render() {
        return <h1>OrganizationDesc</h1>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current
    };
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        fetchOrganizationWithId() {
            return dispatch(fetchOrganizationWithId(ownProps.match.params.id));
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationDesc);