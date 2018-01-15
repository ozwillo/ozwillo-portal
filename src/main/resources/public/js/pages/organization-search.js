import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Components
import SideMenu from '../components/side-menu';
import OrganizationDropdown from '../components/dropdown_menus/organization/organization-dropdown';

//action
import { fetchUserOrganizations } from '../actions/organization';

class OrganizationSearch extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            sideMenuIsOpen: false,
            organizationsFilter: '',
            userOrganizationsFilter: '',
            isLoading: true
        };

        //bind methods
        this.sideMenuToogle = this.sideMenuToogle.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.filterOrganizations = this.filterOrganizations.bind(this);
    }

    componentDidMount() {
        this.props.fetchUserOrganizations()
            .then(() => {
                this.setState({ isLoading: false });
            });
    }

    sideMenuToogle() {
        this.setState({
            sideMenuIsOpen: !this.state.sideMenuIsOpen
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
            <div className="flex-row end">
                <Link to="/my/organization/create" className="link undecorated-link">
                    <i className="fa fa-plus action-icon"/>
                </Link>
            </div>

            <section>
                <header>
                    <h1 className="title">{this.context.t('Organizations')}</h1>
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


            <SideMenu isOpen={this.state.sideMenuIsOpen} onClickBackground={this.sideMenuToogle}>
                <h1>Menu !!!</h1>
            </SideMenu>
        </section>;
    }

}

const mapStateToProps = state => {
    return {
        userOrganizations: state.userInfo.organizations
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