// src/components/admin/OverviewPage.jsx
function OverviewPage({ stats }) {
    if (!stats) return <p>Loading reports...</p>;

    return (
        <section className="dashboard-section">
            <h3 style={{ marginBottom: '20px' }}>Business Reports</h3>
            <div className="stats-grid">
                <div className="stat-card"><span className="stat-icon">👥</span><h3>{stats.totalUsers}</h3><p>Total Users</p></div>
                <div className="stat-card"><span className="stat-icon">🚘</span><h3>{stats.totalCars}</h3><p>Total Cars</p></div>
                <div className="stat-card"><span className="stat-icon">📅</span><h3>{stats.totalReservations}</h3><p>Total Bookings</p></div>
                <div className="stat-card" style={{ backgroundColor: '#EAF4E9', borderColor: '#397A45' }}>
                    <span className="stat-icon">💰</span><h3 style={{ color: '#16845c' }}>₹{stats.totalRevenue}</h3><p style={{ color: '#16845c' }}>Total Revenue</p>
                </div>
            </div>
        </section>
    );
}
export default OverviewPage;