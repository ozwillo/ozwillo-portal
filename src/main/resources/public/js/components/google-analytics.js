import React from "react";
import customFetch from '../util/custom-fetch'

const injectGA = (googleKey) => {
    if (typeof window == 'undefined') {
        return;
    }
    window.dataLayer = window.dataLayer || [];
    function gtag() {
        window.dataLayer.push(arguments);
    }
    gtag('js', new Date());

    gtag('config', googleKey);
};

export default class GoogleAnalytics extends React.Component{

    state = {
        isLoading: true,
        googleTag: ''
    };


    componentDidMount = async () => {
        //'UA-71967243-2'
        const {tag : googleTag} = await customFetch('/api/config/googleTag');
        this.setState({googleTag: googleTag, isLoading: false})
    };

    render(){
        const {isLoading, googleTag} = this.state;
        return (
            !isLoading &&
            <div data-th-if="${production}">
                {/* Global site tag (gtag.js) - Google Analytics */}
                <script
                    async
                    src={`https://www.googletagmanager.com/gtag/js?id=${googleTag}`}
                />
                <script>{injectGA(googleTag)}</script>
            </div>
        )
    }

}