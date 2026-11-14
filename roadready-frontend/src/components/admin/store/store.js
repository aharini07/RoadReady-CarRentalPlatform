// src/store/store.js
import { configureStore } from "@reduxjs/toolkit";
import AdminReducer from "./reducer/AdminReducer.js";

export const store = configureStore({
    reducer: AdminReducer
});