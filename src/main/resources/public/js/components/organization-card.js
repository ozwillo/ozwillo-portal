import React from "react";
import PropTypes from 'prop-types';
import {Link} from "react-router-dom";
import RedirectButtonWithTooltip from "./redirect-button-with-tooltip";
import CustomTooltip from "./custom-tooltip";
import customFetch from "../util/custom-fetch";
import Config from "../config/config";
import Popup from "react-popup/dist";


const TIME_DAY = 1000 * 3600 * 24; // millisecondes

export default class OrganizationCard extends React.PureComponent {
    state = {
        orgDetails: {},
        isLoading: true
    };

    componentDidMount() {
        const {organization} = this.props;
        this._getOrganizationDetails(organization.dcOrganizationId);
    }


    _getOrganizationDetails = (dcOrganizationId) => {
        customFetch(`/my/api/organization/light/${dcOrganizationId}`)
            .then(res => this.setState({orgDetails: res, isLoading: false}))
            .catch(err => console.error(err))
    };

    _numberOfDaysBeforeDeletion() {
        const now = Date.now();
        const deletionDate = new Date(this.state.orgDetails.deletion_planned).getTime();

        const days = Math.round((deletionDate - now) / TIME_DAY);

        return (days > 0) ? this.context.t('ui.message.will-be-deleted-plural').format(days) :
            this.context.t('ui.message.will-be-deleted');
    }

    _handleCancelRemoveOrganization = (e) => {
        e.preventDefault();
        const {orgDetails} = this.state;
        orgDetails.status = Config.organizationStatus.available;
        let org = {id: orgDetails.id, status: Config.organizationStatus.available};
        customFetch(`/my/api/organization/${orgDetails.id}/status`, {
            method: 'PUT',
            json: org
        }).then((res) => {
            console.log("delete", res);
            this.setState({error: '', orgDetails: res});
        })
            .catch(err => {
                this.setState({error: err.error});
            });
    };


    _handleRemoveOrganization = (e) => {
        e.preventDefault();
        const {orgDetails} = this.state;
        let org = {id: orgDetails.id, status: Config.organizationStatus.deleted};
        customFetch(`/my/api/organization/${orgDetails.id}/status`, {
            method: 'PUT',
            json: org
        }).then((res) => {
            console.log("delete", res);
            this.setState({error: '', orgDetails: res});
        })
            .catch(err => {
                if (err.status === 403) {
                    this.setState({error: ''});
                    const lines = err.error.split('\n');

                    Popup.create({
                        title: orgDetails.name,
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
    };

    render() {
        const {dcOrganizationId, name} = this.props.organization;
        const {isLoading, orgDetails} = this.state;
        const url = `/my/organization/${dcOrganizationId}/`;

        const isAvailable = orgDetails.status === Config.organizationStatus.available;
        const isAdmin = orgDetails.admin;


        if (isLoading) {
            return (
                <div className="organization-card container-loading text-center">
                    <div className={"content-card"}>
                        <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                </div>)
        }

        if (orgDetails.personal) {
            return (
                <CustomTooltip key={`${dcOrganizationId}-instance-tab`}
                               title={this.context.t('tooltip.instances')}>
                    <Link className="btn icon"
                          to={`/my/organization/${dcOrganizationId}/instances`}>
                        <i className="fa fa-list-alt option-icon"/>
                    </Link>
                </CustomTooltip>
            )
        }

        if (isAvailable) {
            return (
                <Link to={url} className={"btn btn-default wrap-organization-card"}>
                    <div className={'organization-card'}>
                        <p className={"card-title"}>{name}</p>
                        <div className={"content-card"}>
                            <RedirectButtonWithTooltip link={url + 'instances'}
                                                       tooltipTitle={this.context.t('tooltip.instances')}>
                                <i className="fa fa-list-alt option-icon"/>
                            </RedirectButtonWithTooltip>
                            <RedirectButtonWithTooltip link={url + 'members'}
                                                       tooltipTitle={this.context.t('tooltip.members')}>
                                <i className="fa fa-users option-icon"/>
                            </RedirectButtonWithTooltip>
                            {isAdmin &&
                            <RedirectButtonWithTooltip link={url + 'admin'}
                                                       tooltipTitle={this.context.t('tooltip.admin')}>
                                <i className="fa fa-info-circle option-icon"/>
                            </RedirectButtonWithTooltip>
                            }
                            <CustomTooltip
                                className={`delete ${!dcOrganizationId && 'invisible' || ''}`}
                                title={this.context.t('tooltip.delete.organization')}>
                                <i onClick={this._handleRemoveOrganization} className="fa fa-trash option-icon"/>
                            </CustomTooltip>
                        </div>
                    </div>
                </Link>
            )
        } else if (!isAvailable && isAdmin) {
            return (
                <div className={"wrap-organization-card btn btn-default"}>
                    <div className={"organization-card"}>
                        <div className={"card-title"}>{this._numberOfDaysBeforeDeletion()}</div>
                        <div className={"content-card"}>
                            <button
                                onClick={this._handleCancelRemoveOrganization}
                                className="btn btn-default-inverse">
                                {this.context.t('ui.cancel')}
                            </button>
                        </div>
                    </div>
                </div>
            )
        }
    }
}

OrganizationCard.propTypes = {
    organization: PropTypes.object.isRequired
};

OrganizationCard.contextTypes = {
    t: PropTypes.func.isRequired
};