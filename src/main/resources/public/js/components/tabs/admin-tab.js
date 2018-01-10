import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

//Components
import OrganizationForm from '../forms/organization-form';

//actions
import { fetchCountries } from "../../actions/config";

class AdminTabHeader extends React.Component {

    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/admin`}>
            <header className="tab-header">Admin</header>
        </Link>;
    }

}
const AdminTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(AdminTabHeader);

class AdminTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false
        };

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
    }

    componentDidMount() {
        this.props.fetchCountries();
    }

    onSubmit(organization) {
        console.log('submit ', organization);
    }

    render() {
        return <article className="admin-tab">
            <OrganizationForm onSubmit={this.onSubmit} countries={this.props.countries}
                              isLoading={this.state.isLoading}
                              organization={this.state.organization}/>
        </article>;
    }
}

const mapStateToProps = state => {
    return {

        countries: state.config.countries,
        organization: state.organization.current
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchCountries() {
            return dispatch(fetchCountries());
        }
    };
};

const AdminTabWithRedux = connect(mapStateToProps, mapDispatchToProps)(AdminTab);


export {
    AdminTabHeaderWithRedux as AdminTabHeader,
    AdminTabWithRedux as AdminTab
};