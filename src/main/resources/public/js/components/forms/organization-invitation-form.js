import React from 'react';
import PropTypes from 'prop-types';

import {DropdownBlockError, DropdownBlockSuccess} from '../notification-messages';
import CSVReader from "../CSVReader";
import OrganizationService from "../../util/organization-service";
import { i18n } from "../../config/i18n-config"
import { t , Trans} from "@lingui/macro"



import OrganizationInvitationInstances from "./organization-invitation-instances";
import Stepper from "../stepper";
import PillButton from "../pill-button";
import InstanceService from "../../util/instance-service";

export default class OrganizationInvitationForm extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            email: '',
            admin: false,
            isLoading: false,
            success: '',
            error: '',
            emailsFromCSV: [],
            csvFileName: "",
            csvLoading: false,
            isFetchingUsers: false,
            activeStep: 1,
            instancesSelected: []
        };


        this._organizationService = new OrganizationService();
        this._instanceService = new InstanceService();

        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);

    }

    handleChange(e) {
        const el = e.currentTarget;

        this.setState({
            [el.name]: el.type === 'checkbox' ? el.checked : el.value
        });
    }

    _removeAlreadyPresentMembers = (emailArray) => {
        const {members} = this.props;

        members.map(member => {
            const emailIndex = emailArray.indexOf(member.email);
            emailIndex > -1 ? emailArray.splice(emailIndex, 1) : null;
        });
    };

    _inviteMultipleUsers = async (emailArray) => {
        this.setState({isFetchingUsers: true});
        try {
            this._removeAlreadyPresentMembers(emailArray);
            const response = await this._organizationService.inviteMultipleUsers(this.props.organization.id, emailArray);
            this.setState({
                success: i18n._('ui.request.send'),
                error: ''
            });
            this.props.callBackMembersInvited(response);
        } catch (e) {
            this.setState({
                error: e.error,
                success: ''
            })
        } finally {
            await this._addMembersToInstances(emailArray);
            this.setState({isFetchingUsers: false, emailsFromCSV: []});
        }
    };


    _addMembersToInstances = async (emails) => {
        const {instancesSelected} = this.state;
        const fetchResult = instancesSelected.map(async instance => {
            emails.map(async email => {
                await this._instanceService.fetchCreateAcl({email: email}, instance);
            })
        });
        return await Promise.all(fetchResult);
    };


    _handleCSVRead = (emails) => {
        this.setState({emailsFromCSV: emails, csvLoading: false, csvFileName: this.csvReader.state.fileName});
    };

    _handleInstancesSelected = (instances) => {
        this.setState({instancesSelected: instances})
    };

    _handleStep = (wantedStep) => {
        const {email, emailsFromCSV} = this.state;
        if ((wantedStep > 1 && (email || emailsFromCSV.length > 0)) || wantedStep === 1) {
            this.setState({activeStep: wantedStep});
        }
    };

    onSubmit = async (e) => {
        e.preventDefault();
        const {email, admin, emailsFromCSV} = this.state;


        if (emailsFromCSV.length > 0) {
            await this._inviteMultipleUsers(emailsFromCSV);
            this._resetForm();
        }

        if (email !== '') {
            this.setState({isLoading: true});
            this._organizationService.inviteUser(this.props.organization.id, email, admin)
                .then((res) => {
                    this._addMembersToInstances([res.email]);
                    this.props.callBackMembersInvited(res);
                    this.setState({
                        success: i18n._('ui.request.send')
                    });
                    this._resetForm();
                })
                .catch((err) => {
                    this.setState({
                        isLoading: false,
                        error: i18n._('error.msg.user-already-invited')
                    });
                });
        }
    };

    _resetForm = () => {
        this.setState({
            email: '',
            admin: false,
            isLoading: false,
            error: '',
            emailsFromCSV: [],
            csvFileName: "",
            csvLoading: false,
            isFetchingUsers: false,
            activeStep: 1,
            instancesSelected: []
        });
    };

    render() {
        const {csvLoading, isFetchingUsers, emailsFromCSV, isLoading, email, activeStep, csvFileName, instancesSelected} = this.state;
        const submitButton =
            <button type="submit" className="btn btn-submit" disabled={(isFetchingUsers || csvLoading || isLoading)}
                    onClick={this.onSubmit}>
                {i18n._(t`my.network.invite-user`)}
            </button>;
        const required = !(emailsFromCSV.length > 0 || email !== '');


        return<header className="organization-invitation-form">
            <p className={"invitation-title"}>{i18n._(t`organization.desc.add-new-members`)}</p>

            <Stepper activeStep={activeStep} nbSteps={3} onClickStep={(activeStep) => this._handleStep(activeStep)}/>

            <form className="invitation-form">
                {/*STEP 1*/}
                {activeStep === 1 &&
                <div>
                    <div className={"organization-form-sentence sentence"}>
                        {/*email input*/}
                        <p>{i18n._(t`organization.desc.from-email`)}</p>
                        <input required={required} name="email" type="email"
                               className="field form-control no-auto"
                               onChange={this.handleChange} value={this.state.email}/>
                        {/*CSV INPUT*/}
                        <p>{i18n._(t`organization.desc.from-CSV`)}*</p>
                        <CSVReader
                            fileName={csvFileName}
                            required={required}
                            ref={csvReader => this.csvReader = csvReader}
                            onFileReading={() => this.setState({csvLoading: true})}
                            onFileRead={(emails) => this._handleCSVRead(emails)}/>
                    </div>
                    <div className={"organization-form-sentence"}>
                        <p className={"helper-text"}>(*)<em>&nbsp;{i18n._("organization.desc.CSV-helper")}.</em>
                        </p>
                    </div>
                </div>
                }

                {/*STEP 2*/}
                {activeStep === 2 &&
                <OrganizationInvitationInstances
                    instances={this.props.organization.instances}
                    callBackInstances={this._handleInstancesSelected}
                    instancesSelected={instancesSelected}/>
                }

                {/*STEP 3*/}
                {activeStep === 3 &&
                <div className={"summarize"}>
                    <div>
                        <ul>
                            {emailsFromCSV.length > 0 && <li><Trans>Emails from CSV file <strong>{csvFileName}</strong> will be invited to the organization</Trans></li>}
                            {email && <li><Trans><strong>{email}</strong> will be invited to the organization</Trans></li>}
                            {instancesSelected.length > 0 &&
                            <React.Fragment>
                                <li>{i18n._(t`organization.desc.summarize-apps-added`)} :</li>
                                <div className={"instances-summarize"}>
                                    {
                                        instancesSelected.map(instance => {
                                            return (
                                                <PillButton key={instance.id}
                                                            text={instance.applicationInstance.name}
                                                />
                                            )
                                        })
                                    }
                                </div>
                            </React.Fragment>
                            }
                        </ul>
                    </div>

                </div>
                }


            </form>

            {/*NEXT STEP BUTTONS*/}
            {activeStep < 3 ?
                <div className={"next-step-container"}>
                    <button className={"btn btn-default next-step"}
                            onClick={() => this._handleStep(activeStep + 1)}
                            disabled={!emailsFromCSV.length > 0 && !email}
                    >
                        {i18n._(t`ui.next-step`)}
                    </button>
                </div>
                :
                <div className={"next-step-container"}>
                    {/*Submit button*/}
                    <div>
                        {submitButton}
                    </div>
                    {(isLoading || csvLoading || isFetchingUsers) &&
                    <div className={"spinner-container"}>
                        <i className="fa fa-spinner fa-spin action-icon"/>
                    </div>
                    }
                </div>
            }

            {
                this.state.error && <DropdownBlockError errorMessage={this.state.error}/>
            }

            {
                this.state.success && <DropdownBlockSuccess successMessage={this.state.success}/>
            }

        </header>;
    }

}

OrganizationInvitationForm.propTypes = {
    organization: PropTypes.object.isRequired,
    callBackMembersInvited: PropTypes.func,
    members: PropTypes.array
};


