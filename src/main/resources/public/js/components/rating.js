'use strict';

import React from 'react';
import PropTypes from 'prop-types';

const Rating = require('react-rating');

export default class RatingWrapper extends React.PureComponent{
    render() {
        const {rateable, rating} = this.props;
        let empty = React.createElement('img', {
            src: '/img/star-empty.png',
            className: 'icon'
        });

        let full = React.createElement('img', {
            src: '/img/star-yellow.png',
            className: 'icon'
        });


        return (
            <Rating start={0} stop={5} step={1} readonly={!rateable} initialRate={rating}
                    onChange={(rate) => this.props.rate(rate)} empty={empty} full={full}/>
        );
    }
};

RatingWrapper.propType ={
    rating: PropTypes.number.isRequired,
    rateable: PropTypes.bool.isRequired,
    rate: PropTypes.func.isRequired
};

