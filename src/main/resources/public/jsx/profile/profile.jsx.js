'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import renderIf from "render-if";

const moment = require('moment');

import '../csrf';
import '../my';
import t from '../util/message';

import { Form, InputText, Select, SubmitButton } from '../util/form'

class MyProfile extends React.Component {
    state = {
        user: {}
    }
    componentDidMount() {
        $.ajax({
            url: profile_service
        })
        .done(data => this.setState({ user: data }))
        .fail((xhr, status, err) => {
            this.setState({ error : "Unable to retrieve profile info" })
        })
    }
    onValueChange(field, value) {
        const fields = this.state.user
        if (field.indexOf('.') == -1) {
            fields[field] = value
        } else {
            const splittedField = field.split('.')
            const parentObject = fields[splittedField[0]]
            parentObject[splittedField[1]] = value
        }
        this.setState({ user: fields })
    }
    onSubmit(e) {
        e.preventDefault()
        $.ajax({
            url: profile_service,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(this.state.user)
        })

    }
    render() {
        return (
            <div>
                <ProfileAccount user={this.state.user} onValueChange={this.onValueChange.bind(this)}
                                onSubmit={this.onSubmit.bind(this)} />
            </div>
        )
    }
}

class ProfileAccount extends React.Component {
    static propTypes = {
        user: React.PropTypes.object.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
        onSubmit: React.PropTypes.func.isRequired
    }
    state = {
        options: []
    }
    componentDidMount() {
        $.ajax({
            url: locale_service
        })
        .done(data => this.setState({ options: data }))
    }
    render() {
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.title.account')}</h2>
                </div>
                <Form id="profile-account" onSubmit={this.props.onSubmit}>
                    <InputText name="nickname" value={this.props.user.nickname} isRequired={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.personal.nickname')} />
                    <InputText name="email_address" value={this.props.user.email_address} isRequired={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.account.email')} />
                    <LanguageSelector value={this.props.user.locale} options={this.state.options} />
                    <SubmitButton label={t('ui.save')} />
                </Form>
            </div>
        )
    }
}

class LanguageSelector extends React.Component {
    static propTypes = {
        value: React.PropTypes.string,
        options: React.PropTypes.array.isRequired
    }
    onChange() {
        console.log("onChange")
    }
    render() {
        return (
            <Select name="language" value={this.props.value} onChange={this.onChange}
                    label={t('my.profile.account.language')}>
                { this.props.options.map(option =>
                    <option key={option} name={option}>{t('my.profile.account.language.' + option)}</option>)
                }
            </Select>
        )
    }
}

ReactDOM.render(<MyProfile />, document.getElementById("myprofile"));

module.exports = { MyProfile }
