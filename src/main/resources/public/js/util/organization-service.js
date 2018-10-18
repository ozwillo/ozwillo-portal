import customFetch from "./custom-fetch";


export default class OrganizationService {

    inviteUser = async (orgId, email, admin) => {
            return await customFetch(`/my/api/organization/invite/${orgId}`, {
                method: 'POST',
                json: {email, admin}
            });
    };

    inviteMultipleUsers = async (orgId, emailArray) => {
        const invitations = emailArray.map(email => {
           return {email: email,admin:false}
        });
        return await customFetch(`/my/api/organization/invite/multiple/${orgId}`, {
            method: 'POST',
            json: invitations
        });
    }
}