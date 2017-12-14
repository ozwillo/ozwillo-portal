export default (url, params = { headers: {} }) => {
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