import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import Router from './config/router';
import customFetch from "./util/custom-fetch";
import GoogleAnalytics from "./components/google-analytics";
import HtmlHead from './components/html-head';


class App extends React.Component {

    componentDidMount = async () => {
        //Change css var, depends on the domain name
        const styleProperties = await customFetch("/api/config/style");
        styleProperties.map(cssVar => {
            document.body.style.setProperty(cssVar.key, cssVar.value);
        });
    };

    render() {
        return (
            <React.Fragment>
                <BrowserRouter>
                    <Router/>
                </BrowserRouter>
                <GoogleAnalytics/>
                <HtmlHead/>
            </React.Fragment>
        );
    }

}

export default App;