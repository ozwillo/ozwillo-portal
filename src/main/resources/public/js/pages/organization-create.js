import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

//Component
import OrganizationForm from '../components/forms/organization-form';

//Action
import { fetchCreateOrganization } from '../actions/organization';
import { fetchCountries } from '../actions/config';

class OrganizationCreate extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
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
        this.setState({ isLoading: true });

        this.props.fetchCreateOrganization(organization)
            .then(() => {
                this.props.history.push('/my/organization');
            })
            .catch(() => {
                this.setState({ isLoading: false });
            });
    }

    render() {
        return <section className="organization-create oz-body wrapper flex-col">
            <header className="header flex-row">
                <h1 className="title">Create an organization</h1>
            </header>
            <section>
                <OrganizationForm onSubmit={this.onSubmit} isLoading={this.state.isLoading}
                    countries={this.props.countries}/>
            </section>
        </section>;
    }

}

const mapStateToProps = state => {
    return {
        countries: state.config.countries,
        userInfo: state.userInfo
    }
};

const mapDispatchToProps = dispatch => {
    return {
        fetchCountries() {
            return dispatch(fetchCountries());
        },
        fetchCreateOrganization(organization) {
            return dispatch(fetchCreateOrganization(organization))
        }
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(OrganizationCreate);