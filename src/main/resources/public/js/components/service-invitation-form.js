import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

class ServiceInvitationForm extends React.Component {

    static propTypes = {
        service: PropTypes.object.isRequired,
        members: PropTypes.array.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
            options: this.createOptions(this.props.members)
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.createOptions = this.createOptions.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            options: this.createOptions(nextProps.members)
        });
    }

    onOptionChange(selectedOption) {
        this.setState({ selectedOption: selectedOption.value })
    }

    createOptions(members) {
        const options = [];

        members.forEach((member) => {
            options.push({
                value: member.id,
                label: `${member.firstname} ${member.lastname}`
            });
        });

        return options;
    }

    onSubmit(e) {
        e.preventDefault();
    }

    render() {
        return <form onSubmit={this.onSubmit} className={`service-invitation-form flex-row end ${this.props.className || ''}`}>
            <div className="content flex-row">
                <Select
                    className="select"
                    name="members"
                    value={this.state.selectedOption}
                    onChange={this.onOptionChange}
                    options={this.state.options}
                    clearable={false}
                    placeholder="Members" />

                <em className="sep-text">or</em>

                <div className="new-user-fieldset flex-row">
                    <label className="label">
                        Email
                        <input name="email" type="email" className="field"/>
                    </label>


                    <label className="label">
                        <input name="addInOrganization" type="checkbox" className="field"/>
                        Add in organization
                    </label>
                </div>
            </div>


            <button type="submit" className="submit btn icon">
                <i className="fa fa-paper-plane send-icon"/>
            </button>
        </form>;
    }

}

export default ServiceInvitationForm;