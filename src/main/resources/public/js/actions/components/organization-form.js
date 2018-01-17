export const UPDATE_ORG_FORM = 'UPDATE_ORG_FORM';
export const RESET_ORG_FORM = 'RESET_ORG_FORM';

export const updateOrganizationForm = (organization) => {
    return {
        type: UPDATE_ORG_FORM,
        organization
    };
};

export const resetOrganizationForm = () => {
    return {
        type: RESET_ORG_FORM
    };
};