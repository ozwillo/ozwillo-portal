import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';

//Action
import InstanceService from "../../util/instance-service";
import { i18n } from "../../config/i18n-config"
import NotificationMessageBlock from '../notification-message-block';

export default class InstanceInvitationForm extends React.Component {

    static propTypes = {
        instance: PropTypes.object.isRequired,
        members: PropTypes.array,
        sendInvitation: PropTypes.func
    };

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
            email: '',
            isLoading: false,
            error: '',
            success: ''
        };

        this._instanceService = new InstanceService();
    }

    onOptionChange = (selectedOption) => {
        this.setState({selectedOption: selectedOption})
    };

    handleChange = (e) => {
        const el = e.currentTarget;
        this.setState({
            [el.name]: el.type === 'checkbox' ? el.checked : el.value
        });
    };

    onSubmit = async (e) => {
        e.preventDefault();

        this.setState({isLoading: true, success: ''});

        const user = this.state.selectedOption || {email: this.state.email};
        const response = await this.props.sendInvitation(user);

        if (!response.error) {
            this.setState({
                isLoading: false,
                selectedOption: null,
                email: '',
                success: i18n._('ui.request.send'),
                error: ''
            });
        } else {
            console.error(response.message);
            this.setState({
                isLoading: false,
                success: '',
                error: response.message
            });
        }
    };

    _formatMembers = () => {
        const {members} = this.props;
        if (members) {
            return members.map(member => {
                if (member.name) {
                    return {
                        ...member,
                        displayedInfo: member.name + ' - ' + member.email
                    }
                } else {
                    return {
                        ...member,
                        displayedInfo: member.email
                    }
                }
            });
        } else {
            return null;
        }
    };

    render() {
        const formattedMembers = this._formatMembers();

        return <form className={`instance-invitation-form flex-col end ${this.props.className || ''}`}
                     onSubmit={this.onSubmit}>
            <div className={'instance-invitation-title'}>{i18n._('organization.desc.add-user-to-instance')}</div>
            <div className="content">
                <label className="label">
                    {i18n._('organization.desc.add-to-instance-from-members')}
                </label>
                {this.props.members ?
                    <Select
                        className="select add-member-instance-select"
                        name="members"
                        value={this.state.selectedOption}
                        labelKey="displayedInfo"
                        valueKey="id"
                        onChange={this.onOptionChange}
                        options={formattedMembers}
                        placeholder={i18n._('organization.desc.members')}
                        required={!this.state.email}/>
                    :
                    <div className="container-loading text-center">
                        <i className="fa fa-spinner fa-spin loading"/>
                    </div>
                }

                <em className="sep-text">{i18n._('ui.or')}</em>

                <div className="new-user-fieldset flex-row">
                    <label className="label">
                        {i18n._('organization.desc.add-to-instance-by-email')}
                        <input name="email" type="email" className="form-control field"
                               required={!this.state.selectedOption}
                               value={this.state.email} onChange={this.handleChange}/>
                    </label>
                </div>

                <button type="submit" className="btn btn-submit" disabled={this.state.isLoading}>
                    {
                        !this.state.isLoading &&
                        i18n._('ui.invite')
                    }

                    {
                        this.state.isLoading &&
                        <i className="fa fa-spinner fa-spin send-icon loading"/>
                    }
                </button>
            </div>

            <NotificationMessageBlock type={this.state.error ? 'danger' : 'success'}
                                      display={this.state.error !== '' || this.state.success !== ''}
                                      close={() => this.setState({error: '', success: ''})}
                                      message={this.state.error ? this.state.error : this.state.success}/>
        </form>;
    }

}
