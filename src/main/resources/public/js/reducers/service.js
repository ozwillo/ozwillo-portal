const defaultState = {
    services: [
        { id: 1, name: "App 1", isPublic: true, members: [] },
        { id: 2, name: "App 2", isPublic: false, members: [] },
        { id: 3, name: "App 3", isPublic: true, members: [] }
    ],
    current: {},
    newService: { isPublic: true }
};

export default (state = defaultState, action) => {
    return state;
}