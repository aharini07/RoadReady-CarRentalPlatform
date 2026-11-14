import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/Payments.css";

function Payments() {
    const navigate = useNavigate();
    const [pendingBookings, setPendingBookings] = useState([]);
    const [paymentHistory, setPaymentHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    
    const [paymentMethod, setPaymentMethod] = useState("CARD");

    useEffect(() => {
        const fetchPayments = async () => {
            try {
                const token = localStorage.getItem("token");
                const headers = { "Authorization": `Bearer ${token}` };

                const resBookings = await fetch("http://localhost:8080/api/v1/reservations/my-bookings", { headers });
                const bookings = await resBookings.json();
                
                const needsPayment = bookings.filter(b => 
                    b.status === "PENDING" || (b.status === "COMPLETED" && b.penaltyFee > 0)
                );
                setPendingBookings(needsPayment);

                const resHistory = await fetch("http://localhost:8080/api/v1/payments/my-history", { headers });
                if (resHistory.ok) {
                    const historyData = await resHistory.json();
                    const historyWithCar = historyData.map(payment => {
                        const matchingBooking = bookings.find(b => b.reservationId === payment.reservationId);
                        return { ...payment, carName: matchingBooking ? matchingBooking.carName : "Unknown Car" };
                    });
                    setPaymentHistory(historyWithCar);
                }

            } catch (err) {
                setError("Failed to load payment data");
            } finally {
                setLoading(false);
            }
        };
        fetchPayments();
    }, []);

    const handlePay = async (reservationId) => {
        try {
            setError("");
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/payments/${reservationId}/pay`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ paymentMethod })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Payment failed");
            }

            alert(`Payment Successful! Transaction ID: ${data.transactionId}`);
            window.location.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const logout = () => { localStorage.clear(); navigate("/"); };

    const menuItems = [
        { name: "Dashboard", path: "/customer/dashboard" },
        { name: "Browse Cars", path: "/customer/cars"},
        { name: "My Bookings", path: "/customer/bookings" },
        { name: "Payments", path: "/customer/payments"},
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
                    {/* Removed the icon span here */}
                    <button className="sidebar-logout" onClick={logout}>Logout</button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div><h2>Payments</h2><p>Manage pending payments and view history</p></div>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}
                {loading ? <p>Loading...</p> : (
                    <>
                        <section className="dashboard-section">
                            <h3 style={{ marginBottom: '15px', color: '#20352A' }}>Pending Payments</h3>
                            {pendingBookings.length === 0 ? <p>No pending payments.</p> : (
                                pendingBookings.map(b => (
                                    <div className="booking-card" key={b.reservationId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: b.penaltyFee > 0 ? '#FFF3CD' : '#F9FAF8' }}>
                                        <div>
                                            <h4>{b.carName} {b.penaltyFee > 0 && <span style={{color: '#856404', fontSize: '14px'}}>(Damage Penalty)</span>}</h4>
                                            <p>Amount Due: <strong>Rs {b.penaltyFee > 0 ? b.penaltyFee : b.totalAmount}</strong></p>
                                        </div>
                                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                                            <select className="form-input" value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
                                                <option value="CARD">Credit Card</option>
                                                <option value="UPI">UPI</option>
                                                <option value="NETBANKING">Net Banking</option>
                                            </select>
                                            <button className="btn-primary-green" onClick={() => handlePay(b.reservationId)}>Pay Now</button>
                                        </div>
                                    </div>
                                ))
                            )}
                        </section>

                        <section className="dashboard-section">
                            <h3 style={{ marginBottom: '15px', color: '#20352A' }}>Payment History</h3>
                            {paymentHistory.length === 0 ? <p>No payment history.</p> : (
                                <table className="payments-table">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Car</th>
                                            <th>Transaction ID</th>
                                            <th>Amount</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {paymentHistory.map(p => (
                                            <tr key={p.paymentId}>
                                                <td>{new Date(p.paymentDate).toLocaleDateString()}</td>
                                                <td>{p.carName}</td>
                                                <td>{p.transactionId}</td>
                                                <td>Rs {p.amount}</td>
                                                <td><span className={`status ${p.paymentStatus.toLowerCase()}`}>{p.paymentStatus}</span></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </section>
                    </>
                )}
            </main>
        </div>
    );
}
export default Payments;