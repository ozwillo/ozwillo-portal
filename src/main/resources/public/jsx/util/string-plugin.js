'use strict';

if(!String.format) {
    String.prototype.format = function (format) {
        const args = arguments;
        return this.replace(/{\d+}/g, (match) => {
            const number = parseInt(match.substring(1, match.length -1), 0);
            return args[number]? args[number] : match;
        })
    }
}
