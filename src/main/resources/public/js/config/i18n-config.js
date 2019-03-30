import {I18nProvider} from '@lingui/react';
import React from 'react';
import PropTypes from 'prop-types';
import {setupI18n} from '@lingui/core';

export const i18n = setupI18n();


export default class I18nConfig extends React.Component {

    state = {
        env: this.props.env,
        language: this.props.language,
        catalogs: {}
    };

    componentDidMount() {
        const {env, language, loaded} = this.props;

        if(env && language) {
            this.setState({env, language}, async () => {
                await this.loadLanguage(language);
            });
        }
    }

    componentWillReceiveProps = async (nextProps, nextContext) =>{
        const {env, language} = this.state;

        if ((env || nextProps.env) && (language || nextProps.language)
            && (nextProps.env !== env || nextProps.language !== language)) {
            this.setState({env: nextProps.env, language: nextProps.language}, async () => {
                await this.loadLanguage(nextProps.language);
            });
        }
    };

    loadLanguage = async (language) => {
        const {env} = this.state;
        let catalog = await import(`../../locales/${env}/${language}/messages.js`);

        this.setState(state => ({
            catalogs: {
                ...state.catalogs,
                [language]: catalog
            }
        }), async () => {
            await i18n.load(this.state.catalogs);
            await i18n.activate(language);
            this.props.loaded();
        });
    };


    render() {
        const {catalogs} = this.state;

        return (
            <I18nProvider i18n={i18n} catalogs={catalogs}>
                {this.props.children ? this.props.children : null}
            </I18nProvider>
        )

    }
}

I18nConfig.propTypes = {
    loaded: PropTypes.func,
    language: PropTypes.string,
    env: PropTypes.string
};
