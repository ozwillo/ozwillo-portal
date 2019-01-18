import React from "react";
import PropTypes from 'prop-types';
import {Link} from "react-router-dom";
import RedirectButtonWithTooltip from "./redirect-button-with-tooltip";
import CustomTooltip from "./custom-tooltip";
import Config from "../config/config";
import Popup from "react-popup/dist";
import OrganizationService from '../util/organization-service';

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"

const TIME_DAY = 1000 * 3600 * 24; // millisecondes

export default class OrganizationCard extends React.PureComponent {
    state = {
        orgDetails: {},
        isLoading: true
    };

    constructor(){
        super();
        this._organizationService = new OrganizationService();
    }

    componentDidMount = async () => {
        const {organization} = this.props;
        await this._getOrganizationDetails(organization.dcOrganizationId);
    }


    _getOrganizationDetails = async (dcOrganizationId) => {
        try {
            let res = await this._organizationService.getOrganizationLight(dcOrganizationId);
            this.setState({orgDetails: res, isLoading: false})
        }catch(err) {
            this.props.callBackError(err, dcOrganizationId);
        }
    };

    _numberOfDaysBeforeDeletion() {
        const now = Date.now();
        const deletionDate = new Date(this.state.orgDetails.deletion_planned).getTime();

        const days = Math.round((deletionDate - now) / TIME_DAY);

        return (days > 0) ? i18n._(`ui.message.will-be-deleted-plural`, {value: days}) :
            i18n._(t`ui.message.will-be-deleted`);
    }

    _handleCancelRemoveOrganization = async (e) => {
        e.preventDefault();
        const {orgDetails} = this.state;
        orgDetails.status = Config.organizationStatus.available;
        let org = {id: orgDetails.id, status: Config.organizationStatus.available};

        try {
            let res = await this._organizationService.updateOrganizationStatus(org);
            this.setState({error: '', orgDetails: res});
        }catch(err){
            this.setState({error: err.error});
        }
    };


    _handleRemoveOrganization = async (e) => {
        e.preventDefault();
        const {orgDetails} = this.state;
        let org = {id: orgDetails.id, status: Config.organizationStatus.deleted};

        try {
            let res = await this._organizationService.updateOrganizationStatus(org);
            this.setState({error: '', orgDetails: res});
        }catch(err){
            if (err.status === 403) {
                this.setState({error: ''});
                const lines = i18n._(`error.msg.delete-organization`).split('\n');

                Popup.create({
                    title: orgDetails.name,
                    content: <p className="alert-message">
                        {lines.map((msg, i) => <span key={i} className="line">{msg}</span>)}
                    </p>,
                    buttons: {
                        right: [{
                            text: i18n._(t`ui.ok`),
                            action: () => {
                                Popup.close();
                            }
                        }]
                    }

                });
            } else {
                this.setState({error: err.error});
            }
        }
    };

    render() {
        const {dcOrganizationId, name} = this.props.organization;
        const {isLoading, orgDetails} = this.state;
        const url = `/my/organization/${dcOrganizationId}/`;

        const isAvailable = orgDetails.status === Config.organizationStatus.available;
        const isAdmin = orgDetails.admin;
        const isPersonal = orgDetails.personal;


        if (isLoading) {
            return (
                <div className={"wrap-organization-card"}>
                    <div className="card organization-card-spinner container-loading text-center">
                            <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                </div>)
        }

        if (isPersonal) {
            return (
                <Link to={url} className={"btn btn-default wrap-organization-card"}>
                    <div className={'card'}>
                        <p className={"card-title"}>{name}</p>
                        <div className={"card-body"}>
                            <RedirectButtonWithTooltip link={url + 'instances'}
                                                       tooltipTitle={i18n._(t`tooltip.instances`)}>
                                <i className="fa fa-list-alt option-icon"/>
                            </RedirectButtonWithTooltip>
                        </div>
                    </div>
                </Link>
            )
        }

        if (isAvailable) {
            return (
                <Link to={url} className={"btn btn-default wrap-organization-card"}>
                    <div className={"card"}>
                        <p className={"card-title"}>{name}</p>
                        <div className={"card-body"}>
                            <div className={"flex-row"}>
                                <RedirectButtonWithTooltip link={url + 'members'}
                                                           tooltipTitle={i18n._(t`tooltip.members`)}>
                                    <i className="fa fa-users option-icon"/>
                                </RedirectButtonWithTooltip>

                                {isAdmin &&
                                <React.Fragment>
                                    <RedirectButtonWithTooltip link={url + 'instances'}
                                                               tooltipTitle={i18n._(t`tooltip.instances`)}>
                                        <i className="fa fa-list-alt option-icon"/>
                                    </RedirectButtonWithTooltip>
                                    <RedirectButtonWithTooltip link={url + 'admin'}
                                                               tooltipTitle={i18n._(t`tooltip.admin`)}>
                                        <i className="fa fa-info-circle option-icon"/>
                                    </RedirectButtonWithTooltip>
                                    <CustomTooltip className={`delete ${!dcOrganizationId && 'invisible' || ''}`}
                                                   title={i18n._(t`tooltip.delete.organization`)}>
                                        <i onClick={this._handleRemoveOrganization}
                                           className="fa fa-trash option-icon"/>
                                    </CustomTooltip>
                                </React.Fragment>
                                }
                            </div>
                        </div>
                    </div>
                </Link>
            )
        } else if (!isAvailable && isAdmin) {
            return (
                <div className={"btn btn-default wrap-organization-card"}>
                    <div className={"card"}>
                        <div className={"card-title"}>{name}</div>
                        <div className={"card-body"}>
                            <div className={"card-message"}>{this._numberOfDaysBeforeDeletion()}</div>
                            <button
                                onClick={this._handleCancelRemoveOrganization}
                                className="btn btn-default-inverse">
                                {i18n._(t`ui.cancel`)}
                            </button>
                        </div>
                    </div>
                </div>
            )
        }
    }
}

OrganizationCard.propTypes = {
    organization: PropTypes.object.isRequired,
    callBackError: PropTypes.func.isRequired
};
