import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom'

import Config from '../../../config/config';
const organizationStatus = Config.organizationStatus;

const TIME_DAY = 1000 * 3600 * 24; // millisecondes

class OrganizationDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        onRemoveOrganization: PropTypes.func.isRequired,
        onCancelRemoveOrganization: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            error: ''
        };

        this.onRemoveOrganization = this.onRemoveOrganization.bind(this);
        this.onCancelRemoveOrganization = this.onCancelRemoveOrganization.bind(this);
    }

    onRemoveOrganization(e) {
        e.preventDefault();
        this.props.onRemoveOrganization(this.props.organization)
            .then(() => { this.setState({ error: '' }); })
            .catch(err => { this.setState({ error: err.error }); });
    }

    onCancelRemoveOrganization(e) {
        e.preventDefault();
        this.props.onCancelRemoveOrganization(this.props.organization)
            .then(() => { this.setState({ error: '' }); })
            .catch(err => { this.setState({ error: err.error }); });
    }

    get numberOfDaysBeforeDeletion() {
        const now = Date.now();
        const deletionDate = new Date(this.props.organization.deletion_planned).getTime();

        const days = Math.round((deletionDate - now ) / TIME_DAY);

        return days;
    }

    render() {
        const org = this.props.organization;
        const isAvailable = org.status === organizationStatus.available;
        const isPersonal = this.props.userInfo.sub === org.id;

        return <header className="dropdown-header">
            <form className="form flex-row"
                  onSubmit={ (isAvailable && this.onRemoveOrganization) || this.onCancelRemoveOrganization }>
                <span className="dropdown-name">
                    <Link className="link" to={`/my/organization/${org.id}/`}>
                        {org.name}
                    </Link>
                </span>

                {
                    this.state.error &&
                    <span className="error-message ">
                        { this.state.error }
                    </span>
                }

                <div className="options flex-row end">
                    {
                        (isAvailable || isPersonal) && [
                            <Link key={`${org.id}-instance-tab`} className="btn icon"
                                  to={`/my/organization/${org.id}/instances`}>
                                <i className="fa fa-list-alt option-icon"/>
                            </Link>,

                            <Link key={`${org.id}-member-tab`} className="btn icon"
                                  to={`/my/organization/${org.id}/members`}>
                                <i className="fa fa-users option-icon"/>
                            </Link>,

                            <Link key={`${org.id}-admin-tab`} className="btn icon"
                                  to={`/my/organization/${org.id}/admin`}>
                                <i className="fa fa-info-circle option-icon"/>
                            </Link>,
                            <button key={`${org.id}-delete`} type="submit"
                                    className={`btn icon ${!org.admin && 'invisible' || ''}`}>
                                <i className="fa fa-trash option-icon"/>
                            </button>
                        ]
                    }

                    {
                        !isAvailable && !isPersonal && org.admin &&
                        [
                            <span key={`${org.id}-message`} className="message delete">
                                Will be deleted in {this.numberOfDaysBeforeDeletion} days
                            </span>,
                            <button key={`${org.id}-btn`} type="submit" className="btn btn-default-inverse">Cancel</button>
                        ]
                    }


                </div>
            </form>
        </header>;
    }
}

const mapStateToProps = state => {
    return {
        userInfo: state.userInfo
    };
};

export default connect(mapStateToProps)(OrganizationDropdownHeader);