import React from 'react';
import "../csrf";
import t from "../util/message";
import { SubmitButton } from "../util/form";


class FranceConnectBtn extends React.Component {

    static PropTypes = {
        userProfile: React.PropTypes.object.isRequired
    };

    render() {
        const userProfile = this.props.userProfile;

        return <form id="FranceConnectButton"
                     method="post" action="https://accounts.ozwillo-dev.eu/a/franceconnect/login"
                     className="form-horizontal">
            <div>
                <h2>France Connect</h2>
            </div>

            {
                userProfile.franceconnect_sub && userProfile.email_verified &&
               <a href="https://accounts.ozwillo-dev.eu/a/franceconnect/unlink" className="btn btn-lg oz-btn-danger">
                   {t("franceconnect.form.desynchronize")}
               </a>

            }

            {
                userProfile.franceconnect_sub && !userProfile.email_verified &&
                <a href="https://accounts.ozwillo-dev.eu/a/password" className="btn btn-lg btn-warning">
                    {t("franceconnect.form.desynchronize")}
                </a>
            }

            {
                !userProfile.franceconnect_sub && userProfile.email_verified &&
                <fieldset>
                    <input type="hidden" name="continue" value="http://localhost:3000/my/profile/franceconnect" />
                    <SubmitButton label={t("franceconnect.form.synchronise")} className="btn btn-lg btn-warning"/>
                </fieldset>
            }
        </form>;
    }
}

export default FranceConnectBtn;