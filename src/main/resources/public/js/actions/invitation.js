import customFetch from '../util/custom-fetch';

export const sendInvitationToJoinOrganization = (email, orgId) => {
    return customFetch(`/my/api/organization/invite/${orgId}`, {
        method: 'POST',
        json: { email }
    });
};