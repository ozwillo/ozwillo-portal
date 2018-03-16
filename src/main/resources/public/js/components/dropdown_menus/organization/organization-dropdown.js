import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';

//Component
import Service from '../../service';
import DropdownMenu from '../../dropdown-menu';
import OrganizationDropdownHeader from '../../dropdown_menus/organization/organization-dropdown-header'
import OrganizationInvitationForm from '../../forms/organization-invitation-form';

//Actions
import {fetchUpdateStatusOrganization} from "../../../actions/organization";

//Config
import Config from '../../../config/config';

const organizationStatus = Config.organizationStatus;

class OrganizationDropdown extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.removeOrganization = this.removeOrganization.bind(this);
        this.cancelRemoveOrganization = this.cancelRemoveOrganization.bind(this);
    }

    removeOrganization(org) {
        return this.props.fetchUpdateStatusOrganization({id: org.id, status: organizationStatus.deleted});
    }

    cancelRemoveOrganization(org) {
        return this.props.fetchUpdateStatusOrganization({id: org.id, status: organizationStatus.available});
    }

    render() {
        const org = this.props.organization;
        const Header = <OrganizationDropdownHeader organization={org}
                                                   onRemoveOrganization={this.removeOrganization}
                                                   onCancelRemoveOrganization={this.cancelRemoveOrganization}/>;

        return <DropdownMenu header={Header} isOpen={true}>
            {
                (org.admin || org.services || null) &&
                <section className='dropdown-content flex-row'>
                    <section className="apps flex-row">
                        {
                            org.services &&
                            <ul className="list undecorated-list flex-row">
                                {
                                    org.services.map((service) => {
                                        return <li key={service.catalogEntry.id} className="app">
                                            <Service service={service} className="launcher"/>
                                        </li>;
                                    })
                                }
                            </ul> || null
                        }

                        {
                            !org.services &&
                            <span className="empty-message">
                                {this.context.t('organization.search.no-apps-installed')}
                            </span>
                        }
                    </section>

                    {
                        org.admin &&
                        [
                            <div key={`sep-${org.id}`} className="sep"/>,
                            <section key={`invitation-${org.id}`} className="invitation">
                                <OrganizationInvitationForm organization={org}/>
                            </section>
                        ]
                    }

                </section>
            }
        </DropdownMenu>

    }

}

const mapDispatchToProps = dispatch => {
    return {
        fetchUpdateStatusOrganization(organization, status) {
            return dispatch(fetchUpdateStatusOrganization(organization, status));
        }
    };
};

export default connect(null, mapDispatchToProps)(OrganizationDropdown);