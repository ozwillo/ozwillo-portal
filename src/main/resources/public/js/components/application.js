import React from "react";
import PropTypes from "prop-types";
import { withRouter } from "react-router-dom";
import { i18n } from "../config/i18n-config";
import { t } from "@lingui/macro";
import ReactTooltip from "react-tooltip";

class Application extends React.PureComponent {
  openInstallAppPage = () => {
    const { app, config } = this.props;
    const installAppPage = `/${config.language}/store/${app.type}/${app.id}`;
    this.props.history.push({
      pathname: installAppPage,
      state: { app: app, config: config },
    });
  };

  render = () => {
    const { app } = this.props;

    const indicatorStatus = app.installed
      ? "installed"
      : app.paid
      ? "paid"
      : "free";

    return (
      <div className={`col-6 col-sm-4 container-app ${indicatorStatus}-app`}>
        <div className="app" onClick={() => this.openInstallAppPage()}>
          <div className="logo">
            <img src={app.icon} />
          </div>
          <div className="description">
            <div className="app-header">
              <span className="app-name" data-tip data-for={app.id}>
                {app.name}
              </span>
              <ReactTooltip id={app.id} place="top" effect="solid">
                <span>{app.name}</span>
              </ReactTooltip>
              <p className="app-provider">{app.provider}</p>
            </div>
            <p className="app-description">{app.description}</p>
            <Indicator status={indicatorStatus} />
          </div>
        </div>
      </div>
    );
  };
}

Application.propTypes = {
  app: PropTypes.object,
  config: PropTypes.object,
};

export default withRouter(Application);

export class Indicator extends React.PureComponent {
  render() {
    let btns;
    const status = this.props.status;
    if (status === "installed") {
      btns = [
        <button
          type="button"
          key="indicator_button"
          className="btn btn-lg btn-installed"
        >
          {i18n._(t`store.installed`)}
        </button>,
        <button
          type="button"
          key="indicator_icon"
          className="btn btn-lg btn-installed-indicator"
        >
          <i className="fa fa-check" />
        </button>,
      ];
    } else if (status === "free") {
      btns = [
        <button
          type="button"
          key="indicator_button"
          className="btn btn-lg btn-free"
        >
          {i18n._(t`store.free`)}
        </button>,
        <button
          type="button"
          key="indicator_icon"
          className="btn btn-lg btn-free-indicator"
        >
          <i className="fa fa-gift" />
        </button>,
      ];
    } else {
      btns = [
        <button
          type="button"
          key="indicator_button"
          className="btn btn-lg btn-buy"
        >
          {i18n._(t`store.paid`)}
        </button>,
        <button
          type="button"
          key="indicator_icon"
          className="btn btn-lg btn-buy-indicator"
        >
          <i className="fa fas fa-euro-sign" />
        </button>,
      ];
    }

    return <div className="app-status text-center">{btns}</div>;
  }
}

Indicator.propTypes = {
  status: PropTypes.string,
};
