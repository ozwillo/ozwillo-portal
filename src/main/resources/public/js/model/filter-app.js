export default class FilterApp {
    audience = {
        citizens: false,
        publicbodies: false,
        companies: false
    };
    payment = {
        paid: false,
        free: false
    };
    selectedLanguage = 'all';
    selectedOrganizationId = '';
    geoArea =  {
        name:'',
        ancestors: []
    };
    searchText = '';
    installStatus = '';
    last = 0;

}