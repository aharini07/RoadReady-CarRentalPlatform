import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../config";
import axios from "axios";
import "./SignUp.css";

function SignUp() {
    // We keep the fields, but only username, password, and role will be saved for now
    const [name, setName] = useState("")
    const [email, setEmail] = useState("")
    const [phoneNo, setPhoneNo] = useState("")
    const [address, setAddress] = useState("")
    const [city, setCity] = useState("")
    const [country, setCountry] = useState("")
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [role, setRole] = useState("CUSTOMER")
    const [errMsg, setErrMsg] = useState("")
    const navigate = useNavigate()
    
    const signUpApi = API_SERVER_URL_DEV + "/api/v1/users"

    const processSignup = async (e) => {
        e.preventDefault()
        setErrMsg("")
        
        let userDto = {
            'username': username,
            'password': password,
            'role': role
        }

        try {
            await axios.post(signUpApi, userDto)
            navigate("/login")
        }
        catch(err){
            setErrMsg(err?.response?.data?.message || "Registration failed")
        }
    }

    return(
        <div className="container signup-page-wrapper">
            <div className="row justify-content-center w-100 m-0">
                <div className="col-12 col-sm-10 col-md-8 col-lg-6 col-xl-5">
                    <div className="card signup-card shadow-sm border-0 rounded-4 p-4 p-sm-5 bg-white">
                        <div className="card-body p-0">
                            <div className="text-center mb-4">
                                <h1 className="h4 fw-bold signup-title mb-1">Road<span>Ready</span></h1>
                                <p className="text-muted small">Please enter your details to sign up.</p>
                            </div>
                            <div className="text-center mb-3">
                                {errMsg && <p style={{ color: 'red', fontSize: '0.9rem' }}>{errMsg}</p>}
                            </div>
                            <form onSubmit={processSignup}>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Full Name</label>
                                    <input type="text" className="form-control form-control-lg" required
                                        value={name} onChange={(e) => setName(e.target.value)} />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Email</label>
                                    <input type="email" className="form-control form-control-lg" required
                                        value={email} onChange={(e) => setEmail(e.target.value)} />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Phone Number</label>
                                    <input type="tel" className="form-control form-control-lg" required placeholder="e.g., 9000000123"
                                        value={phoneNo} onChange={(e) => setPhoneNo(e.target.value)} />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Address</label>
                                    <input type="text" className="form-control form-control-lg" required placeholder="Street address"
                                        value={address} onChange={(e) => setAddress(e.target.value)} />
                                </div>
                                <div className="row">
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label small fw-semibold text-secondary">City</label>
                                        <input type="text" className="form-control form-control-lg" required
                                            value={city} onChange={(e) => setCity(e.target.value)} />
                                    </div>
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label small fw-semibold text-secondary">Country</label>
                                        <input type="text" className="form-control form-control-lg" required
                                            value={country} onChange={(e) => setCountry(e.target.value)} />
                                    </div>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Username</label>
                                    <input type="text" className="form-control form-control-lg" required
                                        value={username} onChange={(e) => setUsername(e.target.value)} />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Password</label>
                                    <input type="password" className="form-control form-control-lg" placeholder="••••••••" required
                                        value={password} onChange={(e) => setPassword(e.target.value)} />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Select Role</label>
                                    <select className="form-select form-control-lg" value={role} onChange={(e) => setRole(e.target.value)}>
                                        <option value="CUSTOMER">Customer</option>
                                        <option value="RENTAL_AGENT">Rental Agent</option>
                                        <option value="ADMIN">Administrator</option>
                                    </select>
                                </div>
                                <div className="d-grid mb-3">
                                    <button type="submit" className="btn btn-primary btn-lg fw-semibold">Sign up</button>
                                </div>
                                <div className="text-center">
                                    <p className="small text-muted mb-0">Have an account? <Link to="/login" className="text-primary text-decoration-none fw-semibold">Login</Link></p>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default SignUp;