import {
    UPDATE_ORG_FORM,
    RESET_ORG_FORM
} from "../../actions/components/organization-form";


const defaultState = {
    cedex: '',
    city: '',
    city_uri: '',
    country: '',
    country_uri: '',
    email: '',
    in_activity: false,
    jurisdiction: '',
    jurisdiction_uri: '',
    legal_name: '',
    phone_number: '',
    po_box: '',
    sector_type: '',
    street_and_number: '',
    tax_reg_activity: '',
    tax_reg_activity_uri: '',
    tax_reg_num: '',
    web_site: '',
    zip: '',
    alt_name: '',
    version: '0'
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case UPDATE_ORG_FORM:
            nextState = Object.assign(nextState, action.organization);

            //remove null and undefined values
            Object.keys(nextState).forEach(attr => {
                nextState[attr] = nextState[attr] || '';
            });
            break;
        case RESET_ORG_FORM:
            nextState = defaultState;
            break;
        default:
            return state;
    }

    return nextState;
}