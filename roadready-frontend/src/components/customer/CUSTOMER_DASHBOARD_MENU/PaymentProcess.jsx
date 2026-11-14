import { useState } from "react";
import { useNavigate, useParams, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/Payments.css";

function PaymentProcess() {
    const navigate = useNavigate();
    const { reservationId } = useParams();
    const [paymentMethod, setPaymentMethod] = useState("CARD");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handlePayment = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const token = localStorage.getItem("token");
            
            const response = await fetch(`http://localhost:8080/api/v1/payments/${reservationId}/pay`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ paymentMethod })
            });

            const responseText = await response.text();
            
            if (!response.ok) {
                let errorMsg = "Payment failed";
                if (responseText) {
                    try {
                        const errorData = JSON.parse(responseText);
                        errorMsg = errorData.message || errorMsg;
                    } catch (e) {
                        errorMsg = `Server Error: ${response.status}`;
                    }
                }
                throw new Error(errorMsg);
            }

            alert("Payment Successful! Your booking is now CONFIRMED.");
            navigate("/customer/bookings");

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const logout = () => { localStorage.clear(); navigate("/"); };

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
                        <Link key={item.name} to={item.path} className={item.name === "Payments" ? "menu-item active" : "menu-item"}>
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
                    <div><h2>Complete Payment</h2><p>Secure your reservation</p></div>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px', fontWeight: 'bold' }}>{error}</div>}

                <section className="dashboard-section" style={{ maxWidth: '500px' }}>
                    <form onSubmit={handlePayment} className="profile-form">
                        <div className="form-group" style={{ gridColumn: "span 2" }}>
                            <label>Select Payment Method</label>
                            <select 
                                className="profile-input" 
                                value={paymentMethod} 
                                onChange={(e) => setPaymentMethod(e.target.value)}
                                required
                            >
                                <option value="CARD">Credit / Debit Card</option>
                                <option value="UPI">UPI</option>
                                <option value="NETBANKING">Net Banking</option>
                                <option value="WALLET">Wallet</option>
                            </select>
                        </div>
                        
                        <div style={{ gridColumn: "span 2", marginTop: "20px" }}>
                            <button type="submit" className="btn-primary-green" disabled={loading} style={{ width: '100%' }}>
                                {loading ? "Processing Payment..." : "Pay Now"}
                            </button>
                            
                            <button type="button" onClick={() => navigate("/customer/bookings")} className="btn-outline-green" style={{ width: '100%', marginTop: "10px" }}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </section>
            </main>
        </div>
    );
}
export default PaymentProcess;