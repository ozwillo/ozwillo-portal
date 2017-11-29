'use strict';

import React from "react";
import ReactDOM from "react-dom";
import "../csrf";
import "../my";
import t from "../util/message";

import {
    Form,
    InputText
} from "../util/form";

/**
 * This class is used to update user's information with data from FranceConnect
 */
class SynchronizeFCProfile extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            userProfile: {
                given_name: 'toto',
                family_name: 'toto',
                email_address: 'tata@tata',
                gender: 'toto',
                phone_number: '0000000000'
            },
            franceConnectProfile: {
                nickname: '',
                given_name: 'tata',
                family_name: 'tata',
                email_address: 'tata@tata',
                gender: 'tata',
                phone_number: '1111111111'
            }
        };

        //bind functions
        this.save = this.save.bind(this);
    }

    componentDidMount() {
        //Load FranceConnect data
        $.ajax({
            url: franceconnect_service
        })
        .done(data => {
            console.log(data);
            this.setState({ 
                userProfile: data.userProfile,
                franceConnectProfile: data.franceConnectProfile
            });
        })
        .fail((xhr, status, err) => {
            console.error(err);
        })
    }

    save(e) {
        //cancel submit
        e.preventDefault();

        //get all data from form
        const formData = new FormData(this.refs.form);
        const jsonData = Object.assign({}, this.state.userProfile);

        for(let pair of formData.entries()){
            console.log(pair[0], ' ', pair[1]);
            jsonData[pair[0]] = pair[1];
        }

        //Update user's data
        $.ajax({
            url: profile_service,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(jsonData)
        }).done(() => {
            window.location.assign('/my/profile');
        }).fail((xhr, status, err) => {
            console.error('Update user\'s data: ', err);
        })

    }

    render() {
        const userProfile = this.state.userProfile;
        const franceConnectProfile = this.state.franceConnectProfile;

        return <section className="synchronize-fc-profile">
            <form ref="form" onSubmit={this.save}>
                <fieldset>
                    <legend>{t('my.profile.personal.firstname')}</legend>
                    <div className="row">
                        <label className="item">
                            <input id="givenName" type="radio" name="given_name" value={userProfile.given_name}/>
                            {userProfile.given_name}
                        </label>
                        <label className="item">
                            <input id="givenName" type="radio" name="given_name" value={franceConnectProfile.given_name}/>
                            {franceConnectProfile.given_name}
                        </label>
                    </div>
                </fieldset>

                <fieldset>
                    <legend>{t('my.profile.personal.lastname')}</legend>
                    <div className="row">
                        <label className="item">
                            <input id="familyName" type="radio" name="family_name" value={userProfile.family_name}/>
                            {userProfile.family_name}
                        </label>

                        <label className="item">
                            <input id="familyName" type="radio" name="family_name" value={franceConnectProfile.family_name}/>
                            {franceConnectProfile.family_name}
                        </label>
                    </div>
                </fieldset>

                <fieldset>
                    <legend>{t('my.profile.account.email')}</legend>
                    <div className="row">
                        <label className="item">
                            <input id="email_address" type="radio" name="email_address" value={userProfile.email_address}/>
                            {userProfile.email_address}
                        </label>

                        <label className="item">
                            <input id="email_address" type="radio" name="email_address" value={franceConnectProfile.email_address}/>
                            {franceConnectProfile.email_address}
                        </label>
                    </div>
                </fieldset>

                <fieldset>
                    <legend>{t('my.profile.personal.gender')}</legend>
                    <div className="row">
                        <label className="item">
                            <input id="gender" type="radio" name="gender" value={userProfile.gender}/>
                            {userProfile.gender}
                        </label>


                        <label className="item">
                            <input id="gender" type="radio" name="gender" value={franceConnectProfile.gender}/>
                            {franceConnectProfile.gender}
                        </label>
                    </div>
                </fieldset>

                <fieldset>
                    <legend>{t('my.profile.personal.phonenumber')}</legend>
                    <div className="row">
                        <label className="item">
                            <input id="phone_number" type="radio" name="phone_number" value={userProfile.phone_number}/>
                            {userProfile.phone_number}
                        </label>

                        <label className="item">
                            <input id="phone_number" type="radio" name="phone_number" value={franceConnectProfile.phone_number}/>
                            {franceConnectProfile.phone_number}
                        </label>
                    </div>
                </fieldset>

                <div className="row submit">
                    <input type="submit" value="save" className="btn oz-btn-save"/>
                </div>
            </form>
        </section>
    }
}

ReactDOM.render(<SynchronizeFCProfile />, document.getElementById("synchronize-fc-profile"));
