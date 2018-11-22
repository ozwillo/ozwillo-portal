import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import Router from './config/router';
import {I18nProvider} from "@lingui/react";
import customFetch from "./util/custom-fetch";
import GoogleAnalytics from "./components/google-analytics";
import HtmlHead from './components/html-head';
import {setupI18n} from "@lingui/core"

import catalogEn from '@lingui/loader!../locales/en/messages.json'
import catalogFr from '@lingui/loader!../locales/fr/messages.json'

export const i18n = setupI18n();
// mind the `en` key
i18n.load({
    en: catalogEn,
    fr: catalogFr
});


class App extends React.Component {

    componentDidMount(){
        i18n.activate("en");

        //Change css var, depends on the domain name
        const styleProperties = await customFetch("/api/config/style");
        styleProperties.map(cssVar => {
            document.body.style.setProperty(cssVar.key, cssVar.value);
        });
    }

    render() {
        return (
            <I18nProvider i18n={i18n}>
                <BrowserRouter>
                    <Router/>
                </BrowserRouter>
                <GoogleAnalytics/>
                <HtmlHead/>
            </I18nProvider>
        )
            ;
    }

}

export default App;