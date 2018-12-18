import React from "react";
import customFetch from '../util/custom-fetch'
import * as ReactGA from 'react-ga';
import { PropTypes } from 'prop-types';


// const injectGA = (googleKey) => {
//     if (typeof window == 'undefined') {
//         return;
//     }
//     window.dataLayer = window.dataLayer || [];
//     function gtag() {
//         window.dataLayer.push(arguments);
//     }
//     gtag('js', new Date());
//
//     gtag('config', googleKey);
// };
//
// export default class GoogleAnalytics extends React.Component{
//
//     state = {
//         isLoading: true,
//         googleTag: ''
//     };
//
//
//     componentDidMount = async () => {
//         //'UA-71967243-2'
//         const {tag : googleTag} = await customFetch('/api/config/googleTag');
//         this.setState({googleTag: googleTag, isLoading: false})
//     };
//
//     render(){
//         const {isLoading, googleTag} = this.state;
//         return (
//             !isLoading &&
//             <div data-th-if="${production}">
//                 {/* Global site tag (gtag.js) - Google Analytics */}
//                 <script
//                     async
//                     src={`https://www.googletagmanager.com/gtag/js?id=${googleTag}`}
//                 />
//                 <script>{injectGA(googleTag)}</script>
//             </div>
//         )
//     }
//
// }



export default class GoogleAnalytics extends React.Component {
    static contextTypes = {
        router: PropTypes.object
    };

    componentDidMount = async () => {
        const {tag : googleTag} = await customFetch('/api/config/googleTag');
        ReactGA.ga("create", googleTag, 'auto', 'tracker1');
        this.sendPageView(this.context.router.history.location);
        this.context.router.history.listen(this.sendPageView);
    }

    sendPageView(location) {
        ReactGA.set({ page: location.pathname });
        ReactGA.pageview(location.pathname);
    }

    render() {
        return this.props.children;
    }
}