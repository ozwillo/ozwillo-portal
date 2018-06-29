import React from 'react';
import PropTypes from 'prop-types';


class Service extends React.Component {
    static propTypes = {
        service: PropTypes.object.isRequired
    };

    render() {
        const service = this.props.service;
        const iconUrl = service.catalogEntry.icon || service.iconUrl;
        return <article className={`service-icon flex-col ${this.props.className}`}>
            <a className="link undecorated-link" href={service.catalogEntry.service_uri} target="_blank">
                <img className="icon" src={iconUrl}/>
                <p className="name">{service.name}</p>
            </a>
        </article>

    }
}

export default Service;