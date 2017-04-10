'use strict';

import React from "react";
import ReactDOM from "react-dom";
import renderIf from "render-if";
import "../csrf";
import "../my";
import t from "../util/message";

import {Form, InputText, Select, SubmitButton, InputDatePicker} from "../util/form";
import {GeoAreaAutosuggest} from "../util/geoarea-autosuggest.jsx";

const moment = require('moment');

class Profile extends React.Component {
    state = {
        userProfile: {
            nickname: '',
            email_address: '',
            locale: '',
            given_name: '',
            middle_name: '',
            family_name: '',
            phone_number: '',
            gender: '',
        },
        genders: ['male','female'],
        languages: [],
        passwordChangeEndpoint: '',
    }
    componentDidMount() {
        $.ajax({
            url: profile_service
        })
        .done(
            function (data) {
                if (data.userProfile.birthdate) {
                    // console.log("brith - 1")
                    let dayofbirth = data.userProfile.birthdate
                    dayofbirth[1] = dayofbirth[1] - 1
                    data.userProfile.birthdate = dayofbirth
                } else {
                    data.userProfile.birthdate = undefined
                }
                this.setState({ userProfile: data.userProfile, languages: data.languages, passwordChangeEndpoint: data.passwordChangeEndpoint })
            }.bind(this)
            //data => this.setState({ userProfile: data.userProfile, languages: data.languages, passwordChangeEndpoint: data.passwordChangeEndpoint })
        )
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

            let parentObject = undefined
            if (fields[splittedField[0]] == null) {
                parentObject = {}
                fields[splittedField[0]] = parentObject
            } else {
                parentObject = fields[splittedField[0]]
            }
            parentObject[splittedField[1]] = value
        }
        this.setState({ userProfile: fields })
    }
    onSubmit(e) {
        e.preventDefault()
        if (this.state.userProfile.birthdate) {
            // console.log("brith + 1")
            let dayofbirth = this.state.userProfile.birthdate
            dayofbirth[1] = dayofbirth[1] + 1
            this.state.userProfile.birthdate = dayofbirth
        }
        $.ajax({
            url: profile_service,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(this.state.userProfile)
        })
        .done(function (data) {
            this.setState({ updateSucceeded: true })
            this.componentDidMount()
        }.bind(this))
        // .done(() => this.setState({ updateSucceeded: true }))
    }
    render() {
        let address = this.state.userProfile.address
        if (address == null) {
            address = undefined
        }
        return (
            <div>
                <Form id="account" onSubmit={this.onSubmit.bind(this)}>
                    {renderIf(this.state.updateSucceeded) (
                        <div className="alert alert-success" role="alert">
                            <button type="button" className="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            Les modifications ont été enregistrées !
                        </div>
                    )}
                    {/*<SubmitButton label={t('ui.save')} />*/}
                    <ProfileAccount userProfile={this.state.userProfile} languages={this.state.languages}
                                    onValueChange={this.onValueChange.bind(this)}
                                     />
                    <IdentityAccount userProfile={this.state.userProfile} genders={this.state.genders}
                                     onValueChange={this.onValueChange.bind(this)}
                                     startDate={this.state.startDate} />
                    <AddressAccount address={address}
                                    onValueChange={this.onValueChange.bind(this)}
                                     />
                </Form>
                <PasswordAccount passwordChangeEndpoint={this.state.passwordChangeEndpoint} />

            </div>
        )
    }
}

class ProfileAccount extends React.Component {
    static propTypes = {
        userProfile: React.PropTypes.object.isRequired,
        languages: React.PropTypes.array.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
    }
    render() {
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.title.account')}</h2>
                </div>
                    <InputText name="nickname" value={this.props.userProfile.nickname} isRequired={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.personal.nickname')} />
                    <InputText name="email_address" value={this.props.userProfile.email_address} isRequired={true} disabled={true}
                               onChange={e => this.props.onValueChange('nickname', e.target.value)}
                               label={t('my.profile.account.email')} />
                    <LanguageSelector value={this.props.userProfile.locale} languages={this.props.languages}
                                      onChange={e => this.props.onValueChange('locale', e.target.value)}/>
                    <SubmitButton label={t('ui.save')} />
            </div>
        )
    }
}

class IdentityAccount extends React.Component {
    static propTypes = {
        userProfile: React.PropTypes.object.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
        genders: React.PropTypes.array.isRequired,
    }
    state = {
        birthdate: undefined,
    }

    handleChange(date) {
        date = moment.utc(date).add(3, 'hours')
        // date = moment.tz("Europe/Paris")
        this.props.onValueChange('birthdate', date)
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.userProfile.birthdate) {
            this.state.birthdate = moment(nextProps.userProfile.birthdate)
        } else {
            this.state.birthdate = undefined
        }
    }

    render () {
        moment.utc(this.state.birthdate)
        moment.locale(this.props.userProfile.locale)
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.personal.identity')}</h2>
                </div>
                <InputText name="given_name" value={this.props.userProfile.given_name}
                           onChange={e => this.props.onValueChange('given_name', e.target.value)}
                           label={t('my.profile.personal.firstname')} />
                <InputText name="middle_name" value={this.props.userProfile.middle_name}
                           label={t('my.profile.personal.middlename')}
                           onChange={e => this.props.onValueChange('middle_name', e.target.value)} />
                <InputText name="family_name" value={this.props.userProfile.family_name}
                           label={t('my.profile.personal.lastname')}
                           onChange={e => this.props.onValueChange('family_name', e.target.value)} />
                <InputDatePicker name="birthdate" label={t('my.profile.personal.birthdate')}
                                 onChange={this.handleChange.bind(this)} startDate={this.state.birthdate} />
                <InputText name="phone_number" value={this.props.userProfile.phone_number}
                           label={t('my.profile.personal.phonenumber')}
                           onChange={e => this.props.onValueChange('phone_number', e.target.value)} />
                <GenderSelector value={this.props.userProfile.gender} genders={this.props.genders}
                                onChange={e => this.props.onValueChange('gender', e.target.value)} />
                <SubmitButton label={t('ui.save')} />
            </div>
        )
    }
}

class AddressAccount extends React.Component {
    static propTypes = {
        address: React.PropTypes.object,
        onValueChange: React.PropTypes.func.isRequired,
    }
    static defaultProps = {
        address: {
            country: '',
            locality: '',
            postal_code: '',
            street_address: ''
        }
    }
    handleChange(locality) {
        this.props.onValueChange('address.locality', locality.name)
        this.props.onValueChange('address.postal_code', locality.postalCode)
    }
    render () {
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.personal.address')}</h2>
                </div>
                <CountrySelector value={this.props.address.country}
                                 onChange={e => this.props.onValueChange('address.country', e.target.value)}
                                 url={store_service + "/dc-countries"} />
                <div className='form-group'>
                    <label className="control-label col-sm-3">
                        {t('my.profile.personal.locality')}
                    </label>
                    <div className="col-sm-7">
                        <GeoAreaAutosuggest onChange={this.handleChange.bind(this)}
                                            countryUri={this.props.address.country}
                                            endpoint="/dc-cities" placeholder={t('my.profile.personal.locality')}
                                            initialValue={this.props.address.locality} />
                    </div>
                </div>

                <InputText name="address.postal_code" value={this.props.address.postal_code}
                           label={t('my.profile.personal.postalcode')}
                           onChange={e => this.props.onValueChange('address.postal_code', e.target.value)} />

                <InputText name="address.street_address" value={this.props.address.street_address}
                           label={t('my.profile.personal.streetaddress')}
                           onChange={e => this.props.onValueChange('address.street_address', e.target.value)} />
                <SubmitButton label={t('ui.save')} />
            </div>
        )
    }
}

class PasswordAccount extends React.Component {
    static propTypes = {
        passwordChangeEndpoint: React.PropTypes.string.isRequired
    }
    render() {
        return (
            <div className="row">
                <div className="col-sm-12">
                    <h2>{t('my.profile.account.password')}</h2>
                </div>
                <div className="col-sm-12">
                    <PasswordLink passwordChangeEndpoint={this.props.passwordChangeEndpoint} />
                </div>
            </div>
        )
    }
}

const PasswordLink = ({ passwordChangeEndpoint }) =>
    <div className="form-group">
        <div className="col-sm-9 col-sm-offset-3">
            <a className="change-password btn btn-lg btn-warning" href={passwordChangeEndpoint}>{t('my.profile.account.changepassword')}</a>
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

class CountrySelector extends React.Component {
    static propTypes = {
        value: React.PropTypes.string,
        url: React.PropTypes.string.isRequired,
        onChange: React.PropTypes.func.isRequired,
    }
    state = {
        countries: []
    }
    componentWillMount () {
        // get countries from DC
        $.ajax({
            url: this.props.url,
            type: 'get',
            dataType: 'json',
            data: {q: ' '}
        })
        .done(data => {
            let areas = data.areas.filter(function (n) {
                return n !== null;
            });
            let options = [];
            for (let i = 0; i < areas.length; i++) {
                options.push({key:i ,value: areas[i].uri, label: areas[i].name})
            }
            this.setState({ countries: options });
            // this.getUserCountry();
        })
        .fail((xhr, status, err) => {
                this.setState({ error : "Unable to retrieve countries info" + err.toString() })
            }
        )
    }
    render() {
        return (
            <Select name="country" value={this.props.value}
                    onChange={this.props.onChange}
                    label={t('my.profile.personal.country')}>
                {this.state.countries.map(option =>
                    <option key={option.key} value={option.value}>{option.label}</option>)
                }
            </Select>
        )
    }
}

class GenderSelector extends React.Component {
    static propTypes = {
        value: React.PropTypes.string,
        genders: React.PropTypes.array.isRequired,
        onChange: React.PropTypes.func.isRequired
    }
    render() {
        return (
            <Select name="gender" value={this.props.value}
                    label={t('my.profile.personal.gender')}
                    onChange={this.props.onChange}>
                {
                    this.props.genders.map(option =>
                        <option key={option} value={option}>{t('my.profile.personal.gender.' + option)}</option>
                    )
                }
            </Select>
        )
    }
}

ReactDOM.render(<Profile />, document.getElementById("profile"));

module.exports = { Profile }
