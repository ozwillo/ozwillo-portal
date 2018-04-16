import React from 'react';
import PropTypes from 'prop-types';
import Popup from 'react-popup';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom'
import CustomTooltip from '../../custom-tooltip';

import Config from '../../../config/config';

const organizationStatus = Config.organizationStatus;

const TIME_DAY = 1000 * 3600 * 24; // millisecondes

class OrganizationDropdownHeader extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        onRemoveOrganization: PropTypes.func.isRequired,
        onCancelRemoveOrganization: PropTypes.func.isRequired
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
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
            .then(() => {
                this.setState({error: ''});
            })
            .catch(err => {
                if (err.status === 403) {
                    this.setState({error: ''});
                    const lines = err.error.split('\n');

                    Popup.create({
                        title: this.props.organization.name,
                        content: <p className="alert-message">
                            {lines.map((msg, i) => <span key={i} className="line">{msg}</span>)}
                        </p>,
                        buttons: {
                            right: [{
                                text: this.context.t('ui.ok'),
                                action: () => {
                                    Popup.close();
                                }
                            }]
                        }

                    });
                } else {
                    this.setState({error: err.error});
                }
            });
    }

    onCancelRemoveOrganization(e) {
        e.preventDefault();
        this.props.onCancelRemoveOrganization(this.props.organization)
            .then(() => {
                this.setState({error: ''});
            })
            .catch(err => {
                this.setState({error: err.error});
            });
    }

    get numberOfDaysBeforeDeletion() {
        const now = Date.now();
        const deletionDate = new Date(this.props.organization.deletion_planned).getTime();

        const days = Math.round((deletionDate - now) / TIME_DAY);

        return (days > 0) ? this.context.t('ui.message.will-be-deleted-plural').format(days) :
            this.context.t('ui.message.will-be-deleted');
    }

    render() {
        const org = this.props.organization;
        const isAvailable = org.status === organizationStatus.available;
        const isPersonal = this.props.userInfo.sub === org.id;

        return <header className="dropdown-header">
            <form className="form flex-row"
                  onSubmit={(isAvailable && this.onRemoveOrganization) || this.onCancelRemoveOrganization}>
                <span className="dropdown-name">
                    <Link className="link" to={`/my/organization/${org.id}/`}>
                        {org.name}
                    </Link>
                </span>

                {
                    this.state.error &&
                    <span className="error-message ">
                        {this.state.error}
                    </span>
                }

                <div className="options flex-row end">
                    {
                        (isAvailable && !isPersonal) && [
                            <CustomTooltip key={`${org.id}-instance-tab`}
                                           title={this.context.t('tooltip.instances')}>
                                <Link className="btn icon"
                                      to={`/my/organization/${org.id}/instances`}>
                                    <i className="fa fa-list-alt option-icon"/>
                                </Link>
                            </CustomTooltip>,

                            <CustomTooltip key={`${org.id}-member-tab`}
                                           title={this.context.t('tooltip.members')}>
                                <Link className="btn icon"
                                      to={`/my/organization/${org.id}/members`}>
                                    <i className="fa fa-users option-icon"/>
                                </Link>
                            </CustomTooltip>,

                            <CustomTooltip key={`${org.id}-admin-tab`}
                                           title={this.context.t('tooltip.admin')}>
                                <Link className="btn icon"
                                      to={`/my/organization/${org.id}/admin`}>
                                    <i className="fa fa-info-circle option-icon"/>
                                </Link>
                            </CustomTooltip>,

                            <CustomTooltip key={`${org.id}-delete`} className={`${!org.admin && 'invisible' || ''}`}
                                           title={this.context.t('tooltip.delete.organization')}>
                                <button type="submit"
                                        className="btn icon">
                                    <i className="fa fa-trash option-icon"/>
                                </button>
                            </CustomTooltip>
                        ]
                    }

                    {
                        isPersonal &&
                            <CustomTooltip key={`${org.id}-instance-tab`}
                                           title={this.context.t('tooltip.instances')}>
                                <Link className="btn icon"
                                      to={`/my/organization/${org.id}/instances`}>
                                    <i className="fa fa-list-alt option-icon"/>
                                </Link>
                            </CustomTooltip>
                    }

                    {
                        !isAvailable && !isPersonal && org.admin &&
                        [
                            <span key={`${org.id}-message`} className="message delete">
                                {this.numberOfDaysBeforeDeletion}
                            </span>,
                            <button key={`${org.id}-btn`} type="submit" className="btn btn-default-inverse">
                                {this.context.t('ui.cancel')}
                            </button>
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