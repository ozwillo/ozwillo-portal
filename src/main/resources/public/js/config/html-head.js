import React from 'react';
import {Helmet} from 'react-helmet';
import PropTypes from 'prop-types';

export default class HtmlHead extends React.PureComponent {


    render() {
        const {env} = this.props;
        return (
            <React.Fragment>
                {env ?
                    <Helmet>
                        <link rel="apple-touch-icon" sizes="180x180"
                              href={`/img/favicons/${env}/apple-touch-icon.png`}/>
                        <link rel="icon" type="image/png" sizes="32x32"
                              href={`/img/favicons/${env}/favicon-32x32.png`}/>
                        <link rel="icon" type="image/png" sizes="16x16"
                              href={`/img/favicons/${env}/favicon-16x16.png`}/>
                        <link rel="manifest" href={`/img/favicons/${env}/manifest.json`}/>
                        <link rel="mask-icon" href={`/img/favicons/${env}/safari-pinned-tab.svg" color="#5bbad5`}/>
                    </Helmet>
                    : null
                }
            </React.Fragment>
        )

    }

}

HtmlHead.propTypes = {
    env: PropTypes.string
};
