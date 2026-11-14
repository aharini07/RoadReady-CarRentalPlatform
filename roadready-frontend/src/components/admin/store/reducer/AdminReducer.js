// src/components/admin/store/reducer/AdminReducer.js
const initialState = {
    stats: null,
    cars: [],
    users: [],
    allBookings: [],
    reviews: []
}

const AdminReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'FETCH_ADMIN_STATS':
            return { ...state, stats: action.payload }
        case 'FETCH_ADMIN_CARS':
            return { ...state, cars: action.payload }
        case 'FETCH_ADMIN_USERS':
            return { ...state, users: action.payload }
        case 'FETCH_ADMIN_BOOKINGS':
            return { ...state, allBookings: action.payload }
        case 'FETCH_ADMIN_REVIEWS':
            return { ...state, reviews: action.payload }
        default:
            return state
    }
}

export default AdminReducer;