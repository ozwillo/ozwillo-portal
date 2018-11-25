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
    };

    removeUserInvitation = async (orgId, invitation) => {
        return await customFetch(`/my/api/organization/${orgId}/invitation/${invitation.id}`, {
            method: 'DELETE',
            json: invitation
        })
    };

    removeUser = async (orgId, member) => {
        return await customFetch(`/my/api/organization/${orgId}/membership/${member.id}`, {
            method: 'DELETE'
        });
    };

    createOrganization = async (organization) => {
        Object.keys(organization).forEach(key => {
            organization[key] = organization[key] || null;
        });

        return await customFetch('/my/api/organization', {
            method: 'POST',
            json: {...organization}
        })
    };
}