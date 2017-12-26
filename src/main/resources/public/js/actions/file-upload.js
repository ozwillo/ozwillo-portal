import customFetch from '../util/custom-fetch';


export const uploadFile = (file, id) => {
    // also fills "filename" etc. in multipart Boundary line
    // else HTTP error Required MultipartFile parameter 'iconFile' is not present or Multipart boundary missing
    const formData = new FormData();
    formData.append('iconFile', file);

    return customFetch(`/media/objectIcon/${id}`, {
        method: 'POST',
        body: formData
    });
};