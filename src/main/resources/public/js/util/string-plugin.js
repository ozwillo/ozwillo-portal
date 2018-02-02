

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