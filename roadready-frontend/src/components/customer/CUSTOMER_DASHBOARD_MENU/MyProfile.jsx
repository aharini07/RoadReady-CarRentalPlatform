import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import "./Styles/MyProfile.css";

function MyProfile() {
    const navigate = useNavigate();
    
    const [user, setUser] = useState({ username: "", role: "" });
    const [profile, setProfile] = useState({ name: "", phoneNo: "", address: "", city: "", country: "" });
    
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const getHeaders = () => ({
        "Authorization": `Bearer ${localStorage.getItem("token")}`,
        "Content-Type": "application/json"
    });

    useEffect(() => {
        setSuccess("");
        setError("");
        
        const fetchData = async () => {
            try {
                const userRes = await fetch("http://localhost:8080/api/v1/auth/user-details", { headers: getHeaders() });
                if (userRes.ok) setUser(await userRes.json());

                const profRes = await fetch("http://localhost:8080/api/v1/auth/profile", { headers: getHeaders() });
                if (profRes.ok) {
                    const data = await profRes.json();
                    setProfile({
                        name: data.name || "",
                        phoneNo: data.phoneNo || data.phone || "",
                        address: data.address || "",
                        city: data.city || "",
                        country: data.country || ""
                    });
                }
            } catch (err) {
                setError("Failed to load profile.");
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleSave = async () => {
        console.log("=== SENDING TO BACKEND ===");
        console.log(JSON.stringify(profile));

        setError(""); 
        setSuccess("");

        try {
            const response = await fetch("http://localhost:8080/api/v1/auth/profile", {
                method: "PUT",
                headers: getHeaders(),
                body: JSON.stringify(profile)
            });

            if (!response.ok) throw new Error("Failed to update profile");

            setSuccess("Profile updated successfully!");
            setIsEditing(false);
            setTimeout(() => setSuccess(""), 3000);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        console.log(`Field changed: ${name} = ${value}`);
        setProfile(prev => ({ ...prev, [name]: value }));
    };

    const logout = () => { localStorage.clear(); navigate("/"); };

    // Removed the icon property from menuItems
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
                        <Link key={item.name} to={item.path} className={item.name === "My Profile" ? "menu-item active" : "menu-item"}>
                            {item.name}
                        </Link>
                    ))}
                </nav>
                <div className="sidebar-bottom">
                    {/* Removed the icon span from the logout button */}
                    <button className="sidebar-logout" onClick={logout}>Logout</button>
                </div>
            </aside>

            <main className="dashboard-main">
                <header className="dashboard-header">
                    <div><h2>My Profile</h2><p>Manage your account details</p></div>
                </header>

                {error && <div style={{ color: '#721C24', backgroundColor: '#F8D7DA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{error}</div>}
                {success && <div style={{ color: '#155724', backgroundColor: '#D4EDDA', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{success}</div>}

                {loading ? <p>Loading...</p> : (
                    <section className="dashboard-section" style={{ maxWidth: '700px' }}>
                        <div className="profile-form">
                            
                            <div className="form-group">
                                <label>Username</label>
                                <input type="text" value={user.username} className="profile-input" disabled />
                            </div>
                            
                            <div className="form-group">
                                <label>Role</label>
                                <input type="text" value={user.role} className="profile-input" disabled />
                            </div>

                            <div className="form-group">
                                <label>Full Name</label>
                                <input 
                                    type="text" 
                                    name="name" 
                                    value={profile.name} 
                                    onChange={handleChange} 
                                    className="profile-input" 
                                    disabled={!isEditing} 
                                />
                            </div>

                            <div className="form-group">
                                <label>Phone Number</label>
                                <input 
                                    type="text" 
                                    name="phoneNo" 
                                    value={profile.phoneNo} 
                                    onChange={handleChange} 
                                    className="profile-input" 
                                    disabled={!isEditing} 
                                />
                            </div>

                            <div className="form-group" style={{ gridColumn: 'span 2' }}>
                                <label>Address</label>
                                <input 
                                    type="text" 
                                    name="address" 
                                    value={profile.address} 
                                    onChange={handleChange} 
                                    className="profile-input" 
                                    disabled={!isEditing} 
                                />
                            </div>

                            <div className="form-group">
                                <label>City</label>
                                <input 
                                    type="text" 
                                    name="city" 
                                    value={profile.city} 
                                    onChange={handleChange} 
                                    className="profile-input" 
                                    disabled={!isEditing} 
                                />
                            </div>

                            <div className="form-group">
                                <label>Country</label>
                                <input 
                                    type="text" 
                                    name="country" 
                                    value={profile.country} 
                                    onChange={handleChange} 
                                    className="profile-input" 
                                    disabled={!isEditing} 
                                />
                            </div>

                            <div style={{ gridColumn: 'span 2', marginTop: '20px', display: 'flex', gap: '15px' }}>
                                {!isEditing ? (
                                    <button 
                                        type="button" 
                                        onClick={() => { setIsEditing(true); setSuccess(""); setError(""); }} 
                                        className="btn-primary-green"
                                    >
                                        Edit Profile
                                    </button>
                                ) : (
                                    <>
                                        <button 
                                            type="button" 
                                            onClick={handleSave} 
                                            className="btn-primary-green"
                                        >
                                            Save Changes
                                        </button>
                                        <button 
                                            type="button" 
                                            onClick={() => { setIsEditing(false); setSuccess(""); setError(""); }} 
                                            className="btn-outline-red"
                                        >
                                            Cancel
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
}
export default MyProfile;