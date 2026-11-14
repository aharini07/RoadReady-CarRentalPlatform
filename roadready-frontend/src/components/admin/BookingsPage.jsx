// src/components/admin/BookingsPage.jsx
import { useDispatch } from "react-redux";
import { cancelAdminBooking } from "./store/action/AdminAction";

function BookingsPage({ allBookings, setError, setSuccess }) {
    const dispatch = useDispatch();

    const handleAdminCancel = async (reservationId) => {
        if (!window.confirm("Are you sure you want to FORCE CANCEL this booking?")) return;
        dispatch(cancelAdminBooking(reservationId));
        setSuccess(`Booking #${reservationId} was force cancelled.`);
    };

    return (
        <section className="dashboard-section">
            <h3 style={{ marginBottom: '20px' }}>All System Bookings</h3>
            {allBookings.length === 0 ? <p>No bookings found.</p> : (
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '800px' }}>
                        <thead>
                            <tr style={{ borderBottom: '2px solid #E6EBE3' }}>
                                <th style={{ textAlign: 'left', padding: '10px' }}>ID</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Customer</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Car</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Dates</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Amount</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Status</th>
                                <th style={{ textAlign: 'left', padding: '10px' }}>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {allBookings.map(b => (
                                <tr key={b.reservationId} style={{ borderBottom: '1px solid #E6EBE3' }}>
                                    <td style={{ padding: '10px' }}>#{b.reservationId}</td>
                                    <td style={{ padding: '10px', fontWeight: 'bold' }}>{b.customerUsername}</td>
                                    <td style={{ padding: '10px' }}>{b.carName}</td>
                                    <td style={{ padding: '10px', fontSize: '12px' }}>{b.pickupDate} <br/> {b.dropoffDate}</td>
                                    <td style={{ padding: '10px' }}>₹{b.totalAmount}</td>
                                    <td style={{ padding: '10px' }}>
                                        <span style={{ 
                                            padding: '4px 8px', borderRadius: '12px', fontSize: '12px',
                                            backgroundColor: b.status === 'CONFIRMED' ? '#D4EDDA' : b.status === 'CANCELLED' ? '#F8D7DA' : b.status === 'PENDING' ? '#FFF3CD' : '#CCE5FF',
                                            color: b.status === 'CONFIRMED' ? '#155724' : b.status === 'CANCELLED' ? '#721C24' : b.status === 'PENDING' ? '#856404' : '#004085',
                                        }}>
                                            {b.status}
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px' }}>
                                        {(b.status === "CONFIRMED" || b.status === "PENDING") && (
                                            <button onClick={() => handleAdminCancel(b.reservationId)} style={{ color: '#D9534F', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>
                                                Force Cancel
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}
export default BookingsPage;