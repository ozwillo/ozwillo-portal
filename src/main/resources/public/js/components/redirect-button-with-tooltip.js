import * as React from "react";
import CustomTooltip from "./custom-tooltip";
import PropTypes from "prop-types";
import Redirect from "react-router/es/Redirect";

export default class RedirectButtonWithTooltip extends React.PureComponent {

    state = {
      isClicked: false
    };

    render() {
        const {tooltipTitle, link, children, ...rest} = this.props;
        const {isClicked} = this.state;
        let redirect = null;
        if(isClicked){
            redirect = <Redirect to={link}/>;
        }

        return (
            <CustomTooltip title={tooltipTitle}>
                {redirect}
                <div {...rest} onClick={() => this.setState({isClicked: true})}>
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