import customFetch from './custom-fetch';


export default class UserOrganizationHistoryService {

    getOrganizationHistory = async () => {
        return await customFetch("/my/api/organizationHistory");
    };

    deleteOrganizationHistoryEntry = async (dcOrganizationId) => {
        return await customFetch(`/my/api/organizationHistory/delete/${dcOrganizationId}`, {method: "DELETE"});
    };
}