/*
* Custom fetch
*
* Integrate :
*   - A json parameter to send a json object.
*   - Fix cross-origin problems
*   - Insert in the request header a csrf field
*/

const buildError = (res) => {
    return res.text()
        .then((message) => {
            const err = {
                error: message,
                status: res.status
            };
            console.error(err);
            throw err;
        });
};

export default (url, params = { headers: {} }) => {
    if(!params.headers) {
        params.headers = {};
    }

    //Add csrf
    const token = document.querySelector("meta[name='_csrf']").content;
    const headerField = document.querySelector("meta[name='_csrf_header']").content;

    params.headers[headerField] = token;
    params.credentials = 'same-origin';

    if(params.json){
        params.body = JSON.stringify(params.json);
        params.headers['Content-Type'] = 'application/json';
    }

    return fetch(url, params)
        .then((res) => {
            if(!res.ok) {
                throw res;
            }

            return res;
        })
        .catch(buildError)
        .then((res) => { //Parse to json
            return res.json()
                .catch((err) => {
                    console.debug('Error to parse result in JSON: ', err);
                })
        })
}

export const urlBuilder = (url, params) => {
    if(!params || !Object.keys(params).length) {
        return url;
    }

    const query = Object.keys(params)
        .map(i => `${i}=${params[i]}`)
        .join('&');

    return `${url}?${query}`;
};