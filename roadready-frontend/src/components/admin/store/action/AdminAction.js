// src/components/admin/store/action/AdminAction.js
import axios from "axios";
import { API_SERVER_URL_DEV } from "../../../../../config.js";
const getHeaders = () => ({
    headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
});

export const getAdminStats = () => async (dispatch) => {
    try {
        const res = await axios.get(API_SERVER_URL_DEV + "/api/v1/admin/dashboard", getHeaders());
        dispatch({ type: 'FETCH_ADMIN_STATS', payload: res.data });
    } catch (err) { console.log(err); }
};

export const getAdminCars = () => async (dispatch) => {
    try {
        const res = await axios.get(API_SERVER_URL_DEV + "/api/v1/cars/all", getHeaders());
        dispatch({ type: 'FETCH_ADMIN_CARS', payload: res.data });
    } catch (err) { console.log(err); }
};

export const addAdminCar = (carData) => async (dispatch) => {
    try {
        await axios.post(API_SERVER_URL_DEV + "/api/v1/cars", carData, getHeaders());
        dispatch(getAdminCars());
    } catch (err) { console.log(err); }
};

export const updateAdminCar = (carId, carData) => async (dispatch) => {
    try {
        await axios.put(`${API_SERVER_URL_DEV}/api/v1/cars/${carId}`, carData, getHeaders());
        dispatch(getAdminCars());
    } catch (err) { console.log(err); }
};

export const deleteAdminCar = (carId) => async (dispatch) => {
    try {
        await axios.delete(`${API_SERVER_URL_DEV}/api/v1/cars/${carId}`, getHeaders());
        dispatch(getAdminCars());
    } catch (err) { console.log(err); }
};

export const getAdminUsers = () => async (dispatch) => {
    try {
        const res = await axios.get(API_SERVER_URL_DEV + "/api/v1/admin/users", getHeaders());
        dispatch({ type: 'FETCH_ADMIN_USERS', payload: res.data });
    } catch (err) { console.log(err); }
};

export const deleteAdminUser = (userId) => async (dispatch) => {
    try {
        await axios.delete(`${API_SERVER_URL_DEV}/api/v1/users/${userId}`, getHeaders());
        dispatch(getAdminUsers());
    } catch (err) { console.log(err); }
};

export const getAdminBookings = () => async (dispatch) => {
    try {
        const res = await axios.get(API_SERVER_URL_DEV + "/api/v1/reservations/all", getHeaders());
        dispatch({ type: 'FETCH_ADMIN_BOOKINGS', payload: res.data });
    } catch (err) { console.log(err); }
};

export const cancelAdminBooking = (reservationId) => async (dispatch) => {
    try {
        await axios.post(`${API_SERVER_URL_DEV}/api/v1/reservations/${reservationId}/cancel`, 
            { reason: "Cancelled by Admin Override" }, getHeaders());
        dispatch(getAdminBookings());
    } catch (err) { console.log(err); }
};

export const getReviews = () => async (dispatch) => {
    try {
        const resCars = await axios.get(API_SERVER_URL_DEV + "/api/v1/cars/all", getHeaders());
        let allReviews = [];
        for (const car of resCars.data) {
            const resRev = await axios.get(`${API_SERVER_URL_DEV}/api/v1/cars/${car.carId}/reviews`, getHeaders());
            const reviewsWithCarName = resRev.data.map(r => ({ ...r, carName: car.carName }));
            allReviews = [...allReviews, ...reviewsWithCarName];
        }
        allReviews.sort((a, b) => new Date(b.reviewDate) - new Date(a.reviewDate));
        dispatch({ type: 'FETCH_ADMIN_REVIEWS', payload: allReviews });
    } catch (err) { console.log(err); }
};

export const replyToDispute = (reviewId, replyText) => async (dispatch) => {
    try {
        await axios.post(`${API_SERVER_URL_DEV}/api/v1/reviews/${reviewId}/reply`, 
            { reply: replyText }, getHeaders());
        dispatch(getReviews());
    } catch (err) { console.log(err); }
};