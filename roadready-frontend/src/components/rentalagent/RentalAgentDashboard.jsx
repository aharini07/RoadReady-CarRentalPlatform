import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "../customer/CustomerDashboard.css";

function RentalAgentDashboard() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState("checkin");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // --- TAB 1: CHECK-IN/OUT STATE ---
    const [reservationId, setReservationId] = useState("");
    const [reservation, setReservation] = useState(null);
    const [checkInOutData, setCheckInOutData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [condition, setCondition] = useState("Good");
    const [fuelLevel, setFuelLevel] = useState("Full");
    const [remarks, setRemarks] = useState("");
    const [penaltyFee, setPenaltyFee] = useState(0);
    const [damageDesc, setDamageDesc] = useState("");

    // --- TAB 2: INVENTORY STATE ---
    const [cars, setCars] = useState([]);
    const [inventoryLoading, setInventoryLoading] = useState(false);

    const logout = () => { localStorage.clear(); navigate("/"); };

    // ==========================================
    // TAB 1: CHECK-IN / CHECK-OUT LOGIC
    // ==========================================
    const handleSearch = async (e) => {
        if (e) e.preventDefault();
        setLoading(true); setError(""); setReservation(null); setCheckInOutData(null); setSuccess("");

        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`http://localhost:8080/api/v1/reservations/${reservationId}/details`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Reservation not found or access denied.");
            const resData = await res.json();
            setReservation(resData);

            if (resData.status === "PICKED_UP") {
                const reportRes = await fetch(`http://localhost:8080/api/v1/check-in-out/${reservationId}/report`, {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (reportRes.ok) setCheckInOutData(await reportRes.json());
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handlePickup = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/check-in-out/${reservationId}/pickup`, {
                method: "POST",
                headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
                body: JSON.stringify({ pickupCondition: condition, fuelLevelBefore: fuelLevel, remarks })
            });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || "Failed to check-in");

            setSuccess("Car handed over successfully! Status is now PICKED_UP.");
            setReservation({ ...reservation, status: "PICKED_UP" });
            setTimeout(() => handleSearch(), 1000);
        } catch (err) { setError(err.message); }
    };

    const handleReturn = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/check-in-out/${reservationId}/return`, {
                method: "POST",
                headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
                body: JSON.stringify({
                    returnCondition: condition,
                    fuelLevelAfter: fuelLevel,
                    remarks,
                    penaltyFee: parseFloat(penaltyFee),
                    damageDescription: damageDesc
                })
            });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || "Failed to check-out");

            setSuccess(`Car returned successfully! Penalty Applied: ₹${penaltyFee}`);
            setReservation({ ...reservation, status: "COMPLETED" });
            
            if (penaltyFee > 0) fetchInventory();

        } catch (err) { setError(err.message); }
    };

    // ==========================================
    // TAB 2: INVENTORY MANAGEMENT LOGIC
    // ==========================================
    const fetchInventory = async () => {
        setInventoryLoading(true);
        try {
            const token = localStorage.getItem("token");
            const res = await fetch("http://localhost:8080/api/v1/cars/all", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (res.ok) setCars(await res.json());
        } catch (err) {
            console.error(err);
        } finally {
            setInventoryLoading(false);
        }
    };

    useEffect(() => {
        if (activeTab === "inventory") fetchInventory();
    }, [activeTab]);

    const updateCarStatus = async (carId, newStatus) => {
        try {
            const token = localStorage.getItem("token");
            const carToUpdate = cars.find(c => c.carId === carId);
            
            const payload = {
                carName: carToUpdate.carName,
                brand: carToUpdate.brand,
                model: carToUpdate.model,
                fuelType: carToUpdate.fuelType,
                seatingCapacity: carToUpdate.seatingCapacity,
                dailyRate: carToUpdate.dailyRate,
                status: newStatus
            };

            const response = await fetch(`http://localhost:8080/api/v1/cars/${carId}`, {
                method: "PUT",
                headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) throw new Error("Failed to update status");

            setSuccess(`Car status updated to ${newStatus}.`);
            fetchInventory();
        } catch (err) {
            setError(err.message);
        }
    };

    // Removed the icon from menuItems
    const menuItems = [
        { name: "Agent Dashboard", path: "/agent/dashboard" }
    ];

    return (
        <div className="customer-dashboard">
            <aside className="customer-sidebar">
                <div className="sidebar-logo"><span className="logo-road">Road</span><span className="logo-ready">Ready</span></div>
                <p className="menu-heading">AGENT MENU</p>
                <nav className="sidebar-menu">
                    {menuItems.map((item) => (
                        <Link key={item.name} to={item.path} className="menu-item active">
                            {item.name}
                        </Link>
                    ))}
                </nav>
                <div className="sidebar-bottom">
                    {/* Removed the arrow icon from logout */}
                    <button className="sidebar-logout" onClick={logout}>Logout</button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div><h2>Rental Agent Dashboard</h2><p>Manage fleet and customer rentals</p></div>
                </header>

                {/* TABS */}
                <div style={{ display: 'flex', gap: '15px', marginBottom: '20px' }}>
                    <button 
                        onClick={() => setActiveTab("checkin")}
                        style={{ padding: '10px 20px', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer',
                                 backgroundColor: activeTab === "checkin" ? '#16845c' : '#E6EBE3',
                                 color: activeTab === "checkin" ? 'white' : '#20352A' }}
                    >
                        Check-In / Check-Out
                    </button>
                    <button 
                        onClick={() => setActiveTab("inventory")}
                        style={{ padding: '10px 20px', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer',
                                 backgroundColor: activeTab === "inventory" ? '#16845c' : '#E6EBE3',
                                 color: activeTab === "inventory" ? 'white' : '#20352A' }}
                    >
                        Vehicle Inventory & Maintenance
                    </button>
                </div>

                {error && <div style={{ color: '#721C24', backgroundColor: '#F8D7DA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{error}</div>}
                {success && <div style={{ color: '#155724', backgroundColor: '#D4EDDA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{success}</div>}

                {/* ================= TAB 1: CHECK-IN/OUT ================= */}
                {activeTab === "checkin" && (
                    <>
                        <section className="dashboard-section" style={{ maxWidth: '600px' }}>
                            <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px', alignItems: 'flex-end' }}>
                                <div className="form-group" style={{ flex: 1 }}>
                                    <label>Reservation ID</label>
                                    <input type="number" className="form-input" placeholder="e.g. 12" value={reservationId} onChange={(e) => setReservationId(e.target.value)} required />
                                </div>
                                <button type="submit" className="btn-primary-green" disabled={loading}>{loading ? "Searching..." : "Find Booking"}</button>
                            </form>
                        </section>

                        {reservation && (
                            <section className="dashboard-section" style={{ maxWidth: '800px', marginTop: '20px' }}>
                                <h3 style={{ color: '#20352A', marginBottom: '15px' }}>Booking Details</h3>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '20px' }}>
                                    <p><strong>Customer:</strong> {reservation.customerUsername}</p>
                                    <p><strong>Car:</strong> {reservation.carName} ({reservation.carBrand})</p>
                                    <p><strong>Status:</strong> <span style={{ fontWeight: 'bold' }}>{reservation.status}</span></p>
                                </div>

                                {reservation.status === "CONFIRMED" && (
                                    <div style={{ backgroundColor: '#F9FAF8', padding: '20px', borderRadius: '10px', border: '1px solid #E6EBE3' }}>
                                        <h4 style={{ marginBottom: '15px' }}>Car Pickup (Check-In)</h4>
                                        <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
                                            <div className="form-group" style={{ flex: 1 }}>
                                                <label>Car Condition</label>
                                                <select className="form-input" value={condition} onChange={(e) => setCondition(e.target.value)}>
                                                    <option value="Good">Good (No Damage)</option>
                                                    <option value="Minor Scratches">Minor Scratches</option>
                                                    <option value="Damaged">Damaged</option>
                                                </select>
                                            </div>
                                            <div className="form-group" style={{ flex: 1 }}>
                                                <label>Fuel Level</label>
                                                <select className="form-input" value={fuelLevel} onChange={(e) => setFuelLevel(e.target.value)}>
                                                    <option value="Full">Full</option>
                                                    <option value="3/4">3/4</option>
                                                    <option value="Half">Half</option>
                                                </select>
                                            </div>
                                        </div>
                                        <button className="btn-primary-green" onClick={handlePickup} style={{ width: '100%' }}>Confirm Pickup</button>
                                    </div>
                                )}

                                {reservation.status === "PICKED_UP" && (
                                    <div style={{ backgroundColor: '#F9FAF8', padding: '20px', borderRadius: '10px', border: '1px solid #E6EBE3' }}>
                                        {checkInOutData && (
                                            <div style={{ backgroundColor: '#FFF3CD', padding: '15px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #FFEEBA' }}>
                                                <h5 style={{ marginBottom: '10px', color: '#856404' }}>Pickup Report:</h5>
                                                <p><strong>Condition:</strong> {checkInOutData.pickupCondition}</p>
                                                <p><strong>Fuel Level:</strong> {checkInOutData.fuelLevelBefore}</p>
                                            </div>
                                        )}
                                        <h4 style={{ marginBottom: '15px' }}>Car Return (Check-Out)</h4>
                                        <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
                                            <div className="form-group" style={{ flex: 1 }}>
                                                <label>Return Condition</label>
                                                <select className="form-input" value={condition} onChange={(e) => setCondition(e.target.value)}>
                                                    <option value="Good">Good (No Damage)</option>
                                                    <option value="Minor Scratches">Minor Scratches</option>
                                                    <option value="Damaged">Damaged</option>
                                                </select>
                                            </div>
                                            <div className="form-group" style={{ flex: 1 }}>
                                                <label>Fuel Level After</label>
                                                <select className="form-input" value={fuelLevel} onChange={(e) => setFuelLevel(e.target.value)}>
                                                    <option value="Full">Full</option>
                                                    <option value="3/4">3/4</option>
                                                    <option value="Half">Half</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
                                            <div className="form-group" style={{ flex: 1 }}>
                                                <label>Penalty Fee (₹)</label>
                                                <input type="number" min="0" className="form-input" value={penaltyFee} onChange={(e) => setPenaltyFee(e.target.value)} />
                                            </div>
                                            <div className="form-group" style={{ flex: 2 }}>
                                                <label>Damage Description</label>
                                                <input type="text" className="form-input" placeholder="Explain the damage..." value={damageDesc} onChange={(e) => setDamageDesc(e.target.value)} />
                                            </div>
                                        </div>
                                        <button className="btn-primary-green" onClick={handleReturn} style={{ width: '100%' }}>Confirm Return & Apply Penalty</button>
                                    </div>
                                )}

                                {reservation.status === "COMPLETED" && (
                                    <p style={{ color: '#155724', fontWeight: 'bold' }}>This rental is fully completed.</p>
                                )}
                            </section>
                        )}
                    </>
                )}

                {/* ================= TAB 2: INVENTORY ================= */}
                {activeTab === "inventory" && (
                    <section className="dashboard-section">
                        <h3 style={{ marginBottom: '20px', color: '#20352A' }}>Fleet Status</h3>
                        {inventoryLoading ? <p>Loading cars...</p> : (
                            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                <thead>
                                    <tr style={{ borderBottom: '2px solid #E6EBE3' }}>
                                        <th style={{ padding: '10px' }}>Car Name</th>
                                        <th style={{ padding: '10px' }}>Brand</th>
                                        <th style={{ padding: '10px' }}>Rate/Day</th>
                                        <th style={{ padding: '10px' }}>Status</th>
                                        <th style={{ padding: '10px' }}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {cars.map(car => (
                                        <tr key={car.carId} style={{ borderBottom: '1px solid #E6EBE3' }}>
                                            <td style={{ padding: '10px', fontWeight: 'bold' }}>{car.carName}</td>
                                            <td style={{ padding: '10px' }}>{car.brand}</td>
                                            <td style={{ padding: '10px' }}>₹{car.dailyRate}</td>
                                            <td style={{ padding: '10px' }}>
                                                <span style={{ 
                                                    padding: '5px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold',
                                                    backgroundColor: car.status === 'AVAILABLE' ? '#D4EDDA' : car.status === 'MAINTENANCE' ? '#FFF3CD' : '#F8D7DA',
                                                    color: car.status === 'AVAILABLE' ? '#155724' : car.status === 'MAINTENANCE' ? '#856404' : '#721C24'
                                                }}>
                                                    {car.status}
                                                </span>
                                            </td>
                                            <td style={{ padding: '10px' }}>
                                                {car.status === 'AVAILABLE' ? (
                                                    <button onClick={() => updateCarStatus(car.carId, 'MAINTENANCE')} className="btn-outline-red" style={{ padding: '5px 10px', fontSize: '12px' }}>
                                                        Report Maintenance
                                                    </button>
                                                ) : car.status === 'MAINTENANCE' ? (
                                                    <button onClick={() => updateCarStatus(car.carId, 'AVAILABLE')} className="btn-primary-green" style={{ padding: '5px 10px', fontSize: '12px' }}>
                                                        Mark as Repaired
                                                    </button>
                                                ) : null}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </section>
                )}

            </main>
        </div>
    );
}
export default RentalAgentDashboard;