import customFetch from '../util/custom-fetch';


export const FETCH_DELETE_MEMBER = "FETCH_DELETE_MEMBER";

// actions
const fetchDeleteMemberAction = (memberId) => {
    return {
        type: FETCH_DELETE_MEMBER,
        memberId
    };
};

// async methods
export const fetchDeleteMember = (organizationId, memberId) => {
    return dispatch => {
        customFetch(`/my/api/organization/${organizationId}/membership/${memberId}`, {
            method: 'DELETE'
        }).then(() => {
            return dispatch(fetchDeleteMemberAction(memberId));
        });
    };
};
