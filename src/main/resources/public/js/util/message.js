export default function(key) {
    if (messages != undefined) {
        var v = messages[key];
        if (v != null) {
            return v;
        }
    }
    return key;
}
