import React from 'react';
import PropTypes from 'prop-types';

//Components
import AddServiceDropdown from '../dropdown_menus/service/add-service-dropdown';

class ServicesTabHeader extends React.Component {

    render() {
        return <header>Services</header>
    }

}

class ServicesTab extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
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
            </section>

        </article>;
    }
}
export {
    ServicesTab,
    ServicesTabHeader
};