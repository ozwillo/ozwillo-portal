import customFetch from '../util/custom-fetch';

export const FETCH_ADD_INSTANCE_TO_ORG = 'FETCH_ADD_INSTANCE_TO_ORG';

const fetchAddInstanceToOrgAction = (instance) => {
    return {
        type: FETCH_ADD_INSTANCE_TO_ORG,
        instance
    };
};

export const fetchAddInstanceToOrg = (organizationId, {id}) => {
    return (dispatch) => {
        return customFetch('/api/store/buy/application', {
            method: 'POST',
            json: {
                organizationId,
                appId: id
            }
        }).then((instance) => {
            return dispatch(fetchAddInstanceToOrgAction(instance));
        });
    };
};
