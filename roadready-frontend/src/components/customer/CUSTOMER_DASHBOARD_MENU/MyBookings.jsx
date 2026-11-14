import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/MyBookings.css";

function MyBookings() {
    const navigate = useNavigate();
    const [bookings, setBookings] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBookings = async () => {
            try {
                const token = localStorage.getItem("token");
                const response = await fetch("http://localhost:8080/api/v1/reservations/my-bookings", {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                if (!response.ok) throw new Error("Failed to load bookings");

                const data = await response.json();
                setBookings(data);
            } catch (err) {
                setError(err.message);
            }
        };
        fetchBookings();
    }, []);

    const logout = () => { localStorage.clear(); navigate("/"); };

    const menuItems = [
        { name: "Dashboard", path: "/customer/dashboard" },
        { name: "Browse Cars", path: "/customer/cars" },
        { name: "My Bookings", path: "/customer/bookings"},
        { name: "Payments", path: "/customer/payments" },
        { name: "Cancellations", path: "/customer/cancellations" },
        { name: "My Reviews", path: "/customer/reviews" },
        { name: "My Profile", path: "/customer/profile"}
    ];

    return (
        <div className="customer-dashboard">
            <aside className="customer-sidebar">
                <div className="sidebar-logo"><span className="logo-road">Road</span><span className="logo-ready">Ready</span></div>
                <p className="menu-heading">MENU</p>
                <nav className="sidebar-menu">
                    {menuItems.map((item) => (
                        <Link key={item.name} to={item.path} className={item.name === "My Bookings" ? "menu-item active" : "menu-item"}>
                            {item.name}
                        </Link>
                    ))}
                </nav>
                <div className="sidebar-bottom">
                    <button className="sidebar-logout" onClick={logout}>Logout</button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div><h2>My Bookings</h2><p>Manage your current and past reservations</p></div>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

                <section className="dashboard-section">
                    {bookings.length === 0 ? <p>No bookings found.</p> : bookings.map((booking) => (
                        <div className="booking-card" key={booking.reservationId}>
                            
                            <div className="booking-header" style={{ borderBottom: '1px solid #E6EBE3', paddingBottom: '10px', marginBottom: '15px' }}>
                                <div>
                                    <h3 style={{ margin: 0 }}>{booking.carName}</h3>
                                    <p style={{ color: '#778078', fontSize: '13px', margin: '4px 0 0 0', fontWeight: 'bold' }}>
                                        Booking ID: #{booking.reservationId}
                                    </p>
                                </div>
                                <span className={`status ${booking.status.toLowerCase()}`}>
                                    {booking.status}
                                </span>
                            </div>
                            
                            <p><strong>Pickup Location:</strong> {booking.pickupLocation}</p>
                            <p><strong>Drop-off Location:</strong> {booking.dropoffLocation}</p>
                            <p><strong>Pickup:</strong> {booking.pickupDate} at {booking.pickupTime}</p>
                            <p><strong>Drop-off:</strong> {booking.dropoffDate}</p>
                            
                            <div style={{ marginTop: '15px' }}>
                                <p><strong>Total Amount:</strong> ₹{booking.totalAmount}</p>
                                
                                {booking.penaltyFee > 0 && (
                                    <div style={{ 
                                        backgroundColor: '#FFF3CD', 
                                        color: '#856404', 
                                        padding: '10px 15px', 
                                        borderRadius: '8px', 
                                        marginTop: '10px',
                                        border: '1px solid #FFEEBA',
                                        fontSize: '14px'
                                    }}>
                                        <strong>Damage Penalty Applied: ₹{booking.penaltyFee}</strong>
                                        <p style={{ margin: '5px 0 0 0', fontSize: '13px' }}>
                                            An additional fee was charged for vehicle damage identified during the post-rental inspection.
                                        </p>
                                    </div>
                                )}
                            </div>

                            <div className="booking-actions" style={{ marginTop: '15px' }}>
                                {booking.status === "PENDING" && (
                                    <Link to={`/customer/payments/${booking.reservationId}`} className="btn-primary-green">
                                        Pay Now
                                    </Link>
                                )}
                                {booking.status === "CONFIRMED" && (
                                    <Link to={`/customer/cancellations/${booking.reservationId}`} className="btn-outline-red">
                                        Cancel Booking
                                    </Link>
                                )}
                            </div>
                        </div>
                    ))}
                </section>
            </main>
        </div>
    );
}
export default MyBookings;