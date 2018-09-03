import 'isomorphic-fetch';
import store from './store'

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

export default (url, params = {headers: {}}) => {
    if (!params.headers) {
        params.headers = {};
    }

    if (store && store.getState().config.csrfHeader) {
        let csrfHeader = store.getState().config.csrfHeader;
        params.headers[csrfHeader] = store.getState().config.csrfToken;
    }

    params.credentials = 'same-origin';

    if (params.json) {
        params.body = JSON.stringify(params.json);
        params.headers['Content-Type'] = 'application/json';
    }

    return fetch(url, params)
        .then((res) => {
            if (!res.ok) {
                throw res;
            }

            return res;
        })
        .catch(buildError)
        .catch(error => { //user is disconnected
            if (error.status === 401) {
                location.reload();
            }

            throw error;
        }).then((res) => {
            const contentType = res.headers.get('Content-Type');
            if (!contentType) {
                return;
            }

            if (contentType.split(';').includes('application/json')) {
                return res.json();
            }

            return res.text();
        });
}

export const urlBuilder = (url, params) => {
    if (!params || !Object.keys(params).length) {
        return url;
    }

    const query = Object.keys(params)
        .map(i => `${i}=${params[i]}`)
        .join('&');

    return `${url}?${query}`;
};