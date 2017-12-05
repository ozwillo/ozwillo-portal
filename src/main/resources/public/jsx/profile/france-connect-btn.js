import React from 'react';
import PropTypes from 'prop-types';
import "../csrf";
import t from "../util/message";
import { SubmitButton } from "../util/form";


class FranceConnectBtn extends React.Component {

    static propTypes = {
        userProfile: PropTypes.object.isRequired,
        passwordChangeEndpoint: PropTypes.string,
        unlinkFranceConnectEndpoint: PropTypes.string,
        linkFranceConnectEndpoint: PropTypes.string
    };

    render() {
        const userProfile = this.props.userProfile || {};

        return <form id="FranceConnectButton"
                     method="post" action={this.props.linkFranceConnectEndpoint}
                     className="form-horizontal">
            <div>
                <h2>France Connect</h2>
            </div>

            {
                userProfile.franceconnect_sub && userProfile.email_verified &&
               <a href={this.props.unlinkFranceConnectEndpoint} className="btn btn-lg oz-btn-danger">
                   {t("franceconnect.form.desynchronize")}
               </a>

            }

            {
                userProfile.franceconnect_sub && !userProfile.email_verified &&
                <a href={this.props.passwordChangeEndpoint} className="btn btn-lg btn-warning">
                    {t("franceconnect.form.desynchronize-without-pwd")}
                </a>
            }

            {
                !userProfile.franceconnect_sub && userProfile.email_verified &&
                <fieldset>
                    <input type="hidden" name="continue"
                           value={`${window.location.origin}/my/profile/franceconnect`}/>
                    <SubmitButton label={t("franceconnect.form.synchronise")} className="btn btn-lg btn-warning"/>
                </fieldset>
            }
        </form>;
    }
}

export default FranceConnectBtn;