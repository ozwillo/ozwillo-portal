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
                (org.admin || null) &&
                <section className='dropdown-content flex-row'>
                    {
                        org.admin &&
                            <section key={`invitation-${org.id}`} className="invitation">
                                <OrganizationInvitationForm organization={org}/>
                            </section>
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