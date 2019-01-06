'use strict';

import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import renderIf from 'render-if';
import moment from 'moment';
import Select from 'react-select';
import URLSearchParams from'url-search-params'

// Component
import FranceConnectForm from '../components/forms/france-connect-form';
import {
    Form,
    Label,
    InputText,
    SubmitButton,
    InputDatePicker,
    CountrySelector,
    GenderSelector
} from '../components/forms/form';
import GeoAreaAutosuggest from '../components/autosuggests/geoarea-autosuggest';
import UpdateTitle from '../components/update-title';
import customFetch from "../util/custom-fetch";
import { DropdownBlockSuccess } from '../components/notification-messages';

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"
import {i18nComponentInstance} from '../app';

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
            gender: ''
        },
        genders: [],
        languages: [],
        passwordChangeEndpoint: '',
        unlinkFranceConnectEndpoint: '',
        linkFranceConnectEndpoint: '',
        franceConnectEnabled: false
    };


    componentDidMount() {
        customFetch('/my/api/profile').then(data => this.setState(data));
    }

    onValueChange(field, value) {
        const fields = this.state.userProfile
        if (field.indexOf('.') === -1) {
            fields[field] = value
        } else {
            const splittedField = field.split('.')

            let parentObject = undefined
            if (fields[splittedField[0]] === undefined) {
                parentObject = {}
                fields[splittedField[0]] = parentObject
            } else {
                parentObject = fields[splittedField[0]]
            }
            parentObject[splittedField[1]] = value
        }
        this.setState({userProfile: fields})
    }

    onSubmit(e) {
        e.preventDefault();

        customFetch('/my/api/profile', {
            method: 'POST',
            json: this.state.userProfile
        })
        .then(() => {
            i18nComponentInstance.loadLanguage(this.state.userProfile.locale);

            this.setState({updateSucceeded: true});
            const { voluntaryClaims, essentialClaims } = getConditionalClaims(this.props.location.search);
            if (!!voluntaryClaims.length || !!essentialClaims.length) {
                window.opener.postMessage('updated', '*');
                window.close()
            }
            this.componentDidMount();
        })
    }

    render() {
        const userProfile = this.state.userProfile;
        const { voluntaryClaims, essentialClaims } = getConditionalClaims(this.props.location.search);
        return (
            <section id="profile">
                <header className="title">
                    <span>{i18n._(t`my.profile`)}</span>
                </header>
                <section className="box">
                    <Form id="account" onSubmit={this.onSubmit.bind(this)}>
                        <ProfileAccount userProfile={userProfile} languages={this.state.languages}
                                        onValueChange={this.onValueChange.bind(this)}
                                        voluntaryClaims={voluntaryClaims}
                                        essentialClaims={essentialClaims} />
                        <IdentityAccount userProfile={userProfile}
                                        onValueChange={this.onValueChange.bind(this)}
                                        voluntaryClaims={voluntaryClaims}
                                        essentialClaims={essentialClaims} />
                        <AddressAccount address={userProfile.address}
                                        onValueChange={this.onValueChange.bind(this)}
                                        voluntaryClaims={voluntaryClaims}
                                        essentialClaims={essentialClaims} />
                        <SubmitButton label={i18n._(t`ui.save`)} className="btn-lg"/>
                        {renderIf(this.state.updateSucceeded)(
                            <DropdownBlockSuccess successMessage={i18n._(t`my.profile.account.update`)}/>
                        )}
                    </Form>
                </section>

                {(!voluntaryClaims.length && !essentialClaims.length) &&
                    <Fragment>
                        <PasswordAccount passwordChangeEndpoint={this.state.passwordChangeEndpoint}
                                        passwordExist={!!userProfile.email_verified}/>
                        { this.state.franceConnectEnabled &&
                              <FranceConnectForm passwordChangeEndpoint={this.state.passwordChangeEndpoint}
                                        linkFranceConnectEndpoint={this.state.linkFranceConnectEndpoint}
                                        unlinkFranceConnectEndpoint={this.state.unlinkFranceConnectEndpoint}
                                        userProfile={userProfile} className="box"/>
                        }
                    </Fragment>
                }
            </section>
        )
    }
}

class ProfileAccount extends React.Component {
    static propTypes = {
        userProfile: PropTypes.object.isRequired,
        languages: PropTypes.array.isRequired,
        onValueChange: PropTypes.func.isRequired,
        voluntaryClaims: PropTypes.array,
        essentialClaims: PropTypes.array
    };
    static defaultProps = {
        voluntaryClaims: [],
        essentialClaims: []
    };


    constructor(props) {
        super(props);

        this.state = {
            options: this.createOptions(this.props.languages)
        };

        this.handleSelectChange = this.handleSelectChange.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({options: this.createOptions(nextProps.languages)});
    }

    createOptions = (languages) =>{
        const options = [];
        languages.forEach((lang) => {
            const label = i18n._(`my.profile.account.language.${lang}`);
            options.push({value: lang, label})
        });

        return options;
    }

    handleSelectChange(valueSelected) {
        this.props.onValueChange('locale', valueSelected.value);
    }

    render() {
        const { voluntaryClaims, essentialClaims } = this.props;
        return (
            <ConditionalClaimsForm voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} fields={['nickname']}>
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">{i18n._(t`my.profile.title.account`)}</legend>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims}>
                        <div className="flex-row">
                            <Label className="label" required>
                                {i18n._(t`my.profile.account.email`)}
                            </Label>
                            <span className="field">{this.props.userProfile.email_address}</span>
                        </div>
                    </ConditionalClaimsField>

                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='nickname'>
                        <InputText name="nickname" value={this.props.userProfile.nickname}
                                isRequired={conditionalClaimsRequired('nickname', true, essentialClaims)}
                                onChange={e => this.props.onValueChange('nickname', e.target.value)}
                                label={i18n._(t`my.profile.personal.nickname`)}/>
                    </ConditionalClaimsField>

                    {/*<LanguageSelector value={this.props.userProfile.locale} languages={this.props.languages}
                                    onChange={e => this.props.onValueChange('locale', e.target.value)}/>*/}
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims}>
                        <div className="flex-row">
                            <Label htmlFor="language" className="label" required>
                                {i18n._(t`my.profile.account.language`)}
                            </Label>
                            <Select className="select field" value={this.props.userProfile.locale}
                                    onChange={this.handleSelectChange} placeholder=""
                                    options={this.state.options} clearable={false} required={true}/>
                        </div>
                    </ConditionalClaimsField>
                </fieldset>
            </ConditionalClaimsForm>
        )
    }
}

class IdentityAccount extends React.Component {
    static propTypes = {
        userProfile: PropTypes.object.isRequired,
        onValueChange: PropTypes.func.isRequired,
        voluntaryClaims: PropTypes.array,
        essentialClaims: PropTypes.array
    };
    static defaultProps = {
        voluntaryClaims: [],
        essentialClaims: []
    };


    handleChange(date) {
        this.props.onValueChange('birthdate', moment(date).utc());
    }

    render() {
        const { voluntaryClaims, essentialClaims } = this.props;
        moment.locale(this.props.userProfile.locale);
        const birthdate = moment.utc(this.props.userProfile.birthdate);

        return (
            <ConditionalClaimsForm voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims}
                    fields={['given_name', 'middle_name', 'family_name', 'birthdate', 'phone_number', 'gender']}>
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">{i18n._(t`my.profile.personal.identity`)}</legend>

                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='given_name'>
                        <InputText name="given_name" value={this.props.userProfile.given_name}
                                isRequired={conditionalClaimsRequired('given_name', false, essentialClaims)}
                                onChange={e => this.props.onValueChange('given_name', e.target.value)}
                                label={i18n._(t`my.profile.personal.firstname`)}
                                disabled={this.props.userProfile.franceconnect_sub} />
                    </ConditionalClaimsField>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='middle_name'>
                        <InputText name="middle_name" value={this.props.userProfile.middle_name}
                                isRequired={conditionalClaimsRequired('middle_name', false, essentialClaims)}
                                label={i18n._(t`my.profile.personal.middlename`)}
                                onChange={e => this.props.onValueChange('middle_name', e.target.value)}
                                disabled={this.props.userProfile.franceconnect_sub} />
                    </ConditionalClaimsField>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='family_name'>
                        <InputText name="family_name" value={this.props.userProfile.family_name}
                                isRequired={conditionalClaimsRequired('family_name', false, essentialClaims)}
                                label={i18n._(t`my.profile.personal.lastname`)}
                                onChange={e => this.props.onValueChange('family_name', e.target.value)}
                                disabled={this.props.userProfile.franceconnect_sub} />
                    </ConditionalClaimsField>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='birthdate'>
                        <InputDatePicker name="birthdate" label={i18n._(t`my.profile.personal.birthdate`)}
                                required={conditionalClaimsRequired('birthdate', false, essentialClaims)}
                                onChange={this.handleChange.bind(this)} onSubmit={this.handleChange.bind(this)}
                                value={birthdate} dropdownMode="select"
                                disabled={this.props.userProfile.franceconnect_sub} />
                    </ConditionalClaimsField>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='phone_number'>
                        <InputText name="phone_number" value={this.props.userProfile.phone_number}
                                isRequired={conditionalClaimsRequired('phone_number', false, essentialClaims)}
                                label={i18n._(t`my.profile.personal.phonenumber`)}
                                onChange={e => this.props.onValueChange('phone_number', e.target.value)}/>
                    </ConditionalClaimsField>
                    <ConditionalClaimsField voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} field='gender'>
                        <GenderSelector value={this.props.userProfile.gender}
                                required={conditionalClaimsRequired('gender', false, essentialClaims)}
                                onChange={value => this.props.onValueChange('gender', value)}/>
                    </ConditionalClaimsField>
                </fieldset>
            </ConditionalClaimsForm>
        )
    }
}

class AddressAccount extends React.Component {
    static propTypes = {
        address: PropTypes.object,
        onValueChange: PropTypes.func.isRequired,
        voluntaryClaims: PropTypes.array,
        essentialClaims: PropTypes.array
    };
    static defaultProps = {
        address: {},
        voluntaryClaims: [],
        essentialClaims: []
    };

    constructor(props) {
        super(props);

        //bind methods
        this.onGeoAreaChange = this.onGeoAreaChange.bind(this);
        this.onGeoAreaSelected = this.onGeoAreaSelected.bind(this);
        this.onCountryChange = this.onCountryChange.bind(this);
    }

    onCountryChange(country) {
        //reset fields
        this.props.onValueChange('address.street_address', '');
        this.props.onValueChange('address.postal_code', '');
        this.props.onValueChange('address.locality', '');
        this.props.onValueChange('address.country', '');

        this.props.onValueChange('address.country', country.value)
    }


    onGeoAreaChange(e, value) {
        this.props.onValueChange('address.locality', value);
    }

    onGeoAreaSelected(e, locality) {
        this.props.onValueChange('address.locality', locality.name);
        this.props.onValueChange('address.postal_code', locality.postalCode);
    }

    render() {
        const { address, voluntaryClaims, essentialClaims } = this.props;
        const required = conditionalClaimsRequired('address', false, essentialClaims)
        return (
            <ConditionalClaimsForm voluntaryClaims={voluntaryClaims} essentialClaims={essentialClaims} fields={['address']}>
                <fieldset className="oz-fieldset">
                    <legend className="oz-legend">{i18n._(t`my.profile.personal.address`)}</legend>
                    <CountrySelector value={address.country || ''}
                            required={required}
                            onChange={this.onCountryChange}
                            url="/api/geo/countries"/>

                    {
                        address.country && [
                            <div key={`${address.country}_locality`} className="flex-row">
                                <Label className="label" required={required}>
                                    {i18n._(t`my.profile.personal.locality`)}
                                </Label>
                                <GeoAreaAutosuggest name="locality"
                                                    onGeoAreaSelected={this.onGeoAreaSelected}
                                                    onChange={this.onGeoAreaChange}
                                                    countryUri={address.country || ''}
                                                    endpoint="cities"
                                                    placeholder={i18n._(t`my.profile.personal.locality`)}
                                                    value={address.locality || ''}/>
                            </div>,

                            <InputText key={`${address.country}_postal_code`} name="address.postal_code"
                                    value={address.postal_code}
                                    isRequired={required}
                                    label={i18n._(t`my.profile.personal.postalcode`)}
                                    onChange={e => this.props.onValueChange('address.postal_code', e.target.value)}
                                    disabled={true}/>,

                            <InputText key={`${address.country}_street_address`} name="address.street_address"
                                    value={address.street_address}
                                    isRequired={required}
                                    label={i18n._(t`my.profile.personal.streetaddress`)}
                                    onChange={e => this.props.onValueChange('address.street_address', e.target.value)}/>
                        ]
                    }

                </fieldset>
            </ConditionalClaimsForm>
        )
    }
}

class PasswordAccount extends React.Component {
    static propTypes = {
        passwordChangeEndpoint: PropTypes.string.isRequired,
        passwordExist: PropTypes.bool
    };

    render() {
        return (
            <section className="box flex-col">
                <header className="sub-title">
                    {i18n._(t`my.profile.account.password`)}
                </header>

                <div className="text-center">
                    <a href={this.props.passwordChangeEndpoint}>
                        <span className="btn btn-default">
                            {this.props.passwordExist && i18n._(t`my.profile.account.changepassword`)}
                            {!this.props.passwordExist && i18n._(t`my.profile.account.createpassword`)}
                        </span>
                    </a>
                </div>
            </section>
        )
    }
}

const ConditionalClaimsField = ({ children, voluntaryClaims = [], essentialClaims = [], field }) =>
    ((!voluntaryClaims.length && !essentialClaims.length) || voluntaryClaims.includes(field) || essentialClaims.includes(field)) &&
        children;

const ConditionalClaimsForm = ({ children, voluntaryClaims = [], essentialClaims = [], fields }) =>
    ((!voluntaryClaims.length && !essentialClaims.length) || fields.some(item => voluntaryClaims.includes(item) || essentialClaims.includes(item))) &&
        children;

const conditionalClaimsRequired = (field, defaultRequired, essentialClaims = []) =>
    (!!essentialClaims.length && essentialClaims.includes(field)) || defaultRequired

const getConditionalClaims = (searchLocation) => {
    const searchParams = new URLSearchParams(searchLocation);
    const voluntaryClaims = searchParams.get('voluntary_claims') ? searchParams.get('voluntary_claims').split(' ') : [];
    const essentialClaims = searchParams.get('essential_claims') ? searchParams.get('essential_claims').split(' ') : [];
    return { voluntaryClaims, essentialClaims }
}

class ProfileWrapper extends React.Component {


    render() {
        return <section className="oz-body wrapper">
            <UpdateTitle title={i18n._(t`my.profile`)}/>
            <Profile {...this.props} />
            <div className="push"></div>
        </section>;
    }
}

export default ProfileWrapper;
