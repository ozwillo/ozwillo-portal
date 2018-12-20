import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import Router from './config/router';
import {I18nProvider} from '@lingui/react';
import customFetch from './util/custom-fetch';
import HtmlHead from './components/html-head';
import {setupI18n} from '@lingui/core'

import catalogEn from '@lingui/loader!../locales/en/messages.json';
import catalogFr from '@lingui/loader!../locales/fr/messages.json';
import catalogEs from '@lingui/loader!../locales/es/messages.json';
import catalogCa from '@lingui/loader!../locales/ca/messages.json';
import catalogBg from '@lingui/loader!../locales/bg/messages.json';
import catalogTr from '@lingui/loader!../locales/tr/messages.json';

export const i18n = setupI18n();

// mind the `en` key


class App extends React.Component {

    state = {
        i18nLoaded: false,
        googleTag: null
    };

    componentDidMount = async () => {
        await i18n.load({
            en: catalogEn,
            fr: catalogFr,
            es: catalogEs,
            ca: catalogCa,
            bg: catalogBg,
            tr: catalogTr
        });
        await i18n.activate('en');

        const {tag: googleTag} = await customFetch('/api/config/googleTag');
        localStorage.setItem('googleTag', googleTag);

        this.setState({i18nLoaded: true, googleTag: googleTag});
        //Change css var, depends on the domain name
        const styleProperties = await customFetch('/api/config/style');
        styleProperties.map(cssVar => {
            document.body.style.setProperty(cssVar.key, cssVar.value);
        });
    };

    render() {
        if (!this.state.i18nLoaded || !this.state.googleTag) {
            return null;
        }


        return (
            <I18nProvider i18n={i18n}>
                <BrowserRouter>
                    <Router/>
                </BrowserRouter>
                <HtmlHead/>
            </I18nProvider>

        );
    }

}

export default App;
