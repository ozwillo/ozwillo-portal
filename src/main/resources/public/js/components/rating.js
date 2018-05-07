'use strict';

import React from 'react';
import createClass from 'create-react-class';
import PropTypes from 'prop-types';

var Rating = require('react-rating');

var RatingWrapper = createClass({
    propTypes: {
        rating: PropTypes.number.isRequired,
        appId: PropTypes.string.isRequired,
        rateable: PropTypes.bool.isRequired,
        rate: PropTypes.func.isRequired
    },
    render: function () {
        var empty = React.createElement('img', {
            src: '/img/star-empty.png',
            className: 'icon'
        })
        var full = React.createElement('img', {
            src: '/img/star-yellow.png',
            className: 'icon'
        })

        return (
            <Rating start={0} stop={5} step={1} readonly={!this.props.rateable} initialRate={this.props.rating}
                    onChange={(rate) => this.props.rate(rate)} empty={empty} full={full}/>
        );
    }
});

module.exports = {RatingWrapper};
