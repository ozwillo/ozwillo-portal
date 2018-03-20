export const UPDATE_ORG_FORM = 'UPDATE_ORG_FORM';
export const RESET_ORG_FORM = 'RESET_ORG_FORM';

export const updateOrganizationFormAction = (organization) => {
    return {
        type: UPDATE_ORG_FORM,
        organization
    };
};

export const resetOrganizationFormAction = () => {
    return {
        type: RESET_ORG_FORM
    };
};