'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import renderIf from "render-if";

const moment = require('moment');

import '../csrf';
import '../my';
import t from '../util/message';

import { Form, InputText, Select, SubmitButton } from '../util/form'

class Profile extends React.Component {
    state = {
        userProfile: {
            nickname: '',
            email_address: '',
            locale: ''
        },
        languages: [],
        passwordChangeEndpoint: ''
    }
    componentDidMount() {
        $.ajax({
            url: profile_service
        })
        .done(data => this.setState({ userProfile: data.userProfile, languages: data.languages, passwordChangeEndpoint: data.passwordChangeEndpoint }))
        .fail((xhr, status, err) => {
            this.setState({ error : "Unable to retrieve profile info" })
        })
    }
    onValueChange(field, value) {
        const fields = this.state.userProfile
        if (field.indexOf('.') == -1) {
            fields[field] = value
        } else {
            const splittedField = field.split('.')
            const parentObject = fields[splittedField[0]]
            parentObject[splittedField[1]] = value
        }
        this.setState({ userProfile: fields })
    }
    onSubmit(e) {
        e.preventDefault()
        $.ajax({
            url: profile_service,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(this.state.userProfile)
        })
        .done(() => this.setState({ updateSucceeded: true }))
    }
    render() {
        return (
            <div>
                {renderIf(this.state.updateSucceeded) (
                    <div>Les modifications ont été enregistrées !</div>
                )}
                <ProfileAccount userProfile={this.state.userProfile} languages={this.state.languages}
                                passwordChangeEndpoint={this.state.passwordChangeEndpoint}
                                onValueChange={this.onValueChange.bind(this)}
                                onSubmit={this.onSubmit.bind(this)} />
            </div>
        )
    }
}

class ProfileAccount extends React.Component {
    static propTypes = {
        userProfile: React.PropTypes.object.isRequired,
        languages: React.PropTypes.array.isRequired,
        passwordChangeEndpoint: React.PropTypes.string.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
        onSubmit: React.PropTypes.func.isRequired
    }
    render() {
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.title.account')}</h2>
                </div>
                <Form id="profile-account" onSubmit={this.props.onSubmit}>
                    <InputText name="nickname" value={this.props.userProfile.nickname} isRequired={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.personal.nickname')} />
                    <InputText name="email_address" value={this.props.userProfile.email_address} isRequired={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.account.email')} />
                    <PasswordLink passwordChangeEndpoint={this.props.passwordChangeEndpoint} />
                    <LanguageSelector value={this.props.userProfile.locale} languages={this.props.languages}
                                      onChange={e => this.props.onValueChange('locale', e.target.value)}/>
                    <SubmitButton label={t('ui.save')} />
                </Form>
            </div>
        )
    }
}

const PasswordLink = ({ passwordChangeEndpoint }) =>
    <div className="form-group">
        <div className="col-sm-9 col-sm-offset-3">
            <a className="change-password" href={passwordChangeEndpoint}>{t('my.profile.account.changepassword')}</a>
        </div>
    </div>

class LanguageSelector extends React.Component {
    static propTypes = {
        value: React.PropTypes.string,
        languages: React.PropTypes.array.isRequired,
        onChange: React.PropTypes.func.isRequired
    }
    render() {
        return (
            <Select name="language" value={this.props.value}
                    onChange={this.props.onChange}
                    label={t('my.profile.account.language')}>
                { this.props.languages.map(option =>
                    <option key={option} value={option}>{t('my.profile.account.language.' + option)}</option>)
                }
            </Select>
        )
    }
}

ReactDOM.render(<Profile />, document.getElementById("profile"));

module.exports = { Profile }
