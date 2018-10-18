import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';

import {createOrganizationInvitation} from '../../actions/invitation';
import {DropdownBlockError, DropdownBlockSuccess} from '../notification-messages';
import CSVReader from "../CSVReader";
import OrganizationService from "../../util/organization-service";

class OrganizationInvitationForm extends React.Component {

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

    _inviteMultipleUsers = async (emailArray) => {
        this.setState({isFetchingUsers: true});
        try {
            const response = await this._organizationService.inviteMultipleUsers(this.props.organization.id, emailArray);
            this.props.membersInvited(response);
        } catch(e) {
            this.setState({error: e})
        } finally {
            this.setState({isFetchingUsers: false});
        }
    };

    onSubmit(e) {
        e.preventDefault();

        this.setState({isLoading: true});
        this.props.createOrganizationInvitation(this.props.organization.id, this.state.email, this.state.admin)
            .then(() => {
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

    render() {
        const {csvLoading, isFetchingUsers,emailsFromCSV} = this.state;

        return <header className="dropdown-header">
            <form className="organization-invitation-form flex-row" onSubmit={this.onSubmit}>
                <fieldset className="flex-row">
                    <div className={"invite-user"}>
                        <label className={"flex-row label label-title"}>
                            {this.context.t('organization.form.email')}
                        </label>
                        <div className="flex-row-mobile-column">
                            <div className="flex-row wrapper">
                                <div className="flex-row">
                                    <label className="label">
                                        <input name="email" type="email" className="field form-control no-auto"
                                               required={true}
                                               onChange={this.handleChange} value={this.state.email}/>
                                    </label>
                                </div>
                            </div>


                            <div className="options flex-row">
                                {/*
                        <label className="label">
                            {this.context.t('organization.form.admin')}
                            <input name="admin" type="checkbox" className="field"
                                onChange={this.handleChange} checked={this.state.admin}/>
                        </label>
*/}
                                <button type="submit" className="btn btn-submit" disabled={this.state.isLoading}>
                                    {
                                        !this.state.isLoading &&
                                        this.context.t('my.network.invite-user')
                                    }
                                    {

                                        this.state.isLoading &&
                                        <i className="fa fa-spinner fa-spin action-icon" style={{'marginLeft': '1em'}}/>
                                    }
                                </button>


                            </div>
                        </div>
                    </div>
                    <div className={"invite-multiple-users"}>
                        <label className={"flex-row label label-title"}>
                            Import from CSV
                        </label>
                        <div className={"flex-row"}>
                            <CSVReader
                                onFileReading={() => this.setState({csvLoading: true})}
                                onFileReaded={(emails) => this.setState({emailsFromCSV: emails, csvLoading: false})}/>

                            <button className="btn btn-submit"
                                    onClick={() => this._inviteMultipleUsers(emailsFromCSV)} disabled={(isFetchingUsers || csvLoading)}>
                                {
                                    !this.state.isLoading &&
                                    this.context.t('my.network.invite-user')
                                }
                            </button>

                            {(csvLoading || isFetchingUsers) &&
                            <div className={"spinner-container"}>
                                <i className="fa fa-spinner fa-spin action-icon"/>
                            </div>
                            }

                        </div>
                    </div>
                </fieldset>
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
    membersInvited: PropTypes.func
};


const mapDispatchToProps = dispatch => {
    return {
        createOrganizationInvitation(orgId, email, admin) {
            return dispatch(createOrganizationInvitation(orgId, email, admin));
        }
    };
};

export default connect(null, mapDispatchToProps)(OrganizationInvitationForm);