const defaultState = {
    services: [
        { name: "App 1", isPublic: true },
        { name: "App 2", isPublic: false },
        { name: "App 3", isPublic: true }
    ],
    current: {},
    newService: { isPublic: true }
};

export default (state = defaultState, action) => {
    return state;
}