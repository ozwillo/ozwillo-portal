import customFetch from '../util/custom-fetch';


export const FETCH_UPDATE_ROLE_MEMBER = 'FETCH_UPDATE ROLE_MEMBER';

// actions
const fetchUpdateRoleMemberAction = (organizationId, memberId, isAdmin) => {
    return {
        type: FETCH_UPDATE_ROLE_MEMBER,
        organizationId,
        memberId,
        isAdmin
    };
};

// async methods
export const fetchUpdateRoleMember = (organizationId, memberId, isAdmin) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${organizationId}/membership/${memberId}/role/${isAdmin}`, {
            method: 'PUT'
        }).then(() => {
            return dispatch(fetchUpdateRoleMemberAction(organizationId, memberId, isAdmin));
        });
    };
};
