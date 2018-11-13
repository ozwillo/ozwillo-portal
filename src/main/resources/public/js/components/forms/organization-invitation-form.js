import React from 'react';
import PropTypes from 'prop-types';

import {DropdownBlockError, DropdownBlockSuccess} from '../notification-messages';
import CSVReader from "../CSVReader";
import OrganizationService from "../../util/organization-service";
import CustomTooltip from "../custom-tooltip";
import OrganizationInvitationInstances from "./organization-invitation-instances";

export default class OrganizationInvitationForm extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            email: '',
            admin: false,
            isLoading: false,
            success: '',
            error: '',
            emailsFromCSV: [],
            csvLoading: false,
            isFetchingUsers: false
        };


        this._organizationService = new OrganizationService();

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
                success: this.context.t('ui.request.send'),
                error: ''
            });
            this.props.callBackMembersInvited(response);
        } catch (e) {
            this.setState({
                error: e.error,
                success: ''
            })
        } finally {
            this.setState({isFetchingUsers: false, emailsFromCSV: []});
            this.csvReader.cleanInput();
        }
    };

    onSubmit(e) {
        e.preventDefault();
        const {email, admin, emailsFromCSV} = this.state;


        if (emailsFromCSV.length > 0) {
            this._inviteMultipleUsers(emailsFromCSV);
        }

        if (email !== '') {
            this.setState({isLoading: true});
            this._organizationService.inviteUser(this.props.organization.id, email, admin)
                .then((res) => {
                    this.props.callBackMembersInvited(res);
                    this.setState({
                        email: '',
                        admin: false,
                        isLoading: false,
                        error: '',
                        success: this.context.t('ui.request.send')
                    });
                })
                .catch((err) => {
                    this.setState({
                        isLoading: false,
                        error: err.error
                    });
                });
        }
    }

    render() {
        const {csvLoading, isFetchingUsers, emailsFromCSV, isLoading, email} = this.state;
        const submitButton =
            <button type="submit" className="btn btn-submit" disabled={(isFetchingUsers || csvLoading || isLoading)}>
                {this.context.t('my.network.invite-user')}
            </button>;
        const required = !(emailsFromCSV.length > 0 || email !== '');

        return <header className="organization-invitation-form">
            <p className={"invitation-title"}>{this.context.t("organization.desc.add-new-members")}</p>
            <form className="invitation-form" onSubmit={this.onSubmit}>
                <div className={"organization-form-sentence sentence"}>
                    {/*email input*/}
                    <p>{this.context.t("organization.desc.from-email")}</p>
                    <input required={required} name="email" type="email"
                           className="field form-control no-auto"
                           onChange={this.handleChange} value={this.state.email}/>
                    {/*CSV INPUT*/}
                    <p>{this.context.t("organization.desc.from-CSV")}*</p>
                    <CSVReader
                        required={required}
                        ref={csvReader => this.csvReader = csvReader}
                        onFileReading={() => this.setState({csvLoading: true})}
                        onFileRead={(emails) => this.setState({emailsFromCSV: emails, csvLoading: false})}/>
                    {/*Submit button*/}
                    <div>
                        {emailsFromCSV.length > 0 ?
                            <CustomTooltip title={this.context.t('my.network.csv-email-spec')}>
                                {submitButton}
                            </CustomTooltip>
                            :
                            submitButton

                        }
                    </div>
                    {(isLoading || csvLoading || isFetchingUsers) &&
                    <div className={"spinner-container"}>
                        <i className="fa fa-spinner fa-spin action-icon"/>
                    </div>
                    }
                </div>
                <div className={"organization-form-sentence"}>
                    <p className={"helper-text"}>(*)&nbsp;{this.context.t("organization.desc.CSV-helper")}.</p>
                </div>

                <OrganizationInvitationInstances instances={this.props.organization.instances}/>


            </form>

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


