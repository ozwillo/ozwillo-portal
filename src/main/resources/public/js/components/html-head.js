import React from 'react';
import {Helmet} from 'react-helmet';
import customFetch from '../util/custom-fetch';

export default class HtmlHead extends React.PureComponent {

    state = {
        hostName: null
    };

    componentDidMount = async () => {
        let res = await customFetch(`/api/env`);
        this.stockEnv(res);
    };

    stockEnv = (env) => {
        localStorage.setItem("env", env);
        this.setState({hostName: env});
    };


    render() {
        const {hostName} = this.state;
        return (
            <React.Fragment>
                {hostName ?
                    <Helmet>
                        <link rel="apple-touch-icon" sizes="180x180"
                              href={`/img/favicons/${hostName}/apple-touch-icon.png`}/>
                        <link rel="icon" type="image/png" sizes="32x32"
                              href={`/img/favicons/${hostName}/favicon-32x32.png`}/>
                        <link rel="icon" type="image/png" sizes="16x16"
                              href={`/img/favicons/${hostName}/favicon-16x16.png`}/>
                        <link rel="manifest" href={`/img/favicons/${hostName}/manifest.json`}/>
                        <link rel="mask-icon" href={`/img/favicons/${hostName}/safari-pinned-tab.svg" color="#5bbad5`}/>
                    </Helmet>
                    : null
                }
            </React.Fragment>
        )

    }


}