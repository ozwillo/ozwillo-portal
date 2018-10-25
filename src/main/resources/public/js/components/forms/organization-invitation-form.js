import React from 'react';
import PropTypes from 'prop-types';

import {DropdownBlockError, DropdownBlockSuccess} from '../notification-messages';
import CSVReader from "../CSVReader";
import OrganizationService from "../../util/organization-service";
import CustomTooltip from "../custom-tooltip";

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

        if(email !== '') {
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
        const requiered = !(emailsFromCSV.length > 0 || email !== '');

        return <header className="dropdown-header">
            <form className="organization-invitation-form flex-row" onSubmit={this.onSubmit}>
                {/*email input*/}
                <div className={"form-row-block"}>
                    <div className={"form-column-block"}>
                        <label className={"label label-title"}>
                            {this.context.t('organization.form.email')}
                        </label>
                        <div className="flex-row-mobile-column">

                            <div className="flex-row wrapper">
                                <div className="flex-row">
                                    <label className="label">
                                        <input required={requiered} name="email" type="email"
                                               className="field form-control no-auto"
                                               onChange={this.handleChange} value={this.state.email}/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


                {/*CSV INPUT*/}
                <div className={"form-row-block"}>
                    <div className={"form-column-block"}>
                        <label className={"label label-title"}>
                            {this.context.t('my.network.import-from-csv')}
                        </label>
                        <div className={"flex-row"}>
                            <CSVReader
                                requiered={requiered}
                                ref={csvReader => this.csvReader = csvReader}
                                onFileReading={() => this.setState({csvLoading: true})}
                                onFileReaded={(emails) => this.setState({emailsFromCSV: emails, csvLoading: false})}/>
                        </div>


                    </div>

                </div>

                {/*Submit button*/}
                <div className={"form-column-block form-submit"}>
                    <div className={"form-row-block"}>
                        {emailsFromCSV.length > 0 ?
                            <CustomTooltip title={this.context.t('my.network.csv-email-spec')}>
                                {submitButton}
                            </CustomTooltip>
                            :
                            submitButton

                        }
                        {(isLoading || csvLoading || isFetchingUsers) &&
                        <div className={"spinner-container"}>
                            <i className="fa fa-spinner fa-spin action-icon"/>
                        </div>
                        }
                    </div>
                </div>


            </form>
            {
                this.state.error && <DropdownBlockError errorMessage={this.state.error}/>
            }

            {
                this.state.success && <DropdownBlockSuccess successMessage={this.state.success}/>
            }
        </header>
            ;
    }

}

OrganizationInvitationForm.propTypes = {
    organization: PropTypes.object.isRequired,
    callBackMembersInvited: PropTypes.func,
    members: PropTypes.array
};


