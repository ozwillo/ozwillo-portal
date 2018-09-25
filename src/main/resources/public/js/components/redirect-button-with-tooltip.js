import * as React from "react";
import CustomTooltip from "./custom-tooltip";
import {Link, withRouter} from "react-router-dom";
import PropTypes from "prop-types";

export default class RedirectButtonWithTooltip extends React.PureComponent {

    pushToHistory = (link) => {
        withRouter(({history}) => history.push(link));
    };


    render() {
        const {tooltipTitle, link, children, ...rest} = this.props;
        return (
            <CustomTooltip title={tooltipTitle}>
                <div {...rest} onClick={() => this.pushToHistory(link)}>
                    {children}
                </div>
            </CustomTooltip>
        )
    }

}

RedirectButtonWithTooltip.propTypes = {
    tooltipTitle: PropTypes.string.isRequired,
    link: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired
};