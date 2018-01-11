'use strict';

import React from "react";
import ReactDOM from "react-dom";
import "../csrf";
import "../my";
import '../util/string-plugin';
import t from "../util/message";


const languageData = {
    'given_name': 'my.profile.personal.firstname',
    'middle_name': 'my.profile.personal.middlename',
    'family_name': 'my.profile.personal.lastname',
    'birthdate': 'my.profile.personal.birthdate',
    'gender': 'my.profile.personal.gender',
    'phone_number': 'my.profile.personal.phonenumber',
    'address': 'my.profile.personal.address'
};

/**
 * This class is used to update user's information with data from FranceConnect
 */
class LinkFCProfile extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            userProfile: null,
            franceConnectProfile: null,
            error: ''
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
            //Search different data between user profile and franceConnect
            const franceConnectProfile = {};
            Object.keys(data.franceConnectProfile).forEach((field) => {
                if(data.userProfile[field] !== data.franceConnectProfile[field] &&
                    field !== "displayName"){
                    franceConnectProfile[field] = data.franceConnectProfile[field];
                }
            });

            if(!Object.keys(franceConnectProfile).length){
                //No data to update
                window.location.assign('/my/profile');
            }

            this.setState({
                userProfile: data.userProfile,
                franceConnectProfile
            });

        })
        .fail(() => {
            this.setState({
                error: t('franceconnect.error.link-data')
            })
        })
    }

    save(e) {
        //cancel submit
        e.preventDefault();

        //get all data from form
        const formData = new FormData(this.refs.form);
        const jsonData = Object.assign({}, this.state.userProfile);

        for(let pair of formData.entries()){
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
        const franceConnectProfile = this.state.franceConnectProfile

        if(!userProfile && !this.state.error){
            return <section className="link-fc-profile">
                <i className="fa fa-spinner fa-spin spinner"></i>
            </section>
        }

        return <section className="link-fc-profile">

            {
                !this.state.error && <form ref="form" onSubmit={this.save}>
                    <div className="row">
                        <p className="item title">{t('franceconnect.form.your-profile')}</p>
                        <p className="item title">{t('franceconnect.name')}</p>
                    </div>

                    {
                        Object.keys(this.state.franceConnectProfile).map((item) => {
                            return <fieldset key={item}>
                                <legend>{t(languageData[item])}</legend>
                                {
                                    /* Field exist in Ozwillo account */
                                    userProfile[item] &&
                                    <div className="row">
                                        <label className="item">
                                            <input id={item} type="radio" name={item} value={userProfile[item]}/>
                                            {userProfile[item]}
                                        </label>

                                        <label className="item">
                                            <input id={item} type="radio" name={item} value={franceConnectProfile[item]}/>
                                            {franceConnectProfile[item]}
                                        </label>
                                    </div>
                                }

                                {
                                    /* Field not exist in Ozwillo account */
                                    !userProfile[item] &&
                                    <div className="row">
                                        <label className="item no-value">
                                            <p className="line">{t('franceconnect.form.field-not-inform').format(t(languageData[item]))}</p>
                                            <p className="line">{t('franceconnect.form.ask-update-field').format(t(languageData[item]))}</p>
                                        </label>

                                        <label className="item">
                                            <input id={item} type="checkbox" name={item} value={franceConnectProfile[item]}/>
                                            {franceConnectProfile[item]}
                                        </label>
                                    </div>
                                }

                            </fieldset>
                        })
                    }

                    <div className="row submit">
                        <input type="submit" value={t('ui.save')}  className="btn oz-btn-save"/>
                    </div>
                </form>
            }

            {
                this.state.error &&
                <div className="flex-col middle error-message">
                    <p className="item title">
                        {this.state.error}
                    </p>
                    <a href="/my/profile" className="btn oz-btn-ok">
                        {t('ui.confirm')}
                    </a>
                </div>

            }
        </section>
    }
}

ReactDOM.render(<LinkFCProfile />, document.getElementById("link-fc-profile"));
