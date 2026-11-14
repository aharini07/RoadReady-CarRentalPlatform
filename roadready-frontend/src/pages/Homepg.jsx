import { Link } from "react-router";
import "./Home.css";
import Homepg from "../assets/Homepg_img.png";
function Home() {    

    return (
        <div className="home-page">

            <nav className="navbar">
                <Link to="/" className="brand">
                    Road<span>Ready</span>
                </Link>

                <div className="nav-links">
                    <a href="#home">Home</a>
                
                    <a href="#about">About Us</a>
                </div>

                {/* Navigation actions mapped to Login and Sign Up routes */}
                <div className="nav-actions">
                    <Link to="/login" className="login-link">
                        Login
                    </Link>
                    <Link to="/signup" className="signup-btn">
                        Sign Up
                    </Link>
                </div>
            </nav>

            <section className="hero-section" id="home">
                <div className="hero-content">
                    <p className="eyebrow">
                        YOUR JOURNEY, YOUR WAY
                    </p>

                    <h1>
                        Find Your Perfect Ride.
                        <br />
                        <span>Start Your Journey.</span>
                    </h1>

                    <p className="hero-description">
                        Discover cars for every journey.
                        Book your ride with ease and enjoy
                        a comfortable, hassle-free experience.
                    </p>

                    {/* <button
                        className="primary-btn"
                        onClick={exploreCars}
                    >
                        Explore Cars →
                    </button> */}

                    <div className="hero-stats">
                        <div>
                            <h3>Easy</h3>
                            <p>Online Booking</p>
                        </div>
                        <div>
                            <h3>Flexible</h3>
                            <p>Rental Plans</p>
                        </div>
                        <div>
                            <h3>Reliable</h3>
                            <p>Vehicle Checks</p>
                        </div>
                    </div>
                </div>

                <div className="hero-visual">
                    <img
                        src={Homepg}
                        alt="RoadReady Car"
                        className="hero-car-image"
                    />
                </div>
            </section>

            

            <section className="about-section" id="about">
                <p className="eyebrow">WHY ROADREADY?</p>
                <h2>More Than Just a Car Rental</h2>

                <div className="benefit-grid">
                    <div>
                        
                        <h3>Simple Booking</h3>
                        <p>Manage your rental journey online.</p>
                    </div>

                    <div>
                        
                        <h3>Vehicle Inspection</h3>
                        <p>
                            Record vehicle condition before pickup
                            and after return.
                        </p>
                    </div>

                    <div>
                        
                        <h3>Booking Management</h3>
                        <p>Keep track of your reservations and returns.</p>
                    </div>
                </div>
            </section>

            <footer className="footer">
                <Link to="/" className="brand">
                    Road<span>Ready</span>
                </Link>
                <p>Every journey starts with the right ride.</p>
                <p>© 2026 RoadReady. All rights reserved.</p>
            </footer>

        </div>
    );
}

export default Home;