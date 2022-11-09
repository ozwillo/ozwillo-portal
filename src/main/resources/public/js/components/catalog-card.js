import React from 'react';
import '../../css/components/catalog-card.css';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { i18n } from '../config/i18n-config';
import { t } from '@lingui/macro';
import LabelSection from './label-section';

class CatalogCard extends React.PureComponent {
    openInstallAppPage = () => {
        const { app, config } = this.props;
        const installAppPage = `/${config.language}/store/${app.type}/${app.id}`;
        this.props.history.push({
            pathname: installAppPage,
            state: { app: app, config: config }
        });
    };

    getTags = app => {
        let tags = [];

        if (app.installed) {
            tags.push({
                className: 'fa fa-check category-icon',
                text: i18n._(t`store.installed`)
            });
        }

        if (app.paid) {
            tags.push({
                className: 'fa fas fa-euro-sign category-icon',
                text: i18n._(t`store.paid`)
            });
        } else {
            tags.push({
                className: 'fa fa-gift category-icon',
                text: i18n._(t`store.free`)
            });
        }

        const classNameButton = 'audience-btn';
        if (app.target_citizens) {
            tags.push({
                className: 'fa fa-users category-icon',
                text: i18n._(t`store.citizens`),
                classNameButton
            });
        }

        if (app.target_companies) {
            tags.push({
                className: 'fa fa-briefcase category-icon',
                text: i18n._(t`store.companies`),
                classNameButton
            });
        }

        if (app.target_publicbodies) {
            tags.push({
                className: 'fa fa-building category-icon',
                text: i18n._(t`store.publicbodies`),
                classNameButton
            });
        }

        return tags;
    };

    render = () => {
        const { app } = this.props;
        return (
            <div className="content-wrapper">
                <div className="cards-container" onClick={() => this.openInstallAppPage()}>
                    <div id="card" onClick={() => this.openInstallAppPage()}>
                        <div id="main">
                            <div id="icon-wrapper">
                                <img src={app.icon} />
                            </div>
                            <div id="content">
                                <div className="bloc">
                                    <div id="app-name">
                                        <div id="name" className="flex-text">
                                            {app.name}
                                        </div>
                                    </div>
                                </div>
                                <div id="app-provider">{app.provider}</div>
                                <div id="app-description">{app.description}</div>
                            </div>
                        </div>
                        <div id="footer">
                            <div className="footer-infos">
                                {this.getTags(app).map(tagInfo => (
                                    <Tag
                                        className={tagInfo.className}
                                        text={tagInfo.text}
                                        classNameButton={tagInfo.classNameButton}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };
}

CatalogCard.propTypes = {
    app: PropTypes.object,
    config: PropTypes.object
};

export default withRouter(CatalogCard);

export class Tag extends React.PureComponent {
    render() {
        const { className, text, classNameButton } = this.props;

        return (
            <button type="button" key="indicator_button" className={`category-btn ${classNameButton}`}>
                <i className={className}></i>
                <span className="btn-label">{text}</span>
            </button>
        );
    }
}
Tag.propTypes = {
    className: PropTypes.string,
    text: PropTypes.string,
    classNameButton: PropTypes.string
};
