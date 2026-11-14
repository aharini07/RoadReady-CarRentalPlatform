// src/components/admin/ReviewsPage.jsx
import { useDispatch } from "react-redux";
import { replyToDispute } from "./store/action/AdminAction";

function ReviewsPage({ reviews, setError, setSuccess }) {
    const dispatch = useDispatch();

    const handleResolveDispute = async (reviewId) => {
        const replyText = document.getElementById(`reply-${reviewId}`).value;
        if (!replyText) return alert("Please type a resolution message first.");
        
        dispatch(replyToDispute(reviewId, replyText));
        setSuccess("Dispute resolved successfully!");
    };

    const openDisputes = reviews.filter(r => r.isDispute && !r.adminReply);
    const otherReviews = reviews.filter(r => !r.isDispute || r.adminReply);

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            
            <section className="dashboard-section" style={{ border: openDisputes.length > 0 ? '2px solid #D9534F' : '1px solid #E6EBE3' }}>
                <h3 style={{ marginBottom: '20px', color: '#D9534F' }}>🚨 Open Customer Disputes ({openDisputes.length})</h3>
                {openDisputes.length === 0 ? (
                    <p style={{ color: '#16845c', fontWeight: 'bold' }}>✅ No open disputes. Everything is resolved!</p>
                ) : (
                    openDisputes.map(rev => (
                        <div key={rev.reviewId} style={{ padding: '20px', border: '1px solid #F5C6CB', borderRadius: '10px', backgroundColor: '#F8D7DA', marginBottom: '15px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                                <div>
                                    <h4 style={{ margin: 0, color: '#721C24' }}>{rev.carName}</h4>
                                    <small style={{ color: '#721C24' }}>Reported by {rev.customerUsername} on {new Date(rev.reviewDate).toLocaleDateString()}</small>
                                </div>
                                <span style={{ color: '#721C24', fontSize: '18px', fontWeight: 'bold' }}>{"★".repeat(rev.rating)}{"☆".repeat(5 - rev.rating)}</span>
                            </div>
                            <p style={{ margin: '0 0 15px 0', color: '#721C24', fontStyle: 'italic', fontSize: '15px' }}>"{rev.comment}"</p>
                            
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <input id={`reply-${rev.reviewId}`} type="text" className="form-input" placeholder="Type resolution message here..." style={{ flex: 1 }} />
                                <button onClick={() => handleResolveDispute(rev.reviewId)} className="btn-primary-green" style={{ backgroundColor: '#D9534F', borderColor: '#D9534F' }}>
                                    Resolve
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </section>

            <section className="dashboard-section">
                <h3 style={{ marginBottom: '20px' }}>All Feedback & Resolved Disputes</h3>
                {otherReviews.length === 0 ? <p>No reviews submitted yet.</p> : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {otherReviews.map(rev => (
                            <div key={rev.reviewId} style={{ padding: '20px', border: '1px solid #E6EBE3', borderRadius: '10px', backgroundColor: '#F9FAF8' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                                    <div>
                                        <h4 style={{ margin: 0, color: '#20352A' }}>{rev.carName}</h4>
                                        <small style={{ color: '#778078' }}>By {rev.customerUsername} on {new Date(rev.reviewDate).toLocaleDateString()}</small>
                                    </div>
                                    <span style={{ color: '#F5A623', fontSize: '18px', fontWeight: 'bold' }}>{"★".repeat(rev.rating)}{"☆".repeat(5 - rev.rating)}</span>
                                </div>
                                <p style={{ margin: 0, color: '#47534d', fontStyle: 'italic' }}>"{rev.comment || 'No comment provided.'}"</p>
                                
                                {rev.isDispute && rev.adminReply && (
                                    <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#D4EDDA', borderRadius: '8px', border: '1px solid #C3E6CB' }}>
                                        <strong style={{ color: '#155724', fontSize: '13px' }}>✅ Resolved by Admin:</strong>
                                        <p style={{ margin: 0, color: '#155724', fontSize: '13px', fontStyle: 'italic' }}>"{rev.adminReply}"</p>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
export default ReviewsPage;