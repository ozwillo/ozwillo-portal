import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

// Components
import OrganizationDropdown from '../components/dropdown_menus/organization/organization-dropdown';
import UpdateTitle from '../components/update-title';

//action
import {fetchUserOrganizations} from '../actions/organization';

class OrganizationSearch extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            userOrganizationsFilter: '',
            isLoading: true
        };

        this.handleChange = this.handleChange.bind(this);
        this.filterOrganizations = this.filterOrganizations.bind(this);
    }

    componentDidMount() {
        this.props.fetchUserOrganizations()
            .then(() => {
                this.setState({isLoading: false});
            });
    }

    handleChange(e) {
        this.setState({
            [e.currentTarget.name]: e.currentTarget.value
        });
    }

    filterOrganizations(organizations, filter) {
        if (!filter) {
            return organizations;
        }
        const regex = new RegExp(`(\\w|\\S)*${filter.toUpperCase()}(\\w|\\S)*`);

        return organizations.filter((org) => {
            return regex.test(org.name.toUpperCase());
        });
    }

    render() {
        const userOrganizations = this.props.userOrganizations;
        const userOrganizationsFilter = this.state.userOrganizationsFilter;

        return <section className="organization-search oz-body wrapper flex-col">

            <UpdateTitle title={this.context.t('organization.search.title')}/>

            <div className="flex-row end options">
                <Link to="/my/organization/create" className="btn btn-default undecorated-link">
                    {this.context.t('organization.search.new')}
                </Link>
            </div>

            <section>
                <header className="title">
                    <span>{this.context.t('organization.search.title')}</span>
                </header>

                <form className="search oz-form">
                    <input name="userOrganizationsFilter" className="field form-control" type="text"
                           placeholder={this.context.t('ui.search')} value={userOrganizationsFilter}
                           onChange={this.handleChange}/>
                </form>

                <ul className="organisations-list undecorated-list">
                    {
                        !this.state.isLoading &&
                        this.filterOrganizations(userOrganizations, userOrganizationsFilter).map((org) => {
                            return <li key={org.id} className="organization">
                                <OrganizationDropdown organization={org}/>
                            </li>;
                        })
                    }
                </ul>


                {
                    this.state.isLoading &&
                    <div className="loading-container">
                        <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                }
            </section>

        </section>;
    }

}

const mapStateToProps = state => {
    return {
        userOrganizations: state.organization.organizations
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchUserOrganizations() {
            return dispatch(fetchUserOrganizations());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationSearch);