import { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/BookCar.css";

function BookCar() {
    const navigate = useNavigate();
    const { carId } = useParams(); 
    
    const [pickupLocation, setPickupLocation] = useState("RoadReady Main Branch");
    const [dropoffLocation, setDropoffLocation] = useState("RoadReady Main Branch");
    
    const [isCustomPickup, setIsCustomPickup] = useState(false);
    const [isCustomDropoff, setIsCustomDropoff] = useState(false);

    const [pickupDate, setPickupDate] = useState("");
    const [pickupTime, setPickupTime] = useState("");
    const [dropoffDate, setDropoffDate] = useState("");
    const [numPassengers, setNumPassengers] = useState(1);
    
    const [priceEstimate, setPriceEstimate] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (pickupDate && dropoffDate) {
            const fetchEstimate = async () => {
                try {
                    const token = localStorage.getItem("token");
                    const response = await fetch("http://localhost:8080/api/v1/pricing/estimate", {
                        method: "POST",
                        headers: {
                            "Authorization": `Bearer ${token}`,
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            carId: parseInt(carId),
                            pickupDate: pickupDate,
                            dropoffDate: dropoffDate
                        })
                    });
                    
                    if (response.ok) {
                        const data = await response.json();
                        setPriceEstimate(data);
                        setError("");
                    } else {
                        setPriceEstimate(null);
                    }
                } catch (err) {
                    console.error("Estimate failed");
                }
            };
            fetchEstimate();
        }
    }, [pickupDate, dropoffDate, carId]);

    const handleBook = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const token = localStorage.getItem("token");
            if (!token) {
                throw new Error("No token found. Please log in again.");
            }

            const finalPickup = pickupLocation === "Custom" ? "" : pickupLocation;
            const finalDropoff = dropoffLocation === "Custom" ? "" : dropoffLocation;

            if (!finalPickup || !finalDropoff) {
                throw new Error("Please enter your custom location address.");
            }

            const formattedTime = pickupTime.length === 5 ? `${pickupTime}:00` : pickupTime;

            const response = await fetch("http://localhost:8080/api/v1/reservations/book", {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    carId: parseInt(carId),
                    pickupLocation: finalPickup,
                    dropoffLocation: finalDropoff,
                    pickupDate: pickupDate,
                    pickupTime: formattedTime,
                    dropoffDate: dropoffDate,
                    numPassengers: parseInt(numPassengers)
                })
            });

            const responseText = await response.text();
            
            if (!response.ok) {
                let errorMsg = "Failed to book car";
                if (responseText) {
                    try {
                        const errorData = JSON.parse(responseText);
                        errorMsg = errorData.message || errorData.description || errorMsg;
                    } catch (parseErr) {
                        errorMsg = `Server Error: ${response.status} ${response.statusText}`;
                    }
                }
                throw new Error(errorMsg);
            }

            alert("Booking Created Successfully! Please proceed to payment.");
            navigate("/customer/bookings");

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const logout = () => { localStorage.clear(); navigate("/"); };

    // Removed icons from menuItems
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
                        <Link key={item.name} to={item.path} className="menu-item">
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
                    <div><h2>Book Your Car</h2><p>Select your locations and dates to complete the booking</p></div>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px', fontWeight: 'bold' }}>{error}</div>}

                <section className="dashboard-section" style={{ maxWidth: '600px' }}>
                    <form onSubmit={handleBook} className="booking-form">
                        
                        <div className="form-group">
                            <label>Pickup Location</label>
                            <select 
                                className="form-input" 
                                value={pickupLocation} 
                                onChange={(e) => {
                                    setPickupLocation(e.target.value);
                                    setIsCustomPickup(e.target.value === "Custom");
                                }} 
                                required
                            >
                                <optgroup label="Chennai City Hubs">
                                    <option value="RoadReady Main Branch">RoadReady Main Branch</option>
                                    <option value="Chennai Airport">Chennai Airport (MAA)</option>
                                    <option value="Chennai Central">Chennai Central Railway Station</option>
                                    <option value="T Nagar">T. Nagar</option>
                                    <option value="OMR IT Corridor">OMR IT Corridor</option>
                                </optgroup>
                                <optgroup label="Outstation">
                                    <option value="Pondicherry">Pondicherry</option>
                                    <option value="Mahabalipuram">Mahabalipuram</option>
                                    <option value="Tirupati">Tirupati</option>
                                </optgroup>
                                <option value="Custom">Other (Type Custom Address)</option>
                            </select>
                            
                            {isCustomPickup && (
                                <input 
                                    type="text" 
                                    className="form-input" 
                                    placeholder="Enter your full pickup address..." 
                                    value={pickupLocation === "Custom" ? "" : pickupLocation}
                                    onChange={(e) => setPickupLocation(e.target.value)}
                                    required
                                    style={{ marginTop: '10px' }}
                                />
                            )}
                        </div>

                        <div className="form-group">
                            <label>Drop-off Location</label>
                            <select 
                                className="form-input" 
                                value={dropoffLocation} 
                                onChange={(e) => {
                                    setDropoffLocation(e.target.value);
                                    setIsCustomDropoff(e.target.value === "Custom");
                                }} 
                                required
                            >
                                <optgroup label="Chennai City Hubs">
                                    <option value="RoadReady Main Branch">RoadReady Main Branch</option>
                                    <option value="Chennai Airport">Chennai Airport (MAA)</option>
                                    <option value="Chennai Central">Chennai Central Railway Station</option>
                                    <option value="T Nagar">T. Nagar</option>
                                    <option value="OMR IT Corridor">OMR IT Corridor</option>
                                </optgroup>
                                <optgroup label="Outstation">
                                    <option value="Pondicherry">Pondicherry</option>
                                    <option value="Mahabalipuram">Mahabalipuram</option>
                                    <option value="Tirupati">Tirupati</option>
                                </optgroup>
                                <option value="Custom">Other (Type Custom Address)</option>
                            </select>

                            {isCustomDropoff && (
                                <input 
                                    type="text" 
                                    className="form-input" 
                                    placeholder="Enter your full drop-off address..." 
                                    value={dropoffLocation === "Custom" ? "" : dropoffLocation}
                                    onChange={(e) => setDropoffLocation(e.target.value)}
                                    required
                                    style={{ marginTop: '10px' }}
                                />
                            )}
                        </div>

                        <div className="form-group">
                            <label>Pickup Date</label>
                            <input type="date" className="form-input" required value={pickupDate} onChange={(e) => setPickupDate(e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label>Pickup Time</label>
                            <input type="time" className="form-input" required value={pickupTime} onChange={(e) => setPickupTime(e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label>Dropoff Date</label>
                            <input type="date" className="form-input" required value={dropoffDate} onChange={(e) => setDropoffDate(e.target.value)} min={pickupDate} />
                        </div>

                        <div className="form-group">
                            <label>Number of Passengers</label>
                            <input type="number" min="1" className="form-input" required value={numPassengers} onChange={(e) => setNumPassengers(e.target.value)} />
                        </div>

                        {priceEstimate && (
                            <div className="price-estimate-box">
                                <h4>Price Estimate</h4>
                                <p>Subtotal: Rs.{priceEstimate.subtotal}</p>
                                <p>Tax (5%): Rs.{priceEstimate.tax}</p>
                                <h3>Total: Rs.{priceEstimate.totalAmount}</h3>
                            </div>
                        )}

                        <button type="submit" className="btn-primary-green" disabled={loading} style={{ marginTop: '20px', width: '100%' }}>
                            {loading ? "Processing..." : "Confirm Booking"}
                        </button>
                    </form>
                </section>
            </main>
        </div>
    );
}
export default BookCar;