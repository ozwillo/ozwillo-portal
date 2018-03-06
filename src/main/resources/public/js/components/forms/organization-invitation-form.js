import React from 'react';
import PropTypes from 'prop-types';

import { sendInvitationToJoinOrganization } from '../../actions/invitation';

class OrganizationInvitationForm extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired,
        hideTitle: PropTypes.bool
    };

    static defaultProps = {
        hideTitle: false
    };

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            email: '',
            isLoading: false,
            success: '',
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
        return <form className="organization-invitation-form flex-row" onSubmit={this.onSubmit}>
            <fieldset className="flex-col">
                <legend className={`legend ${this.props.hideTitle && 'hidden' || ''}`}>
                    {this.context.t('organization.form.invite-new-collaborator')}
                </legend>
                <div className="flex-row">
                    <label className="label">
                        {this.context.t('organization.form.email')}
                        <input name="email" type="email" className="field form-control" required={true}
                            onChange={this.handleChange} value={this.state.email}/>
                    </label>


                    <div className="options flex-row end">
                        <button type="submit" className="btn btn-submit icon" disabled={this.state.isLoading}>
                            {
                                !this.state.isLoading &&
                                this.context.t('ui.send')
                            }
                            {

                                this.state.isLoading &&
                                <i className="fa fa-spinner fa-spin action-icon loading"/>
                            }
                        </button>
                    </div>
                </div>

                {
                    this.state.error &&
                    <div className="error">
                        <span>{this.state.error}</span>
                    </div>
                }

                {
                    this.state.success &&
                    <div className="success">
                        <span>{this.state.success}</span>
                    </div>
                }
            </fieldset>
        </form>;
    }

}

export default OrganizationInvitationForm;