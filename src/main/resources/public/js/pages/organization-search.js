import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Components
import DropdownMenu from '../components/dropdown-menu';
import SideMenu from '../components/side-menu';

//action
import { fetchOrganizations } from '../actions/organization';
import { fetchUserOrganizations } from '../actions/organization';

class OrganizationSearch extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            sideMenuIsOpen: false
        };

        //bind methods
        this.sideMenuToogle = this.sideMenuToogle.bind(this);
    }

    componentDidMount() {
        this.props.fetchUserOrganizations();
    }

    sideMenuToogle() {
        this.setState({
            sideMenuIsOpen: !this.state.sideMenuIsOpen
        });
    }

    render() {
        return <section className="organization-search oz-body wrapper flex-col">

            <div className="flex-row end">
                <button onClick={this.sideMenuToogle} className="btn icon"><i className="fa fa-bars menu-icon"/></button>
            </div>

            <section>
                <header>
                    <h1 className="title">{this.context.t('search')}</h1>
                </header>

                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>
            </section>


            <section>
                <header>
                    <h1 className="title">{this.context.t('my organization')}</h1>
                </header>

                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="organisations-list undecorated-list">
                {

                    this.props.userOrganizations.map((org) => {
                        const Header = <Link to={`/my/organization/${org.id}`} className="link">
                            {org.name}
                        </Link>;

                        return <li key={org.id} className="item">
                            <DropdownMenu header={Header} className="organization">
                                <h1>{org.label}</h1>
                            </DropdownMenu>
                        </li>
                    })
                }
                </ul>
            </section>


            <SideMenu isOpen={this.state.sideMenuIsOpen} onClickBackground={this.sideMenuToogle}>
                <h1>Menu !!!</h1>
            </SideMenu>
        </section>;
    }

}

const mapStateToProps = state => {
    return {
        organizations: state.organization.organizations,
        userOrganizations: state.userInfo.organizations
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchOrganizations() {
            return dispatch(fetchOrganizations());
        },
        fetchUserOrganizations() {
            return dispatch(fetchUserOrganizations());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrganizationSearch);