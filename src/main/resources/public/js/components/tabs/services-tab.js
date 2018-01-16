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

    render() {
        return <article className="services-tab">

            <section className="add-service">
                <header>Add a service</header>
                <AddServiceDropdown />
            </section>

            <section className="search-service">
                <header>Search a service</header>
                <form className="search oz-form">
                    <input className="field form-control" type="text" placeholder={this.context.t('ui.search')}/>
                </form>

                <ul className="services-list undecorated-list flex-col">
                    {
                        this.props.services.map((service) => {
                            return <li key={service.id} className="service">
                                <ServiceDropdown service={service}/>
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
        services: state.organization.current.services
    };
};
const ServiceTabWithRedux = connect(mapStateToProps)(ServicesTab);


export {
    ServiceTabWithRedux as ServicesTab,
    ServicesTabHeaderWithRedux as ServicesTabHeader
};