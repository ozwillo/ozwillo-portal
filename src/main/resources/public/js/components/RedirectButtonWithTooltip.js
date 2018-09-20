import * as React from "react";
import CustomTooltip from "./custom-tooltip";
import {Link} from "react-router-dom";
import PropTypes from "prop-types";

export default class RedirectButtonWithTooltip extends React.PureComponent {


    render() {
        const {tooltipTitle, link, children, ...rest} = this.props;
        return (
            <CustomTooltip title={tooltipTitle}>
                <Link className="btn icon" to={link} {...rest}>
                    {children}
                </Link>
            </CustomTooltip>
        )
    }

}

RedirectButtonWithTooltip.propTypes = {
    tooltipTitle: PropTypes.string.isRequired,
    link: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired
};