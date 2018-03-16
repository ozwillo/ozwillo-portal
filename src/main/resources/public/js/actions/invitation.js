import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_ORGANIZATION_INVITATION = 'FETCH_CREATE_ORGANIZATION_INVITATION';
export const FETCH_DELETE_ORGANIZATION_INVITATION = 'FETCH_DELETE_ORGANIZATION_INVITATION';

const createOrganizationInvitationAction = (invitation) => {
    return {
        type: FETCH_CREATE_ORGANIZATION_INVITATION,
        invitation
    };
};

const fetchRemoveOrganizationInvitationAction = (invitation) => {
    return {
        type: FETCH_DELETE_ORGANIZATION_INVITATION,
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

// Aync methods
export const fetchRemoveOrganizationInvitation = (orgId, invitation) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${orgId}/invitation/${invitation.id}`, {
            method: 'DELETE',
            json: invitation
        }).then(() => {
            return dispatch(fetchRemoveOrganizationInvitationAction(invitation));
        });
    };
};