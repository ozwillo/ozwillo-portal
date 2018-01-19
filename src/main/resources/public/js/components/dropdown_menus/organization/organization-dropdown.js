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
        const organization = this.props.organization;
        const Header = <OrganizationDropdownHeader organization={organization}/> ;

        return <DropdownMenu header={Header} isOpen={true}>
            {
                (organization.isAdmin || organization.services.length || null) &&
                <section className='dropdown-content flex-row end'>
                    <section className="apps">
                        <ul className="list undecorated-list flex-row">
                            {
                                organization.services.map((service) => {
                                    return <li key={service.catalogEntry.id} className="app">
                                        <Service service={service} className="launcher"/>
                                    </li>;
                                })
                            }
                        </ul>
                    </section>

                    {
                        organization.services &&
                        (organization.services.length || null) &&
                        <div className="vertical-sep"/>
                    }

                    <section className="invitation">
                        <OrganizationInvitationForm organization={organization}/>
                    </section>
                </section>
            }
        </DropdownMenu>

    }

}

export default OrganizationDropdown;