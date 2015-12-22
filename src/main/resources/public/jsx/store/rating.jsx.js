'use strict';

import React from 'react';

import t from '../util/message';

var Rating = require('react-rating');

var RatingWrapper = React.createClass({
    propTypes: {
        rating: React.PropTypes.number.isRequired,
        appId: React.PropTypes.string.isRequired,
        rateable: React.PropTypes.bool.isRequired,
        rate: React.PropTypes.func.isRequired
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
                    onChange={(rate) => this.props.rate(rate)} empty={empty} full={full} />
        );
    }
});

module.exports = { RatingWrapper };
