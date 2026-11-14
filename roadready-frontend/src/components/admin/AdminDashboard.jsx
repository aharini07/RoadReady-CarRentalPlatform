// src/components/admin/AdminDashboard.jsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useDispatch, useSelector } from "react-redux";
import { getAdminStats, getAdminBookings, getAdminUsers, getAdminCars, getReviews } from "./store/action/AdminAction.js";
import OverviewPage from "./OverviewPage";
import BookingsPage from "./BookingsPage";
import FleetPage from "./FleetPage";
import UsersPage from "./UsersPage";
import ReviewsPage from "./ReviewsPage";
import "./AdminDashboard.css";

function AdminDashboard() {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [activeTab, setActiveTab] = useState("overview");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // Redux State
    const stats = useSelector(state => state.stats);
    const cars = useSelector(state => state.cars);
    const users = useSelector(state => state.users);
    const allBookings = useSelector(state => state.allBookings);
    const reviews = useSelector(state => state.reviews);

    useEffect(() => {
        dispatch(getAdminStats());
    }, [dispatch]);

    useEffect(() => {
        if (activeTab === "bookings") dispatch(getAdminBookings());
        if (activeTab === "fleet") dispatch(getAdminCars());
        if (activeTab === "users") dispatch(getAdminUsers());
        if (activeTab === "reviews") dispatch(getReviews());
    }, [activeTab, dispatch]);

    const logout = () => { localStorage.clear(); navigate("/"); };

    const menuItems = [
        { id: "overview", name: "Dashboard", icon: "▦" },
        { id: "bookings", name: "All Bookings", icon: "📅" },
        { id: "fleet", name: "Fleet Management", icon: "🚘" },
        { id: "users", name: "Manage Users", icon: "👥" },
        { id: "reviews", name: "Reviews & Disputes", icon: "★" }
    ];

    return (
        <div className="customer-dashboard">
            <aside className="customer-sidebar">
                <div className="sidebar-logo"><span className="logo-road">Road</span><span className="logo-ready">Ready</span></div>
                <p className="menu-heading">ADMIN MENU</p>
                <nav className="sidebar-menu">
                    {menuItems.map((item) => (
                        <button 
                            key={item.id} 
                            onClick={() => { setActiveTab(item.id); setError(""); setSuccess(""); }}
                            className={activeTab === item.id ? "menu-item active" : "menu-item"}
                        >
                            <span className="menu-icon">{item.icon}</span>{item.name}
                        </button>
                    ))}
                </nav>
                <div className="sidebar-bottom">
                    <button className="sidebar-logout" onClick={logout}><span className="menu-icon">↪</span> Logout</button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div><h2>Admin Dashboard</h2><p>Manage your rental business</p></div>
                </header>

                {error && <div style={{ color: '#721C24', backgroundColor: '#F8D7DA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{error}</div>}
                {success && <div style={{ color: '#155724', backgroundColor: '#D4EDDA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{success}</div>}

                {activeTab === "overview" && <OverviewPage stats={stats} />}
                {activeTab === "bookings" && <BookingsPage allBookings={allBookings} setError={setError} setSuccess={setSuccess} />}
                {activeTab === "fleet" && <FleetPage cars={cars} setError={setError} setSuccess={setSuccess} />}
                {activeTab === "users" && <UsersPage users={users} setError={setError} setSuccess={setSuccess} />}
                {activeTab === "reviews" && <ReviewsPage reviews={reviews} setError={setError} setSuccess={setSuccess} />}
            </main>
        </div>
    );
}
export default AdminDashboard;