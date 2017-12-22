export default (url, params = { headers: {} }) => {
    if(!params.headers) {
        params.headers = {};
    }

    //Add csrf
    const token = $("meta[name='_csrf']").attr("content");
    const headerField = $("meta[name='_csrf_header']").attr("content");

    params.headers[headerField] = token;
    params.credentials = 'same-origin';

    return fetch(url, params)
        .then((res) => {
            if(!res.ok) {
                throw new Error(res.error);
            }

            return res;
        })
        .then((res) => res.json())
        .catch((err) => {
            console.error(err);
        });
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