import React from 'react';
import PropTypes from 'prop-types';


class OrganizationInvitationForm extends React.Component {

    static propTypes = {
        organization: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        //bind methods
        this.onSubmit = this.onSubmit.bind(this);
    }

    onSubmit(e) {
        e.preventDefault();


    }

    render() {
        return <form className="organization-invitation-form flex-row" onSubmit={this.onSubmit}>
            <label className="label">
                Email
                <input name="email" type="email" className="field"/>
            </label>


            <div className="options flex-row end">
                <button type="submit" className="btn icon"><i className="fa fa-paper-plane action-icon"/></button>
            </div>

        </form>;
    }

}

export default OrganizationInvitationForm;