import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "../CustomerDashboard.css";
import "./Styles/BrowseCars.css";

// 1. IMPORT THE IMAGES DIRECTLY HERE
import hyundaiCreta from "../../../assets/Hyundai_Creta.png";
import hyundaiI20 from "../../../assets/Hyundai_i20.png";
import marutiSwift from "../../../assets/Maruti_swift_vxi.png";
import toyotaFortuner from "../../../assets/Toyoto_fortuner.png";
import toyotaInnova from "../../../assets/Toyoto_innova_crysta_gx.png";

function BrowseCars() {
    const navigate = useNavigate();
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isRecommended, setIsRecommended] = useState(false);

    const [showSurvey, setShowSurvey] = useState(false);
    const [passengers, setPassengers] = useState("2");
    const [budget, setBudget] = useState("100");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");
    const [isSearching, setIsSearching] = useState(false);

    const carImages = {
        "i20": hyundaiI20,
        "Creta": hyundaiCreta,
        "Swift": marutiSwift,
        "Innova Crysta": toyotaInnova,
        "Fortuner": toyotaFortuner,
    };

    const fetchAllCars = async (page = 0) => {
        setLoading(true);
        setError("");
        setIsRecommended(false);
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/cars/search?page=${page}&size=6`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!response.ok) throw new Error("Failed to fetch cars");
            const data = await response.json();
            
            setCars(data.content || []);
            setCurrentPage(data.page);
            setTotalPages(data.totalPages);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllCars(0);
    }, []);

    const handleNext = () => {
        if (currentPage < totalPages - 1) {
            fetchAllCars(currentPage + 1);
        }
    };

    const handlePrevious = () => {
        if (currentPage > 0) {
            fetchAllCars(currentPage - 1);
        }
    };

    const handleRecommend = async (e) => {
        e.preventDefault();
        setIsSearching(true);
        setError("");

        try {
            const token = localStorage.getItem("token");
            
            const url = `http://localhost:8080/api/v1/cars/available?pickupDate=${fromDate}&returnDate=${toDate}&passengers=${passengers}&size=50`;
            
            const response = await fetch(url, {
                headers: { "Authorization": `Bearer ${token}` }
            });

            if (!response.ok) throw new Error("Failed to fetch recommendations");

            const data = await response.json();
            
            let finalCars = data.content.filter(car => car.dailyRate <= parseFloat(budget));
            
            if (finalCars.length === 0 && data.content.length > 0) {
                const sortedCars = data.content.sort((a, b) => a.dailyRate - b.dailyRate);
                finalCars = sortedCars.slice(0, 3);
                alert(`No cars found under Rs.${budget}/day. Here are our cheapest available cars for those dates.`);
            } else if (finalCars.length === 0 && data.content.length === 0) {
                alert("All cars are booked for these dates, or no car has that many seats. Try different dates.");
            }
            
            setCars(finalCars);
            setIsRecommended(true);
            setShowSurvey(false);

        } catch (err) {
            setError(err.message);
        } finally {
            setIsSearching(false);
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
                        <Link key={item.name} to={item.path} className={item.name === "Browse Cars" ? "menu-item active" : "menu-item"}>
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
                    <div>
                        <h2>Browse Cars</h2>
                        <p>Find your perfect ride for your next journey</p>
                    </div>
                    <button 
                        className="btn-primary-green" 
                        onClick={() => setShowSurvey(true)}
                        style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                        ✨ Find My Perfect Car
                    </button>
                </header>

                {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

                {loading ? (
                    <p>Loading cars...</p>
                ) : (
                    <section className="car-grid">
                        {cars.length === 0 ? (
                            <p>No cars available matching your criteria.</p>
                        ) : (
                            cars.map((car) => {
                                const carImage = carImages[car.carName];

                                return (
                                    <div className="car-card" key={car.carId}>
                                        
                                        {carImage ? (
                                            <img 
                                                src={carImage} 
                                                alt={car.carName} 
                                                className="car-image-real" 
                                            />
                                        ) : (
                                            <div className="car-image-placeholder"></div>
                                        )}

                                        <div className="car-info">
                                            <h3>{car.carName}</h3>
                                            <p>{car.brand} • {car.model} • {car.fuelType}</p>
                                            <p>{car.seatingCapacity} Seats</p>
                                            <div className="car-price-row">
                                                <span className="price">Rs.{car.dailyRate} / day</span>
                                                <Link to={`/customer/book-car/${car.carId}`} className="btn-primary-green">
                                                    Book Now
                                                </Link>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </section>
                )}

                {!loading && !isRecommended && totalPages > 1 && (
                    <div className="cars-pagination">
                        <button
                            type="button"
                            className="pagination-button"
                            onClick={handlePrevious}
                            disabled={currentPage === 0}
                        >
                            Previous
                        </button>

                        <span className="pagination-info">
                            Page {currentPage + 1} of {totalPages}
                        </span>

                        <button
                            type="button"
                            className="pagination-button"
                            onClick={handleNext}
                            disabled={currentPage >= totalPages - 1}
                        >
                            Next
                        </button>
                    </div>
                )}

            
                {showSurvey && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <button className="modal-close" onClick={() => setShowSurvey(false)}>✕</button>
                            <h2>Find Your Perfect Car </h2>
                            <p style={{ marginBottom: '20px', color: '#778078' }}>Tell us your requirements and we'll filter the best cars for you.</p>
                            
                            <form onSubmit={handleRecommend} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                                <div className="form-group">
                                    <label>Passengers</label>
                                    <select className="form-input" value={passengers} onChange={(e) => setPassengers(e.target.value)}>
                                        <option value="1">1 Person</option>
                                        <option value="2">2 People</option>
                                        <option value="4">4 People</option>
                                        <option value="5">5 People</option>
                                        <option value="7">7+ People</option>
                                    </select>
                                </div>

                                <div className="form-group">
                                    <label>Max Daily Budget (Rs)</label>
                                    <input type="number" min="500" className="form-input" value={budget} onChange={(e) => setBudget(e.target.value)} required />
                                </div>

                                <div className="form-group">
                                    <label>From Date</label>
                                    <input type="date" className="form-input" value={fromDate} onChange={(e) => setFromDate(e.target.value)} required />
                                </div>

                                <div className="form-group">
                                    <label>To Date</label>
                                    <input 
                                        type="date" 
                                        className="form-input" 
                                        value={toDate} 
                                        onChange={(e) => setToDate(e.target.value)} 
                                        min={fromDate ? new Date(new Date(fromDate).getTime() + 86400000).toISOString().split('T')[0] : ""} 
                                        required 
                                    />
                                </div>

                                <div style={{ gridColumn: 'span 2', marginTop: '15px' }}>
                                    <button type="submit" className="btn-primary-green" style={{ width: '100%' }} disabled={isSearching}>
                                        {isSearching ? "Searching..." : "Show Recommended Cars"}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}
export default BrowseCars;