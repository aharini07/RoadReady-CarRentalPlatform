
import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { API_SERVER_URL_DEV } from "../../config";
import axios from "axios";
import "./Login.css";

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errMsg, setErrMsg] = useState("");
    const [errComment, setErrComment] = useState("");
    const [loading, setLoading] = useState(false);

    const apiUrl = API_SERVER_URL_DEV + "/api/v1/auth/login";
    const userDetailsUrl =
        API_SERVER_URL_DEV + "/api/v1/auth/user-details";

    const navigate = useNavigate();

    const processLogin = async (e) => {
        e.preventDefault();

        setErrMsg("");
        setErrComment("");
        setLoading(true);

        try {
            const response = await axios.post(apiUrl, {
                username: username.trim(),
                password: password
            });

            const jwtToken = response.data.token;

            if (!jwtToken) {
                setErrMsg("Login failed: no authentication token received.");
                return;
            }

            localStorage.setItem("token", jwtToken);
            localStorage.setItem("username", username.trim());

            // Fetch the user's role using the JWT
            const resp = await axios.get(userDetailsUrl, {
                headers: {
                    Authorization: `Bearer ${jwtToken}`
                }
            });

            const role = resp.data.role;

            if (!role) {
                setErrMsg("Unable to retrieve your user role.");
                return;
            }

            localStorage.setItem("role", role);

            // Role-based navigation
            switch (role) {
                case "CUSTOMER":
                     navigate("/customer/dashboard");
                    break;
                case "ADMIN":
                    navigate("/admin/dashboard");
                    break;

                case "RENTAL_AGENT":
                    navigate("/rental-agent/dashboard");
                    break;

                default:
                    setErrMsg("Something went wrong with role routing.");
                    break;
            }

        } catch (err) {
            console.error(
                "Login error:",
                err.response?.status,
                err.response?.data || err.message
            );

            const status = err.response?.status;

            if (status === 401) {
                setErrMsg("Invalid username or password.");
            } else if (status === 403) {
                setErrMsg("Access forbidden. Please check backend security.");
            } else if (status === 404) {
                setErrMsg(
                    "API endpoint not found. Check the login or user-details endpoint."
                );
            } else {
                setErrMsg(
                    err.response?.data?.message ||
                    "Login failed. Please try again."
                );

                setErrComment(
                    err.response?.data?.comments || ""
                );
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container login-page-wrapper">
            <div className="row justify-content-center w-100 m-0">
                <div className="col-12 col-sm-9 col-md-7 col-lg-5 col-xl-4">
                    <div className="card login-card shadow-sm border-0 rounded-4 p-4 p-sm-5 bg-white">
                        <div className="card-body p-0">

                            <div className="text-center mb-4">
                                <h1 className="h4 fw-bold login-title mb-1">
                                    Road<span>Ready</span>
                                </h1>
                                <p className="text-muted small">
                                    Please enter your details to sign in.
                                </p>
                            </div>

                            <div className="text-center mb-3">
                                {errMsg && (
                                    <p style={{
                                        color: "red",
                                        fontSize: "0.9rem"
                                    }}>
                                        {errMsg}
                                    </p>
                                )}

                                {errComment && (
                                    <p className="text-muted small">
                                        {errComment}
                                    </p>
                                )}
                            </div>

                            <form onSubmit={processLogin}>
                                <div className="mb-3">
                                    <label
                                        htmlFor="username"
                                        className="form-label small fw-semibold text-secondary"
                                    >
                                        Username
                                    </label>

                                    <input
                                        id="username"
                                        type="text"
                                        className="form-control form-control-lg"
                                        autoComplete="username"
                                        required
                                        value={username}
                                        onChange={(e) =>
                                            setUsername(e.target.value)
                                        }
                                    />
                                </div>

                                <div className="mb-3">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <label
                                            htmlFor="password"
                                            className="form-label small fw-semibold text-secondary"
                                        >
                                            Password
                                        </label>

                                        <a
                                            href="#"
                                            className="small text-decoration-none text-primary fw-semibold"
                                            onClick={(e) => e.preventDefault()}
                                        >
                                            Forgot password?
                                        </a>
                                    </div>

                                    <input
                                        id="password"
                                        type="password"
                                        className="form-control form-control-lg"
                                        placeholder="••••••••"
                                        autoComplete="current-password"
                                        required
                                        value={password}
                                        onChange={(e) =>
                                            setPassword(e.target.value)
                                        }
                                    />
                                </div>

                                <div className="d-grid mb-3">
                                    <button
                                        type="submit"
                                        className="btn btn-primary btn-lg fw-semibold"
                                        disabled={loading}
                                    >
                                        {loading ? "Signing in..." : "Sign in"}
                                    </button>
                                </div>

                                <div className="text-center">
                                    <p className="small text-muted mb-0">
                                        Don't have an account?{" "}
                                        <Link
                                            to="/signup"
                                            className="text-primary text-decoration-none fw-semibold"
                                        >
                                            Sign up
                                        </Link>
                                    </p>
                                </div>
                            </form>

                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Login;
