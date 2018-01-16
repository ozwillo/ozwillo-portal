import React from 'react';
import PropTypes from 'prop-types';

import { sendInvitationToJoinOrganization } from '../../actions/invitation';

class OrganizationInvitationForm extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            email: '',
            isLoading: false,
            error: ''
        };

        //bind methods
        this.handleChange = this.handleChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    handleChange(e) {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.value
        });
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({ isLoading: true });
        sendInvitationToJoinOrganization(this.state.email, this.props.organization.id)
            .then(() => {
                this.setState({
                    email: '',
                    isLoading: false,
                    error: ''
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
        return <form className="organization-invitation-form flex-row" onSubmit={this.onSubmit}>
            <fieldset className="flew-col">
                <legend className="legend">Invite a new collaborator</legend>
                <div className="flex-row">
                    <label className="label">
                        Email
                        <input name="email" type="email" className="field form-control" required={true}
                            onChange={this.handleChange} value={this.state.email}/>
                    </label>


                    <div className="options flex-row end">
                        <button type="submit" className="btn icon" disabled={this.state.isLoading}>
                            {
                                !this.state.isLoading &&
                                <i className="fa fa-paper-plane action-icon"/>
                            }
                            {

                                this.state.isLoading &&
                                <i className="fa fa-spinner fa-spin action-icon"/>
                            }
                        </button>
                    </div>
                </div>
                <div className="error">
                    <span>{this.state.error}</span>
                </div>
            </fieldset>
        </form>;
    }

}

export default OrganizationInvitationForm;