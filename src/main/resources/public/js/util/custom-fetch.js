import 'isomorphic-fetch';

/*
* Custom fetch
*
* Integrate :
*   - A json parameter to send a json object.
*   - Fix cross-origin problems
*   - Insert in the request header a csrf field
*/

const buildError = (error) => {
    const err = {
        error: error.message,
        status: error.status
    };
    console.error(err);
    throw err;

};

function getCookie(name) {
    var cookieValue = null;
    if (document.cookie && document.cookie !== '') {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = jQuery.trim(cookies[i]);
            if (cookie.substring(0, name.length + 1) === (name + '=')) {
                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                break;
            }
        }
    }
    return cookieValue;
}

export default (url, params = {headers: {}}) => {
    if (!params.headers) {
        params.headers = {};
    }

    const csrftoken = getCookie('XSRF-TOKEN');
    if (csrftoken) {
        params.headers['X-XSRF-TOKEN'] = csrftoken;
    }

    params.credentials = 'same-origin';

    if (params.json) {
        params.body = JSON.stringify(params.json);
        params.headers['Content-Type'] = 'application/json';
    }

    if (params.urlParams) {
        let query = Object.keys(params.urlParams)
            .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params.urlParams[k]))
            .join('&');
        url += '?' + query;
    }

    return fetch(url, params)
        .then((res) => {
            if (!res.ok) {
                throw buildError(res);
            }

            return res;
        })
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
