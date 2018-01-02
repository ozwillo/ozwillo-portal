

const defaultState = {
    members: [
        {
            id: 1,
            firstname: 'firstname',
            lastname: 'lastname'
        },
        {
            id: 2,
            firstname: 'firstname 2',
            lastname: 'lastname 2'
        },
        {
            id: 3,
            firstname: 'firstname 3',
            lastname: 'lastname 3'
        }
    ]
};

export default (state = defaultState, actions) => {
    return state;
};