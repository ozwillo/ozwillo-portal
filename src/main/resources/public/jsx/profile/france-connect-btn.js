import React from 'react';
import "../csrf";
import t from "../util/message";
import { SubmitButton } from "../util/form";


class FranceConnectBtn extends React.Component {

    static PropTypes = {
        userProfile: React.PropTypes.object.isRequired,
        passwordChangeEndpoint: React.PropTypes.string,
        unlinkFranceConnectEndpoint: React.PropTypes.string,
        linkFranceConnectEndpoint: React.PropTypes.string
    };

    render() {
        const userProfile = this.props.userProfile || {};

        return <form id="FranceConnectButton"
                     method="post" action={this.props.linkFranceConnectEndpoint}
                     className="form-horizontal">
            <div>
                <h2>France Connect</h2>
            </div>
            <div className="flex-row middle">
                {
                    userProfile.franceconnect_sub && userProfile.email_verified &&
                   <a href={this.props.unlinkFranceConnectEndpoint} className="btn btn-lg oz-btn-danger">
                       {t("franceconnect.form.unlink")}
                   </a>

                }

                {
                    userProfile.franceconnect_sub && !userProfile.email_verified &&
                    <a href={this.props.passwordChangeEndpoint} className="btn btn-lg btn-warning">
                        {t("franceconnect.form.unlink-without-pwd")}
                    </a>
                }

                {
                    !userProfile.franceconnect_sub && userProfile.email_verified &&
                    <fieldset>
                        <input type="hidden" name="continue"
                               value={`${window.location.origin}/my/profile/franceconnect`}/>
                        <SubmitButton label={t("franceconnect.form.link")} className="btn btn-lg btn-warning"/>
                    </fieldset>
                }
            </div>
        </form>;
    }
}

export default FranceConnectBtn;