'use strict';

if (!String.prototype.toAcronyme) {
    String.prototype.toAcronyme = function () {
        if (!this) {
            return this;
        }

        const words = this.match(/\w+/g);
        return words.map(s => s.charAt(0).toUpperCase())
            .join('.');
    };
}

if (!String.format) {
    String.prototype.format = function (format) {
        const args = arguments;
        return this.replace(/{\d+}/g, (match) => {
            const number = parseInt(match.substring(1, match.length - 1), 0);
            return args[number] ? args[number] : match;
        })
    }
}