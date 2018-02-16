import customFetch from '../util/custom-fetch';


export const FETCH_DELETE_MEMBER = 'FETCH_DELETE_MEMBER';
export const FETCH_UPDATE_ROLE_MEMBER = 'FETCH_UPDATE ROLE_MEMBER';

// actions
const fetchDeleteMemberAction = (memberId) => {
    return {
        type: FETCH_DELETE_MEMBER,
        memberId
    };
};

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
export const fetchDeleteMember = (organizationId, memberId) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${organizationId}/membership/${memberId}`, {
            method: 'DELETE'
        }).then(() => {
            return dispatch(fetchDeleteMemberAction(memberId));
        });
    };
};

export const fetchUpdateRoleMember = (organizationId, memberId, isAdmin) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${organizationId}/membership/${memberId}/role/${isAdmin}`, {
            method: 'PUT'
        }).then(() => {
            return dispatch(fetchUpdateRoleMemberAction(organizationId, memberId, isAdmin));
        });
    };
};
