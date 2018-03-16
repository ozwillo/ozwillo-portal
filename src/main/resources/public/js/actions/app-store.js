import customFetch, {urlBuilder} from '../util/custom-fetch';

export const FETCH_APPLICATIONS = 'FETCH_APPLICATIONS';
export const FETCH_ADD_INSTANCE_TO_ORG = 'FETCH_ADD_INSTANCE_TO_ORG';

//Actions
const fetchApplicationsAction = (apps) => {
    return {
        type: FETCH_APPLICATIONS,
        apps
    };
};

const fetchAddInstanceToOrgAction = (instance) => {
    return {
        type: FETCH_ADD_INSTANCE_TO_ORG,
        instance
    };
};

//Async methods
export const fetchApplications = () => {
    return (dispatch) => {
        customFetch(urlBuilder('/api/store/applications', {
            target_citizens: true,
            target_publicbodies: true,
            target_companies: true,
            free: true,
            paid: true
        })).then((data) => {
            return dispatch(fetchApplicationsAction(data.apps));
        });
    };
};

export const fetchAddInstanceToOrg = (organizationId, {id}, members) => {
    return (dispatch) => {
        return customFetch('/api/store/buy/application', {
            method: 'POST',
            json: {
                organizationId,
                members,
                appId: id
            }
        }).then((instance) => {
            return dispatch(fetchAddInstanceToOrgAction(instance));
        });
    };
};