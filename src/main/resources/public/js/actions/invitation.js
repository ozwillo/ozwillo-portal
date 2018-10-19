import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_ORGANIZATION_INVITATION = 'FETCH_CREATE_ORGANIZATION_INVITATION';

const createOrganizationInvitationAction = (invitation) => {
    return {
        type: FETCH_CREATE_ORGANIZATION_INVITATION,
        invitation
    };
};

export const createOrganizationInvitation = (orgId, email, admin) => {
    return dispatch => {
        return customFetch(`/my/api/organization/invite/${orgId}`, {
            method: 'POST',
            json: {email, admin}
        }).then(invitation => {
            return dispatch(createOrganizationInvitationAction(invitation));
        });
    };
};
