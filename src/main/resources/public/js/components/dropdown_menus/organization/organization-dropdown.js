import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

//Component
import DropdownMenu from '../../dropdown-menu';
import OrganizationDropdownHeader from '../../dropdown_menus/organization/organization-dropdown-header'
import OrganizationInvitationForm from '../../organization-invitation-form';


class OrganizationDropdown extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };

    render() {
        const organization = this.props.organization;
        const Header = <OrganizationDropdownHeader organization={organization}/> ;

        return <DropdownMenu header={Header} isOpen={true}>
            <section className='dropdown-content flex-row end'>
                <div className='container flex-row'>
                    <section className="apps">
                        <ul className="list undecorated-list flex-row">
                            {
                                organization.services &&
                                organization.services.map((service) => {
                                    return <li key={service.id} className="app">
                                        {service.name}
                                    </li>
                                })
                            }
                        </ul>
                    </section>
                    <div className="vertical-sep"/>
                    <section className="invitation">
                        <OrganizationInvitationForm organization={organization}/>
                    </section>
                </div>

                <div className='container flex-row end'>
                    <Link to={`/my/organization/${organization.id}/services`} className="link">
                        <em>Display more</em>
                    </Link>
                </div>
            </section>
        </DropdownMenu>

    }

}

export default OrganizationDropdown;