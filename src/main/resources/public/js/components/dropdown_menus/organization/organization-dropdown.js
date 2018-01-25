import React from 'react';
import PropTypes from 'prop-types';

//Component
import Service from '../../service';
import DropdownMenu from '../../dropdown-menu';
import OrganizationDropdownHeader from '../../dropdown_menus/organization/organization-dropdown-header'
import OrganizationInvitationForm from '../../forms/organization-invitation-form';


class OrganizationDropdown extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };

    render() {
        const org = this.props.organization;
        const Header = <OrganizationDropdownHeader organization={org}/>;

        return <DropdownMenu header={Header} isOpen={true}>
            {
                (org.admin || org.services.length || null) &&
                <section className='dropdown-content flex-row'>
                    <section className={`apps ${!org.services.length ? 'empty' : ''}`}>
                        <ul className="list undecorated-list flex-row">
                            {
                                org.services.map((service) => {
                                    return <li key={service.catalogEntry.id} className="app">
                                        <Service service={service} className="launcher"/>
                                    </li>;
                                })
                            }
                        </ul>
                    </section>

                    {
                        ((org.admin &&
                        org.services &&
                        org.services.length) || null) &&
                        <div className="vertical-sep"/>
                    }

                    {
                        org.admin &&
                        <section className="invitation">
                            <OrganizationInvitationForm organization={org}/>
                        </section>
                    }

                </section>
            }
        </DropdownMenu>

    }

}

export default OrganizationDropdown;