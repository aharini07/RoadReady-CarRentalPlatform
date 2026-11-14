import { Routes, Route } from 'react-router'
import Home from './pages/Homepg.jsx'
import Login from './pages/Login.jsx'
import SignUp from './pages/Signup.jsx'
import CustomerDashboard from './components/customer/CustomerDashboard.jsx'
import RentalAgentDashboard from './components/rentalagent/RentalAgentDashboard.jsx'
import AdminDashboard from './components/admin/AdminDashboard.jsx'
import PageNotFound from './components/PageNotFound.jsx'
import BrowseCars from "./components/customer/CUSTOMER_DASHBOARD_MENU/BrowseCars";
import MyBookings from "./components/customer/CUSTOMER_DASHBOARD_MENU/MyBookings";
import Payments from "./components/customer/CUSTOMER_DASHBOARD_MENU/Payments";
import Cancellations from "./components/customer/CUSTOMER_DASHBOARD_MENU/Cancellations";
import MyReviews from "./components/customer/CUSTOMER_DASHBOARD_MENU/MyReviews"; // ✅ Fixed missing slash here
import MyProfile from "./components/customer/CUSTOMER_DASHBOARD_MENU/MyProfile";
import BookCar from "./components/customer/CUSTOMER_DASHBOARD_MENU/BookCar";
import PaymentProcess from "./components/customer/CUSTOMER_DASHBOARD_MENU/PaymentProcess";


function App() {
  return(
      <Routes>
        {/* Public & Landing Pages */}
        <Route path="/" element={<Home/>}/>
        <Route path="/login" element={<Login/>}/>
        <Route path="/signup" element={<SignUp/>}/>
        <Route path="customer/signup" element={<SignUp/>}/>

        {/* Role-Based Dashboards */}
        <Route path="customer/:token" element={<CustomerDashboard/>}/>
         <Route path="/customer/cars" element={<BrowseCars />} />
        <Route path="/customer/bookings" element={<MyBookings />} />
        <Route path="/customer/payments" element={<Payments />} />
        <Route path="/customer/cancellations/:reservationId" element={<Cancellations />} />
        <Route path="/customer/reviews" element={<MyReviews />} />
        <Route path="/customer/profile" element={<MyProfile />} />
        <Route path="rental-agent/dashboard" element={<RentalAgentDashboard/>}/>
        <Route path="admin/:token" element={<AdminDashboard/>}/>
        <Route path="/customer/book-car/:carId" element={<BookCar />} />
        <Route path="/customer/payments/:reservationId" element={<PaymentProcess />} />

        {/* 404 Catch-All Route */}
        <Route path="*" element={<PageNotFound/>}/>
      </Routes>
  )
}

export default App