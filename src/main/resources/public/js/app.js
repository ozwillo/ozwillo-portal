import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import Router from './config/router';
import customFetch from './util/custom-fetch';
import HtmlHead from './config/html-head';
import I18nConfig from './config/i18n-config';

export let i18nComponentInstance;

class App extends React.Component {

    state = {
        i18nLoaded: false,
        hostName: null,
        googleTag: null
    };


    componentDidMount = async () => {
        let res = await customFetch(`/api/env`);
        let config = await customFetch('/api/config');
        const googleTag = await customFetch('/api/config/googleTag');

        localStorage.setItem('googleTag', googleTag);
        this.setState({language: config.language, googleTag: googleTag})
        this.stockEnv(res);


        //Change css var, depends on the domain name
        const styleProperties = await customFetch('/api/config/style');
        styleProperties.map(cssVar => {
            document.body.style.setProperty(cssVar.key, cssVar.value);
        });
    };

    stockEnv = (env) => {
        localStorage.setItem('env', env);
        this.setState({hostName: env});
    };

    i18nLoaded = () => {
        this.setState({i18nLoaded: true})
    };


    render() {
        const {hostName, i18nLoaded, language, googleTag} = this.state;
        return (
            <I18nConfig loaded={this.i18nLoaded} env={hostName} language={language} ref={ref => i18nComponentInstance = ref}>
                {i18nLoaded && googleTag ?
                    <React.Fragment>
                        <BrowserRouter>
                            <Router/>
                        </BrowserRouter>
                        <HtmlHead env={hostName}/>
                    </React.Fragment>
                    : null
                }
            </I18nConfig>

        );
    }

}

export default App;
