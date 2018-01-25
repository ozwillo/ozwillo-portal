import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

//Components
import AddServiceDropdown from '../dropdown_menus/service/add-service-dropdown';
import ServiceDropdown from '../dropdown_menus/service/service-dropdown';

class ServicesTabHeader extends React.Component {
    render() {
        return <Link className="undecorated-link" to={`/my/organization/${this.props.organization.id}/services`}>
            <header className="tab-header">Services</header>
        </Link>;
    }
}
const ServicesTabHeaderWithRedux = connect(state => {
    return {
        organization: state.organization.current
    };
})(ServicesTabHeader);


class ServicesTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        this.state = {
            servicesFilter: ''
        };


        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.filterServices = this.filterServices.bind(this);

    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.value
        });
    }

    filterServices(services, filter) {
        if (!filter) {
            return services;
        }
        const regex = new RegExp(`(\\w|\\S)*${filter.toUpperCase()}(\\w|\\S)*`);

        return services.filter((service) => {
            return regex.test(service.name.toUpperCase());
        });
    }

    render() {
        const org = this.props.organization;
        const servicesFilter = this.state.servicesFilter;

        return <article className="services-tab">

            {
                org.admin &&
                <section className="add-service">
                    <header>Add a service</header>
                    <AddServiceDropdown />
                </section>
            }


            <section className="search-service">
                <header>Search a service</header>
                <form className="search oz-form">
                    <input name="servicesFilter" className="field form-control" type="text" value={servicesFilter}
                           placeholder={this.context.t('ui.search')} onChange={this.handleChange}/>
                </form>

                <ul className="services-list undecorated-list flex-col">
                    {
                        this.filterServices(org.services, servicesFilter).map((service) => {
                            return <li key={service.id} className="service">
                                <ServiceDropdown service={service} members={org.members}/>
                            </li>
                        })
                    }
                </ul>
            </section>

        </article>;
    }
}

const mapStateToProps = state => {
    return {
        organization: state.organization.current,
    };
};

const ServiceTabWithRedux = connect(mapStateToProps)(ServicesTab);


export {
    ServiceTabWithRedux as ServicesTab,
    ServicesTabHeaderWithRedux as ServicesTabHeader
};