import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "./CustomerDashboard.css";

function CustomerDashboard() {
    const navigate = useNavigate();
    
    const [username, setUsername] = useState("Customer");
    const [stats, setStats] = useState({ total: 0, active: 0, completed: 0, cancelled: 0 });
    const [recentBookings, setRecentBookings] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const token = localStorage.getItem("token");
                const headers = { "Authorization": `Bearer ${token}` };

                const userRes = await fetch("http://localhost:8080/api/v1/auth/user-details", { headers });
                if (userRes.ok) {
                    const userData = await userRes.json();
                    setUsername(userData.username);
                }

                const bookingRes = await fetch("http://localhost:8080/api/v1/reservations/my-bookings", { headers });
                if (bookingRes.ok) {
                    const bookings = await bookingRes.json();
                    
                    let activeCount = 0;
                    let completedCount = 0;
                    let cancelledCount = 0;

                    bookings.forEach(b => {
                        const status = b.status.toUpperCase();
                        if (status === "CONFIRMED" || status === "PICKED_UP" || status === "PENDING") {
                            activeCount++;
                        } else if (status === "COMPLETED") {
                            completedCount++;
                        } else if (status === "CANCELLED") {
                            cancelledCount++;
                        }
                    });

                    setStats({
                        total: bookings.length,
                        active: activeCount,
                        completed: completedCount,
                        cancelled: cancelledCount
                    });

                    setRecentBookings(bookings.slice(0, 3));
                }
            } catch (err) {
                console.error("Error fetching dashboard data:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const logout = () => {
        localStorage.clear();
        navigate("/");
    };

    const menuItems = [
        { name: "Dashboard", path: "/customer/dashboard" },
        { name: "Browse Cars", path: "/customer/cars" },
        { name: "My Bookings", path: "/customer/bookings" },
        { name: "Payments", path: "/customer/payments"},
        { name: "Cancellations", path: "/customer/cancellations" },
        { name: "My Reviews", path: "/customer/reviews" },
        { name: "My Profile", path: "/customer/profile" }
    ];

    return (
        <div className="customer-dashboard">
            <aside className="customer-sidebar">
                <div className="sidebar-logo">
                    <span className="logo-road">Road</span><span className="logo-ready">Ready</span>
                </div>

                <p className="menu-heading">MENU</p>

                <nav className="sidebar-menu">
                    {menuItems.map((item) => (
                        <Link
                            key={item.name}
                            to={item.path}
                            className={item.name === "Dashboard" ? "menu-item active" : "menu-item"}
                        >
                            {item.name}
                        </Link>
                    ))}
                </nav>

                <div className="sidebar-bottom">
                    <button className="sidebar-logout" onClick={logout}>
                        Logout
                    </button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div>
                        <h2>Customer Dashboard</h2>
                        <p>Manage your rental journey</p>
                    </div>
                    <div className="header-profile">
                        <div className="profile-avatar">{username.charAt(0).toUpperCase()}</div>
                        <div>
                            <strong>{username}</strong>
                            <p>Welcome !</p>
                        </div>
                    </div>
                </header>

                <section className="welcome-section">
                    <div>
                        <p className="welcome-label">WELCOME TO ROADREADY</p>
                        <h1>Ready for your next journey, {username}?</h1>
                        <p>Discover your perfect ride and make every journey memorable.</p>
                        <Link to="/customer/cars" className="btn-primary-green">
                            Browse Cars →
                        </Link>
                    </div>
                    <div className="welcome-illustration"></div>
                </section>

                <section className="dashboard-section">
                    <div className="section-heading">
                        <h2>Booking Overview</h2>
                    </div>
                    <div className="stats-grid">
                        <div className="stat-card">
                            <h3>{loading ? "..." : stats.total}</h3>
                            <p>Total Bookings</p>
                        </div>
                        <div className="stat-card">
                            <h3>{loading ? "..." : stats.active}</h3>
                            <p>Active Bookings</p>
                        </div>
                        <div className="stat-card">
                            <h3>{loading ? "..." : stats.completed}</h3>
                            <p>Completed Bookings</p>
                        </div>
                        <div className="stat-card">
                            <h3>{loading ? "..." : stats.cancelled}</h3>
                            <p>Cancelled Bookings</p>
                        </div>
                    </div>
                </section>

                <section className="dashboard-section">
                    <div className="section-heading">
                        <h2>Recent Bookings</h2>
                        <Link to="/customer/bookings">View All →</Link>
                    </div>
                    
                    {loading ? (
                        <p style={{ textAlign: 'center', color: '#778078' }}>Loading bookings...</p>
                    ) : recentBookings.length === 0 ? (
                        <div className="empty-state">
                            <h3>No bookings yet</h3>
                            <p>Your reservations will appear here once you book a vehicle.</p>
                            <Link to="/customer/cars" className="btn-primary-green">
                                Explore Cars
                            </Link>
                        </div>
                    ) : (
                        <div className="recent-bookings-list">
                            {recentBookings.map((booking) => (
                                <div className="recent-booking-item" key={booking.reservationId}>
                                    <div>
                                        <h4>{booking.carName}</h4>
                                        <p>{booking.pickupDate} to {booking.dropoffDate}</p>
                                    </div>
                                    <span className={`status ${booking.status.toLowerCase()}`}>
                                        {booking.status}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}

export default CustomerDashboard;