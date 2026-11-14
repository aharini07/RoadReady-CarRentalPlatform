import { useState, useEffect } from "react";
import { Link, useNavigate, useParams } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/Cancellations.css";

function Cancellations() {
    const navigate = useNavigate();
    const { reservationId } = useParams();
    
    const [eligibility, setEligibility] = useState(null);
    const [reason, setReason] = useState("");
    const [error, setError] = useState("");
    const [successData, setSuccessData] = useState(null);

    useEffect(() => {
        const checkEligibility = async () => {
            try {
                const token = localStorage.getItem("token");
                const response = await fetch(`http://localhost:8080/api/v1/cancellations/${reservationId}/eligibility`, {
                    method: "GET",
                    headers: { "Authorization": `Bearer ${token}` }
                });

                if (!response.ok) throw new Error("Failed to check eligibility");
                const data = await response.json();
                setEligibility(data);
            } catch (err) {
                setError(err.message);
            }
        };
        if (reservationId) checkEligibility();
    }, [reservationId]);

    const handleCancel = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/reservations/${reservationId}/cancel`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ reason })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Failed to cancel reservation");
            }

            setSuccessData(data);
            setError("");
        } catch (err) {
            setError(err.message);
        }
    };

    const logout = () => { localStorage.clear(); navigate("/"); };

    // Icons removed from menuItems
    const menuItems = [
        { name: "Dashboard", path: "/customer/dashboard" },
        { name: "Browse Cars", path: "/customer/cars" },
        { name: "My Bookings", path: "/customer/bookings" },
        { name: "Payments", path: "/customer/payments" },
        { name: "Cancellations", path: "/customer/cancellations" },
        { name: "My Reviews", path: "/customer/reviews" },
        { name: "My Profile", path: "/customer/profile" }
    ];

    return (
        <div className="customer-dashboard">
            <aside className="customer-sidebar">
                <div className="sidebar-logo"><span className="logo-road">Road</span><span className="logo-ready">Ready</span></div>
                <p className="menu-heading">MENU</p>
                <nav className="sidebar-menu">
                    {menuItems.map((item) => (
                        <Link key={item.name} to={item.path} className={item.name === "Cancellations" ? "menu-item active" : "menu-item"}>
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
                    <div><h2>Cancellations</h2><p>Request to cancel an active reservation</p></div>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px', fontWeight: 'bold' }}>{error}</div>}

                {/* SUCCESS STATE UI */}
                {successData && (
                    <section className="dashboard-section">
                        <div style={{ textAlign: 'center', padding: '40px 20px' }}>
                            <div style={{ fontSize: '50px', marginBottom: '20px' }}>✅</div>
                            <h2 style={{ color: '#16845c', marginBottom: '15px' }}>Booking Cancelled Successfully</h2>
                            
                            <p style={{ fontSize: '16px', marginBottom: '10px', color: '#20352A' }}>
                                A refund of <strong>₹{successData.refundAmount}</strong> has been initiated.
                            </p>
                            
                            <p style={{ color: '#778078', fontSize: '14px', marginBottom: '30px' }}>
                                The refund will be credited to your original payment method within 3-5 business days.
                            </p>

                            <Link to="/customer/bookings" className="btn-primary-green" style={{ width: 'auto', padding: '12px 30px' }}>
                                Go to My Bookings
                            </Link>
                        </div>
                    </section>
                )}

                {/* ELIGIBILITY CHECK FORM */}
                {eligibility && !successData && (
                    <section className="dashboard-section">
                        <div className="cancel-card">
                            <div className="cancel-info">
                                <h3>Cancellation Policy Check</h3>
                                <p><strong>Policy:</strong> {eligibility.policyReason}</p>
                                <p><strong>Refund Amount:</strong> ₹{eligibility.refundAmount} ({eligibility.refundPercentage}%)</p>
                            </div>
                            
                            <div className="form-group" style={{ marginTop: '15px' }}>
                                <label>Reason for Cancellation</label>
                                <input 
                                    type="text" 
                                    className="form-input" 
                                    placeholder="Enter reason..."
                                    value={reason}
                                    onChange={(e) => setReason(e.target.value)}
                                />
                            </div>

                            <button 
                                className="btn-outline-red" 
                                onClick={handleCancel}
                                disabled={!eligibility.eligible || !reason}
                                style={{ marginTop: '15px' }}
                            >
                                Confirm Cancellation
                            </button>
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
}
export default Cancellations;