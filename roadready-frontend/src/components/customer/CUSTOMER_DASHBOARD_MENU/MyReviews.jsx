import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/MyReviews.css";

function MyReviews() {
    const navigate = useNavigate();
    const [completedBookings, setCompletedBookings] = useState([]);
    const [pastReviews, setPastReviews] = useState([]);
    
    // Form State
    const [selectedBooking, setSelectedBooking] = useState("");
    const [rating, setRating] = useState(5);
    const [hoverRating, setHoverRating] = useState(0);
    const [comment, setComment] = useState("");
    const [isDispute, setIsDispute] = useState(false);
    
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = localStorage.getItem("token");
                const headers = { "Authorization": `Bearer ${token}` };

                // 1. Fetch Bookings to find COMPLETED ones
                const resBookings = await fetch("http://localhost:8080/api/v1/reservations/my-bookings", { headers });
                const bookings = await resBookings.json();
                const completed = bookings.filter(b => b.status.toUpperCase() === "COMPLETED");
                setCompletedBookings(completed);

                // 2. Fetch Reviews for the cars the user has booked
                const reviewPromises = completed.map(async (b) => {
                    const resRev = await fetch(`http://localhost:8080/api/v1/cars/${b.carId}/reviews`, { headers });
                    if (resRev.ok) {
                        const reviews = await resRev.json();
                        return reviews.filter(r => r.customerUsername === localStorage.getItem("username"))
                                      .map(r => ({ ...r, carName: b.carName }));
                    }
                    return [];
                });

                const allReviews = await Promise.all(reviewPromises);
                setPastReviews(allReviews.flat());

            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        if (!selectedBooking) {
            setError("Please select a completed booking.");
            return;
        }

        try {
            const token = localStorage.getItem("token");
            const response = await fetch("http://localhost:8080/api/v1/reviews", {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    reservationId: parseInt(selectedBooking),
                    rating: rating,
                    comment: comment,
                    isDispute: isDispute
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Failed to submit review");
            }

            setSuccess("Review submitted successfully! Thank you for your feedback.");
            setComment("");
            setSelectedBooking("");
            setRating(5);
            setIsDispute(false);
            
            setTimeout(() => window.location.reload(), 1500);

        } catch (err) {
            setError(err.message);
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
                        <Link key={item.name} to={item.path} className={item.name === "My Reviews" ? "menu-item active" : "menu-item"}>
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
                    <div><h2>My Reviews</h2><p>Rate and review your rental experiences</p></div>
                </header>

                {error && <div style={{ color: '#721C24', backgroundColor: '#F8D7DA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{error}</div>}
                {success && <div style={{ color: '#155724', backgroundColor: '#D4EDDA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{success}</div>}

                {loading ? (
                    <p>Loading...</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '40px' }}>
                        
                        <section className="dashboard-section">
                            <h3 style={{ marginBottom: '20px', color: '#20352A' }}>Write a Review</h3>
                            
                            {completedBookings.length === 0 ? (
                                <p style={{ color: '#778078' }}>You have no completed bookings to review.</p>
                            ) : (
                                <form onSubmit={handleSubmit} className="review-container">
                                    
                                    <div className="form-group" style={{ marginBottom: '20px' }}>
                                        <label>Select Completed Booking</label>
                                        <select 
                                            className="form-input" 
                                            value={selectedBooking} 
                                            onChange={(e) => setSelectedBooking(e.target.value)}
                                            required
                                        >
                                            <option value="">-- Choose a rental --</option>
                                            {completedBookings.map(b => (
                                                <option key={b.reservationId} value={b.reservationId}>
                                                    {b.carName} (Pickup: {b.pickupDate})
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="form-group" style={{ marginBottom: '20px' }}>
                                        <label>Overall Rating</label>
                                        <div className="star-rating">
                                            {[1, 2, 3, 4, 5].map((star) => (
                                                <span 
                                                    key={star}
                                                    className={(hoverRating || rating) >= star ? "active" : ""}
                                                    onClick={() => setRating(star)}
                                                    onMouseEnter={() => setHoverRating(star)}
                                                    onMouseLeave={() => setHoverRating(0)}
                                                >
                                                    ★
                                                </span>
                                            ))}
                                        </div>
                                    </div>

                                    <div className="form-group" style={{ marginBottom: '20px' }}>
                                        <label>Comment (Optional)</label>
                                        <textarea 
                                            className="form-input" 
                                            rows="4" 
                                            placeholder="comments"
                                            value={comment}
                                            onChange={(e) => setComment(e.target.value)}
                                        />
                                    </div>

                                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px', padding: '15px', backgroundColor: '#FFF3CD', borderRadius: '8px', border: '1px solid #FFEEBA' }}>
                                        <input 
                                            type="checkbox" 
                                            id="dispute" 
                                            checked={isDispute} 
                                            onChange={(e) => setIsDispute(e.target.checked)} 
                                            style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                                        />
                                        <label htmlFor="dispute" style={{ fontWeight: 'bold', color: '#856404', cursor: 'pointer', margin: 0 }}>
                                            Report an Issue / File a Dispute with Admin
                                        </label>
                                    </div>

                                    <button type="submit" className="btn-primary-green" style={{ width: '100%' }}>
                                        Submit Review
                                    </button>
                                </form>
                            )}
                        </section>

                        {pastReviews.length > 0 && (
                            <section className="dashboard-section">
                                <h3 style={{ marginBottom: '20px', color: '#20352A' }}>Your Past Reviews</h3>
                                <div>
                                    {pastReviews.map(review => (
                                        <div key={review.reviewId} className="past-review-card" style={{ marginBottom: '15px' }}>
                                            <div className="past-review-header">
                                                <h4>{review.carName}</h4>
                                                <span className="review-stars">
                                                    {"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}
                                                </span>
                                            </div>
                                            <p className="review-comment">"{review.comment}"</p>
                                            <span className="review-date">{new Date(review.reviewDate).toLocaleDateString()}</span>
                                            
                                            {review.isDispute && (
                                                <div style={{ 
                                                    marginTop: '15px', 
                                                    padding: '12px', 
                                                    borderRadius: '8px',
                                                    backgroundColor: review.adminReply ? '#D4EDDA' : '#FFF3CD',
                                                    border: review.adminReply ? '1px solid #C3E6CB' : '1px solid #FFEEBA'
                                                }}>
                                                    <p style={{ margin: 0, fontSize: '13px', fontWeight: 'bold', color: review.adminReply ? '#155724' : '#856404' }}>
                                                        {review.adminReply ? "✅ Issue Resolved by Admin:" : "⏳ Issue Pending Admin Review"}
                                                    </p>
                                                    {review.adminReply && (
                                                        <p style={{ margin: '5px 0 0 0', fontSize: '13px', fontStyle: 'italic', color: '#155724' }}>
                                                            "{review.adminReply}"
                                                        </p>
                                                    )}
                                                </div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </section>
                        )}

                    </div>
                )}
            </main>
        </div>
    );
}
export default MyReviews;