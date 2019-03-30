import React from "react";
import * as ReactGA from 'react-ga';
import { PropTypes } from 'prop-types';

ReactGA.initialize(localStorage.getItem("googleTag"));

export default class GoogleAnalytics extends React.Component {

    componentDidMount() {
        this.sendPageView(this.context.router.route.location.pathname,this.context.router.route.location.search);
    }

    componentWillReceiveProps(nextProps, nextContext) {
        // When props change, check if the URL has changed or not
        //play with life cycle to avoid multiple time calling sendPageView at the initialization
        if ((nextContext.router.route.location && this.context.router.route.location)
            && (this.context.router.route.location.pathname !== nextContext.router.route.location.pathname
                || this.context.router.route.location.search !== nextContext.router.route.location.search)) {
            this.sendPageView(nextContext.router.route.location.pathname,nextContext.router.route.location.search)
        }
    }

    sendPageView(pathname, search = "") {
        const location = pathname + search;
        ReactGA.set({ page: location });
        ReactGA.pageview(location);
    }

    render() {
        return this.props.children;
    }
}


GoogleAnalytics.contextTypes = {
    router: PropTypes.object
};

