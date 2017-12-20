export const FETCH_CONFIG = 'FETCH_CONFIG';

export const fetchConfigAction = (config) => {
    return {
        type: FETCH_CONFIG,
        config
    }
};